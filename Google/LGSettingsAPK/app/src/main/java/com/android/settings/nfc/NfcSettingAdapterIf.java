/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.nfc;

public interface NfcSettingAdapterIf {

    public void resume();
    public void pause();
    public void destroy();
    public void onConfigChange();
    public boolean processPreferenceEvent(String EventValue);
	
}
