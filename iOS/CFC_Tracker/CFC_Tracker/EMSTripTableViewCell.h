//
//  EMSTableViewCell.h
//  E-Mission
//
//  Created by Neeraj Baid on 2/5/15.
//  Copyright (c) 2015 Kalyanaraman Shankari. All rights reserved.
//

#define EMSTripTableViewCellReuseIdentifier @"TripCell"

#import <UIKit/UIKit.h>

@class TripSection;
@interface EMSTripTableViewCell : UITableViewCell

- (void)prepareWithSection:(TripSection *)section;

@end
