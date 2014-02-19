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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;

import com.bitsinharmony.recognito.distances.DistanceCalculator;

public class RecognitoTest {
    
    private static final int DEFAULT_SAMPLE_RATE = 22050;

    private Recognito<String> recognito;
    private double[] vocalSample;
    
    @Before
    public void setUp() {
        recognito = new Recognito<String>();
        vocalSample = new double[0];
    }
    
    @Test(expected = NullPointerException.class)
    public void createVocalPrintThrowsNullPointerExceptionWhenTheUserKeyIsNull() {
        recognito.createVocalPrint(null, vocalSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test
    public void createVocalPrintReturnsVocalPrint() {
        recognito.createVocalPrint("duh", vocalSample, DEFAULT_SAMPLE_RATE);
        
    }
    
    @Test(expected = NullPointerException.class)
    public void addVocalSampleForThrowsNullPointerExceptionWhenTheUserKeyIsNull() {
        recognito.mergeVocalSample(null, vocalSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void addVocalSampleForThrowsIllegalArgumentExceptionWhenTheUserKeyIsUnknown() {
        recognito.mergeVocalSample("duh", vocalSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test
    public void addVocalSampleForAddsTheVocalPrintToThePreviousVocalPrintInstance(@Mocked final VocalPrint unused) {
        final VocalPrint initial = recognito.createVocalPrint("test", vocalSample, DEFAULT_SAMPLE_RATE);
        recognito.mergeVocalSample("test", vocalSample, DEFAULT_SAMPLE_RATE);
        
        new Verifications() {{
            onInstance(initial).merge((double[]) any);
        }};
    }
    
    @Test
    public void recognizeReturnsListOf3UserKeysOrderedByClosestDistance() {
        final VocalPrint vp1 = recognito.createVocalPrint("1", vocalSample, DEFAULT_SAMPLE_RATE);
        final VocalPrint vp2 = recognito.createVocalPrint("2", vocalSample, DEFAULT_SAMPLE_RATE);
        final VocalPrint vp3 = recognito.createVocalPrint("3", vocalSample, DEFAULT_SAMPLE_RATE);
        final VocalPrint vp4 = recognito.createVocalPrint("4", vocalSample, DEFAULT_SAMPLE_RATE);
        final VocalPrint vp5 = recognito.createVocalPrint("5", vocalSample, DEFAULT_SAMPLE_RATE);

        new NonStrictExpectations(vp1, vp2, vp3, vp4, vp5) {{
            vp1.getDistance((DistanceCalculator) any, (VocalPrint) any); result = 5.0D;
            vp2.getDistance((DistanceCalculator) any, (VocalPrint) any); result = 4.0D;
            vp3.getDistance((DistanceCalculator) any, (VocalPrint) any); result = 3.0D;
            vp4.getDistance((DistanceCalculator) any, (VocalPrint) any); result = 2.0D;
            vp5.getDistance((DistanceCalculator) any, (VocalPrint) any); result = 1.0D;
        }};
        
        List<String> keys = recognito.recognize(vocalSample, DEFAULT_SAMPLE_RATE);

        assertThat(keys.get(0), is(equalTo("5")));
        assertThat(keys.get(1), is(equalTo("4"))); 
        assertThat(keys.get(2), is(equalTo("3")));
        assertThat(keys.size(), is(equalTo(3)));
    }
    
    @Test
    public void identifySpeakerDoesntBreakWithOnlyTwoSpeakers() {
        
        final VocalPrint vp1 = recognito.createVocalPrint("1", vocalSample, DEFAULT_SAMPLE_RATE);
        final VocalPrint vp2 = recognito.createVocalPrint("2", vocalSample, DEFAULT_SAMPLE_RATE);

        new NonStrictExpectations(vp1, vp2) {{
            vp1.getDistance((DistanceCalculator) any, (VocalPrint) any); result = 5.0D;
            vp2.getDistance((DistanceCalculator) any, (VocalPrint) any); result = 4.0D;
        }};
        
        List<String> keys = recognito.recognize(vocalSample, DEFAULT_SAMPLE_RATE);

        assertThat(keys.get(0), is(equalTo("2")));
        assertThat(keys.get(1), is(equalTo("1"))); 
        assertThat(keys.size(), is(equalTo(2)));
    }

}
