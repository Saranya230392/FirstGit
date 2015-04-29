/*
***************************************************************************
** FontDeleteActivity.java
***************************************************************************
**
** 2013.10.22 Android KK
**
** Mobile Communication R&D Center, Hanyang
** Sangmin, Lee (TMSword) ( Mobile Communication R&D Center / Senior Researcher )
**
** This code is a program that changes the font.
**
***************************************************************************
*/
// CAPP_FONTS_HYFONTS
package com.android.settings;

import android.app.Activity;
import android.os.Bundle;

public class FontDeleteActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.font_delete);
        FontDelete fragment = new FontDelete();
        if (findViewById(R.id.font_delete_fragment) != null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.font_delete_fragment, fragment)
                    .commit();
        }

    }
}
// CAPP_FONTS_HYFONTS_END