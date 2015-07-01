DST="$1"
mv android/CordovaLib android/CordovaLib.old
cp -r $DST/platforms/android/CordovaLib android/
cp -r $DST/platforms/android/project.properties android/

mv iOS/CFC_Tracker/CordovaLib iOS/CFC_Tracker/CordovaLib.old
cp $DST/platforms/ios/eMission/Classes/AppDelegate.m iOS/CFC_Tracker/CFC_Tracker/Cordova/AppDelegate.m.cordova 
cp -r $DST/platforms/ios/CordovaLib iOS/CFC_Tracker/
