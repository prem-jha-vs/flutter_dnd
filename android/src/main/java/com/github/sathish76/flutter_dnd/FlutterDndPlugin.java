package com.github.sathish76.flutter_dnd;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.content.Intent;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class FlutterDndPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private Context applicationContext;
    private NotificationManager notificationManager;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        applicationContext = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_dnd");
        channel.setMethodCallHandler(this);
        notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (!isAboveMarshmallow()) {
            result.error("ERROR_INCOMPATIBLE_ANDROID_VERSION", 
                "This method requires Android version 23 (Marshmallow) or higher", null);
            return;
        }

        switch (call.method) {
            case "isNotificationPolicyAccessGranted":
                result.success(isNotificationPolicyAccessGranted());
                break;
            case "gotoPolicySettings":
                gotoPolicySettings();
                result.success(null);
                break;
            case "setInterruptionFilter":
                if (call.arguments != null) {
                    int interruptionFilter = (int) call.arguments;
                    result.success(setInterruptionFilter(interruptionFilter));
                } else {
                    result.error("ERROR_NULL_ARGUMENT", "Interruption filter argument cannot be null", null);
                }
                break;
            case "getCurrentInterruptionFilter":
                result.success(getCurrentInterruptionFilter());
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private boolean isAboveMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessGranted() {
        return notificationManager.isNotificationPolicyAccessGranted();
    }

    private void gotoPolicySettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationContext.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean setInterruptionFilter(int interruptionFilter) {
        if (notificationManager.isNotificationPolicyAccessGranted()) {
            notificationManager.setInterruptionFilter(interruptionFilter);
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private int getCurrentInterruptionFilter() {
        return notificationManager.getCurrentInterruptionFilter();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
