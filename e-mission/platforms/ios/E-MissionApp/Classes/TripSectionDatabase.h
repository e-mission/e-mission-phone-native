//
//  TripSectionDatabase.h
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/25/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <sqlite3.h>

#import "TripSection.h"


@interface TripSectionDatabase : NSObject {
    sqlite3 *_database;
}

// We implement the same interface as the android code, to use somewhat tested code
- (NSMutableArray*)getUncommittedSections;
- (void) storeNewUnclassifiedTrips:(NSArray*) fromServer;
- (void) deleteUnclassifiedTrips;
- (void) clear;
- (NSArray*)getAndDeleteClassifiedSections;
- (void) storeUserClassification:(NSString*)userMode tripId:(NSString*)tripId sectionId:(NSString*)sectionId;
- (void)storeUserClassificationForTripSection: (TripSection*)trip;
@end
