/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.nfc;

import android.view.View;

public class NfcStateListener {
    static final int NONE_STATE = 0x00;
    static final int SYSTEM_STATE = 0x01;
    static final int RW_STATE = 0x02;
    static final int P2P_STATE = 0x04;

    public void resume() { }

    public void pause() { }

    // 20131015_rebestm_add_cardState 
    public void handleNfcStateChanged(int newState, int cardState) { }
    
    public void handleRwStateChanged(int newState) { }

    public void handleP2pStateChanged(int newState) { }

    public interface NfcSwitchListener {
        void onSwitchChange(View view);
    }
}

