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

public class CallNoSleep extends CordovaPlugin {

    private PowerManager.WakeLock wakeLock;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startCall")) {
            String phoneNumber = args.getString(0);
            this.startCall(phoneNumber);
            callbackContext.success();
            return true;
        } else if (action.equals("preventSleep")) {
            this.preventSleep();
            callbackContext.success();
            return true;
        } else if (action.equals("allowSleep")) {
            this.allowSleep();
            callbackContext.success();
            return true;
        } else if (action.equals("getAppInfo")) {
            this.getAppInfo(callbackContext);
            return true;
        }
        return false;
    }

    private void startCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        this.cordova.getActivity().startActivity(intent);
    }

    private void preventSleep() {
        PowerManager powerManager = (PowerManager) this.cordova.getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "CallNoSleep::WakeLock");
        wakeLock.acquire();
    }

    private void allowSleep() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
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
}
