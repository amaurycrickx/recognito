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
package com.bitsinharmony.recognito.algorithms;

/**
 * Discrete autocorrelation at lag j algorithm
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Autocorrelation">Autocorrelation</a>
 * @author Amaury Crickx
 */
public class DiscreteAutocorrelationAtLagJ {

    /**
     * Computes discrete autocorrelation at lag j
     * 
     * @param buffer the buffered signal
     * @param lag the lag, in the range -1 &lt; lag &lt; voiceSample size
     * @return the computed autocorrelation result
     */
    public double autocorrelate(double[] buffer, int lag) {
        if(lag > -1 && lag < buffer.length) {
            double result = 0.0;
            for (int i = lag; i < buffer.length; i++) {
                result += buffer[i] * buffer[i - lag];
            }
            return result;
        } else {
            throw new IndexOutOfBoundsException("Lag parameter range is : -1 < lag < buffer size. Received [" 
                    + lag + "] for buffer size of [" + buffer.length + "]");
        }
    }

}
