//
//  TrackLocation.h
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/24/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface TrackLocation : NSObject <MKAnnotation>
@property NSDate* sampleTime;
@property NSNumber* lat;
@property NSNumber* lng;
@property BOOL first;
@property BOOL last;
@property (nonatomic, readonly) CLLocationCoordinate2D coordinate;

- (void) loadFromJSON:(NSDictionary*)jsonData;
- (NSDictionary*) saveToJSON;
@end
