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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.bitsinharmony.recognito.distances.DistanceCalculator;
import com.bitsinharmony.recognito.distances.EuclideanDistanceCalculator;
import com.bitsinharmony.recognito.enhancements.Normalizer;
import com.bitsinharmony.recognito.features.FeaturesExtractor;
import com.bitsinharmony.recognito.features.LpcFeaturesExtractor;
import com.bitsinharmony.recognito.utils.FileHelper;
import com.bitsinharmony.recognito.vad.AutocorrellatedVoiceActivityDetector;

/**
 * Front class for accessing Recognito's speaker recognition features
 * <p>
 * {@code Recognito} holds a set of vocal prints associated with user keys and allows 
 * execution of different tasks on them :
 * </p>
 * <ul>
 * <li>Create a vocal print from an audio sample and store it with an associated user key</li>
 * <li>Merge a new vocal sample into an existing vocal print</li>
 * <li>Speaker recognition : analyse vocal characteristics from an unknown sample and return user keys 
 * whose vocal print are closest matches</li>
 * </ul>
 * <p>
 * Recognito expects all vocal samples to be comprised of a single channel (i.e. mono). Using a stereo sample 
 * whose channels are identical will merely double processing time. Using a real stereo sample will make 
 * the processing less accurate while doubling processing time.<br/>
 * </p>
 * <p>
 * {@code Recognito} is generified in order to allow the user to specify its own type of user keys.
 * The constraints on user keys are the same as those for {@code java.util.Map} a key<br/>
 * </p>
 * <p>
 * It is up to the client to manage persistence of the created vocal print objects. Persisted vocal prints
 * may be passed into an alternate constructor as a Map of user keys pointing to a vocal print.
 * </p>
 * <p>
 * For methods taking a file handle :<br/>
 * Supporting each and every file formats is a real pain and not the primary goal of Recognito. As such,
 * the conversion capabilities of the javax.sound.sampled are used internally.
 * Depending on your particular JVM implementation, some file types may or may not be supported.
 * If you're looking for MP3 or Ogg support, check Javazoom SPI's.
 * In case you may choose, Recognito's preferred file format is PCM 16bit mono 22050 Hz (WAV files are PCM)<br/>
 * You may also want to check http://sox.sourceforge.net for dedicated conversion software.
 * </p>
 * <p>
 * Threading : usage of Recognito is thread safe, see methods documentation for details
 * </p>
 * @author Amaury Crickx
 * @see {@link java.util.Map} for the constraints on Key objects
 */
public class Recognito<K> {

    private final ConcurrentHashMap<K, VocalPrint> store = new ConcurrentHashMap<K, VocalPrint>();

    /**
     * Default constructor
     */
    public Recognito() {
    }
    
    /**
     * Constructor taking previously extracted vocal prints directly into the system
     */
    public Recognito(Map<K, VocalPrint> vocalPrintsByUserKey) {
        store.putAll(vocalPrintsByUserKey);
    }
    
    /**
     * Creates a vocal print and stores it along with the user key for later comparison with new samples
     * <p>
     * Threading : this method is synchronized to prevent inadvertently erasing an existing user key
     * </p>
     * @param userKey the user key associated with this vocal print
     * @param vocalSample the vocal sample, values between -1.0 and 1.0
     * @param sampleRate the sample rate, at least 8000.0 Hz (preferably higher)
     * @return the vocal print extracted from the given sample
     */
    public synchronized VocalPrint createVocalPrint(K userKey, double[] vocalSample, float sampleRate) {
        if(userKey == null) {
            throw new NullPointerException("The userKey is null");
        }
        if(store.containsKey(userKey)) {
            throw new IllegalArgumentException("The userKey already exists");
        }
        double[] features = extractFeatures(vocalSample, sampleRate);
        VocalPrint vocalPrint = new VocalPrint(features);
        
        store.put(userKey, vocalPrint);
        
        return vocalPrint;
    }
    
    /**
     * Convenience method to load vocal samples from files.
     * <p>
     * See class description for details on files
     * </p>
     * @param userKey the user key associated with this vocal print
     * @param vocalSampleFile the file containing the vocal sample
     * @return the vocal print
     * @throws UnsupportedAudioFileException when the JVM does not support the file format
     * @throws IOException when an I/O exception occurs
     * @see Recognito#createVocalPrint(Object, double[], float)
     */
    public VocalPrint createVocalPrint(K userKey, File vocalSampleFile) 
            throws UnsupportedAudioFileException, IOException {
        
        AudioInputStream sample = AudioSystem.getAudioInputStream(vocalSampleFile);
        AudioFormat format = sample.getFormat();
        double[] audioSample = FileHelper.readAudioInputStream(sample);

        return createVocalPrint(userKey, audioSample, format.getSampleRate());
    }
    
