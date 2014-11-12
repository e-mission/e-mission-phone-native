//
//  InfoViewController.m
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "InfoViewController.h"

@interface InfoViewController ()

@end

@implementation InfoViewController

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
    
    self.infoWebView.delegate = self;
    
    // NSURL *url = [NSURL URLWithString:@"/consent" relativeToURL:[[ConnectionSettings sharedInstance] getConnectUrl]; // to get the terms from online
    NSURL *url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"EMissionSummary" ofType:@"html"]];
    NSURLRequest *urlRequest = [NSURLRequest requestWithURL:url];
    [self.infoWebView loadRequest:urlRequest];
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

- (IBAction)onGetStarted:(id)sender {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSLog(@"Saving version number");
    [defaults setBool:YES forKey:[MasterNavController viewedInfoKey]];
    [self callStateMachine];
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
