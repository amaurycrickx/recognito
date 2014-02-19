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
package com.bitsinharmony.recognito.algorithms.windowing;

import java.util.HashMap;
import java.util.Map;

/**
 * Hamming Window Function
 * <p>
 * Threading : this class is thread safe
 * </p>
 * @see <a href="http://en.wikipedia.org/wiki/Window_function#Hamming_window">Hamming window<a/>
 * @see WindowFunction
 * @author Amaury Crickx
 */
public class HammingWindowFunction 
        extends WindowFunction {

    private static final Map<Integer, double[]> factorsByWindowSize = new HashMap<Integer, double[]>();

    /**
     * Constructor imposed by WindowFunction
     * @param windowSize the windowSize
     * @see WindowFunction#WindowFunction(int)
     */
    public HammingWindowFunction(int windowSize) {
        super(windowSize);
    }

    @Override
    protected double[] getPrecomputedFactors(int windowSize) {
        // precompute factors for given window, avoid re-calculating for several instances
        synchronized (HammingWindowFunction.class) {
            double[] factors;
            if(factorsByWindowSize.containsKey(windowSize)) {
                factors = factorsByWindowSize.get(windowSize);
            } else {
                factors = new double[windowSize];
                int sizeMinusOne = windowSize - 1;
                for(int i = 0; i < windowSize; i++) {
                    factors[i] = 0.54d - (0.46d * Math.cos((TWO_PI * i) / sizeMinusOne));
                }
                factorsByWindowSize.put(windowSize, factors);
            }
            return factors;
        }
    }

}
