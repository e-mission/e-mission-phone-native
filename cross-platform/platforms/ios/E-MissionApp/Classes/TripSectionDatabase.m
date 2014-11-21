//
//  TripSectionDatabase.m
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/25/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//


#import "TripSectionDatabase.h"

// Table name
#define TABLE_CURR_TRIPS @"currTrips"

// Column names
#define KEY_TRIP_ID @"tripId"
#define KEY_SECTION_ID @"sectionID"
#define KEY_USER_CLASSIFICATION @"userClassification"
#define KEY_SECTION_BLOB @"sectionJsonBlob"

#define WHERE_ID_CLAUSE = [NSString stringWithFormat:@"WHERE %@ = ? AND %@ = ?", KEY_TRIP_ID, KEY_SECTION_ID]

#define DB_FILE_NAME @"TripSections.db"

@implementation TripSectionDatabase

static TripSectionDatabase *_database;

+ (TripSectionDatabase*)database {
    if (_database == nil) {
        _database = [[TripSectionDatabase alloc] init];
    }
    return _database;
}

- (id)init {
    if ((self = [super init])) {
        NSString *sqLiteDb = [self dbPath:DB_FILE_NAME];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        
        if (![fileManager fileExistsAtPath: sqLiteDb]) {
            // Copy existing database over to create a blank DB.
            // Apparently, we cannot create a new file there to work as the database?
            // http://stackoverflow.com/questions/10540728/creating-an-sqlite3-database-file-through-objective-c
            NSError *error = nil;
            NSString *readableDBPath = [[NSBundle mainBundle] pathForResource:DB_FILE_NAME
                                                                       ofType:nil];
            NSLog(@"Copying file from %@ to %@", sqLiteDb, readableDBPath);
            BOOL success = [[NSFileManager defaultManager] copyItemAtPath:readableDBPath
                                                                   toPath:sqLiteDb
                                                                    error:&error];
            if (!success)
            {
                NSCAssert1(0, @"Failed to create writable database file with message '%@'.", [  error localizedDescription]);
                return nil;
            }
        }
        // if we didn't have a file earlier, we just created it.
        // so we are guaranteed to always have a file when we get here
        assert([fileManager fileExistsAtPath: sqLiteDb]);
        int returnCode = sqlite3_open([sqLiteDb UTF8String], &_database);
        if (returnCode != SQLITE_OK) {
            NSLog(@"Failed to open database because of error code %d", returnCode);
            return nil;
        }
    }
    return self;
}

- (NSString*)dbPath:(NSString*)dbName {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                         NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *documentsPath = [documentsDirectory
                               stringByAppendingPathComponent:dbName];

    return documentsPath;
}

- (void)dealloc {
    sqlite3_close(_database);
}

