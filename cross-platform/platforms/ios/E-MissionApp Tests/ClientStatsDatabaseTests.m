//
//  ClientStatsDatabaseTests.m
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 3/25/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "ClientStatsDatabase.h"

@interface ClientStatsDatabaseTests : XCTestCase

@end

@interface ClientStatsDatabaseTests() {
    ClientStatsDatabase* clientSectionDb;
    NSArray* fakeFromHost;
}
@end

@implementation ClientStatsDatabaseTests

- (void)setUp
{
    [super setUp];
    clientSectionDb = [[ClientStatsDatabase alloc] init];
    [clientSectionDb clear];
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [clientSectionDb clear];
    [super tearDown];
}

- (void) testOneValue {
    [clientSectionDb storeMeasurement:@"button_sync_forced" value:@"10000" ts:@"11111"];
    [clientSectionDb storeMeasurement:@"confirmlist_ucs_size" value:@"20000" ts:@"22222"];
    [clientSectionDb storeMeasurement:@"result_display_failed" value:@"30000" ts:@"33333"];
    NSDictionary *retVal = [clientSectionDb getMeasurements];
    assert([retVal count] == 2);

    NSDictionary *statsVal = [retVal objectForKey:@"Readings"];
    NSDictionary *metadataVal = [retVal objectForKey:@"Metadata"];
    
    assert([metadataVal count] == 2);
    assert([statsVal count] == 3);

    assert([[statsVal objectForKey:@"button_sync_forced"] count] == 1);
    assert([[statsVal objectForKey:@"confirmlist_ucs_size"] count] == 1);
    assert([[statsVal objectForKey:@"result_display_failed"] count] == 1);
}

- (void) testMultipleValues {
    [clientSectionDb storeMeasurement:@"button_sync_forced" value:@"10000" ts:@"11111"];
    [clientSectionDb storeMeasurement:@"confirmlist_ucs_size" value:@"20000" ts:@"22221"];
    [clientSectionDb storeMeasurement:@"result_display_failed" value:@"30000" ts:@"33331"];
    
    /* Now store some more stuff */
    [clientSectionDb storeMeasurement:@"button_sync_forced" value:@"10001" ts:@"11112"];
    [clientSectionDb storeMeasurement:@"confirmlist_ucs_size" value:@"20001" ts:@"22222"];
    [clientSectionDb storeMeasurement:@"result_display_failed" value:@"30001" ts:@"33332"];
    
    [clientSectionDb storeMeasurement:@"button_sync_forced" value:@"10002" ts:@"11113"];
    [clientSectionDb storeMeasurement:@"confirmlist_ucs_size" value:@"20002" ts:@"22223"];
    [clientSectionDb storeMeasurement:@"result_display_failed" value:@"30002" ts:@"33333"];
    
    NSDictionary *retVal = [clientSectionDb getMeasurements];
    assert([retVal count] == 2);
    
    NSDictionary *statsVal = [retVal objectForKey:@"Readings"];
    NSDictionary *metadataVal = [retVal objectForKey:@"Metadata"];
    
    assert([metadataVal count] == 2);
    assert([statsVal count] == 3);
    
    assert([[statsVal objectForKey:@"button_sync_forced"] count] == 3);
    assert([[statsVal objectForKey:@"confirmlist_ucs_size"] count] == 3);
    assert([[statsVal objectForKey:@"result_display_failed"] count] == 3);
    
    NSArray* buttonSyncForcedReadings = [statsVal objectForKey:@"button_sync_forced"];
    assert([[[buttonSyncForcedReadings objectAtIndex:0] objectAtIndex:0] isEqual:@"11111"]);
    assert([[[buttonSyncForcedReadings objectAtIndex:0] objectAtIndex:1] isEqual:@"10000"]);
    
    assert([[[buttonSyncForcedReadings objectAtIndex:2] objectAtIndex:0] isEqual:@"11113"]);
    assert([[[buttonSyncForcedReadings objectAtIndex:2] objectAtIndex:1] isEqual:@"10002"]);
}

- (void) testClear {
    [clientSectionDb storeMeasurement:@"button_sync_forced" value:@"10000" ts:@"11111"];
    [clientSectionDb storeMeasurement:@"confirmlist_ucs_size" value:@"20000" ts:@"22222"];
    [clientSectionDb storeMeasurement:@"result_display_failed" value:@"30000" ts:@"33333"];
    NSDictionary *retVal = [clientSectionDb getMeasurements];
    assert([retVal count] == 2);
    
    NSDictionary *statsVal = [retVal objectForKey:@"Readings"];
    NSDictionary *metadataVal = [retVal objectForKey:@"Metadata"];
    
    assert([metadataVal count] == 2);
    assert([statsVal count] == 3);
    
    [clientSectionDb clear];
    retVal = [clientSectionDb getMeasurements];
    assert([retVal count] == 0);
}

@end
