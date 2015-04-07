Note that all these steps have already been done for this project. This is
documentation in case we want to do this for a new project.

## Android ##

- Import the CordovaLib module from the reference project using File -> New Module
  - This will copy CordovaLib to your home directory as well
- Edit the build.gradle in CordovaLib and make the following changes:
  - Change the gradle version to match the one in app/build.gradle
  - Change the buildToolsVersion to match the one in app/build.gradle
  - Change the plugin from "android-library" to "com.android.library"
- Change app/build.gradle to add a dependency to cordova
     dependencies {
         compile 'com.android.support:support-v4:19.1.0'
         compile 'com.google.android.gms:play-services:+'
    +    compile project(':CordovaLib')
     }
- Add an entry to AndroidManifest.xml for the new activity
    +
    +        <activity
    +            android:name="com.ionicframework.referencesidebarapp565061.CordovaApp"
    +            android:label="Cordova List View">
    +        </activity>
- And then launch the activity from wherever you want in the native app


## iOS ##

- Drag and drop the CordovaLib project from the reference project. Note that
  you must drag the xcodeproj file ONTO the project.
    http://stackoverflow.com/questions/9370518/xcode-4-2-how-include-one-project-into-another-one

- Follow the instructions here
  http://cordova.apache.org/docs/en/4.0.0/guide_platforms_ios_webview.md.html#iOS%20WebViews
    on including CordovaLib, but do NOT add any linker or compiler flags
- Copy the Plugins, Resources and config.xml to a folder called Cordova
- Copy the classes over and rename them so that they don''t have conflicts
  - MainViewController to EmbeddedCordovaViewController
  - AppDelegate.* to AppDelegate.*.cordova
- Fix all the imports
- Write code to launch the EmbeddedCordovaViewController just like any other view controller
- Copy the www directories over as well
- Copy over the copy-www-build-step.sh and edit it to match your project name (CFC_Tracker in this case).
- Add a "Copy www files" step that copies the files over to the deployment directory
