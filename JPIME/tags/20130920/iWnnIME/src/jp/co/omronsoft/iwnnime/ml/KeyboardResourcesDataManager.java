/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.Context;
import android.graphics.drawable.Drawable;

/** Manager class for resources data. */
public class KeyboardResourcesDataManager {
    /** The instance of KeyboardResourcesDataManager */
    private static KeyboardResourcesDataManager mManager;

    /**
     * Constructor.
     *
     */
    private KeyboardResourcesDataManager() {
        mManager = null;
    }

    /**
     * Get the instance of KeyboardResourcesDataManager.
     *
     * @return the instance of KeyboardResourcesDataManager,
     */
    synchronized public static KeyboardResourcesDataManager getInstance() {
        if (mManager == null) {
            mManager = new KeyboardResourcesDataManager();
        }
        return mManager;
    }

    /** Getter Drawable */
    /**
     * Get Drawable in the keyboard skin. if keyboard skin invalid, get language pack Drawable, if language pack invalid, get original Drawable.
     *
     * @param context     context
     * @param resourceId  The ID in the IME Resource ID
     * @param key         The key in the language pack
     * @return Drawable of ID. (keyboard skin > language pack > original)
     */
    public Drawable getDrawable(Context context, int resourceId, String langpackKey) {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable result = keyskin.getDrawable(resourceId);

        if (result == null) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getDrawable(langpackKey);
            if (result == null && context != null) {
                // not language pack data
                result = context.getResources().getDrawable(resourceId);
            }
        }
        return result;
    }

    /**
     * Get Drawable in the keyboard skin. if keyboard skin invalid, get language pack Drawable.
     *
     * @param  skinKey     The key in the skin data
     * @param  langpackKey The key in the language pack
     * @return Drawable of ID. (keyboard skin > language pack)
     */
    public Drawable getDrawable(String skinKey, String langpackKey) {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable result = keyskin.getDrawable(skinKey);

        if (result == null) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getDrawable(langpackKey);
        }
        return result;
    }

    /**
     * Get Drawable in the keyboard skin. if keyboard skin invalid, get language pack Drawable.
     *
     * @param  key      The key in the apkfile. (skin data key and language pack key are the same.)
     * @return Drawable of ID. (keyboard skin > language pack)
     */
    public Drawable getDrawable(String key) {
        return getDrawable(key, key);
    }


    /** Getter Color */
    /**
     * Get Color in the keyboard skin. if keyboard skin invalid, get language pack color, if language pack invalid, get original color.
     *
     * @param context     context
     * @param resourceId  The ID in the IME Resource ID
     * @param langpackKey The key in the language pack
     * @return Color Code of apkfile. (keyboard skin > language pack > original)
     */
    public int getColor(Context context, int resourceId, String langpackKey) {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        int result = keyskin.getColor(resourceId);

        if (result == 0) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getColor(langpackKey);
            if (result == 0 && context != null) {
                // not language pack data
                result = context.getResources().getColor(resourceId);
            }
        }
        return result;
    }

    /**
     * Get Color in the keyboard skin. if keyboard skin invalid, get language pack color.
     *
     * @param  key   The key in the apkfile.
     * @return Color Code of apkfile. (keyboard skin > language pack > original)
     */
    public int getColor(String key) {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        int result = keyskin.getColor(key);

        if (result == 0) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getColor(key);
        }
        return result;
    }


    /** Getter Float */
    /**
     * Get float in the keyboard skin. if keyboard skin invalid, get language pack float.
     *
     * @param  key   The key in the apkfile.
     * @return float of apkfile. if undefine, return -1.
     */
    public float getFloat(String key) {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        float result = keyskin.getFloat(key);

        if (result == -1f) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getFloat(key);
        }
        return result;
    }


    /** Getter Wrapper API */
    /** Move here customize, what had been defined on KeyboardSkinData. */
    /**
     * Get Drawable Key BackGround.
     *
     * @return Drawable of Key BackGround. (keyboard skin > language pack)
     */
    public Drawable getKeyBg() {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable result = keyskin.getKeyBg();

        if (result == null) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getKeyBg();
        }
        return result;
    }

    /**
     * Get Drawable Key BackGround2nd.
     *
     * @return Drawable of Key BackGround2nd. (keyboard skin > language pack)
     */
    public Drawable getKeyBg2nd() {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable result = keyskin.getKeyBg2nd();

        if (result == null) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getKeyBg2nd();
        }
        return result;
    }

    /**
     * Get Drawable Tab.
     *
     * @return Drawable of Tab. (keyboard skin > language pack)
     */
    public Drawable getTab() {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable result = keyskin.getTab();

        if (result == null) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getTab();
        }
        return result;
    }

    /**
     * Get Drawable No Select Tab.
     *
     * @return Drawable of No Select Tab. (keyboard skin > language pack)
     */
    public Drawable getTabNoSelect() {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable result = keyskin.getTabNoSelect();

        if (result == null) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getTabNoSelect();
        }
        return result;
    }

    /**
     * Get drawable candidate background.
     *
     * @param normal key of normal candidate.
     * @param press key of press candidate.
     * @param focus key of focus candidate.
     * @return Drawable of candidate background. (keyboard skin > language pack)
     */
    private Drawable getCandidateBackgroundDrawable(String normal, String press, String focus) {
        // get in the skin data
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable result = keyskin.getCandidateBackgroundDrawable(normal, press, focus);

        if (result == null) {
            // not skin data
            KeyboardLanguagePackData langpack = KeyboardLanguagePackData.getInstance();
            result = langpack.getCandidateBackgroundDrawable(normal, press, focus);
        }
        return result;
    }


    /** Wrapper API */
    /** Move here as it is, what had been defined on KeyboardSkinData. */
    /**
     * Get Drawable Keyboard BackGround.
     *
     * @return Drawable of Keyboard BackGround.
     */
    public Drawable getKeyboardBg() {
        return getDrawable("Keyboardbackground");
    }

    /**
     * Get Drawable Keyboard BackGround 1Line.
     *
     * @return Drawable of Keyboard BackGround 1Line.
     */
    public Drawable getKeyboardBg1Line() {
        return getDrawable("Keyboardbackground1Line");
    }

    /**
     * Get Drawable Key Preview.
     *
     * @return Drawable of Key Preview.
     */
    public Drawable getKeyPreview() {
        return getDrawable("KeyPreviewBackground");
    }

    /**
     * Get Drawable Candidate Back.
     *
     * @return Drawable of Candidate Back.
     */
    public Drawable getCandidateBack() {
        return getDrawable("CandBack");
    }

    /**
     * Get KeyTextColor in the apkfile.
     *
     * @return KeyTextColor Code of apkfile.
     */
    public int getKeyTextColor() {
        return getColor("KeyColor");
    }

    /**
     * Get KeyTextColor2nd in the apkfile.
     *
     * @return KeyTextColor2nd Code of apkfile.
     */
    public int getKeyTextColor2nd() {
        return getColor("KeyColor2nd");
    }

    /**
     * Get KeyPreviewColor in the apkfile.
     *
     * @return KeyPreviewColor Code of apkfile.
     */
    public int getKeyPreviewColor() {
        return getColor("KeyPreviewColor");
    }

    /**
     * Get ShadowRadius in the apkfile.
     *
     * @return ShadowRadius of apkfile.
     */
    public float getShadowRadius() {
        return getFloat("ShadowRadius");
    }

    /**
     * Get ShadowRadius2nd in the apkfile.
     *
     * @return ShadowRadius2nd of apkfile.
     */
    public float getShadowRadius2nd() {
        return getFloat("ShadowRadius2nd");
    }

    /**
     * Get ShadowColor in the apkfile.
     *
     * @return ShadowColor Code of apkfile.
     */
    public int getShadowColor() {
        return getColor("ShadowColor");
    }

    /**
     * Get ShadowColor2nd in the apkfile.
     *
     * @return ShadowColor2nd Code of apkfile.
     */
    public int getShadowColor2nd() {
        return getColor("ShadowColor2nd");
    }

    /**
     * Get drawable candidate background.
     *
     * @return Drawable of candidate background.
     */
    public Drawable getCandidateBackground() {
        return getCandidateBackgroundDrawable("CandidateBackgroundNormal",
                "CandidateBackgroundPressed", "CandidateBackgroundFocused");
    }

    /**
     * Get drawable candidate background for symbol keyboard view.
     *
     * @return Drawable of candidate background for symbol keyboard view.
     */
    public Drawable getCandidateBackgroundSymbol() {
        return getCandidateBackgroundDrawable("CandidateBackgroundSymbolNormal",
                "CandidateBackgroundSymbolPressed", "CandidateBackgroundSymbolFocused");
    }

    /**
     * Get drawable JoJo candidate background.
     *
     * @return Drawable of JoJo candidate background.
     */
    public Drawable getCandidateBackgroundJoJo() {
        return getCandidateBackgroundDrawable("CandidateBackgroundNormalJoJo",
                "CandidateBackgroundPressedJoJo", "CandidateBackgroundFocusedJoJo");
    }

    /**
     * Get drawable candidate focus background.
     *
     * @return Drawable of candidate focus background.
     */
    public Drawable getCandidateFocusBackground() {
        return getCandidateBackgroundDrawable("CandidateBackgroundFocused",
                "CandidateBackgroundPressed", null);
    }

    /**
     * Get drawable candidate background for one line view.
     *
     * @return Drawable of candidate background for one line view.
     */
    public Drawable getCandidateBackgroundOneLine() {
        return getCandidateBackgroundDrawable("CandidateBackgroundNormalOneLine",
                "CandidateBackgroundPressedOneLine", "CandidateBackgroundFocusedOneLine");
    }

    /**
     * Get drawable candidate focus background for one line view.
     *
     * @return Drawable of candidate background for one line view.
     */
    public Drawable getCandidateFocusBackgroundOneLine() {
        return getCandidateBackgroundDrawable("CandidateBackgroundFocusedOneLine",
                "CandidateBackgroundPressedOneLine", null);
    }

    /**
     * Get drawable candidate background for WebApi.
     *
     * @return Drawable of candidate background for WebApi.
     */
    public Drawable getCandidateBackgroundWebApi() {
        return getCandidateBackgroundDrawable("CandidateBackgroundNormalWebApi",
                "CandidateBackgroundPressedWebApi", "CandidateBackgroundFocusedWebApi");
    }

    /**
     * Get drawable candidate focus background for WebApi.
     *
     * @return Drawable of candidate background for WebApi.
     */
    public Drawable getCandidateFocusBackgroundWebApi() {
        return getCandidateBackgroundDrawable("CandidateBackgroundFocusedWebApi",
                "CandidateBackgroundPressedWebApi", null);
    }
}