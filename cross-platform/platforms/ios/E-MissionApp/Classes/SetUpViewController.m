//
//  SetUpViewController.m
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "SetUpViewController.h"
#import "ConnectionSettings.h"

@interface SetUpViewController ()

@end

@implementation SetUpViewController

//  Notes:
//
//  * you needed the input from Shankari to decide if you can just always do silent authentication at the beginning or if you have to fine tune it so that it doesn’t do it the first time a user uses it (giving impression that we are getting their consent)
//  - if silent authentication only works within the app, you can just store the result of silent authentication in a bool and do normal login if it fails or skip it if it succeeds
//
//  * another thing you need to do is handle the user downloading moves and then having to connect to moves. It will work just like the user relaunching the app so you’ll have to do silent authentication because the user has already logged in at this point
//
//  * make sure you clean up all your code and test everything after the switch is complete

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    AuthCompletionHandler *signIn = [AuthCompletionHandler sharedInstance];
    signIn.scope = @"https://www.googleapis.com/auth/plus.me";
    [signIn registerFinishDelegate:self];
    
    if ([self checkMovesConnection]) {
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setBool:true forKey:[MasterNavController downloadedMovesKey]];
        
        // self.switchMovesButtonsController = YES;
        self.hideDownloadMovesButtonController = YES;
        self.hideConnectMovesButtonController = NO;
    } else {
        self.hideDownloadMovesButtonController = NO;
        self.hideConnectMovesButtonController = YES;
    }
    
    // If the google buttons are to be disabled, make sure that silent authentification works
    // and if it doesn't work, close connect to moves and open sign into google.
    if (self.disableGoogleButtonController == YES) {
        BOOL silentAuthResult = [[AuthCompletionHandler sharedInstance] trySilentAuthentication];
        NSLog(@"silent authentification tried with result: %@", silentAuthResult ? @"success" : @"failed");
    }
    
    [self configureButtons];
}

- (void) applicationDidEnterForeground
{
    //[self configureButtons];
    if ([self checkMovesConnection]) {
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setBool:true forKey:[MasterNavController downloadedMovesKey]];
        
        // self.switchMovesButtonsController = YES;
        self.hideDownloadMovesButtonController = YES;
        self.hideConnectMovesButtonController = NO;
        [self configureButtons];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // TODO:Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (IBAction)onDownloadMovesPress:(id)sender {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Leaving E-Mission" message:@"Remember to switch back after downloading and launching Moves to finish setting up E-Mission." delegate:self cancelButtonTitle:@"OK" otherButtonTitles: nil];
    [alert show];
}

- (IBAction)onConnectToMovesPress:(id)sender {
    if([self checkMovesConnection]) {
        UIApplication *ourApplication = [UIApplication sharedApplication];
        NSURL *movesURL = [[ConnectionSettings sharedInstance] getMovesURL];
        [self showBufferElements];
        
        [ourApplication openURL:movesURL];
    } else {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Moves Not Found" message:@"We couldn't find moves. Try relaunching the moves app." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        NSLog(@"Critical Error! Moves isn't installed. This LOG should never show up.");
    }
}

// This can be moved elsewhere if needed
- (void) fetchCustomSettings
{
    NSString* TAG = @"fetchCustomSettings";
    [CommunicationHelper getCustomSettings:^(NSData *data, NSURLResponse *response, NSError *error) {
        NSDictionary *sectionJSON;
        NSError *conversionError;
        if (error) {
            NSLog(@"In %@, there was an error with connecting to the server!", TAG);
        }
        
        sectionJSON = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableLeaves error:&conversionError];
        
        if (conversionError){
            NSLog(@"In %@, there was a conversion error from JSON to NSDictionary!", TAG);
        }
        
        [[CustomSettings sharedInstance] fillCustomSettingsWithDictionary:sectionJSON];
    }];
}

- (void) createUserProfile
{
    NSString* TAG = @"createUserProfile";
    [CommunicationHelper createUserProfile:^(NSData *data, NSURLResponse *response, NSError *error) {
        NSDictionary *resultJSON;
        NSError *conversionError;
        if (error) {
            NSLog(@"In %@, there was an error with connecting to the server!", TAG);
        }
        
        resultJSON = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableLeaves error:&conversionError];
        
        if (conversionError){
            NSLog(@"In %@, there was a conversion error from JSON to NSDictionary!", TAG);
        }
        // We are going to ignore the resultJSON for now, but won't ignore it in the future, when we improve error handling
        [self fetchCustomSettings];
    }];
}


