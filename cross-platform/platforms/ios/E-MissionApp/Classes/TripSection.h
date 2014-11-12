//
//  TripSection.h
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/23/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TripSection : NSObject

@property (readonly)NSString *tripId;
@property (readonly)NSString *sectionId;
@property (readonly)NSArray *trackPoints;
@property (readonly)NSDate *startTime;
@property (readonly)NSDate *endTime;
@property (readonly)NSString *autoMode;
@property (readonly)NSString *selMode;
@property (readonly)float confidence;
@property NSString *userMode;

- (NSString*) getDisplayMode;

// TODO: Convert this to a constructor?
- (void) loadFromJSON:(NSDictionary*)jsonData;
- (void) loadFromJSONData:(NSData*)jsonData;
- (void) loadFromJSONData:(NSData*)jsonData withUserMode:(NSString*) userMode;
- (void) loadFromJSONString: (NSString*) jsonString;
- (void) loadFromJSONString:(NSData*)jsonData withUserMode:(NSString*) userMode;

- (NSDictionary*) saveToJSON;
- (NSData*) saveToJSONData;
- (NSString*) saveToJSONString;

- (NSDictionary*) saveAllToJSON;
- (NSData*) saveAllToJSONData;
- (NSString*) saveAllToJSONString;

- (void) setSelectedMode:(NSString*)selectedMode;
- (NSString*) getConfidenceAsString;

+ (NSDate*) formatDate:(NSString*)inputDate;
+ (NSArray*) modeChoices;
@end
