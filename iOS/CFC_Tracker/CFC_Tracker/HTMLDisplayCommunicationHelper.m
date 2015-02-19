//
//  HTMLDisplayCommunicationHelper.m
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 4/13/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "HTMLDisplayCommunicationHelper.h"
#import "AuthCompletionHandler.h"
#import "ConnectionSettings.h"

@interface HTMLDisplayCommunicationHelper()

@end

//#define kResultSummaryURL [NSURL URLWithString: @"/compare" \
//                                 relativeToURL:[[ConnectionSettings sharedInstance] getConnectUrl]]
//#define kResultSummaryURL [NSURL URLWithString: ((AppDelegate*)[[UIApplication sharedApplication] delegate]).temp relativeToURL: [[ConnectionSettings sharedInstance] getConnectUrl]];
// #define kResultSummaryURL [NSURL URLWithString: @"http://localhost:8080/compare"]

@implementation HTMLDisplayCommunicationHelper

+(void)displayResultSummary:displayView completionHandler:(void (^)(NSData *data, NSURLResponse *response, NSError *error))completionHandler {
    NSLog(@"HTMLDisplayCommunicationHelper.displayResultSummary called!");
    NSMutableDictionary *blankDict = [[NSMutableDictionary alloc] init];
    
    NSString *str = [[CustomSettings sharedInstance] getResultsURL];
    
    NSURL *url = [NSURL URLWithString:str];
    
    if (str == nil) {
        NSLog(@"No result URL in CustomSettings. Reverting to default url.");
        url = [NSURL URLWithString:@"/compare" relativeToURL:[[ConnectionSettings sharedInstance] getConnectUrl]];
    }
    
    HTMLDisplayCommunicationHelper *executor = [[HTMLDisplayCommunicationHelper alloc] initPost:url data:blankDict
                                                              displayView:displayView completionHandler:completionHandler];
    [executor execute];
}

-(id)initPost:(NSURL *)url data: (NSMutableDictionary *)jsonDict
                    displayView: (UIWebView*)displayView
              completionHandler: (void (^)(NSData *, NSURLResponse *, NSError *))completionHandler {
    self = [super initPost:url data:jsonDict completionHandler:completionHandler];
    self.webView = displayView;
    return self;
}

- (void)postToHost {
    NSLog(@"postToHost called with url = %@", self.mUrl);
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc]
                                    initWithURL:self.mUrl];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json"
   forHTTPHeaderField:@"Content-Type"];
    
    NSString *userToken = [AuthCompletionHandler sharedInstance].getIdToken;
    // At this point, we assume that all the authentication is done correctly
    // Should I try to verify making a remote call or add that to a debug screen?
    [self.mJsonDict setObject:userToken forKey:@"user"];
    
    NSError *parseError;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self.mJsonDict
                                                       options:kNilOptions
                                                         error:&parseError];
    if (parseError) {
        self.mCompletionHandler(jsonData, nil, parseError);
    } else {
        [request setHTTPBody:jsonData];
        [self.webView loadRequest:request];
        self.mCompletionHandler(jsonData, nil, nil);
    }
}

@end
