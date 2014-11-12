//
//  ClientStatsDatabase.m
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 9/18/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "ClientStatsDatabase.h"
#import <UIKit/UIKit.h>

// Table name
#define TABLE_CLIENT_STATS @"clientStats"

// Column names
#define KEY_STAT @"stat"
#define KEY_TIMESTAMP @"timestamp"
#define KEY_VALUE @"value"

#define METADATA_TAG @"Metadata"
#define STATS_TAG @"Readings"

#define DB_FILE_NAME @"ClientStats.db"

@interface ClientStatsDatabase() {
    NSDictionary *statsNamesDict;
    NSString* appVersion;
}
@end

@implementation ClientStatsDatabase

static ClientStatsDatabase *_database;

+ (ClientStatsDatabase*)database {
    if (_database == nil) {
        _database = [[ClientStatsDatabase alloc] init];
    }
    return _database;
}

// TODO: Refactor this into a new database helper class?
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
        // Read the list of valid keys
        NSString *plistStatNamesPath = [[NSBundle mainBundle] pathForResource:@"app_stats" ofType:@"plist"];
        statsNamesDict = [[NSDictionary alloc] initWithContentsOfFile:plistStatNamesPath];
        
        NSString *emissionInfoPath = [[NSBundle mainBundle] pathForResource:@"Info" ofType:@"plist"];
        NSDictionary *infoDict = [[NSDictionary alloc] initWithContentsOfFile:emissionInfoPath];
        appVersion = [infoDict objectForKey:@"CFBundleShortVersionString"];
    }
    return self;
}

/*
 * If we want to be really safe, we should really create methods for each of these. But I am not enthused about that level of typing.
 * While this does not provide a compile time check, it at least provides a run time check. Let's stick with that for now.
 */
