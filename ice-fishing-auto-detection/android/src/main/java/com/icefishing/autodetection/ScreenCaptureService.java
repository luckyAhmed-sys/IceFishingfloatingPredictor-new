package com.icefishing.autodetection;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class ScreenCaptureService extends Service {

    private static final String TAG = "IceFishingCapture";
    private static final String CHANNEL_ID = "ice_fishing_capture";
    private static final int NOTIFICATION_ID = 1001;

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private FrameProcessor frameProcessor = new FrameProcessor();
    private byte[] previousFrameGrid = null;
    private int stableFrameCount = 0;
    private long lastSignificantChangeAt = 0;

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                );
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification()
                );
            }

            int resultCode = intent.getIntExtra("resultCode", 0);

            Intent resultData;

            if (Build.VERSION.SDK_INT >= 33) {
                resultData = intent.getParcelableExtra(
                    "resultData",
                    Intent.class
                );
            } else {
                resultData = intent.getParcelableExtra("resultData");
            }

            int width = intent.getIntExtra("width", 720);
            int height = intent.getIntExtra("height", 1280);
            int density = intent.getIntExtra("density", 1);

            if (resultData == null ||
                width <= 0 || height <= 0 || density <= 0) {
                throw new IllegalArgumentException(
                    "Invalid screen capture configuration"
                );
            }

            MediaProjectionManager manager =
                (MediaProjectionManager) getSystemService(
                    Context.MEDIA_PROJECTION_SERVICE
                );

            if (manager == null) {
                throw new IllegalStateException(
                    "MediaProjection service unavailable"
                );
            }

            mediaProjection =
                manager.getMediaProjection(resultCode, resultData);

            if (mediaProjection == null) {
                throw new IllegalStateException(
                    "MediaProjection unavailable"
                );
            }

            mediaProjection.registerCallback(
                new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        Log.i(TAG, "MediaProjection stopped");
                        releaseCaptureResources();
                        stopSelf();
                    }
                },
                handler
            );

            previousFrameGrid = null;
            stableFrameCount = 0;

            imageReader = ImageReader.newInstance(
                width,
                height,
                android.graphics.PixelFormat.RGBA_8888,
                2
            );

            imageReader.setOnImageAvailableListener(reader -> {
                android.media.Image image = null;

                try {
                    image = reader.acquireLatestImage();

                    if (image != null) {
                        byte[] rgbGrid =
                            frameProcessor.extractRgbGrid(image);

                        if (rgbGrid != null) {
                            long now = System.currentTimeMillis();
                            boolean changed = previousFrameGrid != null &&
                                frameProcessor.hasSignificantChange(previousFrameGrid, rgbGrid);

                            if (changed && now - lastSignificantChangeAt >= 1000) {
                                lastSignificantChangeAt = now;

                                float difference =
                                    frameProcessor.calculateFrameDifference(
                                        previousFrameGrid, rgbGrid
                                    );

                                Intent changeIntent = new Intent(
                                    IceFishingAutoDetectionPlugin.ACTION_SCREEN_CHANGE
                                );
                                changeIntent.setPackage(getPackageName());
                                changeIntent.putExtra("timestamp", now);
                                changeIntent.putExtra("difference", difference);
                                sendBroadcast(changeIntent);

                                Log.i(TAG, "Significant screen change broadcast sent");
                            }

                            if (previousFrameGrid != null &&
                                frameProcessor.isFrameStable(
                                    previousFrameGrid, rgbGrid)) {
                                stableFrameCount++;
                            } else {
                                stableFrameCount = 0;
                            }

                            previousFrameGrid = rgbGrid;

                            Log.d(TAG,
                                "Frame processed; stable count=" +
                                stableFrameCount);
                        } else {
                            Log.w(TAG,
                                "Frame processing returned no data");
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Frame processing error", e);

                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            }, handler);

            virtualDisplay = mediaProjection.createVirtualDisplay(
                "IceFishingScreen",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                handler
            );

            if (virtualDisplay == null) {
                throw new IllegalStateException(
                    "VirtualDisplay creation failed"
                );
            }

            Log.i(TAG, "Screen capture started successfully");

        } catch (Exception e) {
            Log.e(TAG, "Screen capture failed", e);
            stopCapture();
            stopForeground(true);
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private Notification createNotification() {
        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_LOW);
        }

        return builder
            .setContentTitle("Ice Fishing Predictor")
            .setContentText("Screen detection is active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Capture",
                    NotificationManager.IMPORTANCE_LOW
                );

            NotificationManager manager =
                getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    private void releaseCaptureResources() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (imageReader != null) {
            imageReader.setOnImageAvailableListener(null, null);
            imageReader.close();
            imageReader = null;
        }
    }

    private void stopCapture() {
        MediaProjection projection = mediaProjection;
        mediaProjection = null;

        releaseCaptureResources();

        if (projection != null) {
            try {
                projection.stop();
            } catch (Exception e) {
                Log.w(TAG, "Projection already stopped", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        stopCapture();
        stopForeground(true);
        super.onDestroy();
        Log.i(TAG, "Screen capture service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
