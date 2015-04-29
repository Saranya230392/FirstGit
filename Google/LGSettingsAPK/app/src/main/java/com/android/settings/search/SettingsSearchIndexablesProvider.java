/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.search;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.SearchIndexableResource;
import android.provider.SearchIndexablesProvider;

import java.util.Collection;

import static com.lge.provider.LGSearchIndexablesContract.COLUMN_INDEX_XML_RES_RANK;
import static com.lge.provider.LGSearchIndexablesContract.COLUMN_INDEX_XML_RES_RESID;
import static com.lge.provider.LGSearchIndexablesContract.COLUMN_INDEX_XML_RES_CLASS_NAME;
import static com.lge.provider.LGSearchIndexablesContract.COLUMN_INDEX_XML_RES_ICON_RESID;
import static com.lge.provider.LGSearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_ACTION;
import static com.lge.provider.LGSearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE;
import static com.lge.provider.LGSearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS;

import static com.lge.provider.LGSearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS;
import static com.lge.provider.LGSearchIndexablesContract.INDEXABLES_RAW_COLUMNS;
import static com.lge.provider.LGSearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS;

public class SettingsSearchIndexablesProvider extends SearchIndexablesProvider {

    // private static VpnSearchIndexableItemUpdater updater = null; // block code for L MR1

    @Override
    public boolean onCreate() {
        // updater = new VpnSearchIndexableItemUpdater(getContext()); // block code for L MR1
        return true;
    }

    @Override
    public Cursor queryXmlResources(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(INDEXABLES_XML_RES_COLUMNS);
        Collection<SearchIndexableResource> values = SearchIndexableResources.values();
        for (SearchIndexableResource val : values) {
            Object[] ref = new Object[7];
            ref[COLUMN_INDEX_XML_RES_RANK] = val.rank;
            ref[COLUMN_INDEX_XML_RES_RESID] = val.xmlResId;
            ref[COLUMN_INDEX_XML_RES_CLASS_NAME] = val.className;
            ref[COLUMN_INDEX_XML_RES_ICON_RESID] = val.iconResId;
            ref[COLUMN_INDEX_XML_RES_INTENT_ACTION] = null; // intent action
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE] = null; // intent target package
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS] = null; // intent target class
            cursor.addRow(ref);
        }
        return cursor;
    }

    @Override
    public Cursor queryRawData(String[] projection) {
        MatrixCursor result = new MatrixCursor(INDEXABLES_RAW_COLUMNS);
        
        return result;
    }

    @Override
    public Cursor queryNonIndexableKeys(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS);
        return cursor;
    }
}
