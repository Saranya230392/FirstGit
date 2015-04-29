/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.widget.TextView;

import android.location.CountryDetector;

public final class PhoneNumberFormatter {
    private PhoneNumberFormatter() {
        ;
    }

    /**
     * Load {@link TextWatcherLoadAsyncTask} in a worker thread and set it to a {@link TextView}.
     */
    private static class TextWatcherLoadAsyncTask extends
            AsyncTask<Void, Void, PhoneNumberFormattingTextWatcher> {
        private final String mCountryCode;
        private final TextView mTextView;

        public TextWatcherLoadAsyncTask(String countryCode, TextView textView) {
            mCountryCode = countryCode;
            mTextView = textView;
        }

        @Override
        protected PhoneNumberFormattingTextWatcher doInBackground(Void... params) {
            return new PhoneNumberFormattingTextWatcher(mCountryCode);
        }

        @Override
        protected void onPostExecute(PhoneNumberFormattingTextWatcher watcher) {
            if (watcher == null || isCancelled()) {
                return; // May happen if we cancel the task.
            }
            mTextView.addTextChangedListener(watcher);
        }
    }

    /**
     * Delay-set {@link PhoneNumberFormattingTextWatcher} to a {@link TextView}.
     */
    public static final void setPhoneNumberFormattingTextWatcher(Context context,
            TextView textView) {
        new TextWatcherLoadAsyncTask(getCurrentCountryIso(context), textView)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
    }

    public static final String getCurrentCountryIso(Context context) {
        CountryDetector detector =
                (CountryDetector)context.getSystemService(Context.COUNTRY_DETECTOR);
        try {
            return detector.detectCountry().getCountryIso();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }
}
