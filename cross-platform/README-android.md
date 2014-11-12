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

This project is currently intended to be deployed using Android Studio, or
Eclipse with the ADT plugin. This development environment can be downloaded
from: http://developer.android.com/sdk/index.html

Then, this project can be imported using File -> Import -> Existing Android
Code into Workspace, selecting the top level e-mission-phone directory, and
then selecting the MovesConnect project.

In order to compile this project, you need to import google play services into
your workspace. It is best to copy the google play services library into the
source directory, in order to avoid inadvertently checking in a path that is
specific to your build environment.

The project is set up to use google play services from the source directory.
In order to copy over google play services, do:

    $ cd platform/android
    $ cp $ANDROID_HOME/sdk/extras/google/google_play_services/libproject/google-play-services_lib .

In order to import google play services, you need to import the library project
into your workspace.

To do this: Click File > Import, select Android > Existing Android Code into
Workspace, and browse to the copy of the library project to import it.

Or, if you import the main project *after* copying over the google play services,
it will automagically import google play services as well.

IMPORTANT: Remember to use the copy that is in the MovesConnect directory NOT
the one in your SDK directory.

Finally, the unit test project will have errors by default. In order to resolve
those, you need to make it reference the main project. To do this:
- click on project -> properties -> java build path -> projects and add the main project to the list

The onboarding flow is implemented as a state machine, as described at:
https://docs.google.com/a/berkeley.edu/document/d/1OYeo5ccUcTD-9tyMr4PysoS6R9e3rNDl1SCPXnF30b0/edit?usp=sharing
