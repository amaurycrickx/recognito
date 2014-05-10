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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
 * {@code Recognito} holds a set of voice prints associated with user keys and allows 
 * execution of different tasks on them :
 * </p>
 * <ul>
 * <li>Create a voice print from an audio sample and store it with an associated user key</li>
 * <li>Merge a new voice sample into an existing voice print</li>
 * <li>Speaker recognition : analyse voice characteristics from an unknown sample and return a {@code List}
 * of {@code MatchResult}s sorted by distance. A likelihood ratio is provided within each {@code MatchResult}.</li>
 * </ul>
 * <p>
 * {@code Recognito} expects all voice samples to be comprised of a single channel (i.e. mono). Using a stereo sample 
 * whose channels are identical will merely double processing time. Using a real stereo sample will make 
 * the processing less accurate while doubling processing time.<br/>
 * </p>
 * <p>
 * It is up to the user to manage persistence of the created voice print objects. Persisted voice prints
 * may be passed into an alternate {@code Recognito} constructor as a {@code Map} of user keys pointing to a voice print.
 * </p>
 * <p>
 * For methods taking a file handle :<br/>
 * Supporting each and every file formats is a real pain and not the primary goal of Recognito. As such,
 * the conversion capabilities of the javax.sound.sampled package are used internally.
 * Depending on your particular JVM implementation, some file types may or may not be supported.
 * If you're looking for MP3 or Ogg support, check Javazoom SPI's. This said, the higher the sample quality, the better the results.
 * In case you may choose, {@code Recognito}'s preferred file format is PCM 16bit mono 16000 Hz (WAV files are PCM)<br/>
 * You may also want to check http://sox.sourceforge.net for dedicated conversion software.
 * </p>
 * <p>
 * Please note the sample rate is actually twice the highest audio frequency of the sample. E.g. a sample rate of 8KHz means
 * that the highest frequency available in the sample is 4KHz. So you can't resample at a higher frequency and expect 
 * the voice samples to be comparable, some frequencies will be missing. You may downsample, but if don't know how to do that 
 * correctly, you're better off using dedicated software. For the purpose of extracting voice prints, 
 * 16KHz appears to be the most interesting choice. 
 * </p>
 * <p>
 * The likelihood ratio available within the {@code MatchResult}s is calculated as the relative distance between the given voice sample, 
 * a known {@code VoicePrint} and a so called Universal Model. The universal model in {@code Recognito} is by default created as an average of all
 * {@code VoicePrint}s available in the system. The closer you are to the known {@code VoicePrint}, the higher the likelihood. 
 * Each time a new sample is sent to {@code Recognito}'s create or merge methods, the extracted features are added to the model. 
 * You may create your own model by merging a selected set of voice samples into a single {@code VoicePrint}. 
 * Once done, you may set this model once and for all in {@code Recognito}, it won't be updated afterwards.
 * A Universal Model is language dependent. At this very moment, it doesn't look realistic that {@code Recognito} would provide  
 * generic models for each and every language. Furthermore, the recording system you're using will also severely impact the relevance of a 
 * generic model in that the sonic characteristics will be quite different from one system to another. In other words, providing generic 
 * models would most probably create likelihood ratios that are unreasonably high because far from your own recordings and thus irrelevant.  
 * </p>
 * <p>
 * Threading : usage of {@code Recognito} is thread safe, see methods documentation for details
 * </p>
 * @param <K> {@code Recognito} is genericized in order to allow the user to specify its own type of user keys.
 * The constraints on user keys are the same as those for a {@code java.util.Map} key 
 * @author Amaury Crickx
 * @see {@link java.util.Map} for the constraints on Key objects
 */
public class Recognito<K> {
    
    private final ConcurrentHashMap<K, VoicePrint> store = new ConcurrentHashMap<K, VoicePrint>();

    private final AtomicBoolean universalModelWasSetByUser = new AtomicBoolean();
    private VoicePrint universalModel;
    
    /**
     * Default constructor
     */
    public Recognito() {
    }
    
