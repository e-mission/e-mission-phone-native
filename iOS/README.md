Installation instructions:
- Install the libsqlite3 framework
- Install the GoogleOpenSource framework downloaded from here
- Instructions on installing the frameworks and the bundle
  http://stackoverflow.com/questions/19202218/receiving-a-check-dependencies-warning-skipping-file-error-in-xcode

For google+:
- Install all the frameworks described here:
  https://developers.google.com/+/mobile/ios/getting-started
- Install the bundle using
  (Project + Target) -> Build Phases -> Copy Bundle Resources
- Instructions on installing the frameworks and the bundle
  http://stackoverflow.com/questions/19202218/receiving-a-check-dependencies-warning-skipping-file-error-in-xcode

In order to change the bundle name, you have the edit the following locations:
- The bundle ID in the google developer client key 
- The redirect URL in moves
- The URL while sending the moves request from SignInViewController
- The URL that we handle in the app delegate
- The redirect URL in the server (main/auth.py)
