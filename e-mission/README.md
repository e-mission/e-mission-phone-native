This is the directory for the cross-platform UI, which we will build using apache cordova.
In order to build in the code in this directory, you need to first install apache cordova.

After the install is complete, you need to add platform support for the
platforms that you are interested in building (currently ios and android).

    $ cd cordova
    $ cordova platform add ios
    $ cordova platform add android

Add the sqlite plugin to ensure that we can access the database from the javascript
    $ cordova plugin add https://github.com/brodysoft/Cordova-SQLitePlugin

Note: This project was created using the following cordova command
    $ cordova create e-mission edu.berkeley.eecs.e_mission E-MissionApp
