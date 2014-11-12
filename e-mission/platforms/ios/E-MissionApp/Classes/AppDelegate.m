/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

//
//  AppDelegate.m
//  E-MissionApp
//
//  Created by Kalyanaraman Shankari on 3/23/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//
// We want to sync every 2 hours
#define BACKGROUND_SYNC_TIME 2 * 60 * 60
// change this to 30 mins temporarily to ensure that the token expiry is the problem
// #define BACKGROUND_SYNC_TIME 10 * 60

#import "AppDelegate.h"
#import "CommunicationHelper.h"
#import "TripSection.h"
#import "TripSectionDatabase.h"
#import "ClientStatsDatabase.h"
#import "AuthCompletionHandler.h"
#import "Constants.h"
#import "ConnectionSettings.h"
#import "MainViewController.h"

#import <Cordova/CDVPlugin.h>

@interface AppDelegate () {
    TripSectionDatabase* _tripSectionDb;
    ClientStatsDatabase* _statsDb;
}
@end

@implementation AppDelegate

@synthesize window, viewController;

static NSString * const runMultipleTimes = @"appHasRunMoreThanOnce";

//   GLOBAL CONSTANTS   //////////////////////////////////////////////////////////////

+(NSString *)runMultipleTimes
{
    return runMultipleTimes;
}

//////////////////////////////////////////////////////////////////////////////////////

- (id)init
{
    /** If you need to do any extra app-specific initialization, you can do it here
     *  -jm
     **/
    NSHTTPCookieStorage* cookieStorage = [NSHTTPCookieStorage sharedHTTPCookieStorage];

    [cookieStorage setCookieAcceptPolicy:NSHTTPCookieAcceptPolicyAlways];

    int cacheSizeMemory = 8 * 1024 * 1024; // 8MB
    int cacheSizeDisk = 32 * 1024 * 1024; // 32MB
#if __has_feature(objc_arc)
        NSURLCache* sharedCache = [[NSURLCache alloc] initWithMemoryCapacity:cacheSizeMemory diskCapacity:cacheSizeDisk diskPath:@"nsurlcache"];
#else
        NSURLCache* sharedCache = [[[NSURLCache alloc] initWithMemoryCapacity:cacheSizeMemory diskCapacity:cacheSizeDisk diskPath:@"nsurlcache"] autorelease];
#endif
    [NSURLCache setSharedURLCache:sharedCache];

    self = [super init];
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    CGRect screenBounds = [[UIScreen mainScreen] bounds];

#if __has_feature(objc_arc)
        self.window = [[UIWindow alloc] initWithFrame:screenBounds];
#else
        self.window = [[[UIWindow alloc] initWithFrame:screenBounds] autorelease];
#endif
    self.window.autoresizesSubviews = YES;

#if __has_feature(objc_arc)
        self.viewController = [[MainViewController alloc] init];
#else
        self.viewController = [[[MainViewController alloc] init] autorelease];
#endif

    // Set your app's start page by setting the <content src='foo.html' /> tag in config.xml.
    // If necessary, uncomment the line below to override it.
    // self.viewController.startPage = @"index.html";

    // NOTE: To customize the view's frame size (which defaults to full screen), override
    // [self.viewController viewWillAppear:] in your view controller.

    self.window.rootViewController = self.viewController;
    [self.window makeKeyAndVisible];
    // Override point for customization after application launch.
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        UISplitViewController *splitViewController = (UISplitViewController *)self.window.rootViewController;
        UINavigationController *navigationController = [splitViewController.viewControllers lastObject];
        splitViewController.delegate = (id)navigationController.topViewController;
    }
    [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:BACKGROUND_SYNC_TIME];
    _tripSectionDb = [[TripSectionDatabase alloc] init];
    _statsDb = [[ClientStatsDatabase alloc] init];
    // Handle google+ sign on
    [AuthCompletionHandler sharedInstance].clientId = [[ConnectionSettings sharedInstance] getGoogleiOSClientID];
    [AuthCompletionHandler sharedInstance].clientSecret = [[ConnectionSettings sharedInstance] getGoogleiOSClientSecret];
    
    // Might not be needed MARKER#1
    [CustomSettings sharedInstance];
    
    return YES;
}


