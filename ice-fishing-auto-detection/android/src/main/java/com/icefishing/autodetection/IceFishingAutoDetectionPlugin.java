package com.icefishing.autodetection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "IceFishingAutoDetection")
public class IceFishingAutoDetectionPlugin extends Plugin {

    public static final String ACTION_DETECTION_RESULT =
        "com.icefishing.autodetection.DETECTION_RESULT";

    private final IceFishingAutoDetection implementation =
        new IceFishingAutoDetection();

    public static final String ACTION_SCREEN_CHANGE =
        "com.icefishing.autodetection.SCREEN_CHANGE";

    private boolean receiverRegistered = false;

    private final BroadcastReceiver detectionReceiver =
        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;

                if (ACTION_SCREEN_CHANGE.equals(intent.getAction())) {
                    JSObject change = new JSObject();
                    change.put("timestamp", intent.getLongExtra(
                        "timestamp", System.currentTimeMillis()
                    ));
                    change.put("difference", intent.getFloatExtra(
                        "difference", 0
                    ));
                    notifyListeners("screenChange", change);
                    return;
                }

                String category = intent.getStringExtra("category");
                float confidence = intent.getFloatExtra("confidence", 0);
                long timestamp = intent.getLongExtra(
                    "timestamp",
                    System.currentTimeMillis()
                );

                if (category == null) return;

                JSObject result = new JSObject();
                result.put("category", category);
                result.put("confidence", confidence);
                result.put("timestamp", timestamp);

                notifyListeners("detectionResult", result);
            }
        };

    @Override
    public void load() {
        super.load();

        IntentFilter filter =
            new IntentFilter(ACTION_DETECTION_RESULT);
        filter.addAction(ACTION_SCREEN_CHANGE);

        if (Build.VERSION.SDK_INT >= 33) {
            getContext().registerReceiver(
                detectionReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            getContext().registerReceiver(
                detectionReceiver,
                filter
            );
        }

        receiverRegistered = true;
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void requestScreenCapture(PluginCall call) {
        try {
            MediaProjectionManager manager =
                (MediaProjectionManager) getContext().getSystemService(
                    Context.MEDIA_PROJECTION_SERVICE
                );

            if (manager == null) {
                call.reject("MediaProjection service unavailable");
                return;
            }

            Intent permissionIntent =
                manager.createScreenCaptureIntent();

            startActivityForResult(
                call,
                permissionIntent,
                "handleScreenCapturePermission"
            );

        } catch (Exception e) {
            call.reject("Unable to request screen capture permission", e);
        }
    }

    @ActivityCallback
    private void handleScreenCapturePermission(
        PluginCall call,
        ActivityResult result
    ) {
        if (call == null) return;

        if (result.getResultCode() != Activity.RESULT_OK ||
            result.getData() == null) {
            call.reject("Screen capture permission denied");
            return;
        }

        try {
            android.util.DisplayMetrics metrics =
                getContext().getResources().getDisplayMetrics();

            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int density = metrics.densityDpi;

            if (width <= 0 || height <= 0 || density <= 0) {
                call.reject("Invalid screen dimensions");
                return;
            }

            Intent serviceIntent = new Intent(
                getContext(),
                ScreenCaptureService.class
            );

            serviceIntent.putExtra(
                "resultCode",
                result.getResultCode()
            );
            serviceIntent.putExtra(
                "resultData",
                result.getData()
            );
            serviceIntent.putExtra("width", width);
            serviceIntent.putExtra("height", height);
            serviceIntent.putExtra("density", density);

            if (Build.VERSION.SDK_INT >= 26) {
                getContext().startForegroundService(serviceIntent);
            } else {
                getContext().startService(serviceIntent);
            }

            JSObject response = new JSObject();
            response.put("success", true);
            response.put("message", "Screen capture service requested");
            response.put("width", width);
            response.put("height", height);

            call.resolve(response);

        } catch (Exception e) {
            call.reject("Unable to start screen capture service", e);
        }
    }

    @PluginMethod
    public void stopScreenCapture(PluginCall call) {
        try {
            Intent serviceIntent = new Intent(
                getContext(),
                ScreenCaptureService.class
            );

            boolean stopped =
                getContext().stopService(serviceIntent);

            JSObject response = new JSObject();
            response.put("success", true);
            response.put(
                "message",
                stopped ? "Screen capture stopped" : "Capture service was not running"
            );

            call.resolve(response);

        } catch (Exception e) {
            call.reject("Unable to stop screen capture", e);
        }
    }

    @Override
    protected void handleOnDestroy() {
        if (receiverRegistered) {
            try {
                getContext().unregisterReceiver(detectionReceiver);
            } catch (Exception ignored) {
            }
            receiverRegistered = false;
        }

        super.handleOnDestroy();
    }
}
