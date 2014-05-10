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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Random;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;

import com.bitsinharmony.recognito.distances.DistanceCalculator;

public class RecognitoTest {
    
    private final class EqualityDistanceCalculator extends DistanceCalculator {
        @Override
        public double getDistance(double[] features1, double[] features2) {
            for(int i = 0; i < features1.length; i++) {
                if(features1[i] != features2[i]) {
                    return Double.MAX_VALUE;
                }
            }
            return 0.0d;
        }
    }

    private static final int DEFAULT_SAMPLE_RATE = 22050;
    private final Random random = new Random();

    private Recognito<String> recognito;
    private double[] voiceSample;
    
    @Before
    public void setUp() {
        recognito = new Recognito<String>();
        voiceSample = new double[1024];
        fillWithNoise(voiceSample);
    }
    
    @Test(expected = NullPointerException.class)
    public void createVoicePrintThrowsNullPointerExceptionWhenTheUserKeyIsNull() {
        recognito.createVoicePrint(null, voiceSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test
    public void createVoicePrintReturnsVoicePrint() {
        VoicePrint voicePrint = recognito.createVoicePrint("duh", voiceSample, DEFAULT_SAMPLE_RATE);
        assertNotNull(voicePrint);
    }
    
    @Test(expected = NullPointerException.class)
    public void mergeVoiceSampleThrowsNullPointerExceptionWhenTheUserKeyIsNull() {
        recognito.mergeVoiceSample(null, voiceSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void mergeVoiceSampleThrowsIllegalArgumentExceptionWhenTheUserKeyIsUnknown() {
        recognito.mergeVoiceSample("duh", voiceSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test
    public void mergeVoiceSampleForMergesTheNewFeaturesWithThePreviousVoicePrintInstance(@Mocked final VoicePrint unused) {
        final VoicePrint initial = recognito.createVoicePrint("test", voiceSample, DEFAULT_SAMPLE_RATE);
        recognito.mergeVoiceSample("test", voiceSample, DEFAULT_SAMPLE_RATE);
        
        new Verifications() {{
            onInstance(initial).merge((double[]) any);
        }};
    }
    
    @Test(expected = IllegalStateException.class)
    public void identifyBreaksWhenNoVoicePrintWasPreviouslyExtracted() {
       recognito.identify(voiceSample, DEFAULT_SAMPLE_RATE);
    }
    
    @Test
    public void identifyReturnsListOfMatchResultsOrderedByClosestDistance() {
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
        
        List<MatchResult<String>> matches = recognito.identify(voiceSample, DEFAULT_SAMPLE_RATE);

        assertThat(matches.get(0).getKey(), is(equalTo("5")));
        assertThat(matches.get(1).getKey(), is(equalTo("4"))); 
        assertThat(matches.get(2).getKey(), is(equalTo("3")));
        assertThat(matches.get(3).getKey(), is(equalTo("2")));
        assertThat(matches.get(4).getKey(), is(equalTo("1")));
        assertThat(matches.size(), is(equalTo(5)));
    }
    
    @Test
    public void identifyDoesntBreakWithOnlyOneSpeaker() {
        
        recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);

        List<MatchResult<String>> matches = recognito.identify(voiceSample, DEFAULT_SAMPLE_RATE);

        assertThat(matches.get(0).getKey(), is(equalTo("1")));
        assertThat(matches.size(), is(equalTo(1)));
    }

    @Test
    public void likelyhoodRatioForAnySampleIs50PercentWithSingleEntryAvailable() {
        // and that is because the distance to the universal model is equal to the distance to the unique voice print 
        // (i.e. they are both the same, see universalModelIsEqualToSingleEntry test) 
        final double[] voiceSample2 = new double[1024];
        fillWithNoise(voiceSample2);

        recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);
        
        List<MatchResult<String>> matches = recognito.identify(voiceSample2, DEFAULT_SAMPLE_RATE);
        
        MatchResult<String> match = matches.get(0);
        assertThat(match.getLikelihoodRatio(), is(equalTo(50)));
    }
    
    @Test
    public void universalModelIsEqualToSingleEntry() {
        
        VoicePrint vp = recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);
        VoicePrint universalModel = recognito.getUniversalModel();
        
        double distance = vp.getDistance(new EqualityDistanceCalculator(), universalModel);
        
        assertThat(distance, is(equalTo(0d)));
    }
    
    @Test
    public void universalModelIsNotModifiedOnceSetByUser() {
        
        VoicePrint universalModel = new VoicePrint(new double[20]);
        recognito.setUniversalModel(universalModel);

        final double[] voiceSample2 = new double[1024];
        fillWithNoise(voiceSample2);
        recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);
        recognito.mergeVoiceSample("1", voiceSample2, DEFAULT_SAMPLE_RATE);
        
        VoicePrint universalModel2 = recognito.getUniversalModel();
        double distance = universalModel2.getDistance(new EqualityDistanceCalculator(), universalModel);
        
        assertThat(distance, is(equalTo(0d)));
    }
    
    @Test
    public void universalModelIsModifiedByCreateVoicePrintMethod() {
        // single entry test already exists, let's test with 2 entries
        final double[] voiceSample2 = new double[1024];
        fillWithNoise(voiceSample2);
        
        recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);
        VoicePrint universalModel = recognito.getUniversalModel();

        recognito.createVoicePrint("2", voiceSample2, DEFAULT_SAMPLE_RATE);
        VoicePrint universalModel2 = recognito.getUniversalModel();
        
        double distance = universalModel.getDistance(new EqualityDistanceCalculator(), universalModel2);
        
        assertThat(distance, is(equalTo(Double.MAX_VALUE)));
    }

    @Test
    public void universalModelIsModifiedByMergeVoicePrintMethod() {
        // single entry test already exists, let's test with 2 entries
        final double[] voiceSample2 = new double[1024];
        fillWithNoise(voiceSample2);
        
        recognito.createVoicePrint("1", voiceSample, DEFAULT_SAMPLE_RATE);
        VoicePrint universalModel = recognito.getUniversalModel();

        recognito.mergeVoiceSample("1", voiceSample2, DEFAULT_SAMPLE_RATE);
        VoicePrint universalModel2 = recognito.getUniversalModel();
        
        double distance = universalModel.getDistance(new EqualityDistanceCalculator(), universalModel2);
        
        assertThat(distance, is(equalTo(Double.MAX_VALUE)));
    }

    @Test(expected = IllegalArgumentException.class) 
    public void setUniversalModelToNullValueThrowsIllegalArgumentException() {
        recognito.setUniversalModel(null);
    }
    
    private void fillWithNoise(final double[] voiceSample) {
        for(int i = 0; i < voiceSample.length; i++) {
            voiceSample[i] = random.nextDouble() * 2 - 1; // values between -1 and 1
        }
    }
}