- (IBAction)onSignInGooglePress:(id)sender {
    NSLog(@"LogIn With Google button clicked.");
    UIViewController *loginScreen = [[AuthCompletionHandler sharedInstance] getSigninController];
    [[self navigationController] pushViewController:loginScreen animated:YES];
}

- (BOOL)checkMovesConnection
{
    NSLog(@"attempting connection to moves");
    NSURL *movesURL = [[ConnectionSettings sharedInstance] getMovesURL];
    return [[UIApplication sharedApplication] canOpenURL:movesURL];
}

- (void)onMovesConnectionEstablished
{
    NSLog(@"Moves connection has been established.");
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setBool:true forKey:[MasterNavController linkedToMovesKey]];
    [self callStateMachine];
}

- (void)onMovesConnectionFailed
{
    [self hideBufferElements];
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Connection Error" message:@"An error occured. Try to connect to moves again." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
}

- (void)callStateMachine {
    [((MasterNavController*)self.navigationController) updateViewOnState];
}

- (void)configureButtons
{
    if(_disableGoogleButtonController) {
        [self disableGoogleButton];
    } else {
        [self enableGoogleButton];
    }
    
    if(_disableMovesButtonsController) {
        [self disableMovesButtons];
    } else {
        [self enableMovesButtons];
    }
    
    if(_hideDownloadMovesButtonController) {
        [self hideDownloadMovesButton];
    } else {
        [self showDownloadMovesButton];
    }
    
    if(_hideConnectMovesButtonController) {
        [self hideConnectMovesButton];
    } else {
        [self showConnectMovesButton];
    }
}

- (void)disableMovesButtons
{
    _connectToMovesButton.enabled = false;
    _connectToMovesButton.alpha = 0.5;
    _downloadMovesButton.enabled = false;
    _downloadMovesButton.alpha = 0.5;
    _disableMovesButtonsController = YES;
}

- (void)enableMovesButtons
{
    _connectToMovesButton.enabled = true;
    _connectToMovesButton.alpha = 1.0;
    _downloadMovesButton.enabled = true;
    _downloadMovesButton.alpha = 1.0;
    _disableMovesButtonsController = NO;
}

- (void)disableGoogleButton
{
    NSLog(@"disableGoogleButton called");
    _googleButton.enabled = false;
    _googleButton.alpha = 0.5;
    _disableGoogleButtonController = YES;
}

- (void)enableGoogleButton
{
    NSLog(@"enableGoogleButton called");
    _googleButton.enabled = true;
    _googleButton.alpha = 1.0;
    _disableGoogleButtonController = NO;
}

- (void)hideDownloadMovesButton
{
    _downloadMovesButton.hidden = true;
    _hideDownloadMovesButtonController = YES;
}

- (void)showDownloadMovesButton
{
    _downloadMovesButton.hidden = false;
    _hideDownloadMovesButtonController = NO;
}

- (void)hideConnectMovesButton
{
    _connectToMovesButton.hidden = true;
    _hideConnectMovesButtonController = YES;
}

- (void)showConnectMovesButton
{
    _connectToMovesButton.hidden = false;
    _hideConnectMovesButtonController = NO;
}

- (void)showBufferElements
{
    for(UIView *element in self.bufferElements) {
        element.hidden = false;
    }
}

- (void)hideBufferElements
{
    for(UIView *element in self.bufferElements) {
        element.hidden = true;
    }
}

#pragma mark - AuthCompletion delegate

- (void)finishedWithAuth:(GTMOAuth2Authentication *)auth error:(NSError *)error
{
    NSLog(@"OnBoardViewController.finishedWithAuth called with auth = %@ and error = %@", auth, error);
    if (error) {
        NSLog(@"There was an error! %@", error);
        return;
    } else {
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setBool:YES forKey:[MasterNavController signedIntoGoogleKey]];
        
        [self createUserProfile];
        // There might be a problem here that people who currently use the app can't join a research project beacuse they have to get to this part of the onboarding process
        // [self fetchCustomSettings];
        
        [self disableGoogleButton];
        [self enableMovesButtons];
    }
}

#pragma mark - UIAlertView delegate

// open up moves if the user clicks OK
- (void) alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if ([alertView.title  isEqual: @"Leaving E-Mission"]) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://itunes.apple.com/us/app/moves/id509204969?mt=8"]];
    }
}

@end
