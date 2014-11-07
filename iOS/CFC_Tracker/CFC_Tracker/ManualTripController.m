//
//  ManualTripControllerViewController.m
//  E-Mission
//
//  Created by Kalyanaraman Shankari on 10/31/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "ManualTripController.h"
#import "OngoingTripsDatabase.h"
#import "PopupTableViewController.h"
#import "WYPopoverController.h"
#import <CoreLocation/CoreLocation.h>

@interface ManualTripController () <WYPopoverControllerDelegate, UITableViewDelegate, UIPickerViewDataSource, UIPickerViewDelegate, CLLocationManagerDelegate> {
    WYPopoverController *tablePopover;
    int modeSelection;
}

@property (weak, nonatomic) IBOutlet UIButton *startTripButton;
@property (weak, nonatomic) IBOutlet UIButton *stopTripButton;
@property (weak, nonatomic) IBOutlet UILabel *statusLabel;
@property (weak, nonatomic) IBOutlet UIButton *modePicker;
@property (weak, nonatomic) IBOutlet UILabel *startTimeLabel;
@property (weak, nonatomic) IBOutlet UILabel *startLocationLabel;

@end

@implementation ManualTripController {
    OngoingTripsDatabase* _ongoingTripsDb;
}

NSArray* modeChoices;

- (void)viewDidLoad {
    [super viewDidLoad];
    modeChoices = @[@"walking", @"cycling", @"bus", @"train", @"car", @"mixed", @"air", @"not a trip"];
    _ongoingTripsDb = [[OngoingTripsDatabase alloc] init];
    [self refreshTripLabels];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)startTrip:(id)sender {
    NSString *chosenTripMode = modeChoices[modeSelection];
    [_ongoingTripsDb startTrip:self.modePicker.titleLabel.text];
    [self refreshTripLabels];
}

- (IBAction)endTrip:(id)sender {
    // A lot of the computation here happens asynchronously, and when those actions complete,
    // they will in turn trigger others. So not much to do here.
    [self markFinishedTrip];
    // Push value to server
    [self refreshTripLabels];
}

-(void)markFinishedTrip {
    CLLocationManager* locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    locationManager.desiredAccuracy = kCLLocationAccuracyKilometer; //meters
    locationManager.distanceFilter = 100; //meters
    if ([locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
        [locationManager requestWhenInUseAuthorization];
    }
    [locationManager startUpdatingLocation];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    NSDictionary* ongoingTrip = [_ongoingTripsDb getOngoingTrip];
    NSMutableDictionary* completedTrip = [NSMutableDictionary dictionaryWithDictionary:ongoingTrip];
    NSDictionary *endPoint = [OngoingTripsDatabase toGeoJSON:newLocation];
    [completedTrip setObject:endPoint forKey:@"section_end"];
    [completedTrip setObject:[NSDate date] forKey:@"section_end_time"];
    
    // Now let's send!
}

- (IBAction)popupMode:(id)sender {
    self.modePicker.titleLabel.text = @"train";
    [self.startTripButton setEnabled:TRUE];

    /*
     * TODO: Gautham, I tried to copy and paste the code from the detail view, but it doesn't quite work.
     * I suspect that you will be able to debug this significantly faster than me.
     * So right, now, when you pick mode it always chooses "train".
     * Please fix it! Thanks!
     */

    // [self showPopover:sender];
}

-(void)refreshTripLabels {
    NSDictionary* ongoingTrip = [_ongoingTripsDb getOngoingTrip];
    if (ongoingTrip != NULL) {
        [self.statusLabel setText:@"Ongoing"];
        [self.startTimeLabel setText:[ongoingTrip objectForKey:@"section_start_time"]];
        
        self.modePicker.titleLabel.text = [ongoingTrip objectForKey:@"mode"];
        [self.modePicker setEnabled:FALSE];
        NSString* latLngText = [NSString stringWithFormat:@"%@, %@",
                                [ongoingTrip objectForKey:@"startLat"],
                                [ongoingTrip objectForKey:@"startLng"]];
        [self.startLocationLabel setText:latLngText];
        [self.startTripButton setEnabled:FALSE];
        [self.stopTripButton setEnabled:TRUE];
    } else {
        self.statusLabel.text = @"Not started";
        [self.modePicker setEnabled:TRUE];
        self.startTimeLabel.text = @"Not Started";
        self.startLocationLabel.text = @"Not Started";
        [self.startTripButton setEnabled:FALSE];
        [self.stopTripButton setEnabled:FALSE];
    }
}

- (void)showPopover:(id)sender {
    if (tablePopover == nil) {
        UIView *btn = (UIView *) sender;
        UITableViewController *tableView = [[PopupTableViewController alloc] initWithEntries:modeChoices];
        tableView.preferredContentSize = CGSizeMake(120, 200);
        tableView.modalInPopover = NO;
        tableView.tableView.delegate = self;
        
        tablePopover = [[WYPopoverController alloc] initWithContentViewController:self];
        tablePopover.delegate = self;
        tablePopover.passthroughViews = @[btn];
        tablePopover.popoverLayoutMargins = UIEdgeInsetsMake(10, 10, 10, 10);
        tablePopover.wantsDefaultContentAppearance = NO;
        [tablePopover presentPopoverFromRect:btn.bounds
                                      inView:btn
                    permittedArrowDirections:WYPopoverArrowDirectionUp
                                    animated:YES
                                     options:WYPopoverAnimationOptionFade];
    } else {
        [self closePopOver:nil];
    }
}

- (void)closePopOver:(id)sender
{
    [tablePopover dismissPopoverAnimated:YES];
    [tablePopover dismissPopoverAnimated:YES completion:^{
        [self popoverControllerDidDismissPopover:tablePopover];
    }];
}

- (void)popoverControllerDidDismissPopover:(WYPopoverController *)controller
{
    tablePopover.delegate = nil;
    tablePopover = nil;
    [self.startTripButton setEnabled:TRUE];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    modeSelection = (int) indexPath.row;
    [self closePopOver:nil];
    [self.startTripButton setEnabled:TRUE];
}

#pragma mark - PickerViewDataSource
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    // since we have only one category
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    assert(component == 0);
    return modeChoices.count;
}


#pragma mark - PickerViewDelegate

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    assert(component == 0);
    return modeChoices[row];
}

// This should theoretically be in the IBActions, but the picker appears to be a strange component and put this into its delegate
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row   inComponent:(NSInteger)component
{
    assert(component == 0);
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
