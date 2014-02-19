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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class DiscreteAutocorrelationAtLagJTest {

    private DiscreteAutocorrelationAtLagJ autocorrelation;
    private double[] arrayOfTwos;
    
    @Before
    public void setUp() {
        autocorrelation = new DiscreteAutocorrelationAtLagJ();
        arrayOfTwos = new double[20];
        Arrays.fill(arrayOfTwos, 2.0d);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void lagLowerBoundIsTestedBeforeMethodExecutes() {
        autocorrelation.autocorrelate(arrayOfTwos, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void lagUpperBoundIsTestedBeforeMethodExecutes() {
        autocorrelation.autocorrelate(arrayOfTwos, arrayOfTwos.length + 1);
    }
    
    @Test
    public void arrayOfTwosAutocorrelationReturnsAValueEqualToLengthMinusLagTimesFour() {
        for(int i = 0; i < arrayOfTwos.length; i++) {
            double autocorrelate = autocorrelation.autocorrelate(arrayOfTwos, i);

            assertThat(autocorrelate, is(equalTo((arrayOfTwos.length - i) * 4.0d)));
        }
    }
    
}
