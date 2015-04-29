/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.os.Bundle;

/**
 * API for iWnn Morphological analysis.
 */
interface IMorphemeService {
    /**
     * Get the result of Morphological analysis.
     * 
     * @param input     Input string.
     * @param readingsMax   Maximum count of getting readings.
     * @return The result of Morphological analysis.
     */
    Bundle splitWord(String input, int readingsMax);
}

