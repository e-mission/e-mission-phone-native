e-mission is a project to gather data about user travel patterns using phone
apps, and use them to provide an personalized carbon footprint, and aggregate
them to make data available to urban planners and transportation engineers.

It has two components, the backend server and the phone apps. These are the
phone apps - the backend server is available in the e-mission-server repo.

The phone apps are currently written in a mixture of native code and
javascript. The onboarding screen, the trip list and the trip detail view are
all written using native code, and the result screen is javascript from the
server displayed in a WebView.

We are switching to more cordova-based WebViews to use as much crossplatform
code as possible. The instructions on importing cordova-based webviews for use
this project are in the CordovaInstall.md file.

All calls from the phone app are HTTP POST, and the user parameter is a JWT
token generated after the user signed in via google.

These repositories don't have the associated client keys checked in for
security reasons.

If you are associated with the e-mission project and will be integrating with
our server, then you can get the key files from:
https://repo.eecs.berkeley.edu/git/users/shankari/e-mission-keys.git

If not, please get your own copies of the following keys:

* Google Developer Console
  - Android key
  - iOS key
  - webApp key
* Moves app
  - Client key
  - Client secret

And then copy over the sample files from these locations and replace the values appropriately:

    # MovesConnect/res/values/connect.xml
    # iOS/CFC\_Tracker/CFC\_Tracker/connect.plist

Note that platform specific instructions are also available at in README files
in the individual directories (MovesConnect for android and iOS for iOS).

Trouble Shooting:

If you are getting a compile error in which R can not be resolved
it may be because you have an updated version of the android SDK. 

To check if this is the case: 

1. Open up tools/Android/SDKManager. 

2. Check the version number of "SDK Platform"

3. Open build.gradle in project:app

4. Adjust the "compile SDK version" and "target SDK version to be the version number you found in step 2"

5. Clean project

6. Sync project with gradle file 