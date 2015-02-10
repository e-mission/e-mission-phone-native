//
//  MasterViewController.m
//  CFC_Tracker
//
//  Created by Kalyanaraman Shankari on 3/23/14.
//  Copyright (c) 2014 Kalyanaraman Shankari. All rights reserved.
//

#import "MasterViewController.h"

#import "EMSTripTableViewCell.h"
#import "DetailViewController.h"
#import "TripSection.h"
#import "TrackLocation.h"
#import "CommunicationHelper.h"
#import "HTMLDisplayCommunicationHelper.h"
#import "TripSectionDatabase.h"
#import "ClientStatsDatabase.h"
#import "Constants.h"
#import "ActionSheetStringPicker.h"
#import "ManualTripController.h"

// BEGIN: Headers needed for authentication
#import "SignInViewController.h"
#import <GoogleOpenSource/GoogleOpenSource.h>
#import <CoreImage/CoreImage.h>
#import "AuthCompletionHandler.h"
// END: Headers needed for authentication

@interface MasterViewController () {
    NSMutableArray *_sectionList;
    TripSectionDatabase* _tripSectionDb;
    ClientStatsDatabase* _statsDb;
}
@property(strong, nonatomic) SignInViewController *signInViewController;
@property(strong, nonatomic) UIViewController *resultSummaryViewController;
@property(strong, nonatomic) ManualTripController *manualTripController;
@property BOOL hasShownResults;
@end

@implementation MasterViewController

static NSString * const kMainStoryboardName = @"MainStoryboard_iPhone";
static NSString * const kResultSummaryStoryboardID = @"resultSummary";

- (void)awakeFromNib
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        self.clearsSelectionOnViewWillAppear = NO;
        self.preferredContentSize = CGSizeMake(320.0, 600.0);
    }
    [super awakeFromNib];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    NSLog(@"viewDidLoad CALLED");
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(appWillEnterForeground:)
                                             name:UIApplicationWillEnterForegroundNotification
                                             object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(backgroundFetchGotNewData:)
                                                 name:BackgroundRefreshNewData
                                               object:nil];

	// Do any additional setup after loading the view, typically from a nib.
    
    [self initializeAuthResultBarButtons];
    
    UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleLongPress:)];
    longPress.minimumPressDuration = 0.5;
    longPress.delegate = self;
    [self.tableView addGestureRecognizer:longPress];
    
    self.detailViewController = (DetailViewController *)[[self.splitViewController.viewControllers lastObject] topViewController];
    self.signInViewController = [[SignInViewController alloc] initWithNibName:nil bundle:nil];
    self.manualTripController = [[ManualTripController alloc] initWithNibName:nil bundle:nil];
    
    UIStoryboard *sb = [UIStoryboard storyboardWithName:kMainStoryboardName bundle:nil];
    
    self.resultSummaryViewController = [sb instantiateViewControllerWithIdentifier:kResultSummaryStoryboardID];
    self.hasShownResults = NO;
    UIBarButtonItem *editButton = [self editButtonItem];
    editButton.title = @"Select";
    self.navigationItem.rightBarButtonItem = editButton;

    _sectionList = nil;
    
    _tripSectionDb = [[TripSectionDatabase alloc] init];
    _statsDb = [[ClientStatsDatabase alloc] init];
    [self loadInitialData];
}

- (void)handleLongPress:(UILongPressGestureRecognizer *)gestureRecognizer {
    CGPoint point = [gestureRecognizer locationInView:self.tableView];
    
    NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:point];
    if(indexPath == nil) {
        NSLog(@"long press on table view but not on a row");
    } else {
        if (gestureRecognizer.state == UIGestureRecognizerStateBegan) {
            NSLog(@"long press ontable view at row %li", (long)indexPath.row);
            // this needs to be changed to get the selection of frequent items. Store this as a variable in tripsection
            NSArray *choices = [TripSection modeChoices];
            [ActionSheetStringPicker showPickerWithTitle:@"Select Trip Mode" rows:choices initialSelection:0 doneBlock:^(ActionSheetStringPicker *picker, NSInteger index, id value) {
                    [self onUserSelectionOfTripSection: _sectionList[indexPath.row] withPicker:picker index:index value:value];
                }
            cancelBlock:nil origin:self.view];
        } else {
            NSLog(@"gestureRecognizer.state = %ld", gestureRecognizer.state);
        }
    }
}

