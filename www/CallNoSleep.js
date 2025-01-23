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
    }
};

module.exports = CallNoSleep;
