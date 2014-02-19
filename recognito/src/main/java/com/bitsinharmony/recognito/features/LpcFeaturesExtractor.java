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

import com.bitsinharmony.recognito.algorithms.LinearPredictiveCoding;
import com.bitsinharmony.recognito.algorithms.windowing.HammingWindowFunction;
import com.bitsinharmony.recognito.algorithms.windowing.WindowFunction;

public class LpcFeaturesExtractor 
        extends WindowedFeaturesExtractor<double[]> {

    private final int poles;
    private final WindowFunction windowFunction;
    private final LinearPredictiveCoding lpc;

    public LpcFeaturesExtractor(float sampleRate, int poles) {
        super(sampleRate);
        this.poles = poles;
        this.windowFunction = new HammingWindowFunction(windowSize);
        this.lpc = new LinearPredictiveCoding(windowSize, poles);
    }

    @Override
    public double[] extractFeatures(double[] vocalSample) {

        double[] vocalFeatures = new double[poles];
        double[] audioWindow = new double[windowSize];

        int counter = 0;
        int halfWindowLength = windowSize / 2;

        for (int i = 0; (i + windowSize) <= vocalSample.length; i += halfWindowLength) {

            System.arraycopy(vocalSample, i, audioWindow, 0, windowSize);

            windowFunction.applyFunction(audioWindow);
            double[] lpcCoeffs = lpc.applyLinearPredictiveCoding(audioWindow)[0];

            for (int j = 0; j < poles; j++) {
                vocalFeatures[j] += lpcCoeffs[j];
            }
            counter++;
        }

        if (counter > 1) {
            for (int i = 0; i < poles; i++) {
                vocalFeatures[i] /= counter;
            }
        }
        return vocalFeatures;
    }
}
