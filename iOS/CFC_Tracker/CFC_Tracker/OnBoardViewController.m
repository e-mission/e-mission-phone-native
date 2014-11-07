//
//  OnBoardViewController.m
//  E-Mission
//
//  Created by Gautham Kesineni on 6/13/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////DEPRECATED////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////

#import "OnBoardViewController.h"
#import "ConnectionSettings.h"

@interface OnBoardViewController ()

@end

@implementation OnBoardViewController

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
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    // change the download moves button if the application is already installed
    if ([self checkMovesConnection]) {
        [self handleMovesInstalled];
    }
    
    if ([defaults integerForKey:@"onBoardLocation"] == 2) {
        [self handleGoogleConnected];
        BOOL silentAuthResult = [[AuthCompletionHandler sharedInstance] trySilentAuthentication];
        NSLog(@"silent result: %i", silentAuthResult);
    } else {
        [self disableMovesButtons];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)disableMovesButtons
{
    _connectToMovesButton.enabled = false;
    _connectToMovesButton.alpha = 0.5;
    _downloadMovesButton.enabled = false;
    _downloadMovesButton.alpha = 0.5;
}

- (void)enableMovesButtons
{
    _connectToMovesButton.enabled = true;
    _connectToMovesButton.alpha = 1.0;
    _downloadMovesButton.enabled = true;
    _downloadMovesButton.alpha = 1.0;
}

- (void)disableGoogleButton
{
    _googleButton.enabled = false;
    _googleButton.alpha = 0.5;
}

- (void)enableGoogleButton
{
    _googleButton.enabled = true;
    _googleButton.alpha = 1.0;
}

- (void)handleMovesInstalled
{
    _downloadMovesButton.hidden = true;
    _connectToMovesButton.hidden = false;
}

- (void)handleGoogleConnected
{
    [self disableGoogleButton];
    [self enableMovesButtons];
    if (_googleButton.enabled) {
    } else {
    }
}

- (BOOL)checkMovesConnection
{
    NSLog(@"attempting connection to moves");
    UIApplication *ourApplication = [UIApplication sharedApplication];
    NSURL *movesURL = [[ConnectionSettings sharedInstance] getMovesURL];
    return [ourApplication canOpenURL:movesURL];
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

- (void)applicationDidEnterForeground
{
    if ([self checkMovesConnection]) {
        [self handleMovesInstalled];
    }
}

- (void)onMovesConnectionEstablished
{
    NSLog(@"moves connection has been established");
    //self.
    //[[self navigationController] pushViewController:[[self storyboard] instantiateViewControllerWithIdentifier:@"NormalNavController"] animated:YES];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setBool:YES forKey:[AppDelegate runMultipleTimes]];
    [[self navigationController] performSegueWithIdentifier:@"finishedOnBoardingSegue" sender:self];
}

- (void)onMovesConnectionFailed
{
    [self setVisibilityOfBuffer:NO];
}

- (IBAction)onRefuseTerms:(id)sender {
    // exit(0); This is ideal but will cause apple to refuse adding the app to the store
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Conditions Refused" message:@"Please close and uninstall this application. You must accept the terms to use E-Mission." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
}

- (IBAction)onAgreeTerms:(id)sender {
    // you want to remember that the user agreed to the terms so you can open the page if the user's app crashes
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setInteger:1 forKey:@"onBoardLocation"];
}

- (IBAction)onDownloadMovesClick:(id)sender
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Leaving E-Mission" message:@"Remember to switch back after downloading and launching Moves to finish setting up E-Mission." delegate:self cancelButtonTitle:@"OK" otherButtonTitles: nil];
    [alert show];
}

- (IBAction)signIn:(id)sender
{
    NSLog(@"LogIn With Google button clicked.");
    UIViewController *loginScreen = [[AuthCompletionHandler sharedInstance] getSigninController];
    [[self navigationController] pushViewController:loginScreen animated:YES];
}

- (IBAction)connectToMoves:(id)sender {
    if([self checkMovesConnection]){
        UIApplication *ourApplication = [UIApplication sharedApplication];
        NSURL *movesURL = [[ConnectionSettings sharedInstance] getMovesURL];
        [ourApplication openURL:movesURL];
    } else {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Moves Not Found" message:@"Moves is not installed. Try relaunching the moves app." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
        [alert show];
        NSLog(@"critical error! Moves isn't installed!");
    }
    [self setVisibilityOfBuffer:YES];
}

- (void)setVisibilityOfBuffer:(bool)visibility {
    [self.navigationItem setHidesBackButton:visibility];
    for(id item in self.connectingToMoves)
    {
        [item setHidden:!visibility];
    }
}

- (void)finishedWithAuth:(GTMOAuth2Authentication *)auth error:(NSError *)error
{
    NSLog(@"OnBoardViewController.finishedWithAuth called with auth = %@ and error = %@", auth, error);
    if (error) {
        NSLog(@"There was an error! %@", error);
        return;
    } else {
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setInteger:2 forKey:@"onBoardLocation"];
        [self handleGoogleConnected];
    }
}

#pragma mark - UIWebView delegate

// This will open up links in Safari instead of in the web view
- (BOOL) webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    if (navigationType == UIWebViewNavigationTypeLinkClicked) {
        [[UIApplication sharedApplication] openURL:[request URL]];
        return NO;
    }
    return YES;
}

#pragma mark - UIAlertView delegate

//- (void) alertViewCancel:(UIAlertView *)alertView {
//    NSLog(@"thingthing");
//}

- (void) alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if ([alertView.title  isEqual: @"Leaving E-Mission"]) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://itunes.apple.com/us/app/moves/id509204969?mt=8"]];
    }
}

@end
