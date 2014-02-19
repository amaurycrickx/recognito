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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.bitsinharmony.recognito.distances.DistanceCalculator;

/**
 * Tests VocalPrint under concurrent calls
 * @author Amaury Crickx
 */
public class VocalPrintConcurrencyTest {
    
    private static final int CONCURRENCY_LEVEL = 100;
    private static final int FEATURES_LENGTH = 10000;

    private VocalPrint sut;

    private List<Runnable> vocalPrintProducers;
    private List<Callable<Double>> featuresConsistencyVerifiers;
    private double[][] allFeatures;

    @Before
    public void setUp() {

        sut = new VocalPrint(new double[FEATURES_LENGTH]);

        vocalPrintProducers = new ArrayList<Runnable>();
        featuresConsistencyVerifiers = new ArrayList<Callable<Double>>();
        allFeatures = new double[CONCURRENCY_LEVEL ][FEATURES_LENGTH];
        
        for(int i = 0; i < CONCURRENCY_LEVEL ; i++) {
            Arrays.fill(allFeatures[i], i + 1.0d);
            vocalPrintProducers.add(new VocalPrintProducer(sut, allFeatures[i]));
            featuresConsistencyVerifiers.add(new FeaturesConsistencyVerifier(sut));
        }
    }

    @Test
    public void mergeAndGetDistanceMethodsCantExecuteConcurrently() 
            throws InterruptedException, ExecutionException {
        
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            List<Future<Double>> futures = new ArrayList<Future<Double>>();
            for(int i = 0; i < CONCURRENCY_LEVEL; i++) {
                // intermingling merge and getDistance calls
                // featuresConsistencyVerifiers performing getDistance calls should not see half merged features
                executorService.submit(vocalPrintProducers.get(i));
                futures.add(executorService.submit(featuresConsistencyVerifiers.get(i)));
            }
            
            for(int i = 0; i < CONCURRENCY_LEVEL; i++) {
                futures.get(i).get();
            }
            
        } finally {
            executorService.shutdown();
            if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        }

    }
    
    private class VocalPrintProducer implements Runnable {

        private double[] features;
        private VocalPrint sut;

        public VocalPrintProducer(VocalPrint sut, double[] features) {
            this.sut = sut;
            this.features = features;
        }
        
        @Override
        public void run() {
            sut.merge(features);
        }
    }
    
    private class FeaturesConsistencyVerifier implements Callable<Double> {

        private VocalPrint sut;

        public FeaturesConsistencyVerifier(VocalPrint sut) {
            this.sut = sut;
        }
        
        @Override
        public Double call() throws Exception {
            return sut.getDistance(new DistanceCalculator() {
                @Override
                public double getDistance(double[] features1, double[] features2) {
                    double reference = features1[0];
                    for(int i = 1; i < features1.length; i++) {
                        // all features should be equal to the first one unless merging occured at the same time..
                        assertThat("Merging and distance calculations should be serialized regarding each other", 
                                features1[i], is(equalTo(reference)));
                    }
                    return 0;
                }
            }, sut);
        }
    }
}
