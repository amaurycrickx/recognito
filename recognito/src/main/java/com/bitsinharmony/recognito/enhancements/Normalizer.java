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
package com.bitsinharmony.recognito.enhancements;

/**
 * Nomalizes gain of the given voice sample.
 * I.e. : looks for the highest value (positive or negative) and applies uniform gain on all samples
 * bringing the highest value to max value of 1.0 or -1.0
 * <p>
 * Threadin : this class is thread safe
 * </p>
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Audio_normalization">Audio normalization</a>
 * @author Amaury Crickx
 */
public class Normalizer {

    /**
     * Normalize gain of the given sample. The given audio buffer is directly modified.
     * @param audioSample the voice sample
     * @param sampleRate the sample rate
     * @return the applied factor (i.e. 1.0 / Math.abs(maxValue))
     */
    public double normalize(double[] audioSample, float sampleRate) {

        double max = Double.MIN_VALUE;

        for (int i = 0; i < audioSample.length; i++) {
            double abs = Math.abs(audioSample[i]);
            if (abs > max) {
                max = abs;
            }
        }
        if(max > 1.0d) {
            throw new IllegalArgumentException("Expected value for audio are in the range -1.0 <= v <= 1.0 ");
        }
        if (max < 5 * Math.ulp(0.0d)) { // ulp of 0.0 is extremely small ! i.e. as small as it can get
            return 1.0d;
        }
        for (int i = 0; i < audioSample.length; i++) {
            audioSample[i] /= max;
        }
        return 1.0d / max;
    }

}
