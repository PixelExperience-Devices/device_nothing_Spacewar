/*
 * Copyright (C) 2022 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.glyph;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class GlyphNotificationListenerService extends NotificationListenerService {

    private static final boolean DEBUG = false;
    private static final String TAG = "GlyphNotificationListenerService";

    private static final int LED_BLINK_DELAY = 35;
    private static final int LED_SLEEP_DELAY = 150;

    private LedHandler mHandler;
    private int mLedPlayingCount = 0;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        if (DEBUG) Log.d(TAG, "Listener connected");
        mHandler = new LedHandler(Looper.getMainLooper());
    }

    @Override
    public void onListenerDisconnected() {
        if (DEBUG) Log.d(TAG, "Listener disconnected");
        if (mLedPlayingCount != 0) {
            mHandler.removeMessages(LedHandler.MSG_BLINK_LED);
            mHandler.removeMessages(LedHandler.MSG_EXTINCT_LED);
            mLedPlayingCount = 0;
        }
        extinctNotificationLed();
        super.onListenerDisconnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (DEBUG) Log.d(TAG, "Notification received");
        if (GlyphUtils.isGlyphNotificationBlinkEnabled(this)) {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, LedHandler.MSG_BLINK_LED), 0);
        }
    }

    private void blinkNotificationLed() {
        if (DEBUG) Log.d(TAG, "Before: mLedPlayingCount = " + mLedPlayingCount);
        GlyphUtils.writeLedById(1, GlyphUtils.getGlyphBrightness(this));
        mHandler.sendMessageDelayed(Message.obtain(mHandler, LedHandler.MSG_EXTINCT_LED),
                LED_BLINK_DELAY);
        mLedPlayingCount++;
        if (mLedPlayingCount >= 2) {
            mLedPlayingCount = 0;
        } else {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, LedHandler.MSG_BLINK_LED),
                    LED_SLEEP_DELAY);
        }
        if (DEBUG) Log.d(TAG, "After: mLedPlayingCount = " + mLedPlayingCount);
    }

    private void extinctNotificationLed() {
        GlyphUtils.writeLedById(1, 0);
    }

    private class LedHandler extends Handler {

        private static final int MSG_BLINK_LED = 1;
        private static final int MSG_EXTINCT_LED = 2;

        public LedHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BLINK_LED:
                    blinkNotificationLed();
                    break;
                case MSG_EXTINCT_LED:
                    extinctNotificationLed();
                    break;
            }
        }
    }
}
