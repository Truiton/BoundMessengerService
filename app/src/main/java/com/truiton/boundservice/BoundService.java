/*
 * Copyright (c) 2016. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.truiton.boundservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import java.lang.ref.WeakReference;

public class BoundService extends Service {
    private static String LOG_TAG = "BoundService";
    private Chronometer mChronometer;
    static final int MSG_GET_TIMESTAMP = 1000;

    static class BoundServiceHandler extends Handler {
        private final WeakReference<BoundService> mService;

        public BoundServiceHandler(BoundService service) {
            mService = new WeakReference<BoundService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_TIMESTAMP:
                    long elapsedMillis = SystemClock.elapsedRealtime()
                            - mService.get().mChronometer.getBase();
                    int hours = (int) (elapsedMillis / 3600000);
                    int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
                    int seconds = (int) (elapsedMillis - hours * 3600000 - minutes
                            * 60000) / 1000;
                    int millis = (int) (elapsedMillis - hours * 3600000 - minutes
                            * 60000 - seconds * 1000);
                    Messenger activityMessenger = msg.replyTo;
                    Bundle b = new Bundle();
                    b.putString("timestamp", hours + ":" + minutes + ":" + seconds
                            + ":" + millis);
                    Message replyMsg = Message.obtain(null, MSG_GET_TIMESTAMP);
                    replyMsg.setData(b);
                    try {
                        activityMessenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new BoundServiceHandler(this));

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
        mChronometer.stop();
    }
}
