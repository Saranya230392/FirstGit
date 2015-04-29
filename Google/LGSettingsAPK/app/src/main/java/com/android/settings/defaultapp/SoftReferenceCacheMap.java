
package com.android.settings.defaultapp;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;

/**
 * helper class for soft reference cache. base code is ContactPhotoLoader.java in android contact First, request data load asynchronously and update view using loaded data. User should implements two abstract method - loadDataFromDatabase(), updateView()
 * @author  raejoo.ha
 */

public abstract class SoftReferenceCacheMap<K, V, D> implements Callback {
    private static final String TAG                     = "SoftReferenceCacheMap";
    private static final String LOADER_THREAD_NAME      = "DataLoader";
    private static boolean      LOGD                    = false;

    /**
     * Type of message sent by the UI thread to itself to indicate that some data need to be loaded.
     */
    private static final int    MESSAGE_REQUEST_LOADING = 1;

    /**
     * Type of message sent by the loader thread to indicate that some data have been loaded.
     */
    private static final int    MESSAGE_DATA_LOADED     = 2;

    /**
     * Maintains the state of a particular data.
     */
    private class DataHolder {
        private static final int NEEDED          = 0;
        private static final int LOADING         = 1;
        private static final int LOADED          = 2;
        private static final int NEED_INVALIDATE = 3;
        private static final int INVALIDATING    = 4;

        int                      state;
        SoftReference<D>         dataRef;
    }

    /**
     * A soft cache for data.
     */
    private final ConcurrentHashMap<K, DataHolder> mCache             = new ConcurrentHashMap<K, DataHolder>();

    /**
     * A map from View to the corresponding ID. Please note that this ID may change before the photo loading request is started.
     */
    private final ConcurrentHashMap<V, K>          mPendingRequests   = new ConcurrentHashMap<V, K>();

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler                          mMainThreadHandler = new Handler(this);

    /**
     * Thread responsible for loading data from the database. Created upon the first request.
     * @uml.property  name="mLoaderThread"
     * @uml.associationEnd
     */
    private LoaderThread                           mLoaderThread;

    /**
     * A gate to make sure we only send one instance of MESSAGE_REQUEST_LOADING at a time.
     */
    private boolean                                mLoadingRequested;

    /**
     * Flag indicating if the data loading is paused.
     */
    private boolean                                mPaused = false;

    @SuppressWarnings("unused")
    private final Context                          mContext;

    /**
     * Constructor.
     *
     * @param context
     *            content context
     *
     */

    public SoftReferenceCacheMap(Context context) {
        mContext = context;
    }

    /**
     * Load data from database
     *
     * @param id
     *            id to load data
     * @return loaded data class
     */
    protected abstract D loadDataFromDatabase(K id);

    /**
     * Update view using loaded data.
     *
     * @param view
     * @param data
     */
    protected abstract void updateView(V view, D data);

    /**
     * Request load data, This method should be called in UI thread.
     *
     * @param view
     * @param contactId
     * @return If data is already loaded, return true
     */
    public boolean loadData(V view, K contactId) {
        // [UnitTest_RunningProcessFragmentTest002_manual#testHandleMessage001_FI] (120420)
        if (contactId == null) {
            return false;
        }
        int status = loadCachedData(view, contactId);
        switch (status) {
        case DataHolder.LOADED:
            if (LOGD) {
                Log.d(TAG, "cache hit!");
            }
            mPendingRequests.remove(view);
            return true;
        case DataHolder.NEED_INVALIDATE:
        case DataHolder.INVALIDATING:
            mPendingRequests.put(view, contactId);
            if (!mPaused) {
                // Send a request to start loading data
                requestLoading();
            }
            return true;
        default:
            mPendingRequests.put(view, contactId);
            if (!mPaused) {
                // Send a request to start loading data
                requestLoading();
            }
            return false;
        }
    }

    private int loadCachedData(V view, K contactId) {
        DataHolder holder = mCache.get(contactId);

        if (holder == null) {
            holder = new DataHolder();
            mCache.put(contactId, holder);
        } else if (holder.state == DataHolder.LOADED) {
            if (holder.dataRef == null) {
                updateView(view, null);
                return DataHolder.LOADED;
            }

            D data = holder.dataRef.get();
            if (data != null) {
                updateView(view, data);
                return DataHolder.LOADED;
            }
            if (LOGD) {
                Log.d(TAG, "reference is garbage collected requery data!!");
            }

            holder.dataRef = null;
        } else if (holder.state == DataHolder.INVALIDATING || holder.state == DataHolder.NEED_INVALIDATE) {
            if (holder.dataRef == null) {
                updateView(view, null);
                return DataHolder.NEED_INVALIDATE;
            }

            D data = holder.dataRef.get();
            if (data != null) {
                updateView(view, data);
                return DataHolder.NEED_INVALIDATE;
            }
            if (LOGD) {
                Log.d(TAG, "reference is garbage collected requery data!!");
            }

            holder.dataRef = null;
        }

        holder.state = DataHolder.NEEDED;
        return DataHolder.NEEDED;
    }

