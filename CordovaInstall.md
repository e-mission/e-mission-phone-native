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


