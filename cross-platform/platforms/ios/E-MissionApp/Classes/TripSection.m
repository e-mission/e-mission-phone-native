//
//  TripSection.m
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/23/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "TripSection.h"
#import "TrackLocation.h"

@interface TripSection() {
    NSDictionary *_rawDict;
}
@end


@implementation TripSection

- (void)loadFromJSON:(NSDictionary*)jsonDict {
    // The save methods return the subset of the data that we push to the server.
    // But we still want to keep the raw blob around so that we can retain all the information.
    // We could choose to reconstruct it based on
    _rawDict = [NSMutableDictionary dictionaryWithDictionary:jsonDict];
    
    _tripId = [jsonDict objectForKey:@"trip_id"];
    // We need the stringValue here because, by default, _sectionId is loaded as an
    // NSNumber because our values are "0", etc.
    // But we don't use the fact that it is an integer in our code, and it is good
    // to be flexible for later, so let's explicitly
    // convert to a string value. If it already a string, my understanding is that
    // this is a NOP, so it won't break anything.
    _sectionId = [[jsonDict objectForKey:@"section_id"] stringValue];
    _startTime = [TripSection formatDate:[jsonDict objectForKey:@"section_start_time"]];
    _endTime = [TripSection formatDate:[jsonDict objectForKey:@"section_end_time"]];
    _autoMode = [jsonDict objectForKey:@"mode"];
    _userMode = [jsonDict objectForKey:@"confirmed_mode"];
    
    NSDictionary *confidenceLevelsJSON = [jsonDict objectForKey:@"predicted_mode"];
    NSString *predMode = @"";
    float highestConf = -1;
    NSEnumerator *enumerator = [confidenceLevelsJSON keyEnumerator];
    NSString *key = NULL;
    while (key = [enumerator nextObject]) {
        float currentConf = [[confidenceLevelsJSON objectForKey:key] floatValue];
        if (currentConf > highestConf) {
            predMode = key;
            highestConf = currentConf;
        }
    }
    _autoMode = predMode;
    _confidence = highestConf;
    
    NSArray *trackPointJSON = [jsonDict objectForKey:@"track_points"];
    NSMutableArray *trackPointList = [[NSMutableArray alloc] init];
    for (int i = 0; i < trackPointJSON.count; i++) {
        TrackLocation *newLoc = [[TrackLocation alloc] init];
        [newLoc loadFromJSON:trackPointJSON[i]];
        [trackPointList addObject:newLoc];
    }
    _trackPoints = trackPointList;
}

/*
 * We have three modes for each trip section.
 * _userMode: The "confirmed_mode" for the user. As far as I can make out,
 * in the current version of the code, this is set only when the user makes
 * a selection in the detail view.
 * _selMode: The "selected mode" for the user. Set when the user picks a mode
 * from the dropdown in the list view.
 * _autoMode: The prediction from the machine learning algorithm.
 * If multiple of them are set, this is their priority order.
 * This is used both to display in the list view, and to save on confirmation.
 */

- (NSString*)getDisplayMode {
    if (_userMode != nil && ![_userMode  isEqual: @""]) {
        return _userMode;
    } else if (_selMode != nil && ![_selMode  isEqual: @""]) {
        return _selMode;
    } else {
        assert(_autoMode != nil);
        return _autoMode;
    }
}

- (void)loadFromJSONData:(NSData*)jsonData {
    NSError *error;
    NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:jsonData
                                                             options:kNilOptions
                                                               error: &error];
    if (jsonDict == nil) {
        NSLog(@"error %@ while parsing json object %@", error, jsonData);
    }
    [self loadFromJSON:jsonDict];
}

- (void)loadFromJSONData:(NSData*)jsonData withUserMode:(NSString*) userMode {
    NSError *error;
    NSMutableDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:jsonData
                                                             options:NSJSONReadingMutableContainers
                                                               error: &error];
    if (jsonDict == nil) {
        NSLog(@"error %@ while parsing json object %@", error, jsonData);
    }
    [jsonDict setObject:userMode forKey:@"confirmed_mode" ];
    [self loadFromJSON:jsonDict];
}

- (void)loadFromJSONString:(NSString *)jsonString {
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    [self loadFromJSONData:jsonData];
}

- (void)loadFromJSONString:(NSString *)jsonString withUserMode:(NSString*) userMode {
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    [self loadFromJSONData:jsonData];
}

- (NSDictionary*) saveToJSON {
    NSDictionary *result = @{
            @"trip_id": _tripId,
            @"section_id": _sectionId,
            @"userMode": _userMode};
    return result;
}

- (NSData*)saveToJSONData {
    NSDictionary* jsonDict = [self saveToJSON];
    NSError *error;
    NSData *bytesToSend = [NSJSONSerialization dataWithJSONObject:jsonDict
                                                          options:kNilOptions error:&error];
    return bytesToSend;
}

- (NSString*)saveToJSONString {
    NSData *bytesToSend = [self saveToJSONData];
    NSString *strToSend = [NSString stringWithUTF8String:[bytesToSend bytes]];
    return strToSend;
}

/*
 * update the confirmed mode and then just return the raw dictionary.
 * This works because all the other properties are read-only
 * If we want to allow modification of other properties, we might need to change the structure of this class.
 */
- (NSDictionary*) saveAllToJSON {
    [_rawDict setValue: _userMode forKey:@"confirmed_mode"];
    return _rawDict;
}

- (NSData*) saveAllToJSONData {
    NSError *error;
    NSData *rawData = [NSJSONSerialization dataWithJSONObject:[self saveAllToJSON]
                                                          options:kNilOptions error:&error];
    return rawData;
}

- (NSString*) saveAllToJSONString {
    NSData* rawData = [self saveAllToJSONData];
    return [NSString stringWithUTF8String:[rawData bytes]];
}

- (void) setSelectedMode:(NSString*)selectedMode {
    if([[TripSection modeChoices] containsObject:selectedMode]) {
        _selMode = selectedMode;
    } else {
        [NSException raise:@"Invalid trip mode." format:@"mode of %@ is invalid", selectedMode];
    }
}

- (NSString*) getConfidenceAsString {
    NSString *stringConf = [NSString stringWithFormat:@"%i%%", (int)(_confidence*100)];
    return stringConf;
}

+ (NSDate*) formatDate:(NSString*)inputDate {
    NSDateFormatter * df = [[NSDateFormatter alloc] init];
    [df setDateFormat:@"yyyyMMdd'T'HHmmssZZZZ"];
    [df setTimeZone:[NSTimeZone systemTimeZone]];
    [df setFormatterBehavior:NSDateFormatterBehaviorDefault];
    NSDate *date = [df dateFromString:inputDate];
    return date;
}

+ (NSArray*) modeChoices{
    return [NSArray arrayWithObjects:@"walking", @"cycling", @"bus", @"train", @"car", @"mixed", @"air", @"not a trip", nil];
}

@end
