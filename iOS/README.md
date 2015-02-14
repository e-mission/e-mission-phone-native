## Installation instructions: ##
- Install the libsqlite3 framework
- Install the GoogleOpenSource framework using the following instructions:
    - download the google+ client for iOS
        https://developers.google.com/+/mobile/ios/getting-started
    - unzip it
    - drag and drop the GoogleOpenSource.framework bundle from it into the
      Project Navigator, copying if necessary (see the next list entry for more
        details and screenshots)
    - do NOT copy the other bundles and frameworks since we do not use them
- Instructions on installing the frameworks and the bundle
  http://stackoverflow.com/questions/19202218/receiving-a-check-dependencies-warning-skipping-file-error-in-xcode

### Running in the emulator ###
- If you run in the emulator, you cannot install moves.
- In order to allow further testing, we check to see if we are running in the
  emulator, and skip the moves install.
  For the record, the code is [here](https://github.com/e-mission/e-mission-phone/blob/master/iOS/CFC_Tracker/CFC_Tracker/MasterNavController.m#L82).
- In order to trigger this check:
    - work through the onboarding process until the point when you get to the
      moves check
    - stop the app from xcode by hitting the stop button at the top left hand corner
    - start the app again - the onboarding process will be complete
    - I am not sure why the restart is required to trigger the check - if you
      figure it out, send me a pull request!

In order to change the bundle name, you have the edit the following locations:
- The bundle ID in the google developer client key 
- The redirect URL in moves
- The URL while sending the moves request from SignInViewController
- The URL that we handle in the app delegate
- The redirect URL in the server (main/auth.py)