// Handle the custom URL for this application, which allows us to
// interact with both our external services (Moves and GooglePlus).

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
    if (!url) {
        return NO;
    }

    if ([url.scheme isEqualToString:@"edu.berkeley.eecs.E-Mission.moves"]) {
        // Handle the callback here
        NSLog(@"in openURL, url is %@", url);
        NSArray* components = [url.query componentsSeparatedByString:@"&"];
        NSMutableDictionary* params = [[NSMutableDictionary alloc] init];
        for (NSString* component in components) {
            NSArray* subcomponents = [component componentsSeparatedByString:@"="];
            [params setObject:[subcomponents[1] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]
                       forKey:[subcomponents[0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }
        [CommunicationHelper movesCallback:params completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
            if (error != NULL) {
                NSLog(@"Got error %@ while executing the moves callback", error);
                UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Error linking with moves" message:@"Got error %@ while executing moves callback" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alertView show];
                if([self.window.rootViewController isKindOfClass:[UINavigationController class]]) {
                    UIViewController *topController = ((UINavigationController *)self.window.rootViewController).visibleViewController;
                    if([topController isKindOfClass:[SetUpViewController class]]) {
                        SetUpViewController *setUpController = (SetUpViewController *)topController;
                        [setUpController onMovesConnectionFailed];
                    }
                }
            } else {
                NSLog(@"Successfully executed the moves callback!");
                // There might be a simpler way to do this
                if([self.window.rootViewController isKindOfClass:[UINavigationController class]]) {
                    UIViewController *topController = ((UINavigationController *)self.window.rootViewController).visibleViewController;
                    if([topController isKindOfClass:[SetUpViewController class]]) {
                        SetUpViewController *setUpController = (SetUpViewController *)topController;
                        [setUpController onMovesConnectionEstablished];
                    }
                }
            }
        }];
    } else {
      // calls into javascript global function 'handleOpenURL'
      NSString* jsString = [NSString stringWithFormat:@"handleOpenURL(\"%@\");", url];
      [self.viewController.webView stringByEvaluatingJavaScriptFromString:jsString];

      // all plugins will get the notification, and their handlers will be called
      [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:CDVPluginHandleOpenURLNotification object:url]];
    }
    /*
    if ([GPPURLHandler handleURL:url
               sourceApplication:sourceApplication
                      annotation:annotation]) {
        return YES;
    }*/
    // Unsure why this was returning NO in the original code
    // return NO;
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    NSLog(@"applicationDidEnterBackground called");
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    NSLog(@"applicationDidBecomeActive called");
    /*
     * Every time the app is launched, check the battery level. We are not signing up for battery level notifications because we don't want
     * to contribute to the battery drain ourselves. Instead, we are going to check the battery level when the app is launched anyway for other reasons,
     * by the user, or as part of background sync.
     */
    NSString* currTS = [ClientStatsDatabase getCurrentTimeMillisString];
    NSString* batteryLevel = [@([UIDevice currentDevice].batteryLevel) stringValue];
    [_statsDb storeMeasurement:@"battery_level" value:batteryLevel ts:currTS];
    
    // First, clear any badges from the icon
    application.applicationIconBadgeNumber = 0;
    // Second, check if this is the on boarding process and if it is, call the appropriate function
    // The function should check if moves has been installed while the application was in the background and will act accordingly
    NSLog(@"The rootViewController is: %@", [self.window.rootViewController class]);
    if([self.window.rootViewController class] == [MasterNavController class])
    {
        MasterNavController *navController = (MasterNavController *)self.window.rootViewController;
        if ([[navController topViewController] class] == [SetUpViewController class]) {
            [((SetUpViewController *)[navController topViewController]) applicationDidEnterForeground];
        }
    }
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}


- (void)application:(UIApplication*)application performFetchWithCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    NSLog(@"performFetchWithCompletionHandler called at %@", [NSDate date]);
    
    /*
     * Every time the app is launched, check the battery level. We are not signing up for battery level notifications because we don't want
     * to contribute to the battery drain ourselves. Instead, we are going to check the battery level when the app is launched anyway for other reasons,
     * by the user, or as part of background sync.
     */
    NSString* currTS = [ClientStatsDatabase getCurrentTimeMillisString];
    NSString* batteryLevel = [@([UIDevice currentDevice].batteryLevel) stringValue];
    [_statsDb storeMeasurement:@"battery_level" value:batteryLevel ts:currTS];
    
    long msTimeStart = [ClientStatsDatabase getCurrentTimeMillis];
    NSString* currTs = [ClientStatsDatabase getCurrentTimeMillisString];
    [_statsDb storeMeasurement:@"sync_launched" value:CLIENT_STATS_DB_NIL_VALUE ts:currTs];

    // Called in order to download data in the background
    [CommunicationHelper getUnclassifiedSections:^(NSData *data, NSURLResponse *response, NSError *error) {
        if (error != NULL) {
            NSLog(@"Got error %@ while retrieving data", error);
            if ([error.domain isEqualToString:errorDomain] && (error.code == authFailedNeedUserInput)) {
                [self generateErrorNotificationImmediately:1 application:application];
            }
            completionHandler(UIBackgroundFetchResultFailed);
        } else {
            if (data == NULL) {
                NSLog(@"Got data == NULL while retrieving data");
                [_statsDb storeMeasurement:@"sync_pull_list_size" value:CLIENT_STATS_DB_NIL_VALUE ts:currTs];
                completionHandler(UIBackgroundFetchResultNoData);
            } else {
                NSLog(@"Got non NULL data while retrieving data");
                NSInteger newSectionCount = [self fetchedData:data];
                NSLog(@"Section count = %ld", (long)newSectionCount);
                [_statsDb storeMeasurement:@"sync_pull_list_size" value:[@(newSectionCount) stringValue] ts:currTs];
                if (newSectionCount > 0) {
                    [self generateLocalNotificationImmediately:newSectionCount application:application];
                    // Note that we need to update the UI before calling the completion handler, otherwise
                    // when the view appears, users won't see the newly fetched data!
                    [[NSNotificationCenter defaultCenter] postNotificationName:BackgroundRefreshNewData
                                                                        object:self];
                    completionHandler(UIBackgroundFetchResultNewData);
                } else {
                    [_statsDb storeMeasurement:@"sync_pull_list_size" value:@"0" ts:currTs];
                    [[NSNotificationCenter defaultCenter] postNotificationName:BackgroundRefreshNewData
                                                                        object:self];
                    completionHandler(UIBackgroundFetchResultNoData);
                }
            }
            long msTimeEnd = [[NSDate date] timeIntervalSince1970]*1000;
            long msDuration = msTimeEnd - msTimeStart;
            [_statsDb storeMeasurement:@"sync_pull_list_size" value:[@(msDuration) stringValue] ts:currTs];
        }
    }];
}

