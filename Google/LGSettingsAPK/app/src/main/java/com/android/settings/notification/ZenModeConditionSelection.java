/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.notification;

import android.animation.LayoutTransition;
import android.app.INotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.notification.Condition;
import android.service.notification.IConditionListener;
import android.service.notification.ZenModeConfig;
import android.util.Log;

import com.android.settings.R;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class ZenModeConditionSelection {
    private static final String TAG = "ZenModeConditionSelection";
    private static final boolean DEBUG = true;

    private final INotificationManager mNoMan;
    private final H mHandler = new H();
    private final Context mContext;
    private final List<Condition> mConditions;
    private Condition mCondition;
    private int mCondtionIndex = 0;
    ArrayList<String> mConditionDialogEntry;

    public ZenModeConditionSelection(Context context) {
        mContext = context;
        mConditions = new ArrayList<Condition>();
		mConditionDialogEntry = new ArrayList<String>();
        mNoMan = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));

        mConditionDialogEntry.add(mContext.getString(R.string.zen_mode_settings_condition_all_times));
        // FIXME : toTimeCondition needs extra parameters.

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < ZenModeConfig.MINUTE_BUCKETS.length; i++) {
                toTimeConditionForL(i);
            }
        } else {
            for (int i = 0; i < ZenModeConfig.MINUTE_BUCKETS.length; i++) {
                // native : int i = ZenModeConfig.MINUTE_BUCKETS.length - 1; i >= 0; --i
                toTimsConditionAfterMR1(i);
            }
        }
    }

    private void toTimsConditionAfterMR1(int i) {
        Condition result = null;
        try {
            Class klass = Class
                    .forName("android.service.notification.ZenModeConfig");
            Method method = klass.getMethod("toTimeCondition", new Class[] {
                    Context.class, int.class, int.class });
            Object returnResult = method.invoke(klass, new Object[] { mContext,
                    ZenModeConfig.MINUTE_BUCKETS[i], UserHandle.myUserId() });
            result = (Condition)method.getReturnType().cast(returnResult);
            Log.d(TAG, "method.invoke toTimeCondition MR1");
        } catch (ClassNotFoundException e) {
            Log.d(TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(TAG, e.getMessage());
        }
        handleCondition(result);
    }

    private void toTimeConditionForL(int i) {
        Condition result = null;
        try {
            Class klass = Class
                    .forName("android.service.notification.ZenModeConfig");
            Method method = klass.getMethod("toTimeCondition",
                    new Class[] { int.class });
            Object returnResult = method.invoke(klass,
                    new Object[] { ZenModeConfig.MINUTE_BUCKETS[i] });
            result = (Condition)method.getReturnType().cast(returnResult);
            Log.d(TAG, "method.invoke toTimeCondition");
        } catch (ClassNotFoundException e) {
            Log.d(TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(TAG, e.getMessage());
        }
        handleCondition(result);
    }

    protected void onConditionAttachedToWindow() {
        requestZenModeConditions(Condition.FLAG_RELEVANT_NOW);
    }

    protected void onConditionDetachedFromWindow() {
        requestZenModeConditions(0 /*none*/);
    }

    protected void requestZenModeConditions(int relevance) {
        if (DEBUG) {
            Log.d(TAG, "requestZenModeConditions " + Condition.relevanceToString(relevance));
        }
        try {
            mNoMan.requestZenModeConditions(mListener, relevance);
        } catch (RemoteException e) {
            Log.d(TAG, "requestZenModeConditions RemoteException");
        }
    }

    protected void handleConditions(Condition[] conditions) {
        for (Condition c : conditions) {
            handleCondition(c);
        }
    }

    protected void handleCondition(Condition c) {
        if (mConditions.contains(c)) {
            return;
        }

        if (c.state == Condition.STATE_TRUE) {
            mCondtionIndex++;
            mConditionDialogEntry.add(c.summary);
            mConditions.add(c);
        }
    }

    protected void setCondition(Condition c) {
        if (DEBUG) {
            Log.d(TAG, "setCondition " + c);
        }
        mCondition = c;
    }

    public void confirmCondition() {
        if (DEBUG) {
            Log.d(TAG, "confirmCondition " + mCondition);
        }
        try {
            mNoMan.setZenModeCondition(mCondition);
        } catch (RemoteException e) {
            Log.d(TAG, "handleSubscribe " + e);
        }

    }

    private final IConditionListener mListener = new IConditionListener.Stub() {
        @Override
        public void onConditionsReceived(Condition[] conditions) {
            if (conditions == null || conditions.length == 0) {
                return;
            }
            mHandler.obtainMessage(H.CONDITIONS, conditions).sendToTarget();
        }
    };

    private final class H extends Handler {
        private static final int CONDITIONS = 1;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CONDITIONS) {
                handleConditions((Condition[])msg.obj);
            }
        }
    }

    public ArrayList<String> getZenModeConditionEntry() {
        return mConditionDialogEntry;
    }

    public void setZenModeConditionValue(int index) {
        if (0 < index && index <= mCondtionIndex) {
            mCondition = mConditions.get(index - 1);
            Log.d(TAG, "setZenModeConditionValue" + " mCondition : " + mCondition + " , index : " + index + " ,mCondtionIndex : " + mCondtionIndex);
        }
    }
}