- (void)onUserSelectionOfTripSection: (TripSection *)trip withPicker: (ActionSheetStringPicker *) picker index: (NSInteger) selectedIndex value: (id) selectedValue
{
    NSArray *choices = [TripSection modeChoices];
    [trip setSelectedMode:[choices objectAtIndex:selectedIndex]];
    [self refreshData];
}

- (void)initializeAuthResultBarButtons
{
    UIBarButtonItem *authButton = [[UIBarButtonItem alloc] initWithTitle:@"Auth" style:UIBarButtonItemStyleBordered target: self action:@selector(showSignInView:)];
    UIBarButtonItem *resultButton = [[UIBarButtonItem alloc] initWithTitle:@"Result" style:UIBarButtonItemStyleBordered target: self action:@selector(showResults:)];
/*    UIBarButtonItem *stupidAppReviewButton = [[UIBarButtonItem alloc] initWithTitle:@"SAP" style:UIBarButtonItemStyleBordered target: self action:@selector(showManualTripScreen:)];
    self.navigationItem.leftBarButtonItems = @[authButton, resultButton, stupidAppReviewButton];
 */
    self.navigationItem.leftBarButtonItems = @[authButton, resultButton];
}

- (void)viewDidAppear:(BOOL) animated
{
    [super viewDidAppear:animated];
    NSLog(@"MasterViewController.viewDidAppear CALLED");
    [self refreshData];
}

- (void)viewWillAppear:(BOOL) animated
{
    [super viewWillAppear:animated];
    NSLog(@"MasterViewController.viewWillAppear CALLED");
}

- (void)appWillEnterForeground:(NSNotification*) notification
{
    NSLog(@"MasterViewController.appWillEnterForeground CALLED");
    /* We want to launch the result screen once, but not again
     * until the user launches the app again. I am not sure which 
     * other call corresponds to that call (I've seen something about
     * launch with options), so let's keep it simple for now.
     */
    /*
     * We are adding the stats calls here instead of in refreshData because we want to know when the user launched the app.
     * refreshData is also called when the sync was successful (see backgroundFetchGotNewData below)
     */
    NSString* currTs = [ClientStatsDatabase getCurrentTimeMillisString];
    [_statsDb storeMeasurement:@"confirmlist_resume" value:CLIENT_STATS_DB_NIL_VALUE ts:currTs];
    [_statsDb storeMeasurement:@"confirmlist_ucs_size" value:[@([self.tableView numberOfRowsInSection:0]) stringValue] ts:currTs];
    if ([AuthCompletionHandler sharedInstance].currAuth != nil) {
        if ([self.tableView numberOfRowsInSection:0] == 0) {
            self.hasShownResults = YES;
            [self pushToServer:self];
            [self showResults:self];
        } else {
            self.hasShownResults = NO;
        }
    } else {
        [_statsDb storeMeasurement:@"confirmlist_auth_not_done" value:CLIENT_STATS_DB_NIL_VALUE ts:currTs];
    }
}

- (void)loadInitialData
{
    /*
    TripSection *walkSection = [self createDummySection];
    [self->_sectionList addObject:walkSection];
     */
    // We used to refresh data here, but then we ended up refreshing everything twice, once from
    // here and once from viewWillAppear. I don't think we need to load on dynamic data on init
    // because it is invoked so infrequently.
    // [self refreshData];
}

-(void)backgroundFetchGotNewData:(NSNotification*) notification
{
    NSLog(@"Background fetch got new data and saved it to the database, let's refresh the UI");
    [self refreshData];
}

