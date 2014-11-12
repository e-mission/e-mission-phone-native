//
//  OngoingTripsDatabase.m
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 9/18/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "OngoingTripsDatabase.h"
#import <CoreLocation/CoreLocation.h>

// Table name
#define TABLE_ONGOING_TRIP @"ongoingTripsManual"

// Column names
#define KEY_START_TIME @"section_start_time"
#define KEY_LAT @"start_lat"
#define KEY_LNG @"end_lat"
#define KEY_MODE @"mode"

#define DB_FILE_NAME @"OngoingTripsManual.db"

@interface OngoingTripsDatabase() <CLLocationManagerDelegate> {
    NSDictionary *statsNamesDict;
    NSString* appVersion;
}
@end

@implementation OngoingTripsDatabase

static OngoingTripsDatabase *_database;

+ (OngoingTripsDatabase*)database {
    if (_database == nil) {
        _database = [[OngoingTripsDatabase alloc] init];
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

+(double) getCurrentTimeMillis {
    return [[NSDate date] timeIntervalSince1970]*1000;
}

+(NSString*) getCurrentTimeMillisString {
    return [@([self getCurrentTimeMillis]) stringValue];
}

+(NSDictionary*)toGeoJSON:(CLLocation*) currLoc {
    NSMutableDictionary *retVal = [[NSMutableDictionary alloc] init];
    // Note that GeoJSON format is (lng, lat), not (lat, lng)
    NSArray *coords = @[@(currLoc.coordinate.longitude),
                               @(currLoc.coordinate.latitude)];
    [retVal setObject:@"Point" forKey:@"type"];
    [retVal setObject:coords forKey:@"coordinates"];
    return retVal;
}

-(void)startTrip:(NSString*) mode {
    NSString *insertStatement = [NSString stringWithFormat:@"INSERT INTO %@ (%@, %@) VALUES (?, ?)",
                                 TABLE_ONGOING_TRIP, KEY_START_TIME, KEY_MODE];
    sqlite3_stmt *compiledStatement;
    if(sqlite3_prepare_v2(_database, [insertStatement UTF8String], -1, &compiledStatement, NULL) == SQLITE_OK) {
        // The SQLITE_TRANSIENT is used to indicate that the raw data (userMode, tripId, sectionId
        // is not permanent data and the SQLite library should make a copy
        NSDateFormatter *df = [[NSDateFormatter alloc] init];
        sqlite3_bind_text(compiledStatement, 1, [[df stringFromDate:[NSDate date]] UTF8String], -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(compiledStatement, 2, [mode UTF8String], -1, SQLITE_TRANSIENT);
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
    
    // The location information is made available as a callback, so we'll update it when we get it
    [self getAndStoreLocation];
}

-(void)getAndStoreLocation {
    CLLocationManager* locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    locationManager.desiredAccuracy = kCLLocationAccuracyKilometer; //meters
    locationManager.distanceFilter = 100; //meters
    if ([locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
        [locationManager requestWhenInUseAuthorization];
    }
    [locationManager startUpdatingLocation];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    // There is only one entry, so no WHERE clause is needed
    NSString *updateStatement = [NSString stringWithFormat:@"UPDATE %@ SET %@ = ?, %@ = ?",
                                 TABLE_ONGOING_TRIP, KEY_LAT, KEY_LNG];
    sqlite3_stmt *compiledStatement;
    if(sqlite3_prepare_v2(_database, [updateStatement UTF8String], -1, &compiledStatement, NULL) == SQLITE_OK) {
        sqlite3_bind_double(compiledStatement, 1, newLocation.coordinate.latitude);
        sqlite3_bind_double(compiledStatement, 2, newLocation.coordinate.longitude);
    }
    NSInteger execCode = sqlite3_step(compiledStatement);
    if (execCode != SQLITE_DONE) {
        NSLog(@"Got error code %ld while executing statement %@", execCode, updateStatement);
    }
    sqlite3_finalize(compiledStatement);
}

- (NSDictionary*)getOngoingTrip {
    NSMutableDictionary* retVal = [[NSMutableDictionary alloc] init];

    // There is only one entry, so no WHERE clause is needed
    NSString *selectQuery = [NSString stringWithFormat:@"SELECT * FROM %@", TABLE_ONGOING_TRIP];

    sqlite3_stmt *compiledStatement;
    NSInteger selPrepCode = sqlite3_prepare_v2(_database, [selectQuery UTF8String], -1, &compiledStatement, NULL);
    if (selPrepCode == SQLITE_OK) {
        while (sqlite3_step(compiledStatement) == SQLITE_ROW) {
            // Remember that while reading results, the index starts from 0
            NSString* startDate = [[NSString alloc] initWithUTF8String:(char*)sqlite3_column_text(compiledStatement, 0)];
            [retVal setObject:startDate forKey:KEY_START_TIME];
            NSArray *coords = @[@(sqlite3_column_double(compiledStatement, 1)),
                                @(sqlite3_column_double(compiledStatement, 2))];
            [retVal setObject:coords forKey:@"section_start_point"];
            NSString* mode = [[NSString alloc] initWithUTF8String:(char*)sqlite3_column_text(compiledStatement, 3)];
            [retVal setObject:mode forKey:KEY_MODE];
        }
    } else {
        NSLog(@"Error code %ld while compiling query %@", selPrepCode, selectQuery);
    }
    sqlite3_finalize(compiledStatement);
    assert(retVal.count == 0 || retVal.count == 3);
    if (retVal.count == 0) {
        return NULL;
    } else {
        return retVal;
    }
}



/* TODO: Consider refactoring this along with the code in TripSectionDB to have generic read code.
 * Unfortunately, the code in TripSectionDB sometimes reads blobs, which require a different read method,
 * so this refactoring is likely to be non-trivial
 */

- (void)clear {
    NSString *deleteQuery = [NSString stringWithFormat:@"DELETE FROM %@", TABLE_ONGOING_TRIP];
    sqlite3_stmt *compiledStatement;
    NSInteger delPrepCode = sqlite3_prepare_v2(_database, [deleteQuery UTF8String], -1, &compiledStatement, NULL);
    if (delPrepCode == SQLITE_OK) {
        sqlite3_step(compiledStatement);
    }
    sqlite3_finalize(compiledStatement);
}

@end
