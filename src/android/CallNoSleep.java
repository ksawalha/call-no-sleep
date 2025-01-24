package com.example.callnosleep;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CallNoSleep extends CordovaPlugin {
  public static final int CALL_REQ_CODE = 0;
  public static final int PERMISSION_DENIED_ERROR = 20;
  public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;

  private CallbackContext callbackContext;        
  private JSONArray executeArgs;

  private PowerManager.WakeLock wakeLock;
  private static final int REQUEST_CALL_PHONE = 1;

  protected void getCallPermission(int requestCode) {
    cordova.requestPermission(this, requestCode, CALL_PHONE);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.callbackContext = callbackContext;
    this.executeArgs = args;

    if (action.equals("callNumber")) {
      if (cordova.hasPermission(CALL_PHONE)) {
        callPhone(executeArgs);
      } else {
        getCallPermission(CALL_REQ_CODE);
      }
    } else if (action.equals("isCallSupported")) {
        this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, isTelephonyEnabled()));
    } else if (action.equals("startCall")) {
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
    } else {
      return false;
    }

    return true;
  }

  public void onRequestPermissionResult(int requestCode, String[] permissions,
                                        int[] grantResults) throws JSONException {
    for (int r : grantResults) {
      if (r == PackageManager.PERMISSION_DENIED) {
        this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
        return;
      }
    }
    switch (requestCode) {
      case CALL_REQ_CODE:
        callPhone(executeArgs);
        break;
    }
  }

  private void callPhone(JSONArray args) throws JSONException {
    String number = args.getString(0);
    number = number.replaceAll("#", "%23");

    if (!number.startsWith("tel:")) {
      number = String.format("tel:%s", number);
    }
    try {
      boolean bypassAppChooser = Boolean.parseBoolean(args.getString(1));
      boolean enableTelephony = isTelephonyEnabled();

      Intent intent = new Intent(enableTelephony ? (bypassAppChooser ? Intent.ACTION_DIAL : Intent.ACTION_CALL) : Intent.ACTION_VIEW);
     
      intent.setData(Uri.parse(number));

      if (!enableTelephony && bypassAppChooser) {
        intent.setPackage(getDialerPackage(intent));
      }

      cordova.getActivity().startActivity(intent);
      callbackContext.success();
    } catch (Exception e) {
      callbackContext.error("CouldNotCallPhoneNumber");
    }
  }

  private boolean isTelephonyEnabled() {
    TelephonyManager tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
    return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
  }

  private String getDialerPackage(Intent intent) {
    PackageManager packageManager = (PackageManager) cordova.getActivity().getPackageManager();
    List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

    for (int i = 0; i < activities.size(); i++) {
      if (activities.get(i).toString().toLowerCase().contains("com.android.server.telecom")) {
        return "com.android.server.telecom";
      }
      if (activities.get(i).toString().toLowerCase().contains("com.android.phone")) {
        return "com.android.phone";
      } else if (activities.get(i).toString().toLowerCase().contains("call")) {
        return activities.get(i).toString().split("[ ]")[1].split("[/]")[0];
      }
    }
    return "";
  }

  private void startCall(String phoneNumber, CallbackContext callbackContext) {
    if (ContextCompat.checkSelfPermission(this.cordova.getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this.cordova.getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
      callbackContext.error("CALL_PHONE permission not granted");
    } else {
      try {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        this.cordova.getActivity().startActivity(intent);
        callbackContext.success();
      } catch (Exception e) {
        callbackContext.error("Failed to start call: " + e.getMessage());
      }
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
}
