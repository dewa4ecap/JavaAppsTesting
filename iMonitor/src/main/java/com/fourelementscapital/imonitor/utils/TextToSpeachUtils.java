package com.fourelementscapital.imonitor.utils;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Locale;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextToSpeachUtils {

	private static SynthesizerModeDesc synthesizerModeDesc;
	private static Synthesizer synthesizer;
	private static final Logger log = LogManager.getLogger(TextToSpeachUtils.class.getName());

	public static synchronized void speak(String speakText) {
		try {
			if (synthesizerModeDesc == null) {
				init();
			}
			synthesizer.speakPlainText(speakText, null);
			synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
		} catch (EngineStateError | Exception e) {
			log.error(Level.ERROR, e);
		}
	}

	public static void init() throws EngineException, AudioException, EngineStateError, PropertyVetoException, IOException, Exception {
		System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		synthesizerModeDesc = new SynthesizerModeDesc(Locale.getDefault());

		Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
		synthesizer = Central.createSynthesizer(synthesizerModeDesc);
		if (synthesizer == null) {
			log.log(Level.ERROR, "JSAPI Synthesizer cannot be constructed");
		}
		synthesizer.allocate();
		synthesizer.resume();
		Voice voice = new Voice("kevin16", Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, null);
		synthesizer.getSynthesizerProperties().setVoice(voice);
	}

	public static void deallocate() {
		try {
			synthesizer.waitEngineState(Synthesizer.DEALLOCATING_RESOURCES | Synthesizer.DEALLOCATED);
			synthesizer.deallocate();
			synthesizerModeDesc = null;
		} catch (IllegalArgumentException | InterruptedException | EngineException | EngineStateError e) {
			log.error(Level.ERROR, e);
		}
	}

}
