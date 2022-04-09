package com.reactlibrary.ksquad.readsms;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;


public class ReadSmsModule extends ReactContextBaseJavaModule {
    private final String TAG = ReadSmsModule.class.getSimpleName();
    private final ReactApplicationContext reactContext;
    private BroadcastReceiver msgReceiver;

    public ReadSmsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ReadSms";
    }

    @ReactMethod
    public void stopReadSMS() {
        try {
            if (reactContext != null && msgReceiver != null) {
                reactContext.unregisterReceiver(msgReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void startReadSMS(final Callback success, final Callback error) {
      int hadPermissionReciveSms  = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.RECEIVE_SMS);
      int hadPermissionReadSMS  = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.READ_SMS);
        try {
            if ( hadPermissionReciveSms == PackageManager.PERMISSION_GRANTED
                    &&  hadPermissionReadSMS == PackageManager.PERMISSION_GRANTED) {
                msgReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        WritableMap message = getMessageFromMessageIntent(intent);
                        reactContext.getJSModule(RCTDeviceEventEmitter.class)
                                .emit("received_sms",message );
                    }
                };
                String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
                reactContext.registerReceiver(msgReceiver, new IntentFilter(SMS_RECEIVED_ACTION));
                success.invoke("Start Read SMS successfully");
                Log.d(TAG,"success invoke");
            } else {
                // Permission has not been granted
                error.invoke("Required RECEIVE_SMS and READ_SMS permission");
                Log.d(TAG,"error invoke");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WritableMap getMessageFromMessageIntent(Intent intent) {
        final Bundle bundle = intent.getExtras();
        WritableMap params = Arguments.createMap();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                if (pdusObj != null) {
                  Log.d(TAG, "pdusObj len " + pdusObj.length);
                    for (Object aPdusObj : pdusObj) {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                        params.putString("message", currentMessage.getMessageBody());
                        params.putString("sender", currentMessage.getOriginatingAddress());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }
}
