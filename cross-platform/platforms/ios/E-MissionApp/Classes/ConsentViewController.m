//
//  ConsentViewController.m
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "ConsentViewController.h"
#import "TripSectionDatabase.h"

@interface ConsentViewController ()

@end

@implementation ConsentViewController

+ (NSString*)termsVersionNumber
{
    return @"1.0";
}

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
    
    self.conditionWebView.delegate = self;
    
    // NSURL *url = [NSURL URLWithString:@"/consent" relativeToURL:[[ConnectionSettings sharedInstance] getConnectUrl]; // to get the terms from online
    NSURL *url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"EMissionTerms" ofType:@"html"]];
    NSURLRequest *urlRequest = [NSURLRequest requestWithURL:url];
    [self.conditionWebView loadRequest:urlRequest];
    self.automaticallyAdjustsScrollViewInsets = NO;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
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

- (IBAction)onAgreeTerms:(id)sender {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSLog(@"Saving version number");
    [defaults setObject:[ConsentViewController termsVersionNumber] forKey:[MasterNavController viewedVersionNumberKey]];
    // Delete all existing trips in the database because they are the old version and with the old format
    // Should only need this when we make incompatible changes
    // should comment out otherwise?
    TripSectionDatabase* _tripSectionDb = [[TripSectionDatabase alloc] init];
    [_tripSectionDb clear];
    [self callStateMachine];
}

- (IBAction)onRefuseTerms:(id)sender {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Conditions Refused" message:@"Please close and uninstall this application. You must accept the terms to use E-Mission." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
}

- (void)callStateMachine {
    [((MasterNavController*)self.navigationController) updateViewOnState];
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

@end
