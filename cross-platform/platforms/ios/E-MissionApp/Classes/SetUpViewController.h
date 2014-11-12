//
//  SetUpViewController.h
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AuthCompletionHandler.h"
#import "MasterNavController.h"

@interface SetUpViewController : UIViewController <UIAlertViewDelegate, AuthCompletionDelegate>

@property BOOL disableMovesButtonsController;
@property BOOL disableGoogleButtonController;
@property BOOL hideConnectMovesButtonController;
@property BOOL hideDownloadMovesButtonController;

@property (strong, nonatomic) IBOutlet UIButton *downloadMovesButton;
@property (strong, nonatomic) IBOutlet UIButton *connectToMovesButton;
@property (strong, nonatomic) IBOutlet UIButton *googleButton;
@property (strong, nonatomic) IBOutletCollection(UIView) NSArray *descriptionElements;
@property (strong, nonatomic) IBOutletCollection(UIView) NSArray *bufferElements;


- (void)applicationDidEnterForeground;

- (IBAction)onDownloadMovesPress:(id)sender;
- (IBAction)onConnectToMovesPress:(id)sender;
- (IBAction)onSignInGooglePress:(id)sender;

- (void)onMovesConnectionEstablished;
- (void)onMovesConnectionFailed;

@end
