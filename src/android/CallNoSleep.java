package com.example.callnosleep;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Import MSAL library
import com.microsoft.identity.client.*;

public class CallNoSleep extends CordovaPlugin {

    private PowerManager.WakeLock wakeLock;
    private ISingleAccountPublicClientApplication mSingleAccountApp;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startCall")) {
            String phoneNumber = args.getString(0);
            this.startCall(phoneNumber, callbackContext);
            return true;
        } else if (action.equals("preventSleep")) {
            this.preventSleep(callbackContext);
            return true;
        } else if (action.equals("allowSleep")) {
            this.allowSleep(callbackContext);
            return true;
        } else if (action.equals("getAppInfo")) {
            this.getAppInfo(callbackContext);
            return true;
        } else if (action.equals("loginWithMicrosoft")) {
            this.loginWithMicrosoft(callbackContext);
            return true;
        }
        return false;
    }

    private void startCall(String phoneNumber, CallbackContext callbackContext) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            this.cordova.getActivity().startActivity(intent);
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error("Failed to start call: " + e.getMessage());
        }
    }

    private void preventSleep(CallbackContext callbackContext) {
        try {
            PowerManager powerManager = (PowerManager) this.cordova.getActivity().getSystemService(Context.POWER_SERVICE);
            if (wakeLock != null && wakeLock.isHeld()) {
                callbackContext.error("WakeLock is already held");
                return;
            }
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "CallNoSleep::WakeLock");
            wakeLock.acquire();
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error("Failed to prevent sleep: " + e.getMessage());
        }
    }

    private void allowSleep(CallbackContext callbackContext) {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                callbackContext.success();
            } else {
                callbackContext.error("WakeLock is not held");
            }
        } catch (Exception e) {
            callbackContext.error("Failed to allow sleep: " + e.getMessage());
        }
    }

    private void getAppInfo(CallbackContext callbackContext) {
        try {
            Context context = this.cordova.getActivity().getApplicationContext();
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

            JSONObject appInfo = new JSONObject();
            appInfo.put("versionName", packageInfo.versionName);
            appInfo.put("versionCode", packageInfo.versionCode);
            appInfo.put("packageName", context.getPackageName());

            callbackContext.success(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            callbackContext.error("Failed to get app info: " + e.getMessage());
        } catch (JSONException e) {
            callbackContext.error("Failed to create JSON object: " + e.getMessage());
        }
    }

    private void loginWithMicrosoft(CallbackContext callbackContext) {
        PublicClientApplication.createSingleAccountPublicClientApplication(this.cordova.getActivity().getApplicationContext(),
            R.raw.auth_config_single_account,
            new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                @Override
                public void onCreated(ISingleAccountPublicClientApplication application) {
                    mSingleAccountApp = application;
                    mSingleAccountApp.signIn(this.cordova.getActivity(), null, getScopes(), getAuthInteractiveCallback(callbackContext));
                }

                @Override
                public void onError(MsalException exception) {
                    callbackContext.error("Failed to create MSAL application: " + exception.getMessage());
                }
            });
    }

    private String[] getScopes() {
        return new String[]{"User.Read"};
    }

    private AuthenticationCallback getAuthInteractiveCallback(CallbackContext callbackContext) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                callbackContext.success(authenticationResult.getAccount().getUsername());
            }

            @Override
            public void onError(MsalException exception) {
                callbackContext.error("Authentication failed: " + exception.getMessage());
            }

            @Override
            public void onCancel() {
                callbackContext.error("User cancelled the login process");
            }
        };
    }
}
