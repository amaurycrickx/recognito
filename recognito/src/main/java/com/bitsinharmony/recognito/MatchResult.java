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

/**
 * MatchResult represents the result of matching a <code>VoicePrint</code> against a given voice sample. 
 * It holds the user defined key of the voice print and a likelihood ratio expressed as a percentage.
 *
 * @param <K> The type of the key selected by the user
 * @see Recognito#identify(double[], float)
 * @author Amaury Crickx
 */
public class MatchResult<K> {
    
    private final K key;
    private final int likelihoodRatio;
    private final double distance;
    
    /**
     * Default constructor
     * @param key the user defined key for the corresponding VoicePrint
     * @param likelihoodRatio the likelihood ratio expressed as a percentage
     */
    MatchResult(K key, int likelihoodRatio, double distance) {
        super();
        this.key = key;
        this.likelihoodRatio = likelihoodRatio;
        this.distance = distance;
    }

    /**
     * Get the matched key
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Get the likelihoodRatio level
     * @return the likelihoodRatio ratio expressed as a percentage
     */
    public int getLikelihoodRatio() {
        return likelihoodRatio;
    }
    
    /**
     * Get the raw distance between the <code>VoicePrint</code> idenntified by K 
     * and the given voice sample
     * @return the distance
     */
    double getDistance() {
        return distance;
    }
}