    /**
     * Stops loading data, kills the data loader thread and clears all caches.
     */
    public void stop() {
        pause();

        if (mLoaderThread != null) {
            mLoaderThread.quit();
            mLoaderThread = null;
        }

        mPendingRequests.clear();
        mCache.clear();
    }

    public void clear() {
        mPendingRequests.clear();
        mCache.clear();
    }

    public void invalidate() {
        mPendingRequests.clear();
        Iterator<K> keys = mCache.keySet().iterator();
        while (keys.hasNext()) {
            K contactID = keys.next();
            DataHolder holder = mCache.get(contactID);
            if (holder != null) {
                holder.state = DataHolder.NEED_INVALIDATE;
            }
        }
    }

    public void invalidate(long contactID) {
        DataHolder holder = mCache.get(contactID);
        if (holder != null) {
            holder.state = DataHolder.NEED_INVALIDATE;
        }
    }

    /**
     * Temporarily stops loading data from the database.
     */
    public void pause() {
        mPaused = true;
    }

    /**
     * Resumes loading data from the database.
     */
    public void resume() {
        mPaused = false;
        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_REQUEST_LOADING: {
            if (LOGD) {
                Log.d(TAG, "MESSAGE_REQUEST_LOADING");
            }
            mLoadingRequested = false;
            if (!mPaused) {
                if (mLoaderThread == null) {
                    mLoaderThread = new LoaderThread();
                    mLoaderThread.start();
                }

                mLoaderThread.requestLoading();
            }
            return true;
        }

        case MESSAGE_DATA_LOADED: {
            if (!mPaused) {
                if (LOGD) {
                    Log.d(TAG, "MESSAGE_DATA_LOADED");
                }
                processLoadedData();
            }
            return true;
        }
        default:
            break;
        }
        return false;
    }

    /**
     * The thread that performs loading of photos from the database.
     */
    private class LoaderThread extends HandlerThread implements Callback {
        private final ArrayList<K> mContactIds = new ArrayList<K>();
        private Handler            mLoaderThreadHandler;

        public LoaderThread() {
            super(LOADER_THREAD_NAME);
        }

        /**
         * Sends a message to this thread to load requested data.
         */
        public void requestLoading() {
            if (mLoaderThreadHandler == null) {
                Looper looper = getLooper();
                if (looper != null) {
                    mLoaderThreadHandler = new Handler(looper, this);
                }
            }
            if (mLoaderThreadHandler != null) {
                mLoaderThreadHandler.sendEmptyMessage(0);
            }
        }

        /**
         * Receives the above message, loads data and then sends a message to the main thread to process them.
         */
        public boolean handleMessage(Message msg) {
            loadDataFromDatabase();
            mMainThreadHandler.sendEmptyMessage(MESSAGE_DATA_LOADED);
            return true;
        }

        private void loadDataFromDatabase() {
            obtainContactIdsToLoad(mContactIds);
            // Log.d(TAG,"loadSNCDataFromDatabase");
            int count = mContactIds.size();
            if (count == 0) {
                return;
            }

            for (int i = 0; i < count && !mPaused; i++) {
                D ret = SoftReferenceCacheMap.this.loadDataFromDatabase(mContactIds.get(i));
                DataHolder dataHolder = new DataHolder();
                if (ret != null) {
                    dataHolder.dataRef = new SoftReference<D>(ret);
                } else {
                    dataHolder.dataRef = null;
                }
                dataHolder.state = DataHolder.LOADED;
                if (LOGD) {
                    Log.d(TAG, "loaded=" + mContactIds.get(i));
                }
                mCache.put(mContactIds.get(i), dataHolder);
            }
        }
    }

    private void processLoadedData() {
        Iterator<V> iterator = mPendingRequests.keySet().iterator();
        // Log.d("SNC","processLoadedData");
        while (iterator.hasNext()) {
            V view = iterator.next();
            K contactID;
            contactID = mPendingRequests.get(view);

            if (contactID != null) {
                int state;
                state = loadCachedData(view, contactID);
                if (state == DataHolder.LOADED) {
                    if (LOGD) {
                        Log.d(TAG, "processLoadedData Loaded" + contactID);
                    }
                    iterator.remove();
                }
            }
        }

        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    private void obtainContactIdsToLoad(ArrayList<K> contactIds) {
        contactIds.clear();

        Iterator<K> iterator = mPendingRequests.values().iterator();
        while (iterator.hasNext()) {
            K id = iterator.next();
            DataHolder holder = mCache.get(id);
            if (holder != null) {
                if (holder.state == DataHolder.NEEDED) {
                    if (LOGD) {
                        Log.d(TAG, "obtainContactIdsToLoad contactid=" + id);
                    }
                    // Assuming atomic behavior
                    holder.state = DataHolder.LOADING;
                    contactIds.add(id);
                } else if (holder.state == DataHolder.NEED_INVALIDATE) {
                    // Assuming atomic behavior
                    holder.state = DataHolder.INVALIDATING;
                    contactIds.add(id);
                }
            }
        }
    }
}
