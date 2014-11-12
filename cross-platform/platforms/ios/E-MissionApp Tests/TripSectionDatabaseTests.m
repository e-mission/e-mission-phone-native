//
//  TripSectionDatabaseTests.m
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/25/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "TripSectionDatabase.h"
#import "TripSection.h"

@interface TripSectionDatabaseTests : XCTestCase

@end

@interface TripSectionDatabaseTests() {
    TripSectionDatabase* tripSectionDb;
    NSArray* fakeFromHost;
}
@end

@implementation TripSectionDatabaseTests

- (void)setUp
{
    [super setUp];
    tripSectionDb = [[TripSectionDatabase alloc] init];
    
    TripSection *testSection10 = [self getFakeSection:@"test_trip_1" sectionId:0];
    TripSection *testSection20 = [self getFakeSection:@"test_trip_2" sectionId:0];
    TripSection *testSection21 = [self getFakeSection:@"test_trip_2" sectionId:1];
    
    fakeFromHost = @[testSection10, testSection20, testSection21];
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (TripSection*)getFakeSection:(NSString*)tripId sectionId:(int)sectionId
{
    NSString* sampleTripsName = [[NSBundle bundleForClass:[self class]] pathForResource:@"SampleTrips" ofType:@"json"];
    NSError* error;
    NSData* testData = [NSData dataWithContentsOfFile:sampleTripsName];
    NSDictionary *testDictReadonly = [NSJSONSerialization JSONObjectWithData:testData
                                                             options:kNilOptions
                                                               error: &error];
    NSMutableDictionary *testDict = [NSMutableDictionary dictionaryWithDictionary:testDictReadonly];
    [testDict setObject:tripId forKey:@"trip_id"];
    [testDict setObject:@(sectionId) forKey:@"section_id"];
    
    TripSection* testSection = [[TripSection alloc]init];
    [testSection loadFromJSON:testDict];
    return testSection;
}

- (void)testUnclassified
{
    [tripSectionDb storeNewUnclassifiedTrips:fakeFromHost];
    NSArray *retSections = [tripSectionDb getUncommittedSections];
    assert([retSections count] == 3);
    
    TripSection* ret0 = retSections[0];
    assert([ret0.tripId isEqual:@"test_trip_1"]);
    assert([ret0.sectionId isEqual:@"0"]);
    assert([ret0.autoMode isEqual:@"walking"]);
    
    [tripSectionDb deleteUnclassifiedTrips];
    NSArray *retSectionsAfterDel = [tripSectionDb getUncommittedSections];
    assert([retSectionsAfterDel count] == 0);
}

- (void)testClassified
{
    [tripSectionDb storeNewUnclassifiedTrips:fakeFromHost];
    NSArray *unclassifiedStep1 = [tripSectionDb getUncommittedSections];
    assert([unclassifiedStep1 count] == 3);
    
    [tripSectionDb storeUserClassification:@"walking" tripId:@"test_trip_1" sectionId:@"0"];
    [tripSectionDb storeUserClassification:@"cycling" tripId:@"test_trip_2" sectionId:@"0"];

    NSArray* unclassifiedStep2 = [tripSectionDb getUncommittedSections];
    assert([unclassifiedStep2 count] == 1);

    NSArray* classifiedStep1 = [tripSectionDb getAndDeleteClassifiedSections];
    assert([classifiedStep1 count] == 2);
    assert([((TripSection*)classifiedStep1[0]).userMode isEqual:@"walking"]);
    assert([((TripSection*)classifiedStep1[1]).userMode isEqual:@"cycling"]);
    
    // We just deleted the two classified trips, so we don't expect to see them again
    NSArray* classifiedStep2 = [tripSectionDb getAndDeleteClassifiedSections];
    assert([classifiedStep2 count] == 0);
    
    // Now, we classify the last one
    [tripSectionDb storeUserClassification:@"bus" tripId:@"test_trip_2" sectionId:@"1"];
    
    // So we see it in the list
    NSArray* classifiedStep3 = [tripSectionDb getAndDeleteClassifiedSections];
    assert([classifiedStep3 count] == 1);
    assert([((TripSection*)classifiedStep3[0]).userMode isEqual:@"bus"]);
}

@end
