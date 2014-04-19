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
    private double[] voiceSample;
    
    @Before
    public void setUp() {
        recognito = new Recognito<String>();
        voiceSample = new double[0];
    }
    
    @Test(expected = NullPointerException.class)
    public void createVoicePrintThrowsNullPointerExceptionWhenTheUserKeyIsNull() {
        recognito.createVoicePrint(null, voiceSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test
    public void createVoicePrintReturnsVoicePrint() {
        recognito.createVoicePrint("duh", voiceSample, DEFAULT_SAMPLE_RATE);
        
    }
    
    @Test(expected = NullPointerException.class)
    public void addVoiceSampleForThrowsNullPointerExceptionWhenTheUserKeyIsNull() {
        recognito.mergeVoiceSample(null, voiceSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void addVoiceSampleForThrowsIllegalArgumentExceptionWhenTheUserKeyIsUnknown() {
        recognito.mergeVoiceSample("duh", voiceSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test
    public void addVoiceSampleForAddsTheVoicePrintToThePreviousVoicePrintInstance(@Mocked final VoicePrint unused) {
        final VoicePrint initial = recognito.createVoicePrint("test", voiceSample, DEFAULT_SAMPLE_RATE);
        recognito.mergeVoiceSample("test", voiceSample, DEFAULT_SAMPLE_RATE);
        
        new Verifications() {{
            onInstance(initial).merge((double[]) any);
        }};
    }
    
    @Test
    public void recognizeReturnsListOf3UserKeysOrderedByClosestDistance() {
        final VoicePrint vp1 = recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);
        final VoicePrint vp2 = recognito.createVoicePrint("2", voiceSample, DEFAULT_SAMPLE_RATE);
        final VoicePrint vp3 = recognito.createVoicePrint("3", voiceSample, DEFAULT_SAMPLE_RATE);
        final VoicePrint vp4 = recognito.createVoicePrint("4", voiceSample, DEFAULT_SAMPLE_RATE);
        final VoicePrint vp5 = recognito.createVoicePrint("5", voiceSample, DEFAULT_SAMPLE_RATE);

        new NonStrictExpectations(vp1, vp2, vp3, vp4, vp5) {{
            vp1.getDistance((DistanceCalculator) any, (VoicePrint) any); result = 5.0D;
            vp2.getDistance((DistanceCalculator) any, (VoicePrint) any); result = 4.0D;
            vp3.getDistance((DistanceCalculator) any, (VoicePrint) any); result = 3.0D;
            vp4.getDistance((DistanceCalculator) any, (VoicePrint) any); result = 2.0D;
            vp5.getDistance((DistanceCalculator) any, (VoicePrint) any); result = 1.0D;
        }};
        
        List<String> keys = recognito.recognize(voiceSample, DEFAULT_SAMPLE_RATE);

        assertThat(keys.get(0), is(equalTo("5")));
        assertThat(keys.get(1), is(equalTo("4"))); 
        assertThat(keys.get(2), is(equalTo("3")));
        assertThat(keys.size(), is(equalTo(3)));
    }
    
    @Test
    public void identifySpeakerDoesntBreakWithOnlyTwoSpeakers() {
        
        final VoicePrint vp1 = recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);
        final VoicePrint vp2 = recognito.createVoicePrint("2", voiceSample, DEFAULT_SAMPLE_RATE);

        new NonStrictExpectations(vp1, vp2) {{
            vp1.getDistance((DistanceCalculator) any, (VoicePrint) any); result = 5.0D;
            vp2.getDistance((DistanceCalculator) any, (VoicePrint) any); result = 4.0D;
        }};
        
        List<String> keys = recognito.recognize(voiceSample, DEFAULT_SAMPLE_RATE);

        assertThat(keys.get(0), is(equalTo("2")));
        assertThat(keys.get(1), is(equalTo("1"))); 
        assertThat(keys.size(), is(equalTo(2)));
    }

}
