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

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.bitsinharmony.recognito.distances.DistanceCalculator;

/**
 * Represents a voice print in the system
 * 
 * @author Amaury Crickx
 */
public final class VoicePrint
        implements Serializable {

    private static final long serialVersionUID = 5656438598778733593L;
    
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    
    private double[] features;
    private int meanCount;

    
    /**
     * Package visible constructor for Hibernate and the likes
     */
    VoicePrint() {}

    /**
     * Contructor for a voice print
     * @param features the features
     */
    VoicePrint(double[] features) {
        super();
        this.features = features;
        this.meanCount = 1;
    }

    /**
     * Copy constructor
     * @param print the VoicePrint to copy
     */
    VoicePrint(VoicePrint print) {
        this(Arrays.copyOf(print.features, print.features.length));
    }

    /**
     * Returns the distance between this voice print and the given one using the calculator.
     * Threading : it is safe to call this method while other threads may merge this voice print instance
     * with another one in the sense that the distance calculation will not happen on half merged voice print.
     * Since this method is read only, it is safe to call it from multiple threads for a single instance
     * @param calculator the distance calculator
     * @param voicePrint the voice print
     * @return the distance
     */
    double getDistance(DistanceCalculator calculator, VoicePrint voicePrint) {
        r.lock();
        try { 
            return calculator.getDistance(this.features, voicePrint.features);
        } 
        finally { r.unlock(); }
    }

    /**
     * Merges this voice print features with the given one.
     * Threading : it is safe to call this method while other threads may request the distance of this voice 
     * regarding another one in the sense that the distance calculation will not happen on half merged voice print
     * @param features the features to merge
     */
    void merge(double[] features) {
        if(this.features.length != features.length) {
            throw new IllegalArgumentException("Features of new VoicePrint is of different size : [" + 
                    features.length + "] expected [" + this.features.length + "]");
        }
        w.lock();
        try { 
            merge(this.features, features);
            meanCount++;
        } 
        finally { w.unlock(); }
    }

    /**
     * Convenience method to merge voice prints
     * @param print the voice print to merge
     * @see VoicePrint#merge(double[])
     */
    void merge(VoicePrint print) {
        this.merge(print.features); 
    }

    /**
     * Recomputes the mean values for the inner features when adding the outer features
     * @param inner the inner features
     * @param outer the outer features
     */
    private void merge(double[] inner, double[] outer) {
        for (int i = 0; i < inner.length; i++) {
            inner[i] = (inner[i] * meanCount + outer[i]) / (meanCount + 1);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Arrays.toString(features);
    }
    
}
