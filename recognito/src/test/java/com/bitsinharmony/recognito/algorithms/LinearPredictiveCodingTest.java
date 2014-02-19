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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class LinearPredictiveCodingTest {
    
    private static final int POLES = 20;

    private LinearPredictiveCoding lpc;
    private double[] sinusoid;
    
    @Before
    public void setUp() {
        sinusoid = generateSinusoidalTone(100, 100.0d, 10000.0d);
        lpc = new LinearPredictiveCoding(sinusoid.length, POLES);
    }
    
    @Test
    public void preventUnwantedAlgorithmRegressionDueToRefactoring() {
        double[][] lpcCoding = lpc.applyLinearPredictiveCoding(sinusoid);
        
        assertThat(lpcCoding[0], is(equalTo(reference[0])));
        assertThat(lpcCoding[1], is(equalTo(reference[1])));
        
        // In case you need to regenerate the reference
//        StringBuilder coeffs = new StringBuilder();
//        StringBuilder errors = new StringBuilder();
//        for(int i = 0; i < POLES; i++) {
//            coeffs.append(lpcCoding[0][i]);
//            errors.append(lpcCoding[1][i]);
//            if((i + 1) % 5 == 0) {
//                coeffs.append(",\n");
//                errors.append(",\n");
//            } else {
//                coeffs.append(", ");
//                errors.append(", ");
//            }
//        }
//        System.out.println(coeffs.toString());
//        System.out.println(errors.toString());
    }
    
    /**
     * To generate a sinusoidal tone : 
     * f(x) = sin(2*pi*x*freq)
     * x moves along the time axis (i.e. these are the samples corresponding to the sample rate)
     * freq represents the frequency in cycles/seconds
     */
    private double[] generateSinusoidalTone(int sinusoidCount, double frequency, double sampleRate) {
        int size = (int) Math.round(sinusoidCount * sampleRate / frequency);
        double twoPi = 2 * Math.PI;
        double[] audio = new double[size]; 
        for(int i = 0; i < audio.length; i++) {
            double time = i / sampleRate;
            audio[i] = Math.sin(twoPi * frequency * time);
        }
        return audio;
    }
    
    private static final double[][] reference = new double[][] {
        // lpc calculated on generateSinusoidalTone(100, 100.0d, 10000.0d);
        {   // lpc coeffs
            0.0, 1.995953447541858, -0.9998996454107755, -2.1583232709400016E-7, 1.9714270658712986E-8,
            1.205619961543056E-7, -1.1008889658279106E-7, 3.669432608963836E-8, 4.71030304145731E-9, -9.758082872345994E-8,
            3.3253885211279753E-7, -5.856799825816946E-7, 6.603176160971334E-7, -4.932573369012327E-7, 1.8485161014316237E-7,
            6.323128527687099E-8, -1.4506174842230357E-7, 1.3992475254079849E-7, -4.278511815380666E-5, 4.832034963185139E-5
        },
        {   // lpc errors
            4999.9999999999945, 19.71324671394157, 0.007884510823393446, 0.007884510744981957, 0.007884510667244039,
            0.007884510591121436, 0.007884510517160574, 0.007884510445723848, 0.007884510377482199, 0.007884510312835566,
            0.007884510252175807, 0.007884510195968798, 0.00788451014404609, 0.007884510097344307, 0.007884510055177086,
            0.007884510018162975, 0.007884509986052864, 0.0078845099586744, 0.007884509935971734, 0.007884509917562538
        }
    };
}