- (NSString*)getStatName:(NSString*)label {
    return [statsNamesDict objectForKey:label];
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

+(double) getCurrentTimeMillis {
    return [[NSDate date] timeIntervalSince1970]*1000;
}

+(NSString*) getCurrentTimeMillisString {
    return [@([self getCurrentTimeMillis]) stringValue];
}

-(void)storeEventNow:(NSString *)label {
    NSString* now = [ClientStatsDatabase getCurrentTimeMillisString];
    [self storeMeasurement:label value:CLIENT_STATS_DB_NIL_VALUE ts:now];
}


-(void)storeMeasurement:(NSString *)label value:(NSString *)value ts:(NSString *)ts {
    NSString* statName = [self getStatName:label];
    
    if (statName == NULL) {
        [NSException raise:@"unknown stat" format:@"stat %@ not defined in app_stats plist", label];
    }
    
    NSString *insertStatement = [NSString stringWithFormat:@"INSERT INTO %@ (%@, %@, %@) VALUES (?, ?, ?)",
                                 TABLE_CLIENT_STATS, KEY_STAT, KEY_VALUE, KEY_TIMESTAMP];
    sqlite3_stmt *compiledStatement;
    if(sqlite3_prepare_v2(_database, [insertStatement UTF8String], -1, &compiledStatement, NULL) == SQLITE_OK) {
        // The SQLITE_TRANSIENT is used to indicate that the raw data (userMode, tripId, sectionId
        // is not permanent data and the SQLite library should make a copy
        sqlite3_bind_text(compiledStatement, 1, [statName UTF8String], -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(compiledStatement, 2, [value UTF8String], -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(compiledStatement, 3, [ts UTF8String], -1, SQLITE_TRANSIENT);
    }
    // Shouldn't this be within the prior if?
    // Shouldn't we execute the compiled statement only if it was generated correctly?
    // This is code copied from
    // http://stackoverflow.com/questions/2184861/how-to-insert-data-into-a-sqlite-database-in-iphone
    // Need to check from the raw sources and see where we get
    // Create a new sqlite3 database like so:
    // http://www.raywenderlich.com/902/sqlite-tutorial-for-ios-creating-and-scripting
    NSInteger execCode = sqlite3_step(compiledStatement);
    if (execCode != SQLITE_DONE) {
        NSLog(@"Got error code %ld while executing statement %@", execCode, insertStatement);
    }
    sqlite3_finalize(compiledStatement);
}

/*
 * This version supports NULL cstrings
 */

- (NSString*) toNSString:(char*)cString
{
    if (cString == NULL) {
        return CLIENT_STATS_DB_NIL_VALUE;
    } else {
        return [[NSString alloc] initWithUTF8String:cString];
    }
}

- (NSArray*) readSelectResults:(NSString*) selectQuery nCols:(int)nCols {
    NSMutableArray* retVal = [[NSMutableArray alloc] init];
    
    sqlite3_stmt *compiledStatement;
    NSInteger selPrepCode = sqlite3_prepare_v2(_database, [selectQuery UTF8String], -1, &compiledStatement, NULL);
    if (selPrepCode == SQLITE_OK) {
        while (sqlite3_step(compiledStatement) == SQLITE_ROW) {
            NSMutableArray* currRow = [[NSMutableArray alloc] init];
            // Remember that while reading results, the index starts from 0
            for (int resultCol = 0; resultCol < nCols; resultCol++) {
                NSString* currResult = [self toNSString:(char*)sqlite3_column_text(compiledStatement, resultCol)];
                [currRow addObject:currResult];
            }
            [retVal addObject:currRow];
        }
    } else {
        NSLog(@"Error code %ld while compiling query %@", selPrepCode, selectQuery);
    }
    sqlite3_finalize(compiledStatement);
    return retVal;
}

- (NSDictionary*) getMeasurements {
    NSMutableDictionary *resultVal = [[NSMutableDictionary alloc] init];
    
    NSMutableDictionary *metaData = [[NSMutableDictionary alloc] init];
    
    NSString* iosVersion = [[UIDevice currentDevice] systemVersion];
    [metaData setObject:iosVersion forKey:[self getStatName:@"metadata_os_version"]];
    [metaData setObject:appVersion forKey:[self getStatName:@"metadata_app_version"]];

    
    NSString *findKeysQuery = [NSString stringWithFormat:@"SELECT DISTINCT %@ FROM %@",
                               KEY_STAT, TABLE_CLIENT_STATS];
    NSArray *uniqueKeys = [self readSelectResults:findKeysQuery nCols:1];

    NSMutableDictionary *stats = [[NSMutableDictionary alloc] init];
    for (int i = 0; i < uniqueKeys.count; i++) {
        // We know that each row contains an array with a single entry corresponding to the stat column
        NSString *currKey = uniqueKeys[i][0];
        NSString *selectQuery = [NSString stringWithFormat:@"SELECT %@, %@ FROM %@ WHERE %@ = '%@'",
                                 KEY_TIMESTAMP, KEY_VALUE, TABLE_CLIENT_STATS, KEY_STAT, currKey];
        NSArray *currValues = [self readSelectResults:selectQuery nCols:2];
        [stats setObject:currValues forKey:currKey];
    }
    /*
     * If there are no stats, there's no need to send any metadata either
     */
    if (uniqueKeys.count > 0) {
        [resultVal setObject:metaData forKey:METADATA_TAG];
        [resultVal setObject:stats forKey:STATS_TAG];
    }
    return resultVal;
}

/* TODO: Consider refactoring this along with the code in TripSectionDB to have generic read code.
 * Unfortunately, the code in TripSectionDB sometimes reads blobs, which require a different read method,
 * so this refactoring is likely to be non-trivial
 */

- (void)clear {
    NSString *deleteQuery = [NSString stringWithFormat:@"DELETE FROM %@", TABLE_CLIENT_STATS];
    sqlite3_stmt *compiledStatement;
    NSInteger delPrepCode = sqlite3_prepare_v2(_database, [deleteQuery UTF8String], -1, &compiledStatement, NULL);
    if (delPrepCode == SQLITE_OK) {
        sqlite3_step(compiledStatement);
    }
    sqlite3_finalize(compiledStatement);
}

@end
