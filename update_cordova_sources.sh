DST="$1"
cp -r $DST/platforms/android/assets/* android/app/src/main/assets
rm android/app/src/main/assets/www/TripSections.db
cp -r $DST/platforms/android/src/ android/app/src/main/java
cp -r $DST/platforms/android/res/xml/config.xml android/app/src/main/res/xml/
 
cp -r $DST/platforms/ios/eMission/Plugins/ iOS/CFC_Tracker/CFC_Tracker/Cordova
cp -r $DST/platforms/ios/eMission/Resources/ iOS/CFC_Tracker/CFC_Tracker/Cordova
cp -r $DST/platforms/ios/eMission/config.xml iOS/CFC_Tracker/CFC_Tracker/Cordova
cp -r $DST/platforms/ios/www iOS/CFC_Tracker/CFC_Tracker/Cordova
rm iOS/CFC_Tracker/CFC_Tracker/Cordova/www/TripSections.db
