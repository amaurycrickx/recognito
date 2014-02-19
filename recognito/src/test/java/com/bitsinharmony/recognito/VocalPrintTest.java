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
package com.bitsinharmony.recognito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import mockit.Injectable;

import org.junit.Before;
import org.junit.Test;

import com.bitsinharmony.recognito.distances.DistanceCalculator;

/**
 * Tests for the VocalPrint class
 * 
 * @author Amaury Crickx
 */
public class VocalPrintTest {
    
    private VocalPrint vocalPrint;
    
    private double[] features1;
    private double[] features2;
    private double[] features3;

    @Before
    public void setUp() {
        features1 = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
        features2 = new double[] { 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0 };
        features3 = new double[] { 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0 };
        vocalPrint = new VocalPrint(features1);
    }

    @Test
    public void getDistanceDelegatesToDistanceCalculator(@Injectable final VocalPrint vp) {
        final double d = 100;
        
        double distance = vocalPrint.getDistance(new DistanceCalculator() {
            @Override public double getDistance(double[] features1, double[] features2) {
                return d;
            }
        }, vp);
        
        assertThat(distance, is(equalTo(d)));
    }

    @Test
    public void mergeMutatesTheFeaturesByComputingMeanValue(@Injectable final VocalPrint vp) {
        
        vocalPrint.merge(features2);
        vocalPrint.merge(features3);

        vocalPrint.getDistance(new DistanceCalculator() {
            @Override public double getDistance(double[] features1, double[] features2) {
                for(int i = 0; i < features1.length; i++) {
                    // 1.0 + 2.0 + 3.0 / 3 = 2.0
                    assertThat(features1[i], is(equalTo(2.0)));
                }
                return 0.0;
            }
        }, vp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeThrowsIllegalArgumentExceptionWhenFeaturesSizeIsDifferentThanInitial() {
        vocalPrint.merge(new double[features1.length + 1]);
    }
}