- (void)refreshData
{
    if ([AuthCompletionHandler sharedInstance].currAuth != nil) {
//        _sectionList = [_tripSectionDb getUncommittedSections];

        if(_sectionList == nil) {
            _sectionList = [_tripSectionDb getUncommittedSections];
        } else {
            NSArray *uncommittedSections = [_tripSectionDb getUncommittedSections];
            NSArray *newUncommittedSections = [MasterViewController getTripSectionsFrom:uncommittedSections NotIn:_sectionList];
            NSArray *committedSections = [MasterViewController getTripSectionsFrom:_sectionList NotIn:uncommittedSections];
            
            [_sectionList removeObjectsInArray:committedSections];
            [_sectionList addObjectsFromArray:newUncommittedSections];
        }
        
        NSLog(@"Read list of size %lu from database", [_sectionList count]);
        [self.tableView reloadData];
        if ([self.tableView numberOfRowsInSection:0] == 0 && (!self.hasShownResults)) {
            self.hasShownResults = YES;
            [self pushToServer:self];
            [self showResults:self];
        }
    } else {
        // If we are not currently authenticated, popup the sign in view
        // to get user authentication
        [self showSignInView:self];
    }
    
    // every time the data is refreshed, consider activiating/ deactivating the select button
    if (_sectionList.count == 0) {
        [self.navigationItem.rightBarButtonItem setEnabled:NO];
    } else {
        [self.navigationItem.rightBarButtonItem setEnabled:YES];
    }
}

- (void)refreshAuth
{
    // We don't want to make any calls until we are signed in correctly
    if ([AuthCompletionHandler sharedInstance].currAuth == nil) {
        NSLog(@"In MasterViewController.refreshAuth, authentication = NULL, popping up the SignInViewController");
        // We are not signed in, so we don't want to access any private information
        // Make the user sign in to see the app
        [self showSignInView:self];
    }
}


- (void)showSignInView:(id)sender
{
    if ([self.navigationController.viewControllers containsObject:self.signInViewController]) {
        // the sign in view is already visible, don't need to push it again
        NSLog(@"sign in view is already in the navigation chain, skipping the push to the controller...");
    } else {
        [self.navigationController pushViewController:self.signInViewController animated:YES];
    }
}

- (void)showResults:(id)sender
{
    long startTime = [ClientStatsDatabase getCurrentTimeMillis];
    if ([self.navigationController.viewControllers containsObject:self.resultSummaryViewController]) {
        // the result summary is already visible, don't need to push it again
        NSLog(@"resultSummaryView is already in the navigation chain, skipping the push to the controller...");
    } else {
        [self.navigationController pushViewController:self.resultSummaryViewController animated:YES];
    }
    // NSLog(@"subviews are %@", self.resultSummaryViewController.view.subviews);
    // NSLog(@"view with tag 0 is %@", [self.resultSummaryViewController.view viewWithTag:0]);
    // UIWebView *webView = (UIWebView*)[self.resultSummaryViewController.view viewWithTag:0];
    UIWebView *webView = (UIWebView*)self.resultSummaryViewController.view.subviews[0];
    // [webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"http://localhost:8080/compare"]]];
    [HTMLDisplayCommunicationHelper displayResultSummary:webView
                                       completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
    // This is currently used only for generating stats, need to figure out how to display errors
                                           long endTime = [ClientStatsDatabase getCurrentTimeMillis];
                                           NSString* endTimeStr = [ClientStatsDatabase getCurrentTimeMillisString];
                                           [_statsDb storeMeasurement:@"result_display_duration" value:[@(endTime - startTime) stringValue] ts:endTimeStr];
                                           if(error != NULL) {
                                               [_statsDb storeMeasurement:@"result_display_failed" value:NULL ts:endTimeStr];
                                           }
    }];
}

- (void)showManualTripScreen:(id)sender
{
    if ([self.navigationController.viewControllers containsObject:self.signInViewController]) {
        // the sign in view is already visible, don't need to push it again
        NSLog(@"sign in view is already in the navigation chain, skipping the push to the controller...");
    } else {
        [self.navigationController pushViewController:self.manualTripController animated:YES];
    }
}

