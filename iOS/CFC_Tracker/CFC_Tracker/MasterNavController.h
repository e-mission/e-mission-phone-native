//
//  MasterNavController.h
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ConsentViewController.h"
#import "SetUpViewController.h"
#import "MasterViewController.h"
#import "CustomSettings.h"
#import "CommunicationHelper.h"

@interface MasterNavController : UINavigationController

+ (NSString*)viewedInfoKey;
+ (NSString*)viewedVersionNumberKey;
+ (NSString*)signedIntoGoogleKey;
+ (NSString*)downloadedMovesKey;
+ (NSString*)linkedToMovesKey;

- (void)updateViewOnState;

@end
