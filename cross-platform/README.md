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

The unit testing uses the phantomjs headless browser, as recommended by:
    http://blogs.telerik.com/appbuilder/posts/13-12-05/using-qunit-to-unit-test-phonegap-cordova-applications

The browser, and the associated plugin for qunit are packaged into:
    https://github.com/jonkemp/node-qunit-phantomjs

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
