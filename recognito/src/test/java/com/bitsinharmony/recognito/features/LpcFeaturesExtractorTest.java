/*
 * (C) Copyright 2014 Amaury Crickx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.bitsinharmony.recognito.features;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import com.bitsinharmony.recognito.algorithms.LinearPredictiveCoding;
import com.bitsinharmony.recognito.algorithms.windowing.WindowFunction;

public class LpcFeaturesExtractorTest {
    
    private static final int POLES = 20;
    private static final float DEFAULT_SAMPLE_RATE = 22050;

    private static final float[] SAMPLE_RATES = new float[] {
        8000, 10000, 16000, 22050, 44100, 48000, 88200, 96000, 176400, 192000
    };
    // window sizes corresponding to the above sample rates (closest to 24 ms AND power of 2)
    private static final int[] WINDOW_LENGTHS = new int[] {
        256, 256, 512, 512, 1024, 1024, 2048, 2048, 4096, 4096
    };
    
    private LpcFeaturesExtractor lpc;
    
    @Test
    public void lpcExtractorUsesWindowsOfCorrectLengthForMostUsedSampleRates() {
        final int[] i = new int[1];
        new MockUp<WindowFunction>() {
            @Mock void applyFunction(double[] window) {
                assertThat(window.length, is(equalTo(WINDOW_LENGTHS[i[0]])));
            }
        };
        while(i[0] < SAMPLE_RATES.length) {
            lpc = new LpcFeaturesExtractor(SAMPLE_RATES[i[0]], POLES);
            lpc.extractFeatures(new double[1024]);
            i[0]++;
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void lpcExtractorRejectsSampleRatesLowerThan8000() {
        new LpcFeaturesExtractor(8000.0f - Math.ulp(8000.0f), POLES);
    }
    
    @Test
    public void lpcFeaturesAreAveragesOnTheLpcCoefficientsReturnedByLinearPredictiveCodingAlgorithm() {
        lpc = new LpcFeaturesExtractor(DEFAULT_SAMPLE_RATE, POLES);
        double[] voiceSample = new double[4096];
        new MockUp<LinearPredictiveCoding>() {
            private int value = 1;
            @Mock double[][] applyLinearPredictiveCoding(double[] window) {
                double[][] result = new double[2][20];
                Arrays.fill(result[0], value);
                value++;
                return result;
            }
        };
        double[] reference = new double[20];
        // given sliding window 512 with step 256 -> 15 calls to lpc algorithm
        // given the above mock :
        // (1 + 2 + 3 + ... + 15) / 15 = 8
        Arrays.fill(reference, 8.0d);
        
        double[] features = lpc.extractFeatures(voiceSample);
        
        assertThat(features, is(equalTo(reference)));
    }
}
