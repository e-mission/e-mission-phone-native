//
//  E_Mission_Tests.m
//  E-Mission Tests
//
//  Created by Kalyanaraman Shankari on 9/19/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "TripSection.h"
#import "TrackLocation.h"

@interface E_Mission_Tests : XCTestCase

@end

@implementation E_Mission_Tests

- (void)setUp
{
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testDateFormat
{
    NSString *dateStr = @"20140321T094359-0700";
    NSDate *date = [TripSection formatDate:dateStr];
    assert(date != NULL);
}

- (void)testTripSection
{
    NSString* sampleTripsName = [[NSBundle bundleForClass:[self class]] pathForResource:@"SampleTrips" ofType:@"json"];
    NSData* tripJSONData = [[NSData alloc] initWithContentsOfFile:sampleTripsName];
    
    TripSection *testSection = [[TripSection alloc]init];
    NSError *error;
    NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:tripJSONData
                                                                options:kNilOptions
                                                                  error: &error];
    [testSection loadFromJSON:jsonDict];
    assert([testSection.tripId isEqual:@"20140323T183342-0700"]);
    assert([testSection.sectionId isEqual:@"0"]);
    assert([testSection.autoMode isEqual:@"walking"]);
    assert([testSection.userMode isEqual:@""]);
    assert([testSection.startTime timeIntervalSince1970] == (NSTimeInterval)1395624822.000);
    assert([testSection.endTime timeIntervalSince1970] == (NSTimeInterval)1395625538.000);
    
    assert([testSection.trackPoints count] == 29);
    TrackLocation *firstLoc = testSection.trackPoints[0];
    assert([firstLoc.sampleTime timeIntervalSince1970] == (NSTimeInterval)1395624822.000);
    // Note that GeoJSON requires (lng, lat)
    assert([firstLoc.lng doubleValue] == 37.85078);
    assert([firstLoc.lat doubleValue] == -122.26058);
}

@end
