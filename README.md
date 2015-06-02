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
code as possible. However, rather than switch to a full cordova solution, we
are using the Cordova WebViews [embedded in an existing app]
(https://cordova.apache.org/docs/en/4.0.0/guide\_hybrid\_webviews\_index.md.html).
This is for two reasons:
1. Cordova currently has only experimental support for gradle, and does not support the most recent version. This means that we cannot use the gradle-based Android Studio. Our earlier investigation has shown the the most recent google play API works [best for background tracking](http://www.shankari.org/2015/02/unit-testing-for-location-tracking-on.html). Embedding WebViews allows us to continue building with the most recent tools, while retaining crossplatform UI elements.
1. It allows us to update the UI dynamically without requiring an update to the app stores. This provides flexibility in customizing the UI to a users' preferences, while also supporting user studies.
1. It allows us to use native components as needed for performance, although we have not had to do this so far. It also allows us to retain our existing native components and move from working -> working.

However, the instructions on the cordova website for embedding a cordova-based
webview into an existing native project are incorrect and excessively
complicated. A better set of instructions, that reuses the same CordovaActivity
and CordovaUIController that applications created using the CLI do, can be
found in CordovaEmbedding.md file. The maintainer of this project will
periodically update those to be in sync with the current cordova release. Users
who are primarily concerned with creating new cordova views do not need to use
them.

## Creating a new cordova webview
New cordova webviews are created using standard cordova client, and then
imported into this codebase. The standard cordova client is stored in a
different repo for ease of use.
https://github.com/e-mission/e-mission-phone-cordova-plugins

A high level overview of the development flow is:
1. Clone the e-mission-phone-cordova-plugins repo
1. Add a new screen to the list there (see the README of that repo)
1. Test using ionic emulate until it works
1. Run the https://github.com/e-mission/e-mission-phone/blob/master/update\_cordova\_sources.sh files to copy the code for the new screens into this repo
1. If needed, add a new menu item that will launch `CordovaActivity` for the new view (on Android: `ConfirmSectionListActivity.onOptionsItemSelected`, on iOS: MasterNavController:updateViewOnState, the last entry)
1. Test and send pull request on BOTH repos

## Authentication between the phone and the server

### Production
These instructions are for using the app on production. On development, you can
use the shortcut documented later.

All calls from the phone app are authenticated via a JWT token generated after
the user signed in via google. For HTTP GET requests, the token is in the
"User" header. For POST requests, it is in the 'user' parameter.

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

And then add them to the sample files at these locations:

    # https://github.com/e-mission/e-mission-phone/blob/master/android/app/src/main/res/values/connect.xml
    # https://github.com/e-mission/e-mission-phone/blob/master/iOS/CFC_Tracker/CFC_Tracker/connect.plist

Note that platform specific instructions are also available at in README files
in the individual directories (android for android and iOS for iOS).

### Development
In order to make development easier, _iff_ we are connecting to a HTTP URL,
then we don't attempt to authenticate. Instead the "user" field described above
contains the unencrypted user email address. This is clearly horribly insecure
and should never ever be used on a system that is collecting real data, but is
a way to get quickly started with development without mucking around with keys
(and on android, keystores) while still exercising most of the codebase in the
correct way. It would be even better if the authentication code was modularized
better so that we could drop in more authentication mechanisms easily (persona,
anyone), but that will happen in when we integrate the third mechanism that we
need for optimal generality.
