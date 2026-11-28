package com.icefishing.autodetection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;

import com.getcapacitor.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "IceFishingAutoDetection")
public class IceFishingAutoDetectionPlugin extends Plugin {

    private IceFishingAutoDetection implementation =
        new IceFishingAutoDetection();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void requestScreenCapture(PluginCall call) {

        MediaProjectionManager manager =
            (MediaProjectionManager) getContext().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE
            );

        Intent intent = manager.createScreenCaptureIntent();

        startActivityForResult(
            call,
            intent,
            "handleScreenCapturePermission"
        );
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

        DisplayMetrics metrics = getContext()
            .getResources()
            .getDisplayMetrics();

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

        serviceIntent.putExtra("width", metrics.widthPixels);
        serviceIntent.putExtra("height", metrics.heightPixels);
        serviceIntent.putExtra("density", metrics.densityDpi);

        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                getContext().startForegroundService(serviceIntent);
            } else {
                getContext().startService(serviceIntent);
            }

            JSObject response = new JSObject();
            response.put("success", true);
            response.put("message", "Screen capture started");

            call.resolve(response);

        } catch (Exception e) {
            call.reject("Unable to start screen capture", e);
        }
    }
}
