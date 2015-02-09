//
//  EMSTableViewCell.m
//  E-Mission
//
//  Created by Neeraj Baid on 2/5/15.
//  Copyright (c) 2015 Kalyanaraman Shankari. All rights reserved.
//

#import "EMSTripTableViewCell.h"
#import "TripSection.h"

@interface EMSTripTableViewCell ()

@property (weak, nonatomic) IBOutlet UIImageView *modeImageView;
@property (weak, nonatomic) IBOutlet UILabel *modeLabel;
@property (weak, nonatomic) IBOutlet UILabel *durationLabel;
@property (weak, nonatomic) IBOutlet UILabel *timeLabel;
@property (weak, nonatomic) IBOutlet UILabel *dateLabel;
@property (weak, nonatomic) IBOutlet UILabel *confidenceLabel;

@end

@implementation EMSTripTableViewCell

- (void)prepareWithSection:(TripSection *)section {
    NSCalendar *cal = [NSCalendar currentCalendar];
    
    //getting start/end time
    NSDate *startTime = [section startTime];
    NSDate *endTime = [section endTime];
    NSTimeInterval tripDur = [endTime timeIntervalSinceDate:startTime];
    double secToMin = 60;
    NSInteger tripDurMin = tripDur / secToMin;
    
    NSString *tripModeString = section.getDisplayMode;
    
    //get trip startDate
    NSDateComponents *startComps = [cal components:(NSEraCalendarUnit|NSYearCalendarUnit|NSMonthCalendarUnit|NSDayCalendarUnit) fromDate: startTime];
    NSDate *startDate = [cal dateFromComponents:startComps];
    
    //get today's date
    NSDateComponents *todayComps = [cal components:(NSEraCalendarUnit|NSYearCalendarUnit|NSMonthCalendarUnit|NSDayCalendarUnit) fromDate: [NSDate date]];
    NSDate *today = [cal dateFromComponents:todayComps];
    
    //get yesterday's date
    NSDate *yesterday = [today dateByAddingTimeInterval:-86400.00];
    
    //check if trip is from today or yesterday and build appropriate prompt string
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    // Modified by shankari to shorten the detail even further. Andrew plans to tweak this later, but we want to
    // get something good enough by the end of this week
    
    //set the timeLabel to the start time
    [dateFormat setDateFormat:@"HH:mm"];
    NSString *startTimeString = [dateFormat stringFromDate:startTime];
    self.timeLabel.text = startTimeString;
    
    //set durationLabel and modeLabel to appropriate values
    self.durationLabel.text = [NSString stringWithFormat:@"%ld minutes", tripDurMin];
    self.modeLabel.text = tripModeString;
    
    //check tripMode and set image to appropriate one
    UIColor *red = [UIColor redColor];
    UIColor *yellow = [UIColor colorWithRed:0.93 green:0.79 blue:0 alpha:1];
    UIColor *green = [UIColor colorWithRed:0 green:0.5 blue:0 alpha:1];
    
    // you need to use the inverse because masking will take the white space so this makes processing a lot simpler
    UIImage *image = [UIImage imageNamed:[NSString stringWithFormat:@"%@-invert.jpg", tripModeString]];
    
    if (section.selMode == nil) {
        if (section.confidence >= 0.79) {
            self.confidenceLabel.textColor = green;
            image = [UIImage colorizeImage:image withColor:green];
        } else if (section.confidence >= 0.69) {
            self.confidenceLabel.textColor = yellow;
            image = [UIImage colorizeImage:image withColor:yellow];
        } else {
            self.confidenceLabel.textColor = red;
            image = [UIImage colorizeImage:image withColor:red];
        }
        self.confidenceLabel.text = [section getConfidenceAsString];
    } else {
        self.confidenceLabel.textColor = green;
        image = [UIImage colorizeImage:image withColor:green];
        
        self.confidenceLabel.text = @"100%";
    }
    
    self.modeImageView.image = image;
    
    //check for special values of dateLabel (like yesterday and today)
    if([today isEqual:startDate]) {
        self.dateLabel.text = @"today";
    } else if ([yesterday isEqual: startDate]) {
        self.dateLabel.text = @"yesterday";
    } else {
        [dateFormat setDateFormat:@"MM/dd"];
        NSString *startDateString = [dateFormat stringFromDate:startTime];
        self.dateLabel.text = startDateString;
    }
}

@end
