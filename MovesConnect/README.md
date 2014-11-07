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

In order to compile this project, you need to import google play services into
your workspace. It is best to copy the google play services library into the
source directory, in order to avoid inadvertently checking in a path that is
specific to your build environment.

The project is set up to use google play services from the source directory.
In order to copy over google play services, do:

    $ cd MovesConnect
    $ cp $ANDROID_HOME/sdk/extras/google/google_play_services_froyo/libproject/google-play-services_lib .

In order to import google play services, you need to import the library project
into your workspace.

To do this: Click File > Import, select Android > Existing Android Code into
Workspace, and browse to the copy of the library project to import it.

IMPORTANT: Remember to use the copy that is in the MovesConnect directory NOT
the one in your SDK directory.

The onboarding flow is implemented as a state machine, as described at:
https://docs.google.com/a/berkeley.edu/document/d/1OYeo5ccUcTD-9tyMr4PysoS6R9e3rNDl1SCPXnF30b0/edit?usp=sharing
