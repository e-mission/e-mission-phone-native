//
//  DetailViewController.h
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/23/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MKMapView.h>
// TODO: Figure out a way to move this into the .m file later
#import "TripSection.h"
#import "WYPopoverController.h"
#import "PopupTableViewController.h"

@interface DetailViewController : UIViewController <UISplitViewControllerDelegate, UIPickerViewDataSource, UIPickerViewDelegate, MKMapViewDelegate, UITableViewDelegate>

@property (strong, nonatomic) TripSection* detailItem;

@property (weak, nonatomic) IBOutlet UILabel *detailDescriptionLabel;

@property (weak, nonatomic) IBOutlet UILabel *startTimeLabel;
@property (weak, nonatomic) IBOutlet UILabel *endTimeLabel;
@property (weak, nonatomic) IBOutlet UILabel *correctQuestionLabel;
@property (weak, nonatomic) IBOutlet UISegmentedControl *yesNoChooser;
@property (weak, nonatomic) IBOutlet UILabel *autoModeLabel;
@property (weak, nonatomic) IBOutlet UILabel *stringModeLabel;
@property (weak, nonatomic) IBOutlet MKMapView *map;
@property (strong, nonatomic) IBOutlet UIButton *modeSelectionButton;
@property (strong, nonatomic) IBOutlet UILabel *transportQuestionLabel;

@end
