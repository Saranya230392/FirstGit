/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml.decoemoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiAttrInfo;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiContract;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiConstant;
import jp.co.omronsoft.iwnnime.ml.DecoEmojiListener;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

/** Queue class for DecoEmoji operation. */
public class DecoEmojiOperationQueue {
    /** for DEBUG */
    private final static String TAG = "DecoEmojiOperationQueue";

    /** DEBUG flag */
    private static boolean DEBUG = false;

    /** Max number of DecoEmoji operation. */
    private static final int MAX_OPERATION = 20000;

    /** DecoEmoji escape code */
    public static final char ESC_CODE = 0x1b;

    /** DecoEmoji attrinfo loop cnt */
    private static final int ATTRINFO_LOOP_CNT = 10;

    /** DecoEmoji YOMI max length */
    private static final int MAX_YOMI_LEN = 24;

    /** DecoEmoji YOMI min length */
    private static final int MIN_YOMI_LEN = 1;

    /** DecoEmoji HINSH is none */
    private static final byte PART_NONE = 0;

    /** DecoEmoji HINSH is category1 */
    private static final byte PART_CATEGORY_1 = 1;

    /** DecoEmoji id format */
    public static final String DECO_ID_FORMAT = "%04d";

    /** The instance of DecoEmojiOperationQueue. */
    private static DecoEmojiOperationQueue sInstance = new DecoEmojiOperationQueue();

    /** The queue of operation. */
    private LinkedList<DecoEmojiOperation> mOperationQueue = new LinkedList<DecoEmojiOperation>();

    /** Engine Component of Service */
    private static String mComponentName = "";

    /**
     * Constructor.
     */
    private DecoEmojiOperationQueue() {
    }

    /**
     * Gets the instance of DecoEmojiOperationQueue.
     *
     * @return The instance of DecoEmojiOperationQueue.
     */
    synchronized public static DecoEmojiOperationQueue getInstance() {
        return sInstance;
    }

    /**
     * Enqueues the DecoEmoji operation.
     */
    synchronized public void enqueueOperation(DecoEmojiAttrInfo[] decoemojiattrinfo, int type, Context context) {
        iWnnEngine engine = null;
        DecoEmojiOperation operation = null;
        if (decoemojiattrinfo == null) {
            operation = new DecoEmojiOperation(null, type);
            updateOperationEventCache(operation, context);
            mOperationQueue.add(operation);

            engine = iWnnEngine.getEngine();
            if (engine != null) {
                engine.enqueueOperation(operation, context);
            }
            for (int index = 0; index < iWnnEngine.getNumEngineForService(); index++) {
                engine = iWnnEngine.getEngineForService(index);
                if (engine != null) {
                    engine.enqueueOperation(operation, context);
                }
            }
        } else {
            int limit = Math.min(MAX_OPERATION - mOperationQueue.size(), decoemojiattrinfo.length);
            for (int i = 0; i < limit; i++) {
                operation = new DecoEmojiOperation(decoemojiattrinfo[i], type);
                updateOperationEventCache(operation, context);
                mOperationQueue.add(operation);

                engine = iWnnEngine.getEngine();
                if (engine != null) {
                    engine.enqueueOperation(operation, context);
                }
                for (int index = 0; index < iWnnEngine.getNumEngineForService(); index++) {
                    engine = iWnnEngine.getEngineForService(index);
                    if (engine != null) {
                        engine.enqueueOperation(operation, context);
                    }
                }
            }
        }
    }

