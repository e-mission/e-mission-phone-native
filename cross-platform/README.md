This is the directory for the cross-platform UI, which we will build using apache cordova.
In order to build in the code in this directory, you need to first install apache cordova.

After the install is complete, you need to add platform support for the
platforms that you are interested in building (currently ios and android).

    $ cd cordova
    $ cordova platform add ios
    $ cordova platform add android

Add the sqlite plugin to ensure that we can access the database from the javascript
    $ cordova plugin add https://github.com/brodysoft/Cordova-SQLitePlugin

Add plugins to support the ionic project
    $ cordova plugin add org.apache.cordova.device
    $ cordova plugin add org.apache.cordova.console
    $ cordova plugin add com.ionic.keyboard

Note: This project was created using the following cordova command
    $ cordova create e-mission edu.berkeley.eecs.e_mission E-MissionApp

THe unit tests can be run in emulation mode by clicking on the "Run Tests" link
in the main UI screen.

When emulating, the ios console is available at platforms/ios/cordova/console.log, while the android console log is available using adb logcat.

They can also be run without user intervention by using the phantomjs headless
browser, as recommended by:
    http://blogs.telerik.com/appbuilder/posts/13-12-05/using-qunit-to-unit-test-phonegap-cordova-applications

The browser, and the associated plugin for qunit are packaged into:
    https://github.com/jonkemp/node-qunit-phantomjs

NOTE: The following commands only work if the tests are inline (i.e. included
in the index.html file). If they are pulled out into a separate javascript
file, the commands hang. I have filed an issue for this (issue #3).

The tests can then be run using the following commands:

    bash-3.2$ node-qunit-phantomjs platforms/android/assets/www/tests/index.html 
    Testing ../../../../../Users/shankari/e-mission/e-mission-phone/cross-platform/platforms/android/assets/www/tests/index.html
    [object Object]
    SQLitePlugin openargs: {"name":"my.db"}

    Took 16ms to run 2 tests. 2 passed, 0 failed.


    bash-3.2$ node-qunit-phantomjs platforms/ios/www/tests/index.html 
    Testing ../../../../../Users/shankari/e-mission/e-mission-phone/cross-platform/platforms/ios/www/tests/index.html
    [object Object]
    SQLitePlugin openargs: {"name":"my.db"}

    Took 19ms to run 2 tests. 2 passed, 0 failed.
