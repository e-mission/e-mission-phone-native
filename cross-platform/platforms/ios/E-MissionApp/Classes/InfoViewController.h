//
//  ConsentViewController.h
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MasterNavController.h"

@interface InfoViewController : UIViewController <UIWebViewDelegate, UIAlertViewDelegate>

@property (strong, nonatomic) IBOutlet UIWebView *infoWebView;

- (IBAction)onGetStarted:(id)sender;

@end
