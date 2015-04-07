cd /Users/shankari/e-mission/e-mission-phone/android
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/android/assets/* app/src/main/assets
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/android/src/ app/src/main/java
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/android/res/xml/config.xml app/src/main/res/xml/

cd /Users/shankari/e-mission/e-mission-phone/ios
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/ios/cordova .
mkdir -p CFC_Tracker/CFC_Tracker/Cordova
# cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/ios/referenceSidebarApp/Classes/ CFC_Tracker/CFC_Tracker/Cordova
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/ios/referenceSidebarApp/Plugins/ CFC_Tracker/CFC_Tracker/Cordova
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/ios/referenceSidebarApp/Resources/ CFC_Tracker/CFC_Tracker/Cordova
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/ios/referenceSidebarApp/config.xml CFC_Tracker/CFC_Tracker/Cordova
cp -r ~/e-mission/e-mission-phone-cordova/referenceSidebarApp/platforms/ios/www CFC_Tracker/CFC_Tracker/Cordova
