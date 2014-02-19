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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class NormalizerTest {

    private Normalizer normalizer;
    private double[] values;
    
    @Before
    public void setUp() {
        normalizer = new Normalizer();
        values = new double[20];
    }
    
    @Test
    public void normalizingValuesContainingOneIsANoOp() {
        Arrays.fill(values, 0.1d);
        values[10] = 1.0d;
        double[] values2 = Arrays.copyOf(values, values.length);

        double gainMultiplier = normalizer.normalize(values, 22050.0f);

        assertThat(gainMultiplier, is(equalTo(1.0d)));
        assertThat(values, is(equalTo(values2)));
    }

    @Test
    public void normalizingValuesContainingMinusOneIsANoOp() {
        Arrays.fill(values, 0.1d);
        values[10] = -1.0d;
        double[] values2 = Arrays.copyOf(values, values.length);
        
        double gainMultiplier = normalizer.normalize(values, 22050.0f);

        assertThat(gainMultiplier, is(equalTo(1.0d)));
        assertThat(values, is(equalTo(values2)));
    }
    
    @Test
    public void normalizingValuesContainingOnlyZeroesIsANoOp() {
        double[] values2 = Arrays.copyOf(values, values.length);
        
        double gainMultiplier = normalizer.normalize(values, 22050.0f);

        assertThat(gainMultiplier, is(closeTo(1.0d, Math.ulp(0.0d))));
        assertThat(values, is(equalTo(values2)));
    }
    
    @Test
    public void normalizingValuesContainingEightTenths() {
        Arrays.fill(values, 0.1d);
        values[10] = 0.8d;
        
        double gainMultiplier = normalizer.normalize(values, 22050.0f);

        assertThat(gainMultiplier, is(equalTo(1.25d)));
        for(int i = 0; i < values.length; i++) {
            if(i != 10) {
                assertThat(values[i], is(equalTo(0.125d)));
            } else {
                assertThat(values[10], is(equalTo(1.0d)));
            }
        }
    }
    
}
