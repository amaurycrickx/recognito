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
package com.bitsinharmony.recognito.vad;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class AutocorrellatedVoiceActivityDetectorTest {
    
    private static final int DEFAULT_SAMPLE_RATE = 22050;

    private AutocorrellatedVoiceActivityDetector vad;
    
    @Before
    public void setUp() {
        vad = new AutocorrellatedVoiceActivityDetector();
    }
    
    @Test
    public void vadReturnsEmptyArrayWhenFedWithPureSilence() {
        double[] output = vad.removeSilence(new double[8192], DEFAULT_SAMPLE_RATE);
        
        assertThat(output.length, is(equalTo(0)));
    }
    
    @Test
    public void vadReturnsOriginalAudioBufferWhenThereIsNoDetectableSilence() {
        double[] voiceSample = new double[8192];
        Arrays.fill(voiceSample, 1.0);
        
        double[] output = vad.removeSilence(voiceSample, DEFAULT_SAMPLE_RATE);

        assertThat(output, is(sameInstance(voiceSample)));
    }

    @Test
    public void vadReturnsOriginalAudioBufferWhenVoiceSampleIsTooSmall() {
        int vaLength = vad.getMinimumVoiceActivityLength(DEFAULT_SAMPLE_RATE);
        double[] voiceSample = new double[vaLength - 1];
        Arrays.fill(voiceSample, 1.0);
        
        double[] output = vad.removeSilence(voiceSample, DEFAULT_SAMPLE_RATE);

        assertThat(output, is(sameInstance(voiceSample)));
    }
    
    @Test
    public void vadRemovesRandomNoiseAndLeavesTheRest() {
        // for predictability, have to take min activity length into account
        // and autocorrelation buffer length -> unusual sample rate makes it easier
        int sampleRate = 40000; 
        int vaLength = vad.getMinimumVoiceActivityLength(sampleRate);
        double[] noisy = new double[8160];

        Arrays.fill(noisy, 0, vaLength, 1.0);
        makeSomeNoise(noisy, sampleRate, vaLength, noisy.length);
        
        double[] output = vad.removeSilence(noisy, sampleRate);

        assertThat(output.length, is(equalTo(vaLength)));
    }

    /**
     * Create white noise
     * @param noisy the buffer to fill with noise
     * @param sampleRate the sample rate used
     * @see http://www.developer.com/java/other/article.php/3484591/Convolution-and-Frequency-Filtering-in-Java.htm
     */
    private void makeSomeNoise(double[] noisy, int sampleRate, int start, int end) {
        Random random = new Random();
        int whiteningFactor = 5000;
        for(int i = start; i < end; i++) {
            // not making it more random :-) -> trying to approximate the characteristics of white noise, see above link
            for(int j = 0; j < whiteningFactor; j++) {
                // diminishing gain so the test is a whole lot less likely to fail because of unlucky randomness
                // also, noise shouldn't be that loud if you expect to recognize anything 
                // 0.3 is still quite loud -> -10.5 dB (stereo amp volume knob usually has steps of 3dB, so this is 3.5 ticks down)
                noisy[i] += (2 * random.nextDouble() - 1) * 0.3; 
            }
        }
        for(int i = start; i < end; i++) {
            noisy[i] /= whiteningFactor;
        }
    }
}
