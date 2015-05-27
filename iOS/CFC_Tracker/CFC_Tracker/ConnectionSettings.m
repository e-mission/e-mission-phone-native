//
//  ConnectionSettings.m
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 8/23/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "ConnectionSettings.h"

@interface ConnectionSettings() {
    NSDictionary *connSettingDict;
}
@end

@implementation ConnectionSettings
static ConnectionSettings *sharedInstance;

-(id)init{
    NSString *plistConnPath = [[NSBundle mainBundle] pathForResource:@"connect" ofType:@"plist"];
    connSettingDict = [[NSDictionary alloc] initWithContentsOfFile:plistConnPath];
    return [super init];
}

+ (ConnectionSettings*)sharedInstance
{
    if (sharedInstance == nil) {
        sharedInstance = [ConnectionSettings new];
    }
    return sharedInstance;
}

- (NSURL*)getConnectUrl
{
    return [NSURL URLWithString:[connSettingDict objectForKey: @"connect_url"]];
}

- (BOOL)isSkipAuth
{
    if([[self getConnectUrl].scheme isEqualToString:@"http"]) {
        return true;
    } else {
        return false;
    }
}

- (NSString*)getGoogleWebAppClientID
{
    return [connSettingDict objectForKey: @"google_web_app_client_id"];
}

- (NSString*)getGoogleiOSClientID
{
    return [connSettingDict objectForKey: @"google_ios_client_id"];
}

- (NSString*)getGoogleiOSClientSecret
{
    return [connSettingDict objectForKey: @"google_ios_client_secret"];
}

- (NSString*)getMovesClientID
{
    return [connSettingDict objectForKey: @"moves_client_id"];
}

/*
 It is unclear whether people will want to change the package name or not.
 Let's assume for now that they don't.
 Since all instances are localized here, it's easy enough to change around later.
 */

- (NSURL*)getMovesURL
{
    NSArray *movesStringParts = @[@"moves://app/authorize?client_id=",
                                [[ConnectionSettings sharedInstance] getMovesClientID],
                                @"&redirect_uri=edu.berkeley.eecs.E-Mission.moves%3A%2F%2FmovesCallback&scope=location%20activity"];
    NSString *movesString = [movesStringParts componentsJoinedByString:@""];
    NSURL *movesURL = [NSURL URLWithString:movesString];
    return movesURL;
}
@end
