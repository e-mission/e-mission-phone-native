## Quickstart
* This project is intended to be built with gradle, rather than ant.
* In case an IDE is used, the project is intended to be used with the IntelliJ-based Android Studio, not one that is based on eclipse.
  * This development environment can be downloaded from:
    http://developer.android.com/sdk/index.html

Then, this project can be imported using File -> Import Project and then selecting the build.gradle from from the cloned directory. Gradle already handles dependencies and test case imports, so setup is significantly easier.

A video showing the import process is available here:
https://drive.google.com/open?id=0B04dolz7Xr1sYVlwYTRXcjczejQ&authuser=0

The onboarding flow is implemented as a state machine, as described at:
https://docs.google.com/a/berkeley.edu/document/d/1OYeo5ccUcTD-9tyMr4PysoS6R9e3rNDl1SCPXnF30b0/edit?usp=sharing

## Configuration with keys
### Development
In order to ease development, we use the unencrypted email address while
logging in to a HTTP server. This allows us to start developing without any
keys or mucking with keystores. The only edit that might be needed is to put
the correct host name into `connect.xml` (in `app/src/main/res/values`).

While working with real location data, though, we need to obtain the
appropropriate keys as described below. That will allow us to replace the email
address with a signed, encrypted JWT.

### Production
The android project uses google API keys for the following uses:
- login with google+
- displaying maps

The android keys generated for all these uses rely on the SHA1 fingerprint of
the keystore. If the fingerprint is wrong, then things won't work.
Unfortunately, the fingerprint is generated from the debug keystore in
~/.android/debug.keystore, which is different for each developer.

So when you create your keys, you need to use the SHA1 fingerprint from your
keystore.

If you are part of the e-mission team, and are using our keys from the
e-mission-keys repository, then you should also copy over my keystore, which is
checked in the same location.

You also need to replace the placeholders in connect.xml (from
`app/src/main/res/values`) with the correct keys.

### Troubleshooting ###
- The Google Authentication sometimes fails. In particular, clicking on "Auth
  -> Get Server Token" generates an "Unknown Error" visible in the logs.

    This is caused by the app being unable to connect to the server at the time
    that it is installed. he most likely cause of this is that you have not replaced
    connect.xml.sample with version that has the keys filled in (from the keys
    repo or from bcourses). Alternatively:
    - You have issues with your network
    - Your server is not running
    - You have specified a URL for the server that the app can't reach, for example:
        - if your server is running on a VM without a bridged network
        - your server is running on a different NAT than your phone

    In order to fix it, you must completely uninstall and
    reinstall the app. You cannot just re-launch the app, which will just
    update it. In order to uninstall and reinstall the app:

    - in the emulator: close the current emulator and launch a new one after wiping
      all data (AVD Manager -> Actions -> "Wipe Data". Relaunch the emulator.
    - on the phone: go to Settings -> Apps or Settings -> Applications and
      uninstall the e-mission app. Relaunch the app.

- While running in the emulator, walking through the onboarding process
  sometimes causes an OOM error. Increasing the size of the memory on the
  emulator (to 2G) and restarting the emulator seems to fix this issue.

        02-14 16:21:10.714    2348-2348/edu.berkeley.eecs.e_mission E/AndroidRuntime﹕ FATAL EXCEPTION: main
        Process: edu.berkeley.eecs.e_mission, PID: 2348
        java.lang.RuntimeException: Unable to resume activity {edu.berkeley.eecs.e_mission/edu.berkeley.eecs.e_mission.OnboardingActivity}: android.view.InflateException: Binary XML file line #8: Error inflating class <unknown>
        ...
        Caused by: java.lang.OutOfMemoryError: Failed to allocate a 92160012 byte allocation with 4194304 free bytes and 53MB until OOM
                at dalvik.system.VMRuntime.newNonMovableArray(Native Method)

