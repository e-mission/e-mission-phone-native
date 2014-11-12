//
//  OnBoardViewController.h
//  E-Mission
//
//  Created by Gautham Kesineni on 6/13/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////DEPRECATED////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////

#import <UIKit/UIKit.h>
#import <GoogleOpenSource/GoogleOpenSource.h>

#import "AuthInspectorViewController.h"
#import "AuthCompletionHandler.h"
#import "AppDelegate.h"

@interface OnBoardViewController : UIViewController <UIWebViewDelegate, UIAlertViewDelegate, AuthCompletionDelegate>

@property (strong, nonatomic) IBOutlet UIButton *googleButton;
@property (strong, nonatomic) IBOutlet UIButton *downloadMovesButton;
@property (strong, nonatomic) IBOutlet UIButton *connectToMovesButton;
@property (strong, nonatomic) IBOutletCollection(UIView) NSArray *connectingToMoves;
@property (strong, nonatomic) IBOutletCollection(UIView) NSArray *descriptionElements;


- (IBAction)onDownloadMovesClick:(id)sender;
- (IBAction)signIn:(id)sender;
- (IBAction)connectToMoves:(id)sender;
- (void)handleGoogleConnected;
- (void)applicationDidEnterForeground;
- (void)onMovesConnectionEstablished;
- (void)onMovesConnectionFailed;
- (void)setVisibilityOfBuffer:(bool)visibility;

@end