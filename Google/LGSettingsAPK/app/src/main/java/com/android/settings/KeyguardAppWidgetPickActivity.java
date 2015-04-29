/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.LauncherActivity.IconResizer;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.ResolveInfo;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.lockscreen.LockSettingsFeatureConfig;
import java.lang.ref.WeakReference;
import java.util.List;
import com.android.settings.Utils;

/**
 * Displays a list of {@link AppWidgetProviderInfo} widgets, along with any
 * injected special widgets specified through
 * {@link AppWidgetManager#EXTRA_CUSTOM_INFO} and
 * {@link AppWidgetManager#EXTRA_CUSTOM_EXTRAS}.
 * <p>
 * When an installed {@link AppWidgetProviderInfo} is selected, this activity
 * will bind it to the given {@link AppWidgetManager#EXTRA_APPWIDGET_ID},
 * otherwise it will return the requested extras.
 */
public class KeyguardAppWidgetPickActivity extends Activity
    implements GridView.OnItemClickListener,
        AppWidgetLoader.ItemConstructor<KeyguardAppWidgetPickActivity.Item> {
    private static final String TAG = "KeyguardAppWidgetPickActivity";
    private static final int REQUEST_PICK_APPWIDGET = 126;
    private static final int REQUEST_CREATE_APPWIDGET = 127;
    private static final int MAX_WIDGET_ALLOWED_TO_ADD = 5;

    private AppWidgetLoader<Item> mAppWidgetLoader;
    private List<Item> mItems;
    private GridView mGridView;
    private AppWidgetAdapter mAppWidgetAdapter;
    private AppWidgetManager mAppWidgetManager;
    private int mAppWidgetId;
    // Might make it possible to make this be false in future
    private boolean mAddingToKeyguard = true;
    private Intent mResultData;
    private LockPatternUtils mLockPatternUtils;
    private Bundle mExtraConfigureOptions;
    private static AppThemeWidget mAppThemeWidget;
    private boolean mIsCameraEnabled;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.keyguard_appwidget_picker_layout);
        super.onCreate(savedInstanceState);

        // Set default return data
        setResultData(RESULT_CANCELED, null);

        // Read the appWidgetId passed our direction, otherwise bail if not found
        final Intent intent = getIntent();
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        } else {
            finish();
        }
        mExtraConfigureOptions = intent.getBundleExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS);
        mIsCameraEnabled = LockSettingsFeatureConfig.isEnabled(this, "config_feature_camera_widget");
        if (mIsCameraEnabled && !isCameraAppInstalled()) {
            mIsCameraEnabled = false;
        }
        mAppThemeWidget = new AppThemeWidget(this);
        mGridView = (GridView) findViewById(R.id.widget_list);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int maxGridWidth = getResources().getDimensionPixelSize(
                R.dimen.keyguard_appwidget_picker_max_width);

        if (maxGridWidth < dm.widthPixels) {
            mGridView.getLayoutParams().width = maxGridWidth;
        }
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetLoader = new AppWidgetLoader<Item>(this, mAppWidgetManager, this);
        mItems = mAppWidgetLoader.getItems(getIntent());
        if (mIsCameraEnabled) {
            String cameraStr = getResources().getString(R.string.lockscreen_camera_widget_text);
            mItems.add(0, new Item(this, cameraStr));
        }
        mAppWidgetAdapter = new AppWidgetAdapter(this, mItems, mIsCameraEnabled);
        mGridView.setAdapter(mAppWidgetAdapter);
        mGridView.setOnItemClickListener(this);

        mLockPatternUtils = new LockPatternUtils(this); // TEMP-- we want to delete this
    }

    private boolean isCameraAppInstalled() {
        Intent intent = new Intent();
        intent.setClassName("com.lge.camera", "com.lge.camera.CameraAppLauncher");
        PackageManager pm = getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, 0);
        if (info == null) {
            return false;
        }
        return true;
    }
    /**
     * Convenience method for setting the result code and intent. This method
     * correctly injects the {@link AppWidgetManager#EXTRA_APPWIDGET_ID} that
     * most hosts expect returned.
     */
    void setResultData(int code, Intent intent) {
        Intent result = intent != null ? intent : new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        mResultData = result;
        setResult(code, result);
    }

    /**
     * Item that appears in the AppWidget picker grid.
     */
    public static class Item implements AppWidgetLoader.LabelledItem {
        protected static IconResizer sResizer;


        CharSequence label;
        int appWidgetPreviewId;
        int iconId;
        String packageName;
        String className;
        Bundle extras;
        private WidgetPreviewLoader mWidgetPreviewLoader;
        private Context mContext;

        /**
         * Create a list item from given label and icon.
         */
        Item(Context context, CharSequence label) {
            this.label = label;
            mContext = context;
        }

        void loadWidgetPreview(ImageView v) {
            mWidgetPreviewLoader = new WidgetPreviewLoader(mContext, v);
            mWidgetPreviewLoader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);
        }

        void cancelLoadingWidgetPreview() {
            if (mWidgetPreviewLoader != null) {
                mWidgetPreviewLoader.cancel(false);
                mWidgetPreviewLoader = null;
            }
        }

        /**
         * Build the {@link Intent} described by this item. If this item
         * can't create a valid {@link android.content.ComponentName}, it will return
         * {@link Intent#ACTION_CREATE_SHORTCUT} filled with the item label.
         */
        Intent getIntent() {
            Intent intent = new Intent();
            if (packageName != null && className != null) {
                // Valid package and class, so fill details as normal intent
                intent.setClassName(packageName, className);
                if (extras != null) {
                    intent.putExtras(extras);
                }
            } else {
                // No valid package or class, so treat as shortcut with label
                intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
            }
            return intent;
        }

        public CharSequence getLabel() {
            return label;
        }

        class WidgetPreviewLoader extends AsyncTask<Void, Bitmap, Void> {
            private Resources mResources;
            private PackageManager mPackageManager;
            private int mIconDpi;
            private ImageView mView;
            private boolean mIsCameraWidget;
            public WidgetPreviewLoader(Context context, ImageView v) {
                super();
                mResources = context.getResources();
                mPackageManager = context.getPackageManager();
                ActivityManager activityManager =
                        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                mIconDpi = activityManager.getLauncherLargeIconDensity();
                mView = v;
                Object tag = mView.getTag();
                if (null != tag) {
                    mIsCameraWidget = (Boolean)mView.getTag();
                    mView.setTag(null);
                }
            }
            public Void doInBackground(Void... params) {
                if (!isCancelled()) {
                    Bitmap b = null;

                    if (mIsCameraWidget) {
                        b = getCamereBitmap();
                    } else {
                        int appWidgetPreviewWidth = mResources
                                .getDimensionPixelSize(R.dimen.appwidget_preview_width);
                        int appWidgetPreviewHeight = mResources
                                .getDimensionPixelSize(R.dimen.appwidget_preview_height);
                        b = getWidgetPreview(new ComponentName(packageName, className),
                                appWidgetPreviewId, iconId, appWidgetPreviewWidth,
                                appWidgetPreviewHeight);
                    }
                    publishProgress(b);
                }
                return null;
            }

            private Bitmap getCamereBitmap() {
                Bitmap finalBitmap = null;
                Bitmap cameraIcon = null;
                Bitmap checkImage = null;
                int isChecked = android.provider.Settings.Secure.getInt(
                        mContext.getContentResolver(), "show_camera_widget", 0);
                Intent intent = new Intent();
                intent.setClassName("com.lge.camera", "com.lge.camera.CameraApp");
                if (mContext != null) {
                    final ResolveInfo info;
                    PackageManager mPm = mPackageManager;
                    if (mPm != null) {
                        info = mPm.resolveActivity(intent, 0);
                        BitmapDrawable icon = (BitmapDrawable)info.loadIcon(mPm);
                        if (icon != null) {
                            cameraIcon = icon.getBitmap();
                        } else {
                            cameraIcon = BitmapFactory
                                    .decodeResource(mResources, R.drawable.camera);
                        }
                    }
                }
                if (isChecked == 0) {
                    checkImage = BitmapFactory.decodeResource(mResources,
                            R.drawable.check_box_unchecked);
                } else {
                    checkImage = BitmapFactory.decodeResource(mResources,
                            R.drawable.check_box_checked);
                }
                if (cameraIcon != null) {
                    finalBitmap = combineImages(checkImage, cameraIcon);
                }
                return finalBitmap;
            }

            private Bitmap combineImages(Bitmap checkBitmap, Bitmap camBitmap) {
                Bitmap cs = null;
                int maxWidth = mResources.getDimensionPixelSize(R.dimen.appwidget_preview_width);
                int maxHeight = mResources.getDimensionPixelSize(R.dimen.appwidget_preview_height);

                int checkBoxRightMarginRight = mResources
                        .getDimensionPixelSize(R.dimen.camera_check_box_margin_right);
                camBitmap = resizeCameraBitmap(camBitmap, maxWidth, maxHeight);

                int cameraHeight = camBitmap.getHeight();
                float y_pos = (maxHeight - cameraHeight) / 2;
                float x_pos = checkBitmap.getWidth() + checkBoxRightMarginRight;

                cs = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
                Canvas comboImage = new Canvas(cs);
                if (Utils.isRTLLanguage()
                        || Utils.isEnglishDigitRTLLanguage()) {
                    float x_pos_cameraicon_rtl = maxWidth
                            - (camBitmap.getWidth() + checkBitmap.getWidth() + checkBoxRightMarginRight);
                    float x_pos_checkbox_rtl = maxWidth - checkBitmap.getWidth();
                    comboImage.drawBitmap(camBitmap, x_pos_cameraicon_rtl, y_pos, null);
                    comboImage.drawBitmap(checkBitmap, x_pos_checkbox_rtl, 0f, null);

                } else {
                    comboImage.drawBitmap(checkBitmap, 0f, 0f, null);
                    comboImage.drawBitmap(camBitmap, x_pos, y_pos, null);
                }
                return cs;
            }

            private Bitmap resizeCameraBitmap(Bitmap camBitmap, int maxWidth, int maxHeight) {
                //Bitmap cs = null;

                int bitmapWidth = camBitmap.getWidth();
                int bitmapHeight = camBitmap.getHeight();
                Drawable drawable = new BitmapDrawable(camBitmap);

                float scale = 1f;
                int cameraWidgetHeight = mResources
                        .getDimensionPixelSize(R.dimen.camera_widget_preview_height);

                scale = (float)cameraWidgetHeight / (float)bitmapHeight;

                int finalPreviewWidth = (int)(scale * bitmapWidth);
                int finalPreviewHeight = (int)(scale * bitmapHeight);

                bitmapWidth = Math.min(finalPreviewWidth, maxWidth);
                bitmapHeight = Math.min(finalPreviewHeight, maxHeight);

                camBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
                renderDrawableToBitmap(drawable, camBitmap, 0, 0, finalPreviewWidth,
                        finalPreviewHeight);
                return camBitmap;
            }
            public void onProgressUpdate(Bitmap... values) {
                if (!isCancelled()) {
                    Bitmap b = values[0];
                    mView.setImageBitmap(b);
                }
            }
            abstract class WeakReferenceThreadLocal<T> {
                private ThreadLocal<WeakReference<T>> mThreadLocal;
                public WeakReferenceThreadLocal() {
                    mThreadLocal = new ThreadLocal<WeakReference<T>>();
                }

                abstract T initialValue();

                public void set(T t) {
                    mThreadLocal.set(new WeakReference<T>(t));
                }

                public T get() {
                    WeakReference<T> reference = mThreadLocal.get();
                    T obj;
                    if (reference == null) {
                        obj = initialValue();
                        mThreadLocal.set(new WeakReference<T>(obj));
                        return obj;
                    } else {
                        obj = reference.get();
                        if (obj == null) {
                            obj = initialValue();
                            mThreadLocal.set(new WeakReference<T>(obj));
                        }
                        return obj;
                    }
                }
            }

            class CanvasCache extends WeakReferenceThreadLocal<Canvas> {
                @Override
                protected Canvas initialValue() {
                    return new Canvas();
                }
            }

            class PaintCache extends WeakReferenceThreadLocal<Paint> {
                @Override
                protected Paint initialValue() {
                    return null;
                }
            }

            class BitmapCache extends WeakReferenceThreadLocal<Bitmap> {
                @Override
                protected Bitmap initialValue() {
                    return null;
                }
            }

            class RectCache extends WeakReferenceThreadLocal<Rect> {
                @Override
                protected Rect initialValue() {
                    return new Rect();
                }
            }

            // Used for drawing widget previews
            CanvasCache sCachedAppWidgetPreviewCanvas = new CanvasCache();
            RectCache sCachedAppWidgetPreviewSrcRect = new RectCache();
            RectCache sCachedAppWidgetPreviewDestRect = new RectCache();
            PaintCache sCachedAppWidgetPreviewPaint = new PaintCache();

            private Bitmap getWidgetPreview(ComponentName provider, int previewImage,
                    int iconId, int maxWidth, int maxHeight) {
                // Load the preview image if possible
                String packageName = provider.getPackageName();
                if (maxWidth < 0) maxWidth = Integer.MAX_VALUE;
                if (maxHeight < 0) maxHeight = Integer.MAX_VALUE;


                int appIconSize = mResources.getDimensionPixelSize(R.dimen.app_icon_size);

                Drawable drawable = null;
                if (mAppThemeWidget != null) {
                    drawable = mAppThemeWidget.getDrawable(provider.getClassName());
                }

                if (previewImage != 0 && drawable == null) {
                    drawable = mPackageManager.getDrawable(packageName, previewImage, null);
                    if (drawable == null) {
                        Log.w(TAG, "Can't load widget preview drawable 0x" +
                                Integer.toHexString(previewImage) + " for provider: " + provider);
                    }
                }

                int bitmapWidth;
                int bitmapHeight;
                Bitmap defaultPreview = null;
                boolean widgetPreviewExists = (drawable != null);
                if (widgetPreviewExists) {
                    bitmapWidth = drawable.getIntrinsicWidth();
                    bitmapHeight = drawable.getIntrinsicHeight();
                } else {
                    // Generate a preview image if we couldn't load one
                    bitmapWidth = appIconSize;
                    bitmapHeight = appIconSize;
                    defaultPreview = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                            Config.ARGB_8888);

                    try {
                        Drawable icon = null;
                        if (iconId > 0)
                            icon = getFullResIcon(packageName, iconId);
                        if (icon != null) {
                            renderDrawableToBitmap(icon, defaultPreview, 0,
                                    0, appIconSize, appIconSize);
                        }
                    } catch (Resources.NotFoundException e) {
                    }
                }

                // Scale to fit width only - let the widget preview be clipped in the
                // vertical dimension
                float scale = 1f;
                if (bitmapWidth > maxWidth) {
                    scale = maxWidth / (float) bitmapWidth;
                }
                int finalPreviewWidth = (int) (scale * bitmapWidth);
                int finalPreviewHeight = (int) (scale * bitmapHeight);

                bitmapWidth = finalPreviewWidth;
                bitmapHeight = Math.min(finalPreviewHeight, maxHeight);

                Bitmap preview = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                        Config.ARGB_8888);

                // Draw the scaled preview into the final bitmap
                if (widgetPreviewExists) {
                    renderDrawableToBitmap(drawable, preview, 0, 0, finalPreviewWidth,
                            finalPreviewHeight);
                } else {
                    final Canvas c = sCachedAppWidgetPreviewCanvas.get();
                    final Rect src = sCachedAppWidgetPreviewSrcRect.get();
                    final Rect dest = sCachedAppWidgetPreviewDestRect.get();
                    c.setBitmap(preview);
                    src.set(0, 0, defaultPreview.getWidth(), defaultPreview.getHeight());
                    dest.set(0, 0, finalPreviewWidth, finalPreviewHeight);

                    Paint p = sCachedAppWidgetPreviewPaint.get();
                    if (p == null) {
                        p = new Paint();
                        p.setFilterBitmap(true);
                        sCachedAppWidgetPreviewPaint.set(p);
                    }
                    c.drawBitmap(defaultPreview, src, dest, p);
                    c.setBitmap(null);
                }
                return preview;
            }
            public Drawable getFullResDefaultActivityIcon() {
                return getFullResIcon(Resources.getSystem(),
                        android.R.mipmap.sym_def_app_icon);
            }

            public Drawable getFullResIcon(Resources resources, int iconId) {
                Drawable d;
                try {
                    d = resources.getDrawableForDensity(iconId, mIconDpi);
                } catch (Resources.NotFoundException e) {
                    d = null;
                }

                return (d != null) ? d : getFullResDefaultActivityIcon();
            }

            public Drawable getFullResIcon(String packageName, int iconId) {
                Resources resources;
                try {
                    resources = mPackageManager.getResourcesForApplication(packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    resources = null;
                }
                if (resources != null) {
                    if (iconId != 0) {
                        return getFullResIcon(resources, iconId);
                    }
                }
                return getFullResDefaultActivityIcon();
            }

            private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h) {
                renderDrawableToBitmap(d, bitmap, x, y, w, h, 1f);
            }

            private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h,
                    float scale) {
                if (bitmap != null) {
                    Canvas c = new Canvas(bitmap);
                    c.scale(scale, scale);
                    Rect oldBounds = d.copyBounds();
                    d.setBounds(x, y, x + w, y + h);
                    d.draw(c);
                    d.setBounds(oldBounds); // Restore the bounds
                    c.setBitmap(null);
                }
            }
        }
    }

    @Override
    public Item createItem(Context context, AppWidgetProviderInfo info, Bundle extras) {
        CharSequence label = info.label;

        Item item = new Item(context, label);
        item.appWidgetPreviewId = info.previewImage;
        item.iconId = info.icon;
        item.packageName = info.provider.getPackageName();
        item.className = info.provider.getClassName();
        item.extras = extras;
        return item;
    }

    protected static class AppWidgetAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final List<Item> mItems;
        //private Context mContext;
        private boolean mIsCameraEnabled;

        /**
         * Create an adapter for the given items.
         */
        public AppWidgetAdapter(Context context, List<Item> items, boolean isCameraEnabled) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //mContext = context;
            mItems = items;
            mIsCameraEnabled = isCameraEnabled;
        }

        /**
         * {@inheritDoc}
         */
        public int getCount() {
            return mItems.size();
        }

        /**
         * {@inheritDoc}
         */
        public Object getItem(int position) {
            return mItems.get(position);
        }

        /**
         * {@inheritDoc}
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * {@inheritDoc}
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.keyguard_appwidget_item, parent, false);
            }

               Item item = (Item) getItem(position);
               TextView textView = (TextView) convertView.findViewById(R.id.label);
               textView.setText(item.label);
               ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
               iconView.setImageDrawable(null);
            if (mIsCameraEnabled && (position == 0)) {
                iconView.setTag(true);
            }
               item.loadWidgetPreview(iconView);

            return convertView;
        }

        public void cancelAllWidgetPreviewLoaders() {
            for (int i = 0; i < mItems.size(); i++) {
                mItems.get(i).cancelLoadingWidgetPreview();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = mItems.get(position);
        Intent intent = item.getIntent();
        if (mIsCameraEnabled && (position == 0)) {
            //ImageView cameraWidgetIcon = (ImageView)view.findViewById(R.id.icon);
            int selection = android.provider.Settings.Secure.getInt(getContentResolver(),
                    "show_camera_widget", 0);
            int checked = (selection == 0 ? 1 : 0);
            android.provider.Settings.Secure.putInt(getContentResolver(),
                    "show_camera_widget", checked);

        } else {
            int addedWidgets[] = mLockPatternUtils.getAppWidgets();
            // camera widget is not included in the widget count.
            if (addedWidgets.length >= MAX_WIDGET_ALLOWED_TO_ADD) {
                showToast(R.string.sp_lock_screen_widget_add_max_count_reached);
                return;
            }
        }

        int result;
        if (item.extras != null) {
            // If these extras are present it's because this entry is custom.
            // Don't try to bind it, just pass it back to the app.
            result = RESULT_OK;
            setResultData(result, intent);
        } else {
            try {
                if (mAddingToKeyguard && mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                    // Found in KeyguardHostView.java
                    final int KEYGUARD_HOST_ID = 0x4B455947;
                    int userId = ActivityManager.getCurrentUser();
                    mAppWidgetId = 0; /*AppWidgetHost.allocateAppWidgetIdForPackage(KEYGUARD_HOST_ID,
                            userId, "com.android.keyguard");*/ // Temporary blocked on pre-pdk
                }
                mAppWidgetManager.bindAppWidgetId(
                        mAppWidgetId, intent.getComponent(), mExtraConfigureOptions);
                result = RESULT_OK;
            } catch (IllegalArgumentException e) {
                // This is thrown if they're already bound, or otherwise somehow
                // bogus.  Set the result to canceled, and exit.  The app *should*
                // clean up at this point.  We could pass the error along, but
                // it's not clear that that's useful -- the widget will simply not
                // appear.
                result = RESULT_CANCELED;
            }
            setResultData(result, null);
        }
        if (mAddingToKeyguard) {
            onActivityResult(REQUEST_PICK_APPWIDGET, result, mResultData);
        } else {
            finish();
        }
    }

    protected void onDestroy() {
        if (mAppWidgetAdapter != null) {
            mAppWidgetAdapter.cancelAllWidgetPreviewLoaders();
        }
        mAppThemeWidget = null;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET) {
            int appWidgetId;
            if  (data == null) {
                appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID ;
            } else {
                appWidgetId = data.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            if (requestCode == REQUEST_PICK_APPWIDGET && resultCode == Activity.RESULT_OK) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

                AppWidgetProviderInfo appWidget = null;
                appWidget = appWidgetManager.getAppWidgetInfo(appWidgetId);

                if (appWidget.configure != null) {
                    // Launch over to configure widget, if needed
                    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                    intent.setComponent(appWidget.configure);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                    startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
                } else {
                    // Otherwise just add it
                    onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
                }
            } else if (requestCode == REQUEST_CREATE_APPWIDGET && resultCode == Activity.RESULT_OK) {
                mLockPatternUtils.addAppWidget(appWidgetId, 0);
                finishDelayedAndShowLockScreen(appWidgetId);
            } else {
                if (mAddingToKeyguard &&
                        mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    int userId = ActivityManager.getCurrentUser();
                    //AppWidgetHost.deleteAppWidgetIdForSystem(mAppWidgetId, userId); // Temporary blocked on pre-pdk
                }
                finishDelayedAndShowLockScreen(AppWidgetManager.INVALID_APPWIDGET_ID);
            }
        }
    }

    private void finishDelayedAndShowLockScreen(int appWidgetId) {
        IBinder b = ServiceManager.getService(Context.WINDOW_SERVICE);
        IWindowManager iWm = IWindowManager.Stub.asInterface(b);
        Bundle opts = null;
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            opts = new Bundle();
            opts.putInt(LockPatternUtils.KEYGUARD_SHOW_APPWIDGET, appWidgetId);
        }
        try {
            iWm.lockNow(opts);
        } catch (RemoteException e) {
        }

        // Change background to all black
        ViewGroup root = (ViewGroup) findViewById(R.id.layout_root);
        root.setBackgroundColor(0xFF000000);
        // Hide all children
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            root.getChildAt(i).setVisibility(View.INVISIBLE);
        }
        mGridView.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 500);
    }

    void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Settings does not have the permission to launch " + intent, e);
        }
    }

    private void showToast(int strId) {
        if (null == mToast) {
            mToast = Toast.makeText(this, strId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(strId);
        }
        mToast.show();
    }

}
