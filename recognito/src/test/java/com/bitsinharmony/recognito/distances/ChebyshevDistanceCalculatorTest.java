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
package com.bitsinharmony.recognito.distances;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ChebyshevDistanceCalculatorTest {

    private ChebyshevDistanceCalculator distanceCalculator;
    private double[] identicalA;
    private double[] identicalB;
    
    @Before
    public void setUp() {
        distanceCalculator = new ChebyshevDistanceCalculator();
        identicalA = initIncrementalVector(20);
        identicalB = initIncrementalVector(20);
    }
    
    private double[] initIncrementalVector(int size) {
        double[] vector = new double[size];
        for(int i = 0; i < size; i++) {
            vector[i] = (double) i + 1;
        }
        return vector;
    }

    @Test
    public void nullValueOfBothParametersReturnsPositiveInfinity( ) {
        double distance = distanceCalculator.getDistance(null, null);
        assertThat(distance, is(equalTo(Double.POSITIVE_INFINITY)));
    }
    
    @Test
    public void nullValueOfFirstParametersReturnPositiveInfinity( ) {
        double distance = distanceCalculator.getDistance(null, new double[0]);
        assertThat(distance, is(equalTo(Double.POSITIVE_INFINITY)));
    }
    
    @Test
    public void nullValueOfSecondParameterReturnsPositiveInfinity( ) {
        double distance = distanceCalculator.getDistance(new double[0], null);
        assertThat(distance, is(equalTo(Double.POSITIVE_INFINITY)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void parametersOfDifferentLengthThrowsIllegalArgumentException() {
        distanceCalculator.getDistance(identicalA, new double[identicalA.length + 1]);
    }
    
    @Test
    public void identicalParametersReturnsDistanceOfZero() {
        double distance = distanceCalculator.getDistance(identicalA, identicalB);
        assertThat(distance, is(equalTo(0.0d)));
    }

    @Test
    public void reversedIncrementalVectorsOfTwentyReturnsDistanceOfNineTeen() {
        int last = identicalA.length - 1;
        for(int i = 0; i < identicalA.length; i++) {
            identicalB[i] = identicalA[last - i];
        }
        double distance = distanceCalculator.getDistance(identicalA, identicalB);
        assertThat(distance, is(equalTo(19.0)));
    }
}
