var exec = require('cordova/exec');

var CallNoSleep = {
    startCall: function(phoneNumber, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'CallNoSleep', 'startCall', [phoneNumber]);
    },
    preventSleep: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'CallNoSleep', 'preventSleep', []);
    },
    allowSleep: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'CallNoSleep', 'allowSleep', []);
    },
    getAppInfo: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'CallNoSleep', 'getAppInfo', []);
    }
};

module.exports = CallNoSleep;