// This is the callback that is invoked when the async data collection ends.
// We are going to parse the JSON in here for simplicity
- (NSInteger)fetchedData:(NSData *)responseData {
    NSMutableArray *sectionList = [[NSMutableArray alloc] init];
    NSError *error;
    NSDictionary *sectionJSON = [NSJSONSerialization JSONObjectWithData:responseData
                                                                options:kNilOptions
                                                                  error: &error];
    
    NSArray *uncommitedSections = [sectionJSON objectForKey:@"sections"];
    // NSLog(@"sections: %@", uncommitedSections);

    for (int i = 0; i < [uncommitedSections count]; i++) {
        NSLog(@"parsing data for section %u", i);
        TripSection *currSection = [[TripSection alloc] init];
        [currSection loadFromJSON:[uncommitedSections objectAtIndex:i]];
        [sectionList addObject:currSection];
    }
    
    NSLog(@"after loading data, section count is %lu", (unsigned long)[sectionList count]);
    // First we clear the existing trips and then we store new ones. This is fine, since the server is the source of truth.
    [_tripSectionDb clear];
    [_tripSectionDb storeNewUnclassifiedTrips:sectionList];
    return [uncommitedSections count];
}

- (void)generateLocalNotificationImmediately:(NSInteger)count application:(UIApplication*)application
{
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
    if (localNotif) {
        localNotif.alertBody = [NSString stringWithFormat:
                                NSLocalizedString(@"You have %d trips to categorize", nil), count];
        localNotif.alertAction = NSLocalizedString(@"Categorize", nil);
        // localNotif.soundName = @"alarmsound.caf";
        localNotif.applicationIconBadgeNumber = count;
        [application presentLocalNotificationNow:localNotif];
    }
}

- (void)generateErrorNotificationImmediately:(NSInteger)count application:(UIApplication*)application
{
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
    if (localNotif) {
        localNotif.alertBody = [NSString stringWithFormat:
                                NSLocalizedString(@"Please sign in", nil), count];
        localNotif.alertAction = NSLocalizedString(@"Refresh Token", nil);
        // localNotif.soundName = @"alarmsound.caf";
        localNotif.applicationIconBadgeNumber = count;
        [application presentLocalNotificationNow:localNotif];
    }
}

// repost all remote and local notification using the default NSNotificationCenter so multiple plugins may respond
- (void)            application:(UIApplication*)application
    didReceiveLocalNotification:(UILocalNotification*)notification
{
    // re-post ( broadcast )
    [[NSNotificationCenter defaultCenter] postNotificationName:CDVLocalNotification object:notification];
}

- (void)                                application:(UIApplication *)application
   didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    // re-post ( broadcast )
    NSString* token = [[[[deviceToken description]
                         stringByReplacingOccurrencesOfString: @"<" withString: @""]
                        stringByReplacingOccurrencesOfString: @">" withString: @""]
                       stringByReplacingOccurrencesOfString: @" " withString: @""];

    [[NSNotificationCenter defaultCenter] postNotificationName:CDVRemoteNotification object:token];
}

- (void)                                 application:(UIApplication *)application
    didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    // re-post ( broadcast )
    [[NSNotificationCenter defaultCenter] postNotificationName:CDVRemoteNotificationError object:error];
}

- (NSUInteger)application:(UIApplication*)application supportedInterfaceOrientationsForWindow:(UIWindow*)window
{
    // iPhone doesn't support upside down by default, while the iPad does.  Override to allow all orientations always, and let the root view controller decide what's allowed (the supported orientations mask gets intersected).
    NSUInteger supportedInterfaceOrientations = (1 << UIInterfaceOrientationPortrait) | (1 << UIInterfaceOrientationLandscapeLeft) | (1 << UIInterfaceOrientationLandscapeRight) | (1 << UIInterfaceOrientationPortraitUpsideDown);

    return supportedInterfaceOrientations;
}

- (void)applicationDidReceiveMemoryWarning:(UIApplication*)application
{
    [[NSURLCache sharedURLCache] removeAllCachedResponses];
}

@end
