//
//  TrackLocation.m
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/24/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "TrackLocation.h"
#import "TripSection.h"

@implementation TrackLocation

- (void)loadFromJSON:(NSDictionary*)jsonDict {
    NSDictionary *innerStruct = [jsonDict objectForKey:@"track_location"];
    _sampleTime = [TripSection formatDate:[jsonDict objectForKey:@"time"]];
    
    // TODO: Enable later
    //assert([[innerStruct objectForKey:@"type"] isEqual:@"Point"]);

    // Data from the server is currently in lat,lng format
    NSArray *coordinates = [innerStruct objectForKey:@"coordinates"];
    _lat = @([coordinates[1] doubleValue]);
    _lng = @([coordinates[0] doubleValue]);
   
    // Data from the server is currently in lat,lng format 
    CLLocationCoordinate2D currLoc;
    currLoc.latitude = [coordinates[1] doubleValue];
    currLoc.longitude = [coordinates[0] doubleValue];
    _coordinate = currLoc;
}

- (NSDictionary*) saveToJSON {
    NSArray *coordinates = @[_lat, _lng];
    NSDictionary *result = @{@"time": _sampleTime,
                             @"track_location": @{
                                     @"type": @"Point",
                                     @"coordinates": coordinates
                                }
                            };
    return result;
}

@end
