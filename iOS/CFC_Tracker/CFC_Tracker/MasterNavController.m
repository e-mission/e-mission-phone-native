//
//  MasterNavController.m
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "MasterNavController.h"
#import "ConnectionSettings.h"
#import "EmbeddedCordovaViewController.h"
#import "SignInViewController.h"
// Used to determine whether we are running in the simulator
#import <TargetConditionals.h>

static NSString * const kResultSummaryStoryboardID = @"resultSummary";

@interface MasterNavController ()
@property(strong, nonatomic) SignInViewController *signInViewController;
@property(strong, nonatomic) UIViewController *resultSummaryViewController;
@end

@implementation MasterNavController

// CONSTANTS //////////////////////////////////////////////////////////////////

+ (NSString*)viewedInfoKey {
    return @"viewedInfoKey";
}

+ (NSString*)viewedVersionNumberKey {
    return @"viewedVersionNumber";
}

+ (NSString*)signedIntoGoogleKey {
    return @"signedIntoGoogle";
}

+ (NSString*)downloadedMovesKey {
    return @"downloadedMoves";
}

+ (NSString*)linkedToMovesKey {
    return @"linkedToMoves";
}

///////////////////////////////////////////////////////////////////////////////

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
    [self updateViewOnState];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)updateViewOnState
{
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    bool viewedInfo = [defaults stringForKey:[MasterNavController viewedInfoKey]];
    NSString *versionNumber = [ConsentViewController termsVersionNumber];
    NSString *viewedVersionNumber = [defaults stringForKey:[MasterNavController viewedVersionNumberKey]];
    NSLog(@"The last viewed version number: %@", viewedVersionNumber);
    bool signedIntoGoogle = [defaults stringForKey:[MasterNavController signedIntoGoogleKey]];
    // bool downloadedMoves = [defaults stringForKey:[MasterNavController downloadedMovesKey]];
    
    // because of the dependency on moves, this can only be run on a real phone, which makes testing
    // more difficult, specially for the onboarding flow. So we skip the moves checks if we detect that
    // we are running in the simulator
    // http://stackoverflow.com/questions/5122149/iphone-simulator-how-to-detect-when-app-is-running-on-simulator-so-can-setup
    
#if TARGET_IPHONE_SIMULATOR
    // Simulator specific code
    bool downloadedMoves = true;
    bool linkedToMoves = true;
#else // TARGET_IPHONE_SIMULATOR
    // Device specific code
    NSURL *movesURL = [[ConnectionSettings sharedInstance] getMovesURL];
    bool downloadedMoves =  [[UIApplication sharedApplication] canOpenURL:movesURL];
    bool linkedToMoves = [defaults stringForKey:[MasterNavController linkedToMovesKey]];
#endif // TARGET_IPHONE_SIMULATOR
    
    UIStoryboard *board = [UIStoryboard storyboardWithName:@"MainStoryboard_iPhone" bundle:nil];
    UIViewController *controller;
    
    if (!viewedInfo) {
        controller = [board instantiateViewControllerWithIdentifier:@"InfoViewController"];
        [self pushViewController:controller animated:YES];
    } else if (![versionNumber isEqualToString: viewedVersionNumber]) {
        controller = [board instantiateViewControllerWithIdentifier:@"ConsentViewController"];
        [self pushViewController:controller animated:YES];
    } else if (!signedIntoGoogle) {
        controller = [board instantiateViewControllerWithIdentifier:@"SetUpViewController"];
        SetUpViewController *tempController = (SetUpViewController*)controller;
        tempController.disableMovesButtonsController = YES;
        [self pushViewController:controller animated:YES];
    } else if (!downloadedMoves) {
        controller = [board instantiateViewControllerWithIdentifier:@"SetUpViewController"];
        SetUpViewController *tempController = (SetUpViewController*)controller;
        tempController.disableGoogleButtonController = YES;
        [self pushViewController:controller animated:YES];
    } else if (!linkedToMoves) {
        controller = [board instantiateViewControllerWithIdentifier:@"SetUpViewController"];
        SetUpViewController *tempController = (SetUpViewController*)controller;
        tempController.disableGoogleButtonController = YES;
        // tempController.switchMovesButtonsController = YES;
        tempController.hideConnectMovesButtonController = NO;
        tempController.hideDownloadMovesButtonController = YES;
        [self pushViewController:controller animated:YES];
    } else {
        // controller = [board instantiateViewControllerWithIdentifier:@"MasterViewController"];
        EmbeddedCordovaViewController *cordovaController = [[EmbeddedCordovaViewController alloc] init];
        cordovaController.startPage = @"listview.html";
        self.signInViewController = [[SignInViewController alloc] initWithNibName:nil bundle:nil];
        self.resultSummaryViewController = [board instantiateViewControllerWithIdentifier:kResultSummaryStoryboardID];
        UIBarButtonItem *authButton = [[UIBarButtonItem alloc] initWithTitle:@"Auth" style:UIBarButtonItemStyleBordered target: self action:@selector(showSignInView:)];
        UIBarButtonItem *resultButton = [[UIBarButtonItem alloc] initWithTitle:@"Result" style:UIBarButtonItemStyleBordered target: self action:@selector(showResults:)];
        cordovaController.navigationItem.leftBarButtonItems = @[authButton, resultButton];
        [self setViewControllers:[[NSArray alloc] initWithObjects:cordovaController, nil] animated:YES]; // for coming out of onboarding process
        NSLog(@"parent controller is %@", cordovaController.parentViewController);
    }
}

- (void)showSignInView:(id)sender
{
    if ([self.navigationController.viewControllers containsObject:self.signInViewController]) {
        // the sign in view is already visible, don't need to push it again
        NSLog(@"sign in view is already in the navigation chain, skipping the push to the controller...");
    } else {
        [self pushViewController:self.signInViewController animated:YES];
    }
}

- (void)showResults:(id)sender
{
    if ([self.navigationController.viewControllers containsObject:self.resultSummaryViewController]) {
        // the result summary is already visible, don't need to push it again
        NSLog(@"resultSummaryView is already in the navigation chain, skipping the push to the controller...");
    } else {
        [self pushViewController:self.resultSummaryViewController animated:YES];
    }
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

@end
