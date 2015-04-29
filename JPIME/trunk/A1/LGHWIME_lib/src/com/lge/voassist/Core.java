package com.lge.voassist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import android.content.Context;
import android.util.Log;

import com.lge.handwritingime.HandwritingKeyboard;
import com.visionobjects.im.Engine;
import com.visionobjects.im.EventListener;
import com.visionobjects.im.IStroke;
import com.visionobjects.im.LanguageManager;
import com.visionobjects.im.NativeException;
import com.visionobjects.im.Recognizer;
import com.visionobjects.im.Result;

public class Core {
    private final static String TAG = "voassist.Core";
    private final static boolean DEBUG = HandwritingKeyboard.DEBUG;

    private Engine mEngine;
    private LanguageManager mLanguageManager;
    private Recognizer mRecognizer;

//    public enum RECOGNITION_STATE {
//        IDLE, SUBMIT, CANCEL, CANCEL_AND_SUBMIT
//    }

//    private RECOGNITION_STATE mRecognitionState;

    public Core(Context context) throws IOException {
        initialize(context);
    }

    private void initialize(Context context) throws NativeException, IOException {
        Utils.unsplitResources(context);
        File confDir = new File(context.getDir("data", Context.MODE_PRIVATE).getCanonicalFile() + "/conf");
        if (DEBUG)
            Log.d(TAG, "confDir=" + confDir.toString());

        File enginePropertyFile = new File(confDir, "Engine.properties");
        if (!enginePropertyFile.canRead()) {
            throw new FileNotFoundException(enginePropertyFile.getPath());
        }
        File languageManagerPropertyFile = new File(confDir, "LanguageManager.properties");
        if (!languageManagerPropertyFile.canRead()) {
            throw new FileNotFoundException(languageManagerPropertyFile.getPath());
        }
        File recognizerPropertyFile = new File(confDir, "Recognizer.properties");
        if (!recognizerPropertyFile.canRead()) {
            recognizerPropertyFile = null;
        }

//        mRecognitionState = RECOGNITION_STATE.IDLE;
        if (DEBUG)
            Log.d(TAG, "creating Objects...");
        mEngine = Engine.create(MyCertificate.getBytes(), enginePropertyFile, new Properties());
        mLanguageManager = mEngine.createLanguageManager(languageManagerPropertyFile);
        mRecognizer = mEngine.createRecognizer(mLanguageManager, recognizerPropertyFile);

        if (DEBUG)
            Log.d(TAG, "initialize done.");
    }

//    public synchronized void setRecognitionState(RECOGNITION_STATE state) {
//        mRecognitionState = state;
//    }
//
//    public RECOGNITION_STATE getRecognitionState() {
//        return mRecognitionState;
//    }

    public void setRecognizerListener(EventListener l) {
        mRecognizer.setEventListener(l);

    }

    public Result getResult(boolean arg0, boolean arg1) {
        return mRecognizer.getResult(arg0, arg1);
    }

    public void setMode(String languageName, String modeName) {
        mRecognizer.setMode(languageName, modeName);
    }

    public void addStroke(IStroke stroke) {
        mRecognizer.addStroke(stroke);
    }

    public void setPositionAndScaleIndicator(float baselinePosition, float xHeight, float lineSpacing) {
        mRecognizer.setPositionAndScaleIndicator(baselinePosition, xHeight, lineSpacing);
    }

    public void flowSync(int intValue) {
        mRecognizer.flowSync(intValue);
    }

    public void commit() {
        mRecognizer.commit();
    }

    public void cancel() {
        mRecognizer.cancel();
    }

    public void destroy() {
        mRecognizer.destroy();
        mLanguageManager.destroy();
        mEngine.destroy();
        if (DEBUG)
            Log.d(TAG, "Core detroyed.");
        try {
            finalize();
        } catch (Throwable e) {
            // not so important.
            if (DEBUG)
                e.printStackTrace();
        }
    }
}
