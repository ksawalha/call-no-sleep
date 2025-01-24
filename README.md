# call-no-sleep

## Description

`call-no-sleep` is a Cordova plugin that provides three main functions:
1. Start a call with a given phone number.
2. Prevent the screen from sleeping.
3. Allow the screen to sleep again.

## Installation

To install the plugin, use the following command:

```sh
cordova plugin add call-no-sleep
```

## Usage

### Start a Call

To start a call with a given phone number, use the `startCall` function:

```javascript
document.addEventListener('deviceready', function() {
    CallNoSleep.startCall('1234567890', function() {
        console.log('Call started successfully');
    }, function(error) {
        console.error('Error starting call: ' + error);
    });
});
```

### Prevent Screen from Sleeping

To prevent the screen from sleeping, use the `preventSleep` function:

```javascript
document.addEventListener('deviceready', function() {
    CallNoSleep.preventSleep(function() {
        console.log('Screen sleep prevented');
    }, function(error) {
        console.error('Error preventing screen sleep: ' + error);
    });
});
```

### Allow Screen to Sleep

To allow the screen to sleep again, use the `allowSleep` function:

```javascript
document.addEventListener('deviceready', function() {
    CallNoSleep.allowSleep(function() {
        console.log('Screen sleep allowed');
    }, function(error) {
        console.error('Error allowing screen sleep: ' + error);
    });
});
```

### Get App Info

To get the app version number, version code, and package identifier, use the `getAppInfo` function:

```javascript
document.addEventListener('deviceready', function() {
    CallNoSleep.getAppInfo(function(appInfo) {
        console.log('App Info:', appInfo);
    }, function(error) {
        console.error('Error getting app info: ' + error);
    });
});
```

### Login with Microsoft

To login with a Microsoft account, use the `loginWithMicrosoft` function:

```javascript
document.addEventListener('deviceready', function() {
    CallNoSleep.loginWithMicrosoft(function(username) {
        console.log('Logged in as: ' + username);
    }, function(error) {
        console.error('Error logging in with Microsoft: ' + error);
    });
});
```