- (void)pushToServer:(id)sender
{
    if ([AuthCompletionHandler sharedInstance].currAuth) {
        NSMutableArray *jsonArrayToSend = [[NSMutableArray alloc] init];
        
        NSArray* classifiedSections = [_tripSectionDb getAndDeleteClassifiedSections];
        long classifiedSectionCount = [classifiedSections count];
        
        NSString* currTs = [ClientStatsDatabase getCurrentTimeMillisString];
        [_statsDb storeMeasurement:@"sync_push_list_size" value:[@(classifiedSectionCount) stringValue] ts:currTs];

        NSDictionary *statsToSend = [_statsDb getMeasurements];
        if ([statsToSend count] != 0) {
            // Also push the client level stats
            [CommunicationHelper setClientStats:statsToSend
                          completionHandler:^(NSData *data, NSURLResponse* response, NSError* error){
                              NSLog(@"Pushing stats to the server is complete with response = %@i and error = %@", response, error);
                              // If the error was null, the push was successful, and we can clear the database
                              // Should we check for the code in the response, or for the error?
                              if(error == NULL) {
                                  [_statsDb clear];
                              }
                          }];
        } else {
            NSLog(@"No stats, nothing to push, skipping...");
        }

        if (classifiedSections.count == 0) {
            NSLog(@"No data, nothing to push, skipping...");
            return;
        }
        
        for (int i = 0; i < classifiedSections.count; i++) {
            NSDictionary *currDict = [classifiedSections[i] saveToJSON];
            [jsonArrayToSend addObject:currDict];
        }
        
        NSLog(@"About to push %lu objects to the server", (unsigned long)jsonArrayToSend.count);
        
        // We should not do this - if it takes time, then the app will hang.
        // But in case the background stuff stops working, we keep the comment to do the communication in the main thread
        // [self postToURL:bytesToSend];
        
        [CommunicationHelper setClassifiedSections:jsonArrayToSend
                                 completionHandler:^(NSData* data, NSURLResponse* response, NSError* error) {
            NSLog(@"HTTP post is complete with response = %@i and error = %@", response, error);
        }];
        
    } else {
        // TODO: Figure out what (if anything) we want to do here
    }
}

- (void)selectAllRows
{
    for (int row=0; row<[self.tableView numberOfRowsInSection:0]; row++) {
        NSIndexPath *indexPath = [NSIndexPath indexPathForRow:row inSection:0];
        [self.tableView selectRowAtIndexPath:indexPath animated:YES scrollPosition:UITableViewScrollPositionNone];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    _sectionList = NULL;
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table View

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSLog(@"Returning size = %lu", _sectionList.count);
    return _sectionList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EMSTripTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:EMSTripTableViewCellReuseIdentifier
                                                                 forIndexPath:indexPath];
    NSLog(@"Displaying summary view for row %ld", (long)indexPath.row);
    [cell prepareWithSection:_sectionList[indexPath.row]];
    return cell;
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    CGFloat height = scrollView.frame.size.height;
    CGFloat contentYoffset = scrollView.contentOffset.y;
    CGFloat distanceFromBottom = scrollView.contentSize.height - contentYoffset;
    
    if (distanceFromBottom < height && _sectionList.count != 0) {
        // NSLog(@"reached the end of the table");
        [self.navigationItem.rightBarButtonItem setEnabled:YES];
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (!self.editing) {
        [self performSegueWithIdentifier:@"showDetail" sender:self];
    }
//    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
//        TripSection *object = _sectionList[indexPath.row];
//        self.detailViewController.detailItem = object;
//    }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"showDetail"]) {
        NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
        TripSection *object = _sectionList[indexPath.row];
        [[segue destinationViewController] setDetailItem:object];
    }
}

+(NSArray*) getTripSectionsFrom: (NSArray*)originList NotIn: (NSArray*)comparisonList {
    NSMutableArray *comparisonListIds = [NSMutableArray array];
    
    for (TripSection *trip in comparisonList) {
        [comparisonListIds addObject:[trip.tripId stringByAppendingString:trip.sectionId]];
    }
    
    NSMutableArray *newItems = [NSMutableArray array];
    for (TripSection *trip in originList){
        if(![comparisonListIds containsObject:[trip.tripId stringByAppendingString:trip.sectionId]]) {
            [newItems addObject:trip];
        }
    }
    return newItems;
}

@end
