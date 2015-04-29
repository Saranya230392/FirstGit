/*
 * Copyright (C) 2013 LG Electronics Accessibility Project
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

package com.android.settings.accessibility;

import java.util.ArrayList;

/**
 * SettingsAccessibility Utility Class.
 * 
 * 
 * @version 2013/04/15 Initial Creation.<br>
 *          2013/05/07 Add ignoring code for specific Accessibility service. (ex cello)<br>
 * 
 * @author resumet.kwon@lge.com (2013.04.15)
 */

public final class SettingsAccessibilityUtility {
    public final static String EASY_SETTING_GENERAL_TAB_NAME = "general";

    private final static ArrayList<IgnoreAccessibilityService> IGNORE_ACCESSIBILITY_SERVICES_LIST = new ArrayList<IgnoreAccessibilityService>() {
        private static final long serialVersionUID = 8826220227495500025L;

        {
            add(new IgnoreAccessibilityService("com.lge.cello",
                    "com.lge.cello.engine.CelloAccessibilityService"));
            add(new IgnoreAccessibilityService("com.android.settings",
                    "com.android.settings.accessibility.flashalerts.FlashAlertsService"));
            add(new IgnoreAccessibilityService("com.android.settings",
                    "com.android.settings.accessibility.assistivetouchboardservice.AssistiveTouchBoardService"));
            add(new IgnoreAccessibilityService("com.android.settings",
                    "com.android.settings.accessibility.turnoffallsounds.TurnOffAllSoundsService"));
        }
    };

    public static boolean isIgnoreService(String packageName, String serviceName) {
        for (IgnoreAccessibilityService service : IGNORE_ACCESSIBILITY_SERVICES_LIST) {
            if (service.isSameWith(packageName, serviceName)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * Ignore Accessibility Service Class.
 * 
 * @version 2013/05/07 Initial Creation.<br>
 * 
 * @author resumet.kwon
 * 
 */

class IgnoreAccessibilityService {
    private String packageName;
    private String serviceName;

    public IgnoreAccessibilityService(String p, String s) {
        packageName = p;
        serviceName = s;
    }

    public boolean isSameWith(String p, String s) {
        if (p == null && s == null) {
            return false;
        }

        if (packageName.equals(p) && serviceName.equals(s)) {
            return true;
        }
        return false;
    }
}