-(void)storeUserClassification:(NSString*)userMode tripId:(NSString*)tripId sectionId:(NSString*)sectionId {
    
    NSString *updateStatement = [NSString stringWithFormat:@"UPDATE %@ SET %@ = ? WHERE %@ = ? AND %@ = ?",
                                    TABLE_CURR_TRIPS, @"userClassification", @"tripId", @"sectionId"];
    sqlite3_stmt *compiledStatement;
    if(sqlite3_prepare_v2(_database, [updateStatement UTF8String], -1, &compiledStatement, NULL) == SQLITE_OK) {
        // The SQLITE_TRANSIENT is used to indicate that the raw data (userMode, tripId, sectionId
        // is not permanent data and the SQLite library should make a copy
        sqlite3_bind_text(compiledStatement, 1, [userMode UTF8String], -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(compiledStatement, 2, [tripId UTF8String], -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(compiledStatement, 3, [sectionId UTF8String], -1, SQLITE_TRANSIENT);
    }
    // Shouldn't this be within the prior if?
    // Shouldn't we execute the compiled statement only if it was generated correctly?
    // This is code copied from
    // http://stackoverflow.com/questions/2184861/how-to-insert-data-into-a-sqlite-database-in-iphone
    // Need to check from the raw sources and see where we get
    NSInteger execCode = sqlite3_step(compiledStatement);
    if (execCode != SQLITE_DONE) {
        NSLog(@"Got error code %ld while executing statement %@", execCode, updateStatement);
    }
    sqlite3_finalize(compiledStatement);
}

- (void)storeUserClassificationForTripSection: (TripSection*)trip {
    [self storeUserClassification:trip.getDisplayMode tripId:trip.tripId sectionId:trip.sectionId];
}

- (void)deleteUnclassifiedTrips {
    NSString *deleteQuery = [NSString stringWithFormat:@"DELETE FROM %@ WHERE %@ IS NULL",
                             TABLE_CURR_TRIPS, KEY_USER_CLASSIFICATION];
    sqlite3_stmt *compiledStatement;
    NSInteger delPrepCode = sqlite3_prepare_v2(_database, [deleteQuery UTF8String], -1, &compiledStatement, NULL);
    if (delPrepCode == SQLITE_OK) {
        sqlite3_step(compiledStatement);
    }
    sqlite3_finalize(compiledStatement);
}

- (void)clear {
    NSString *deleteQuery = [NSString stringWithFormat:@"DELETE FROM %@", TABLE_CURR_TRIPS];
    sqlite3_stmt *compiledStatement;
    NSInteger delPrepCode = sqlite3_prepare_v2(_database, [deleteQuery UTF8String], -1, &compiledStatement, NULL);
    if (delPrepCode == SQLITE_OK) {
        sqlite3_step(compiledStatement);
    }
    sqlite3_finalize(compiledStatement);
}

-(NSArray*)getAndDeleteClassifiedSections {
    NSMutableArray *resultList = [[NSMutableArray alloc] init];
    NSString *selectQuery = [NSString stringWithFormat:@"SELECT %@, %@  FROM %@ WHERE %@ IS NOT NULL",
                            KEY_USER_CLASSIFICATION, KEY_SECTION_BLOB, TABLE_CURR_TRIPS, KEY_USER_CLASSIFICATION];
    sqlite3_stmt *compiledStatement;
    NSInteger selPrepCode = sqlite3_prepare_v2(_database, [selectQuery UTF8String], -1, &compiledStatement, NULL);
    if (selPrepCode == SQLITE_OK) {
        while (sqlite3_step(compiledStatement) == SQLITE_ROW) {
            // Remember that while reading results, the index starts from 0
            NSString* userMode = [[NSString alloc] initWithUTF8String:(char*)sqlite3_column_text(compiledStatement, 0)];
            NSString* rawClob = [[NSString alloc] initWithUTF8String:(char*)sqlite3_column_text(compiledStatement, 1)];
            TripSection *currSection = [[TripSection alloc] init];
            [currSection loadFromJSONString:rawClob withUserMode:userMode];
            [resultList addObject:currSection];
        }
    } else {
        NSLog(@"Error code %ld while compiling query %@", selPrepCode, selectQuery);
    }
    sqlite3_finalize(compiledStatement);
    
    // Delete all the entries that we just read
    NSString *deleteQuery = [NSString stringWithFormat:@"DELETE FROM %@ WHERE %@ = ? AND %@ = ?",
                                TABLE_CURR_TRIPS, KEY_TRIP_ID, KEY_SECTION_ID];
    sqlite3_stmt *deleteCompiledStatement;
    NSInteger delPrepCode = sqlite3_prepare_v2(_database, [deleteQuery UTF8String], -1, &deleteCompiledStatement, NULL);
    if (delPrepCode == SQLITE_OK) {
        for (int i = 0; i < [resultList count]; i++) {
            // Remember that while binding parameters, the index starts from 1
            TripSection *currSection = resultList[i];
            sqlite3_bind_text(deleteCompiledStatement, 1, [currSection.tripId UTF8String], -1, SQLITE_TRANSIENT);
            sqlite3_bind_text(deleteCompiledStatement, 2, [currSection.sectionId UTF8String], -1, SQLITE_TRANSIENT);
            sqlite3_step(deleteCompiledStatement);
            sqlite3_reset(deleteCompiledStatement);
        }        
    } else {
        NSLog(@"Error code %ld while compiling query %@", delPrepCode, deleteQuery);
    }
    sqlite3_finalize(deleteCompiledStatement);
    return resultList;
}

-(void)storeNewUnclassifiedTrips:(NSArray*)fromServer
{
    NSString *deleteQuery = [NSString stringWithFormat:@"DELETE FROM %@ WHERE %@ = ? AND %@ = ?",
                             TABLE_CURR_TRIPS, KEY_TRIP_ID, KEY_SECTION_ID];
    sqlite3_stmt *deleteCompiledStatement;
    NSInteger delRetCode = sqlite3_prepare_v2(_database, [deleteQuery UTF8String], -1, &deleteCompiledStatement, NULL);
    if (delRetCode != SQLITE_OK) {
        NSLog(@"Error code %ld while compiling statement %@, returning", delRetCode, deleteQuery);
        return;
    }

    NSString *insertQuery = [NSString stringWithFormat:@"INSERT INTO %@ (%@, %@, %@) VALUES (?, ?, ?)",
                             TABLE_CURR_TRIPS, KEY_TRIP_ID, KEY_SECTION_ID, KEY_SECTION_BLOB];
    sqlite3_stmt *insertCompiledStatement;
    NSInteger insRetCode = sqlite3_prepare_v2(_database, [insertQuery UTF8String], -1, &insertCompiledStatement, NULL);
    if (insRetCode != SQLITE_OK) {
        NSLog(@"Error code %ld while compiling statement %@, returning", insRetCode, insertQuery);
        return;
    }

    // upsert each new trip section using the prepared statements above
    for (int i = 0; i < [fromServer count]; i++) {
        [self upsertRecord:fromServer[i] withDelStmt:deleteCompiledStatement withInsStmt:insertCompiledStatement];
    }
    
    // and then finalize the statements since we are done with them
    sqlite3_finalize(deleteCompiledStatement);
    sqlite3_finalize(insertCompiledStatement);
}

-(void) upsertRecord:(TripSection*)currSection withDelStmt:(sqlite3_stmt*)deleteCompiledStatement
         withInsStmt:(sqlite3_stmt*)insertCompiledStatement{
    
    // First delete the existing entry (if any)
    sqlite3_bind_text(deleteCompiledStatement, 1, [currSection.tripId UTF8String], -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(deleteCompiledStatement, 2, [currSection.sectionId UTF8String], -1, SQLITE_TRANSIENT);
    sqlite3_step(deleteCompiledStatement);
    // We need to call reset here because otherwise, we will get code 21 (SQLITE_MISUSE), as described in the
    // documentation
    // http://www.sqlite.org/c3ref/bind_blob.html
    sqlite3_reset(deleteCompiledStatement);

    sqlite3_bind_text(insertCompiledStatement, 1, [currSection.tripId UTF8String], -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(insertCompiledStatement, 2, [currSection.sectionId UTF8String], -1, SQLITE_TRANSIENT);
    NSString* rawClob = [currSection saveAllToJSONString];
    NSLog(@"raw CLOB = %@", rawClob);
//    NSLog(@"DONE PRINTING NEW CLOB");
//    rawClob = @"{'trip_id': 'test_trip_1', 'section_id': 0}";
//    NSLog(@"modified raw CLOB = %@", rawClob);
    sqlite3_bind_text(insertCompiledStatement, 3, [rawClob UTF8String], -1, SQLITE_TRANSIENT);
    NSInteger insExeCode = sqlite3_step(insertCompiledStatement);
    NSLog(@"exec code = %ld while executing insert statement", (long)insExeCode);
    sqlite3_reset(insertCompiledStatement);
}


-(NSMutableArray*) getUncommittedSections {
    NSMutableArray* resultList = [[NSMutableArray alloc] init];
    NSString *selectQuery = [NSString stringWithFormat:@"SELECT %@ FROM %@ WHERE %@ IS NULL",
                             KEY_SECTION_BLOB, TABLE_CURR_TRIPS, KEY_USER_CLASSIFICATION];
    sqlite3_stmt *compiledStatement;
    NSInteger selRetCode = sqlite3_prepare_v2(_database, [selectQuery UTF8String], -1, &compiledStatement, NULL);
    if (selRetCode == SQLITE_OK) {
        while (sqlite3_step(compiledStatement) == SQLITE_ROW) {
            /* This is worth noting, since it just caused me to waste 30 minutes.
             * Although the indices in the prepared statements for update and delete start from 1, the indices
             * for select start from 0. So when inserting, we bind to 1, 2 and 3. and while deleting, we retrieve from 0.
             */
            NSString* rawClob = [[NSString alloc] initWithUTF8String:(char*)sqlite3_column_text(compiledStatement, 0)];
            // NSLog(@"blob = %@", rawClob);
            if (rawClob != NULL) {
                TripSection *currSection = [[TripSection alloc] init];
                [currSection loadFromJSONString:rawClob];
                if (currSection.tripId != nil) {
                    // There was an error in parsing the blob, so we are going to ignore the object
                    // Note that if we had a proper constructor that would take the string as input
                    // directly, we would be able to return a null TripSection for invalid JSON and
                    // have a simpler check here. But since we do the Objective-C style create +
                    // load, we have to check the fields of the object instead. But since we expect that
                    // the primary key is tripId + sectionId, a check for tripId seems safe enough
                    // TODO: Consider switching to a different constructor
                    [resultList addObject:currSection];
                }
                NSLog(@"Current size of the result list is %lu", [resultList count]);
            }
        }
    } else {
        NSLog(@"Error code %ld while compiling statement %@, returning", selRetCode, selectQuery);
    }
    sqlite3_finalize(compiledStatement);
    return resultList;
}

@end
