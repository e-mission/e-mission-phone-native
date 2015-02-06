MovesConnect uses google API keys for the following uses:
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

* This project is intended to be built with gradle, rather than ant.
* In case an IDE is used, the project is intended to be used with the IntelliJ-based Android Studio, not one that is based on eclipse.
  * This development environment can be downloaded from:
    http://developer.android.com/sdk/index.html

Then, this project can be imported using File -> Import Project and then selecting the build.gradle from from the cloned directory. Gradle already handles dependencies and test case imports, so setup is significantly easier.

The onboarding flow is implemented as a state machine, as described at:
https://docs.google.com/a/berkeley.edu/document/d/1OYeo5ccUcTD-9tyMr4PysoS6R9e3rNDl1SCPXnF30b0/edit?usp=sharing
