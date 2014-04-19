package com.bitsinharmony.recognito;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bitsinharmony.recognito.algorithms.DiscreteAutocorrelationAtLagJTest;
import com.bitsinharmony.recognito.algorithms.LinearPredictiveCodingTest;
import com.bitsinharmony.recognito.algorithms.windowing.HammingWindowFunctionTest;
import com.bitsinharmony.recognito.algorithms.windowing.HannWindowFunctionTest;
import com.bitsinharmony.recognito.distances.ChebyshevDistanceCalculatorTest;
import com.bitsinharmony.recognito.distances.EuclideanDistanceCalculatorTest;
import com.bitsinharmony.recognito.enhancements.NormalizerTest;
import com.bitsinharmony.recognito.features.LpcFeaturesExtractorTest;
import com.bitsinharmony.recognito.vad.AutocorrellatedVoiceActivityDetectorTest;

@RunWith(Suite.class)
@SuiteClasses({ 
    HammingWindowFunctionTest.class,
    HannWindowFunctionTest.class,
    DiscreteAutocorrelationAtLagJTest.class,
    LinearPredictiveCodingTest.class,
    ChebyshevDistanceCalculatorTest.class,
    EuclideanDistanceCalculatorTest.class,
    NormalizerTest.class,
    LpcFeaturesExtractorTest.class,
    AutocorrellatedVoiceActivityDetectorTest.class,
    RecognitoTest.class, 
    VoicePrintConcurrencyTest.class, 
    VoicePrintTest.class
})

public class AllTests {}
