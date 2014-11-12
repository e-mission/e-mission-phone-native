//
//  ClientStatsDatabase.h
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 9/18/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <sqlite3.h>

#define CLIENT_STATS_DB_NIL_VALUE @"none"

@interface ClientStatsDatabase : NSObject {
    sqlite3 *_database;
}

+ (double) getCurrentTimeMillis;
+ (NSString*) getCurrentTimeMillisString;

// We implement the same interface as the android code, to use somewhat tested code
- (void) storeMeasurement:(NSString*) label value:(NSString*)value ts:(NSString*)ts;
- (void) storeEventNow:(NSString*) label;
- (NSDictionary*) getMeasurements;
- (void) clear;
@end
