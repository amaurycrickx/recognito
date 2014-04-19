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

import java.util.Arrays;

/**
 * Linear Predictive Coding algorithm
 * <p>
 * Since this algorithm is generally used in an iterative process over small windows of audio data, 
 * it's output reuses the same buffers over and over again in order to avoid memory allocation 
 * and garbage collection alltogether. It's up to the client to copy the returned data should it 
 * want to keep it as-is and not use it in further processing.
 * </p>
 * <p>
 * Threading : this class is _NOT_ thread safe
 * </p>
 * @see <a href="http://en.wikipedia.org/wiki/Linear_predictive_coding"><a/>
 * @author Amaury Crickx
 */
public class LinearPredictiveCoding {

    private final int windowSize;
    private final int poles;
    private final double[] output;
    private final double[] error;
    private final double[] k;
    private final double[][] matrix;


    /**
     * Constructor for LinearPredictiveCoding
     * @param windowSize the window size
     * @param poles
     */
    public LinearPredictiveCoding(int windowSize, int poles) {
        this.windowSize = windowSize;
        this.poles = poles;
        this.output = new double[poles];
        this.error = new double[poles];
        this.k = new double[poles];
        this.matrix = new double[poles][poles];
    }

    /**
     * Apply Linear Predictive Coding 
     * 
     * @param window windowed part of voice sample, must be of the same size as the windowSize passed in constructor
     * @return an array of size 2 containing LPC coefficients in 0 and error coefficients in 1
     */
    public double[][] applyLinearPredictiveCoding(double[] window) {
        
        if(windowSize != window.length) {
            throw new IllegalArgumentException("Given window length was not equal to the one provided in constructor : [" 
                    + window.length +"] != [" + windowSize + "]");
        }
        
        Arrays.fill(k,  0.0d);
        Arrays.fill(output, 0.0d);
        Arrays.fill(error, 0.0d);
        for(double[] d : matrix) {
            Arrays.fill(d, 0.0d);
        }

        DiscreteAutocorrelationAtLagJ dalj = new DiscreteAutocorrelationAtLagJ();
        double[] autocorrelations = new double[poles];
        for(int i = 0; i < poles; i++) {
            autocorrelations[i] = dalj.autocorrelate(window, i);
        }

        error[0] = autocorrelations[0];

        for (int m = 1; m < poles; m++) {
            double tmp = autocorrelations[m];
            for (int i = 1; i < m; i++) {
                tmp -= matrix[m - 1][i] * autocorrelations[m - i];
            }
            k[m] = tmp / error[m - 1];

            for (int i = 0; i < m; i++) {
                matrix[m][i] = matrix[m - 1][i] - k[m] * matrix[m - 1][m - i];
            }
            matrix[m][m] = k[m];
            error[m] = (1 - (k[m] * k[m])) * error[m - 1];
        }

        for (int i = 0; i < poles; i++) {
            if (Double.isNaN(matrix[poles - 1][i])) {
                output[i] = 0.0;
            } else {
                output[i] = matrix[poles - 1][i];
            }
        }
        
        return new double[][] { output, error };
    }
}
