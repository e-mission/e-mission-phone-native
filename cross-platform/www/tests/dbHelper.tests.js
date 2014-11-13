QUnit.test ("hello test", function(assert) {
    assert.ok(1 == "1", "Passed!");
});

QUnit.asyncTest ("testPragmaExample", function(assert) {
    console.log(dbHelper);
    dbHelper.pragmaExample();
    assert.ok(1 == "1", "Passed!");
});