    /**
     * Dequeues the DecoEmoji operation.
     *
     * @param  componentName    : Service ComponentName.
     * @param  context          : iWnn's Context.
     * @return @return {@code true} if operation is executed.
     */
    synchronized public boolean executeOperation(String componentName, Context context) {
        mComponentName = componentName;
        iWnnEngine engine = getEngine();
        if (engine == null) {
            return false;
        }
        if (engine.checkDecoemojiDicset() < 0) {
            return false;
        }
        DecoEmojiOperation operation = mOperationQueue.poll();
        if (operation != null) {
            // Update the preference value with the maximum serial which processed the dictionary information.
            DecoEmojiAttrInfo[] queueInfo = operation.getDecoEmojiAttrInfo();
            int length = queueInfo.length;
            if (length > 0) {
                if (queueInfo[length-1] != null) {
                    SharedPreferences pref;
                    OpenWnn currentIme = OpenWnn.getCurrentIme();
                    if (currentIme != null) {
                        pref = currentIme.getSharedPreferences(OpenWnn.FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                    } else if (context != null) {
                        pref = context.getSharedPreferences(OpenWnn.FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                    } else {
                        return false;
                    }
                    if (pref != null) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt(DecoEmojiListener.PREF_KEY, queueInfo[length-1].getId());
                        editor.commit();
                    }
                }
            }
            switchOperate(queueInfo, operation.getType());
            return true;
        }
        return false;
    }

    /**
     * Removes all of the DecoEmoji operations.
     */
    synchronized public void clearOperation() {
        mOperationQueue.clear();
    }

    /**
     * switchOperate
     *
     * @param  decoemojiattrinfo     : iWnn's dictionary information.
     * @param  type                  : Type of dictionary processing.
     * @return none
     */
    private static void switchOperate(DecoEmojiAttrInfo[] decoemojiattrinfo, int type) {
        if (DEBUG) {Log.d(TAG, "switchOperate() Start");}
        switch (type) {
        // 1) Case of insert
        case IDecoEmojiConstant.FLAG_INSERT:
            if (DEBUG) {Log.d(TAG, "switchOperate()::insertDictionary Start");}
            insertDictionary(decoemojiattrinfo);
            if (DEBUG) {Log.d(TAG, "switchOperate()::insertDictionary End");}
            break;
        // 2) Case of deletion
        case IDecoEmojiConstant.FLAG_DELETE:
            if (DEBUG) {Log.d(TAG, "switchOperate()::deleteDictionary Start");}
            deleteDictionary(decoemojiattrinfo);
            if (DEBUG) {Log.d(TAG, "switchOperate()::deleteDictionary End");}
            break;
        // 3) Case of initialization
        case IDecoEmojiConstant.FLAG_SET_TO_INITIALIZING:
            if (DEBUG) {Log.d(TAG, "switchOperate()::resetDictionary Start");}
            resetDictionary();
            if (DEBUG) {Log.d(TAG, "switchOperate()::resetDictionary End");}
            break;
        // Do not process other than the above.
        default:
            break;
        }
        if (DEBUG) {Log.d(TAG, "switchOperate() End");}
    }

    /**
     * insertDictionary
     *
     * @param  decoemojiattrinfo    : iWnn's dictionary information for addition.
     * @return none
     */
    private static void insertDictionary(DecoEmojiAttrInfo[] decoemojiattrinfo) {
        if (DEBUG) {Log.d(TAG, "insertDictionary() Start");}

        if (checkUpdate()) {
            /** Add the received included Emoji information to the dictionary. */
            controlDecoEmojiDictionary(decoemojiattrinfo, IDecoEmojiConstant.FLAG_INSERT);
        }
        if (DEBUG) {Log.d(TAG, "insertDictionary() End");}
    }

    /**
     * deleteDictionary
     *
     * @param  decoemojiattrinfo    : iWnn's dictionary information for deletion.
     * @return none
     */
    private static void deleteDictionary(DecoEmojiAttrInfo[] decoemojiattrinfo) {
        if (DEBUG) {Log.d(TAG, "deleteDictionary() Start");}

        if (checkUpdate()) {
            /** Remove the received included Emoji information from the dictionary. */
            controlDecoEmojiDictionary(decoemojiattrinfo, IDecoEmojiConstant.FLAG_DELETE);
        }
        if (DEBUG) {Log.d(TAG, "deleteDictionary() End");}
    }

    /**
     * resetDictionary
     *
     * @return none
     */
    private static void resetDictionary() {
        if (DEBUG) {Log.d(TAG, "resetDictionary() Start");}

        if (checkUpdate()) {
            /** Reset the dictionary. */
            resetDecoEmojiDictionary();
        }
        if (DEBUG) {Log.d(TAG, "resetDictionary() End");}
    }

    /**
     * resetDecoEmojiDictionary
     *
     * @return none
     */
    private static int resetDecoEmojiDictionary() {
        if (DEBUG) {Log.d(TAG, "resetDecoEmojiDictionary() Start");}

        iWnnEngine engine = getEngine();
        if (engine == null) {
            return -1;
        }

        int ret = engine.resetDecoEmojiDictionary();

        if (DEBUG) {Log.d(TAG, "resetDecoEmojiDictionary() End");}

        return ret;
    }

    /**
     * controlDecoEmojiDictionary
     *
     * @param  decoemojiattrinfo    : iWnn's dictionary information.
     * @param  control_flag         : Add / Remove.
     * @return none
     */
    private static void controlDecoEmojiDictionary(DecoEmojiAttrInfo[] decoemojiattrinfo, int control_flag) {
        if (DEBUG) {Log.d(TAG, "controlDecoEmojiDictionary() Start");}

        iWnnEngine engine = getEngine();
        if (engine == null) {
            return;
        }

        String id = null;

        for (int i = 0; i < decoemojiattrinfo.length; i++) {
            id = ESC_CODE + String.format(DECO_ID_FORMAT, decoemojiattrinfo[i].getId());
            int emojiType = 0;
            try {
                byte[] charArray = id.getBytes(OpenWnn.CHARSET_NAME_UTF8);
                emojiType = DecoEmojiUtil.getEmojiType(DecoEmojiUtil.convertToEmojiIdInt(charArray, 0));
            } catch (Exception e) {
                Log.e(TAG, "controlDecoEmojiDictionary() Exception", e);
            }
            /** Registered only 20*20 or squrae. */
            if ((emojiType == DecoEmojiContract.KIND_SQ20) || (emojiType == DecoEmojiContract.KIND_SQ)) {
                for (int j = 0; j < ATTRINFO_LOOP_CNT; j++) {

                    if ( !isDecoEmojiAttrInfo(decoemojiattrinfo[i].getName(j), control_flag)) {

                        /** If the part of speech is not specified for registration process, substitute noun. */
                        byte part = repreceDecoEmojiAttrInfo(decoemojiattrinfo[i].getPart(j));

                        if (DEBUG) {Log.d(TAG, "id = " + id + " decoemojiattrinfo[i].getName(j) = "
                                + decoemojiattrinfo[i].getName(j) + " decoemojiattrinfo[i].getPart(j) = "
                                + part + " control_flag = " + control_flag);}
                        engine.controlDecoEmojiDictionary(
                                id, decoemojiattrinfo[i].getName(j), part, control_flag);
                    }
                }
            }
        }
        if (DEBUG) {Log.d(TAG, "controlDecoEmojiDictionary() End");}
    }

    /**
     * isDecoEmojiAttrInfo
     *
     * @param  name                 : Reading.
     * @param  control_flag         : Flag of updating the dictionary.
     * @return                      : true/Registration or deletion of the data is not permitted.
     *                                false/Registration or deletion of the data  will be done.
     */
    private static boolean isDecoEmojiAttrInfo(String name, int control_flag) {
        if (DEBUG) {Log.d(TAG, "isDecoEmojiAttrInfo() Start");}
        boolean ret = false;

        if (control_flag == IDecoEmojiConstant.FLAG_INSERT) {
            /**
             * Dictionary information which meets any of the following conditions is excluded.
             * 1.Reading is set to NULL.
             * 2.The number of characters of reading is not between 1 and 24.
             */
            if (name == null || (name.length() > MAX_YOMI_LEN || name.length() < MIN_YOMI_LEN)) {
                ret = true;
            }
        } else if (control_flag == IDecoEmojiConstant.FLAG_DELETE) {
            /**
             * Dictionary information which meets all of the following conditions is excluded.
             * 1.Reading isn't set to NULL.
             * 2.The number of characters of reading is not between 1 and 24.
             */
            if (name != null) {
                if (name.length() > MAX_YOMI_LEN || name.length() < MIN_YOMI_LEN) {
                    ret = true;
                }
            }
        }
        if (DEBUG) {Log.d(TAG, "isDecoEmojiAttrInfo() End");}
        return ret;
    }

    /**
     * repreceDecoEmojiAttrInfo
     *
     * @param  part                : Part of speech.
     * @return                     : Part of speech.
     */
    private static byte repreceDecoEmojiAttrInfo(byte part) {
        if (DEBUG) {Log.d(TAG, "repreceDecoEmojiAttrInfo() Start");}

        if (part == PART_NONE) {
            if (DEBUG) {Log.d(TAG, "repreceDecoEmojiAttrInfo() 0 to 1");}
            part = PART_CATEGORY_1;
        }
        if (DEBUG) {Log.d(TAG, "repreceDecoEmojiAttrInfo() End");}
        return part;
    }

    /**
     * checkUpdate
     *
     * @return result     : true/Update of the dictionary is OK.
     *                      false/Update of the dictionary is NG.
     */
    private static boolean checkUpdate() {
        if (DEBUG) {Log.d(TAG, "checkUpdate() Start");}

        boolean result = false;

        // Check the engine.
        iWnnEngine engine = getEngine();
        if (engine == null) {
            return false;
        }
        int ret = engine.checkDecoEmojiDictionary();

        // Check the DecoEmoji dictionary.
        if (ret >= 0) {
            result = true;
        } else {
            result = false;
        }

        if (DEBUG) {Log.d(TAG, "checkUpdate() End");}
        return result;
    }

    /**
     * Return an instance of {@code IWnnCore}.
     *
     * @return A concrete {@code IWnnCore} instance.
     */
    private static iWnnEngine getEngine() {
        if (OpenWnn.getCurrentIme() == null) {
            return iWnnEngine.getEngineForService(mComponentName);
        } else {
            return iWnnEngine.getEngine();
        }
    }

    /**
     * Update decoemoji operation of cache file.
     *
     * @param  operation    : Cache the instance of DecoEmoji operation.
     * @param  context      : Context.
     * @return none
     */
    private void updateOperationEventCache(DecoEmojiOperation operation, Context context) {
        if (operation == null || context == null) {
            Log.e(TAG, "updateOperationEventCache() parameter error");
            return;
        }

        StringBuffer writeStrBuf = new StringBuffer();
        SharedPreferences pref = context.getSharedPreferences(iWnnEngine.FILENAME_DECO_OPERATION_PROCESSED_INDEX_CACHE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int type = operation.getType();
        switch(type) {
        // 1) Case of deletion
        case IDecoEmojiConstant.FLAG_DELETE:
            writeStrBuf.append(String.valueOf(type));
            writeStrBuf.append(iWnnEngine.DECO_OPERATION_SEPARATOR);
            DecoEmojiAttrInfo[] attr = operation.getDecoEmojiAttrInfo();
            if ((attr.length > 0) && (attr[0] != null)) {
                writeStrBuf.append(attr[0].getId());
            } else {
                // Invalid data (though it is removed, there is no ID deleted)
                writeStrBuf.append(String.valueOf(iWnnEngine.OPERATION_ID_INIT));  // dummy data
            }
            break;
        // 2) Case of initialization
        case IDecoEmojiConstant.FLAG_SET_TO_INITIALIZING:
            writeStrBuf.append(String.valueOf(type));
            writeStrBuf.append(iWnnEngine.DECO_OPERATION_SEPARATOR);
            writeStrBuf.append(String.valueOf(iWnnEngine.OPERATION_ID_INIT));  // dummy data
            // event file delete
            context.deleteFile(iWnnEngine.FILENAME_DECO_OPERATION_EVENT_CACHE);
            // preferences data all clear
            editor.clear();
            editor.commit();
            break;
        default:
            // non cache event
            return;
        }

        PrintWriter writer = null;
        try{
            OutputStream out = context.openFileOutput(iWnnEngine.FILENAME_DECO_OPERATION_EVENT_CACHE, Context.MODE_PRIVATE|Context.MODE_APPEND);
            writer = new PrintWriter(new OutputStreamWriter(out, OpenWnn.CHARSET_NAME_UTF8));
            writer.println(writeStrBuf.toString());
            // update event count
            long count = pref.getLong(iWnnEngine.KEYNAME_DECO_OPERATION_EVENT_COUNT, 0);
            count++;
            editor.putLong(iWnnEngine.KEYNAME_DECO_OPERATION_EVENT_COUNT, count);
            editor.commit();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "updateOperationEventCache() Exception1["+e+"]");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "updateOperationEventCache() Exception2["+e+"]");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