    /**
     * Extracts vocal features from the given vocal sample and merges them with previous vocal 
     * print extracted for this user key
     * <p>
     * Threading : it is safe to simultaneously add vocal samples for a single userKey from multiple threads
     * </p>
     * @param userKey the user key associated with this vocal print
     * @param vocalSample the vocal sample to analyze, values between -1.0 and 1.0
     * @param sampleRate the sample rate, at least 8000.0 Hz (preferably higher)
     * @return the updated vocal print
     */
    public VocalPrint mergeVocalSample(K userKey, double[] vocalSample, float sampleRate) {
        
        if(userKey == null) {
            throw new NullPointerException("The userKey is null");
        }
        
        VocalPrint original = store.get(userKey);
        if(original == null) {
            throw new IllegalArgumentException("No vocal print linked to this user key");
        }

        original.merge(extractFeatures(vocalSample, sampleRate));
        
        return original;
    }
    
    /**
     * Convenience method to merge vocal samples from files. 
     * <p>
     * See class description for details on files
     * </p>
     * @param userKey the user key associated with this vocal print
     * @param vocalSampleFile the file containing the vocal sample
     * @return the updated vocal print
     * @throws UnsupportedAudioFileException when the JVM does not support the file format
     * @throws IOException when an I/O exception occurs
     * @see Recognito#mergeVocalSample(Object, double[], float)
     */
    public VocalPrint mergeVocalSample(K userKey, File vocalSampleFile) 
            throws UnsupportedAudioFileException, IOException {
        
        AudioInputStream sample = AudioSystem.getAudioInputStream(vocalSampleFile);
        AudioFormat format = sample.getFormat();
        double[] audioSample = FileHelper.readAudioInputStream(sample);

        return mergeVocalSample(userKey, audioSample, format.getSampleRate());
    }

    /**
     * Calculates the distance between this vocal sample and the vocal prints previously extracted 
     * and returns the associated user keys of the 3 closest matches
     * <p>
     * Usage of a closed set is assumed : the speaker's vocal print was extracted before and is known to the system.
     * This means you'll always get results even if the speaker is absolutely unknown to the system.
     * Future versions of the framework will provide a level of likelihood in the response and implement some 
     * decision logic whether the match is worth mentioning at all.
     * </p>
     * @param vocalSample the vocal sample, values between -1.0 and 1.0
     * @param sampleRate the sample rate
     * @return a list of user keys that might match this vocal sample
     */
    public List<K> recognize(double[] vocalSample, float sampleRate) {

        VocalPrint vocalPrint = new VocalPrint(extractFeatures(vocalSample, sampleRate));
        
        DistanceCalculator calculator = new EuclideanDistanceCalculator();
        Map<Double, K> results = new TreeMap<Double, K>();

        for (Entry<K, VocalPrint> entry : store.entrySet()) {
            double distance = entry.getValue().getDistance(calculator, vocalPrint);
            results.put(distance, entry.getKey());
        }
        
        List<K> returnValue = new ArrayList<K>();
        int i = 0;
        for(Entry<Double, K> entry : results.entrySet()) {
            returnValue.add(entry.getValue());
            if(++i == 3) {
                break;
            }
        }
        return returnValue;
    }
  
    /**
     * Convenience method to recognize vocal samples from files.
     * <p>
     * See class description for details on files
     * </p>
     * @param vocalSampleFile the file containing the vocal sample
     * @return a list of user keys that might match this vocal sample
     * @throws UnsupportedAudioFileException when the JVM does not support the file format
     * @throws IOException when an I/O exception occurs
     * @see Recognito#recognize(double[], float)
     */
    public  List<K> recognize(File vocalSampleFile) 
            throws UnsupportedAudioFileException, IOException {
        
        AudioInputStream sample = AudioSystem.getAudioInputStream(vocalSampleFile);
        AudioFormat format = sample.getFormat();
        double[] audioSample = FileHelper.readAudioInputStream(sample);

        return recognize(audioSample, format.getSampleRate());
    }
  
    /**
     * Removes silence, applies normailzation and extracts vocal features from the given sample
     * @param vocalSample the vocal sample
     * @param sampleRate the sample rate
     * @return the extracted features
     */
    private double[] extractFeatures(double[] vocalSample, float sampleRate) {

        AutocorrellatedVoiceActivityDetector voiceDetector = new AutocorrellatedVoiceActivityDetector();
        Normalizer normalizer = new Normalizer();
        FeaturesExtractor<double[]> lpcExtractor = new LpcFeaturesExtractor(sampleRate, 20);

        voiceDetector.removeSilence(vocalSample, sampleRate);
        normalizer.normalize(vocalSample, sampleRate);
        double[] lpcFeatures = lpcExtractor.extractFeatures(vocalSample);

        return lpcFeatures;
    }
}
