//
//  OngoingTripsDatabase.h
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 9/18/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import <CoreLocation/CoreLocation.h>

@interface OngoingTripsDatabase : NSObject {
    sqlite3 *_database;
}

+ (double) getCurrentTimeMillis;
+ (NSString*) getCurrentTimeMillisString;

// We implement the same interface as the android code, to use somewhat tested code
- (void) startTrip:(NSString*) mode;
- (NSDictionary*) getOngoingTrip;
+ (NSDictionary*) toGeoJSON:(CLLocation*) currLoc;
- (void) clear;
@end
