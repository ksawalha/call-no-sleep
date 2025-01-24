var exec = require('cordova/exec');

var CallNoSleep = {
    callNumber: function(successCallback, errorCallback, number, bypassAppChooser) {
        exec(successCallback, errorCallback, "CallNoSleep", "callNumber", [number, bypassAppChooser]);
    },
    isCallSupported: function(successCallback) {
        exec(successCallback, null, "CallNoSleep", "isCallSupported", []);
    },
    startCall: function(successCallback, errorCallback, number) {
        exec(successCallback, errorCallback, "CallNoSleep", "startCall", [number]);
    },
    preventSleep: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "CallNoSleep", "preventSleep", []);
    },
    allowSleep: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "CallNoSleep", "allowSleep", []);
    },
    getAppInfo: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "CallNoSleep", "getAppInfo", []);
    }
};

module.exports = CallNoSleep;