    /**
     * Constructor taking previously extracted voice prints directly into the system
     */
    public Recognito(Map<K, VoicePrint> voicePrintsByUserKey) {
        Iterator<VoicePrint> it = voicePrintsByUserKey.values().iterator();
        if (it.hasNext()) {
            VoicePrint print = it.next();
            universalModel = new VoicePrint(print);            
            while (it.hasNext()) {
                universalModel.merge(it.next());
            }
        }
        store.putAll(voicePrintsByUserKey);
    }
    
    /**
     * Get the universal model
     * @return the universal model
     */
    public VoicePrint getUniversalModel() {
        return new VoicePrint(universalModel);
    }
    
    /**
     * Sets the universal model to be used to calculate likelihood ratios
     * Once set, further voice print create / merge operations won't modify this model
     * @param universalModel the universal model to set, may not be null
     */
    public synchronized void setUniversalModel(VoicePrint universalModel) {
        if(universalModel == null) {
            throw new IllegalArgumentException("The universal model may not be null");
        }
        this.universalModelWasSetByUser.set(false);
        this.universalModel = universalModel;
    }
    
    /**
     * Creates a voice print and stores it along with the user key for later comparison with new samples
     * <p>
     * Threading : this method is synchronized to prevent inadvertently erasing an existing user key
     * </p>
     * @param userKey the user key associated with this voice print
     * @param voiceSample the voice sample, values between -1.0 and 1.0
     * @param sampleRate the sample rate, at least 8000.0 Hz (preferably higher)
     * @return the voice print extracted from the given sample
     */
    public synchronized VoicePrint createVoicePrint(K userKey, double[] voiceSample, float sampleRate) {
        if(userKey == null) {
            throw new NullPointerException("The userKey is null");
        }
        if(store.containsKey(userKey)) {
            throw new IllegalArgumentException("The userKey already exists: [" + userKey + "]");
        }
        
        double[] features = extractFeatures(voiceSample, sampleRate);
        VoicePrint voicePrint = new VoicePrint(features);
         
        synchronized (this) {
            if (!universalModelWasSetByUser.get()) {
                if (universalModel == null) {
                    universalModel = new VoicePrint(voicePrint);
                } else {
                    universalModel.merge(features);
                }
            }
        }
        store.put(userKey, voicePrint);
        
        return voicePrint;
    }
    
    /**
     * Convenience method to load voice samples from files.
     * <p>
     * See class description for details on files
     * </p>
     * @param userKey the user key associated with this voice print
     * @param voiceSampleFile the file containing the voice sample
     * @return the voice print
     * @throws UnsupportedAudioFileException when the JVM does not support the file format
     * @throws IOException when an I/O exception occurs
     * @see Recognito#createVoicePrint(Object, double[], float)
     */
    public VoicePrint createVoicePrint(K userKey, File voiceSampleFile) 
            throws UnsupportedAudioFileException, IOException {
        
        AudioInputStream sample = AudioSystem.getAudioInputStream(voiceSampleFile);
        AudioFormat format = sample.getFormat();
        double[] audioSample = FileHelper.readAudioInputStream(sample);

        return createVoicePrint(userKey, audioSample, format.getSampleRate());
    }
    
    /**
     * Extracts voice features from the given voice sample and merges them with previous voice 
     * print extracted for this user key
     * <p>
     * Threading : it is safe to simultaneously add voice samples for a single userKey from multiple threads
     * </p>
     * @param userKey the user key associated with this voice print
     * @param voiceSample the voice sample to analyze, values between -1.0 and 1.0
     * @param sampleRate the sample rate, at least 8000.0 Hz (preferably higher)
     * @return the updated voice print
     */
    public VoicePrint mergeVoiceSample(K userKey, double[] voiceSample, float sampleRate) {
        
        if(userKey == null) {
            throw new NullPointerException("The userKey is null");
        }
        
        VoicePrint original = store.get(userKey);
        if(original == null) {
            throw new IllegalArgumentException("No voice print linked to this user key [" + userKey + "]");
        }

        double[] features = extractFeatures(voiceSample, sampleRate);
        synchronized (this) {
            if(!universalModelWasSetByUser.get()) {
                universalModel.merge(features);
            }
        }
        original.merge(features);
        
        return original;
    }
    
