//
//  HTMLDisplayCommunicationHelper.h
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 4/13/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "CommunicationHelper.h"
#import "CustomSettings.h"

@interface HTMLDisplayCommunicationHelper : CommunicationHelper
+(void)displayResultSummary:(UIWebView*)webView completionHandler:(void (^)(NSData *data, NSURLResponse *response, NSError *error))completionHandler;

@property (nonatomic, strong) UIWebView* webView;
@end
