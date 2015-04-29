/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
    private static SoundManager sManager;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> map;
    private Context context;

    private SoundManager(){}
    public static SoundManager getInstance(){
        if (sManager==null) {
        sManager = new SoundManager();
        }
        return sManager;
    }

    public void init(Context context){
        if (soundPool != null) {
            release();
        }
        this.context=context;
        soundPool=new SoundPool(5, AudioManager.STREAM_SYSTEM, 0);
        map = new HashMap<Integer, Integer>();
    }

    public void addSound(int index, int resId){
        if (soundPool != null) {
            int id = soundPool.load(context, resId, 1);
            map.put(index, id);
        }
    }

    public void play(int index){
        if (soundPool != null) {
            soundPool.play(map.get(index), 1, 1, 1, 0, 1);
        }
    }

    public void stopSound(int index){
        if (soundPool != null) {
            soundPool.stop(map.get(index));
        }
    }

    public void release(){
        if (soundPool != null && map != null) {
            int num = map.size();
            for (int i = 0; i < num; i++) {
                if (map.get(i) != null) {
                    soundPool.stop(map.get(i));
                    soundPool.unload(map.get(i));
                }
            }
            soundPool.release();
            soundPool = null;
        }
    }

    /** Finalize. */
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }
}