    /**
     * Convenience method to merge voice samples from files. 
     * <p>
     * See class description for details on files
     * </p>
     * @param userKey the user key associated with this voice print
     * @param voiceSampleFile the file containing the voice sample
     * @return the updated voice print
     * @throws UnsupportedAudioFileException when the JVM does not support the file format
     * @throws IOException when an I/O exception occurs
     * @see Recognito#mergeVoiceSample(Object, double[], float)
     */
    public VoicePrint mergeVoiceSample(K userKey, File voiceSampleFile) 
            throws UnsupportedAudioFileException, IOException {
        
        AudioInputStream sample = AudioSystem.getAudioInputStream(voiceSampleFile);
        AudioFormat format = sample.getFormat();
        double[] audioSample = FileHelper.readAudioInputStream(sample);

        return mergeVoiceSample(userKey, audioSample, format.getSampleRate());
    }

    /**
     * Calculates the distance between this voice sample and the voice prints previously extracted 
     * and returns the closest matches sorted by distance
     * <p>
     * Usage of a closed set is assumed : the speaker's voice print was extracted before and is known to the system.
     * This means you'll always get MatchResults even if the speaker is absolutely unknown to the system.
     * The MatchResult class provides a likelihood ratio in order to help determining the usefulness of the result
     * </p>
     * @param voiceSample the voice sample, values between -1.0 and 1.0
     * @param sampleRate the sample rate
     * @return a list MatchResults sorted by distance
     */
    public List<MatchResult<K>> identify(double[] voiceSample, float sampleRate) {
        
        if(store.isEmpty()) {
            throw new IllegalStateException("There is no voice print enrolled in the system yet");
        }

        VoicePrint voicePrint = new VoicePrint(extractFeatures(voiceSample, sampleRate));
        
        DistanceCalculator calculator = new EuclideanDistanceCalculator();
        List<MatchResult<K>> matches = new ArrayList<MatchResult<K>>(store.size());

        double distanceFromUniversalModel = voicePrint.getDistance(calculator, universalModel);
        for (Entry<K, VoicePrint> entry : store.entrySet()) {
            double distance = entry.getValue().getDistance(calculator, voicePrint);
            // likelihood : how close is the given voice sample to the current VoicePrint 
            // compared to the total distance between the current VoicePrint and the universal model 
            int likelihood = 100 - (int) (distance / (distance + distanceFromUniversalModel) * 100);
            matches.add(new MatchResult<K>(entry.getKey(), likelihood, distance));
        }

        Collections.sort(matches, new Comparator<MatchResult<K>>() {
            @Override
            public int compare(MatchResult<K> m1, MatchResult<K> m2) {
                return Double.compare(m1.getDistance(), m2.getDistance());
            }
        });
        
        return matches;
    }
  
    /**
     * Convenience method to identify voice samples from files.
     * <p>
     * See class description for details on files
     * </p>
     * @param voiceSampleFile the file containing the voice sample
     * @return a list MatchResults sorted by distance
     * @throws UnsupportedAudioFileException when the JVM does not support the audio file format
     * @throws IOException when an I/O exception occurs
     * @see Recognito#identify(double[], float)
     */
    public  List<MatchResult<K>> identify(File voiceSampleFile) 
            throws UnsupportedAudioFileException, IOException {
        
        AudioInputStream sample = AudioSystem.getAudioInputStream(voiceSampleFile);
        AudioFormat format = sample.getFormat();
        double[] audioSample = FileHelper.readAudioInputStream(sample);

        return identify(audioSample, format.getSampleRate());
    }
  
    /**
     * Removes silence, applies normalization and extracts voice features from the given sample
     * @param voiceSample the voice sample
     * @param sampleRate the sample rate
     * @return the extracted features
     */
    private double[] extractFeatures(double[] voiceSample, float sampleRate) {

        AutocorrellatedVoiceActivityDetector voiceDetector = new AutocorrellatedVoiceActivityDetector();
        Normalizer normalizer = new Normalizer();
        FeaturesExtractor<double[]> lpcExtractor = new LpcFeaturesExtractor(sampleRate, 20);

        voiceDetector.removeSilence(voiceSample, sampleRate);
        normalizer.normalize(voiceSample, sampleRate);
        double[] lpcFeatures = lpcExtractor.extractFeatures(voiceSample);

        return lpcFeatures;
    }
}
