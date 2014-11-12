//
//  DetailViewController.m
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/23/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "DetailViewController.h"
#import "TrackLocation.h"
#import "TripSectionDatabase.h"

@interface DetailViewController () <WYPopoverControllerDelegate> {
    WYPopoverController *tablePopover;
    int modeSelection;
}
    @property (strong, nonatomic) UIPopoverController *masterPopoverController;
    @property MKPolyline *mapLine;
    - (void)configureView;
    - (MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id<MKOverlay>)overlay;
    - (IBAction)pickerButtonClicked:(id)sender;
    - (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation;
    - (void)popoverControllerDidDismissPopover:(WYPopoverController *)popoverController;
    - (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath;
@end

@implementation DetailViewController
// TODO: See why we can't put this into the interface although we do it in MasterViewController()
TripSectionDatabase* _tripSectionDb;
// TODO: Need to have a unified list - maybe this will become part of the profile?
NSArray* modeChoices;

#pragma mark - Managing the detail item

- (void)setDetailItem:(id)newDetailItem
{
    if (_detailItem != newDetailItem) {
        _detailItem = newDetailItem;
        
        // Update the view.
        [self configureView];
    }
    _tripSectionDb = [[TripSectionDatabase alloc] init];
    modeChoices = @[@"walking", @"cycling", @"bus", @"train", @"car", @"mixed", @"air", @"not a trip"];
    
    if (self.masterPopoverController != nil) {
        [self.masterPopoverController dismissPopoverAnimated:YES];
    }        
}

- (void)configureView
{
    // Update the user interface for the detail item.
    self.map.delegate = self;
    
    self.modeSelectionButton.hidden = true;
    self.transportQuestionLabel.hidden = true;
    
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Confirm" style:UIBarButtonItemStyleDone target:self action:@selector(confirmIndividualTrip)];
    [self.navigationItem.rightBarButtonItem setEnabled:NO];
    
    if (self.detailItem) {
        self.detailDescriptionLabel.text = [self.detailItem description];
    }
    
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"MM/dd HH:mm"];
    self.startTimeLabel.text = [dateFormat stringFromDate:self.detailItem.startTime];
    self.endTimeLabel.text = [dateFormat stringFromDate:self.detailItem.endTime];
    
    NSString *actingAutoMode;
    if(self.detailItem.selMode == nil){
        actingAutoMode = self.detailItem.autoMode;
    } else {
        actingAutoMode = self.detailItem.selMode;
    }
    
    self.autoModeLabel.text = actingAutoMode;
    
    if ([actingAutoMode isEqualToString:@"transport"]) {
        [self adjustDisplayForTransport];
    }
    
    NSArray *trackPoints = self.detailItem.trackPoints;
    [self.map addAnnotations:trackPoints];
    
    [self drawLine];
    
    if (trackPoints.count > 1) {
        // TODO: Shouldn't we center to the midpoint of the first and last point?
        TrackLocation* firstPoint = trackPoints[0];
        TrackLocation* lastPoint = trackPoints[trackPoints.count - 1];
        
        double minLat = [firstPoint.lat doubleValue];
        double minLng = [firstPoint.lng doubleValue];
        double maxLat = minLat;
        double maxLng = minLng;
        for (int i = 0; i < trackPoints.count; i++) {
            CLLocationCoordinate2D currPoint = ((TrackLocation*)trackPoints[i]).coordinate;
//            NSLog(@"Considering point (%f, %f)", currPoint.latitude, currPoint.longitude);
            
            if (currPoint.latitude > maxLat) { maxLat = currPoint.latitude;};
            if (currPoint.latitude < minLat) { minLat = currPoint.latitude;};
        
            if (currPoint.longitude > maxLng) { maxLng = currPoint.longitude;};
            if (currPoint.longitude < minLng) { minLng = currPoint.longitude;};
        }
        NSLog(@"lat(min, max) = (%f, %f), lng(min, max) = (%f, %f)", minLat, maxLat, minLng, maxLng);
        
        CLLocationCoordinate2D mapCenter;
        mapCenter.latitude = (maxLat + minLat)/2;
        mapCenter.longitude = (maxLng + minLng)/2;

        MKCoordinateSpan mapSpan;
        mapSpan.latitudeDelta = (maxLat - minLat) + 0.001;
        mapSpan.longitudeDelta = (maxLng - minLng) + 0.001;
        
        NSLog(@"mapCenter = %f %f, mapSpan = %f %f", (maxLat+minLat)/2, maxLat+minLat/2,
              maxLat - minLat, maxLng - minLng);
        
        MKCoordinateRegion mapRegion;
        mapRegion.center = mapCenter;
        mapRegion.span = mapSpan;
    
        self.map.region = mapRegion;
        self.map.mapType = MKMapTypeStandard;
    }
}

- (void)confirmIndividualTrip {
    [_tripSectionDb storeUserClassificationForTripSection:self.detailItem];
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)drawLine
{
    [self.map removeOverlay:self.mapLine];
    
    NSArray *annotations = self.map.annotations;
    NSSortDescriptor *sorter = [NSSortDescriptor sortDescriptorWithKey:@"sampleTime" ascending:YES];
    
    annotations = [annotations sortedArrayUsingDescriptors:[NSArray arrayWithObject:sorter]];
    ((TrackLocation *)[annotations firstObject]).first = true;
    ((TrackLocation *)[annotations lastObject]).last = true;
    
    CLLocationCoordinate2D coordinates[self.map.annotations.count];
    for (int i=0; i<annotations.count; i++) {
        TrackLocation *temp = [annotations objectAtIndex:i];
        coordinates[i] = ((TrackLocation*)temp).coordinate;
    }
    
    MKPolyline *line = [MKPolyline polylineWithCoordinates:coordinates count:annotations.count];
    self.mapLine = line;
    
    [self.map addOverlay:line];
}

- (MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id<MKOverlay>)overlay
{
    if ([overlay isKindOfClass:[MKPolyline class]]  && self.mapLine.pointCount != 0) {
        MKPolyline *route = self.mapLine;
        MKPolylineView *view = [[MKPolylineView alloc] initWithPolyline:route];
        view.strokeColor = [UIColor purpleColor];
        view.lineWidth = 5;
        return view;
    }
    return nil;
}

- (IBAction)pickerButtonClicked:(id)sender {
    [self showPopover:sender];
}

- (void)showPopover:(id)sender {
    if (tablePopover == nil) {
        UIView *btn = (UIView *) sender;
        
        UITableViewController *tableView = [[PopupTableViewController alloc] initWithEntries:modeChoices];
        
        tableView.preferredContentSize = CGSizeMake(120, 200);
        
        tableView.modalInPopover = NO;
        
        tableView.tableView.delegate = self;
        
        tablePopover = [[WYPopoverController alloc] initWithContentViewController:tableView];
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
}


- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation
{
    MKPinAnnotationView *pinView = (MKPinAnnotationView*)[mapView dequeueReusableAnnotationViewWithIdentifier:@"TrackLocation"];
    
    if (!pinView) {
        pinView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"TrackLocation"];
        
        if (((TrackLocation *) annotation).first) {
            pinView.pinColor = MKPinAnnotationColorGreen;
        } else if (((TrackLocation *) annotation).last) {
            pinView.pinColor = MKPinAnnotationColorRed;
        } else {
            pinView.pinColor = MKPinAnnotationColorPurple;
        }
        
    } else {
        pinView.annotation = annotation;
    }
    return pinView;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    [self configureView];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - TableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    modeSelection = (int) indexPath.row;
    [self closePopOver:nil];
    NSString *chosenTripMode = modeChoices[modeSelection];
    [self.detailItem setSelectedMode: chosenTripMode];
    self.autoModeLabel.text = chosenTripMode;
    [self.yesNoChooser setSelectedSegmentIndex:0];
    [self adjustDisplayForYes];
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

#pragma mark - IBActions

- (IBAction)onUserModeChoice:(UISegmentedControl *)sender {
    if (sender.selectedSegmentIndex == 0) {
        // mode was correct
        [self adjustDisplayForYes];
    } else {
        // mode was incorrect
        
        // Let us quickly check that this has only two modes
        assert(sender.selectedSegmentIndex == 1);
        
        [self adjustDisplayForNo];
    }
}

- (void) adjustDisplayForNo {
    // This single button accomplishes what all the previous labels and buttons tried to do.
    // This will indicate to the user that they need to choose something.
    self.modeSelectionButton.hidden = false;
    [self.navigationItem.rightBarButtonItem setEnabled:NO];
}

- (void) adjustDisplayForYes {
    self.modeSelectionButton.hidden = true;
    [self.navigationItem.rightBarButtonItem setEnabled:YES];
}

- (void) adjustDisplayForTransport {
    // Transport should be handled a little differently than before. A label prompting the user
    // to choose a mode of transportation will popup and the selection button will also appear
    // this differs from what was there in the previous update but I think this simplifies
    // the user interface
    self.modeSelectionButton.hidden = false;
    self.yesNoChooser.hidden = true;
    self.correctQuestionLabel.hidden = true;
    self.transportQuestionLabel.hidden = false;
}

// TODO: Right now, we set user classification in two different code paths
// depending on whether it is correct or not.
// This is the code path for the incorrect case, when the user presses the done button
// If we cache the userMode in a variable, we can unify the code paths.
// TODO: Do this later
// setUserClassification was no longer being used and removed. Its necessary components are now in confirmIndividualTrip.

#pragma mark - Split view

- (void)splitViewController:(UISplitViewController *)splitController willHideViewController:(UIViewController *)viewController withBarButtonItem:(UIBarButtonItem *)barButtonItem forPopoverController:(UIPopoverController *)popoverController
{
    barButtonItem.title = NSLocalizedString(@"Master", @"Master");
    [self.navigationItem setLeftBarButtonItem:barButtonItem animated:YES];
    self.masterPopoverController = popoverController;
}

- (void)splitViewController:(UISplitViewController *)splitController willShowViewController:(UIViewController *)viewController invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    // Called when the view is shown again in the split view, invalidating the button and popover controller.
    [self.navigationItem setLeftBarButtonItem:nil animated:YES];
    self.masterPopoverController = nil;
}

@end
