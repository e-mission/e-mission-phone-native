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
//  MainViewController.h
//  E-MissionApp
//
//  Created by ___FULLUSERNAME___ on ___DATE___.
//  Copyright ___ORGANIZATIONNAME___ ___YEAR___. All rights reserved.
//

#import "MainViewController.h"
#import "SignInViewController.h"
#import "ClientStatsDatabase.h"
#import "HTMLDisplayCommunicationHelper.h"

@interface MainViewController () {
    ClientStatsDatabase* _statsDb;
}
@property(strong, nonatomic) SignInViewController *signInViewController;
@property(strong, nonatomic) UIViewController *resultSummaryViewController;
@property BOOL hasShownResults;
@end


@implementation MainViewController
static NSString * const kMainStoryboardName = @"MainStoryboard_iPhone";
static NSString * const kResultSummaryStoryboardID = @"resultSummary";

- (id)initWithNibName:(NSString*)nibNameOrNil bundle:(NSBundle*)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Uncomment to override the CDVCommandDelegateImpl used
        // _commandDelegate = [[MainCommandDelegate alloc] initWithViewController:self];
        // Uncomment to override the CDVCommandQueue used
        // _commandQueue = [[MainCommandQueue alloc] initWithViewController:self];
    }
    return self;
}

- (id)init
{
    self = [super init];
    if (self) {
        // Uncomment to override the CDVCommandDelegateImpl used
        // _commandDelegate = [[MainCommandDelegate alloc] initWithViewController:self];
        // Uncomment to override the CDVCommandQueue used
        // _commandQueue = [[MainCommandQueue alloc] initWithViewController:self];
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];

    // Release any cached data, images, etc that aren't in use.
}

#pragma mark View lifecycle

- (void)viewWillAppear:(BOOL)animated
{
    // View defaults to full size.  If you want to customize the view's size, or its subviews (e.g. webView),
    // you can do so here.

    [super viewWillAppear:animated];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    // Let's add the auth and result bar buttons just to highlight that this is a hybrid app
    [self initializeAuthResultBarButtons];
    self.signInViewController = [[SignInViewController alloc] initWithNibName:nil bundle:nil];
    UIStoryboard *sb = [UIStoryboard storyboardWithName:kMainStoryboardName bundle:nil];
    self.resultSummaryViewController = [sb instantiateViewControllerWithIdentifier:kResultSummaryStoryboardID];
    _statsDb = [[ClientStatsDatabase alloc] init];
}

- (void)initializeAuthResultBarButtons
{
    UIBarButtonItem *authButton = [[UIBarButtonItem alloc] initWithTitle:@"Auth" style:UIBarButtonItemStyleBordered target: self action:@selector(showSignInView:)];
    UIBarButtonItem *resultButton = [[UIBarButtonItem alloc] initWithTitle:@"Result" style:UIBarButtonItemStyleBordered target: self action:@selector(showResults:)];
    /*    UIBarButtonItem *stupidAppReviewButton = [[UIBarButtonItem alloc] initWithTitle:@"SAP" style:UIBarButtonItemStyleBordered target: self action:@selector(showManualTripScreen:)];
     self.navigationItem.leftBarButtonItems = @[authButton, resultButton, stupidAppReviewButton];
     */
    self.navigationItem.leftBarButtonItems = @[authButton, resultButton];
}

- (void)showSignInView:(id)sender
{
    if ([self.navigationController.viewControllers containsObject:self.signInViewController]) {
        // the sign in view is already visible, don't need to push it again
        NSLog(@"sign in view is already in the navigation chain, skipping the push to the controller...");
    } else {
        [self.navigationController pushViewController:self.signInViewController animated:YES];
    }
}

- (void)showResults:(id)sender
{
    long startTime = [ClientStatsDatabase getCurrentTimeMillis];
    if ([self.navigationController.viewControllers containsObject:self.resultSummaryViewController]) {
        // the result summary is already visible, don't need to push it again
        NSLog(@"resultSummaryView is already in the navigation chain, skipping the push to the controller...");
    } else {
        [self.navigationController pushViewController:self.resultSummaryViewController animated:YES];
    }
    // NSLog(@"subviews are %@", self.resultSummaryViewController.view.subviews);
    // NSLog(@"view with tag 0 is %@", [self.resultSummaryViewController.view viewWithTag:0]);
    // UIWebView *webView = (UIWebView*)[self.resultSummaryViewController.view viewWithTag:0];
    UIWebView *webView = (UIWebView*)self.resultSummaryViewController.view.subviews[0];
    // [webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"http://localhost:8080/compare"]]];
    [HTMLDisplayCommunicationHelper displayResultSummary:webView
                                       completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
                                           // This is currently used only for generating stats, need to figure out how to display errors
                                           long endTime = [ClientStatsDatabase getCurrentTimeMillis];
                                           NSString* endTimeStr = [ClientStatsDatabase getCurrentTimeMillisString];
                                           [_statsDb storeMeasurement:@"result_display_duration" value:[@(endTime - startTime) stringValue] ts:endTimeStr];
                                           if(error != NULL) {
                                               [_statsDb storeMeasurement:@"result_display_failed" value:NULL ts:endTimeStr];
                                           }
                                       }];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return [super shouldAutorotateToInterfaceOrientation:interfaceOrientation];
}

/* Comment out the block below to over-ride */

/*
- (UIWebView*) newCordovaViewWithFrame:(CGRect)bounds
{
    return[super newCordovaViewWithFrame:bounds];
}
*/

#pragma mark UIWebDelegate implementation

- (void)webViewDidFinishLoad:(UIWebView*)theWebView
{
    // Black base color for background matches the native apps
    theWebView.backgroundColor = [UIColor blackColor];

    return [super webViewDidFinishLoad:theWebView];
}

/* Comment out the block below to over-ride */

/*

- (void) webViewDidStartLoad:(UIWebView*)theWebView
{
    return [super webViewDidStartLoad:theWebView];
}

- (void) webView:(UIWebView*)theWebView didFailLoadWithError:(NSError*)error
{
    return [super webView:theWebView didFailLoadWithError:error];
}

- (BOOL) webView:(UIWebView*)theWebView shouldStartLoadWithRequest:(NSURLRequest*)request navigationType:(UIWebViewNavigationType)navigationType
{
    return [super webView:theWebView shouldStartLoadWithRequest:request navigationType:navigationType];
}
*/

@end

@implementation MainCommandDelegate

/* To override the methods, uncomment the line in the init function(s)
   in MainViewController.m
 */

#pragma mark CDVCommandDelegate implementation

- (id)getCommandInstance:(NSString*)className
{
    return [super getCommandInstance:className];
}

- (NSString*)pathForResource:(NSString*)resourcepath
{
    return [super pathForResource:resourcepath];
}

@end

@implementation MainCommandQueue

/* To override, uncomment the line in the init function(s)
   in MainViewController.m
 */
- (BOOL)execute:(CDVInvokedUrlCommand*)command
{
    return [super execute:command];
}

@end
