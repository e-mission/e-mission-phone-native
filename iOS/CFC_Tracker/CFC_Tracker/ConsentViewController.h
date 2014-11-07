//
//  ConsentViewController.h
//  E-Mission
//
//  Created by Gautham Kesineni on 8/5/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MasterNavController.h"

@interface ConsentViewController : UIViewController <UIWebViewDelegate, UIAlertViewDelegate>

@property (strong, nonatomic) IBOutlet UIWebView *conditionWebView;

+ (NSString*)termsVersionNumber;

- (IBAction)onAgreeTerms:(id)sender;
- (IBAction)onRefuseTerms:(id)sender;

@end
