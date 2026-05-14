package tn.esprit.aura.services;

import javax.sound.sampled.*;
import java.io.*;

/**
 * VoiceAnalyzer — Records microphone audio and detects the user's mood.
 * Migrated from the Aura AI-core module (Service.VoiceAnalyzer).
 */
public class VoiceAnalyzer {

    private static final String AUDIO_FILE = "voice_record.wav";
    private double lastEnergy = 0;
    private double lastPitch  = 0;

    public double getLastEnergy() { return lastEnergy; }
    public double getLastPitch()  { return lastPitch; }

    public String analyzeVoiceMood() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

            TargetDataLine microphone = null;
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
                if (mixerInfo.getName().contains("Realtek")) {
                    try {
                        Mixer mixer = AudioSystem.getMixer(mixerInfo);
                        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                        if (mixer.isLineSupported(info)) {
                            microphone = (TargetDataLine) mixer.getLine(info);
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }
            if (microphone == null) {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                microphone = (TargetDataLine) AudioSystem.getLine(info);
            }

            microphone.open(format);
            microphone.start();
            System.out.println("Parle maintenant... (5 secondes)");

            byte[] buffer = new byte[44100 * 2 * 5];
            microphone.read(buffer, 0, buffer.length);
            microphone.stop();
            microphone.close();

            saveToWav(buffer, format);

            double energy = calculateEnergy(buffer);
            double pitch  = calculatePitch(buffer, 44100);
            this.lastEnergy = energy;
            this.lastPitch  = pitch;

            return detectMood(energy, pitch);

        } catch (Exception e) {
            System.out.println("Erreur VoiceAnalyzer : " + e.getMessage());
            return "calme";
        }
    }

    public double calculateEnergy(byte[] buffer) {
        double sum = 0;
        for (int i = 0; i < buffer.length - 1; i += 2) {
            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
            sum += sample * sample;
        }
        return Math.sqrt(sum / (buffer.length / 2));
    }

    public String getAudioFilePath() { return AUDIO_FILE; }

    private void saveToWav(byte[] buffer, AudioFormat format) throws IOException {
        AudioInputStream audioStream = new AudioInputStream(
                new ByteArrayInputStream(buffer), format,
                buffer.length / format.getFrameSize()
        );
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, new File(AUDIO_FILE));
    }

    private double calculatePitch(byte[] buffer, float sampleRate) {
        int windowSize = 1024;
        double totalPitch = 0;
        int count = 0;
        for (int i = 0; i < buffer.length - windowSize * 2; i += windowSize * 2) {
            double[] samples = new double[windowSize];
            for (int j = 0; j < windowSize; j++) {
                short sample = (short) ((buffer[i + j * 2 + 1] << 8) | (buffer[i + j * 2] & 0xFF));
                samples[j] = sample / 32768.0;
            }
            double pitch = estimatePitch(samples, sampleRate);
            if (pitch > 50 && pitch < 500) { totalPitch += pitch; count++; }
        }
        return count > 0 ? totalPitch / count : 150;
    }

    private double estimatePitch(double[] samples, float sampleRate) {
        int minLag = (int) (sampleRate / 500);
        int maxLag = (int) (sampleRate / 50);
        double maxCorr = 0;
        int bestLag = minLag;
        for (int lag = minLag; lag < maxLag && lag < samples.length; lag++) {
            double corr = 0;
            for (int i = 0; i < samples.length - lag; i++) corr += samples[i] * samples[i + lag];
            if (corr > maxCorr) { maxCorr = corr; bestLag = lag; }
        }
        return sampleRate / bestLag;
    }

    private String detectMood(double energy, double pitch) {
        if      (energy < 300)                       return "fatigué";
        else if (energy > 700 && pitch > 220)        return "stressé";
        else if (energy > 900)                       return "énergisé";
        else if (pitch  < 180 && energy > 400)       return "focalisé";
        else                                         return "calme";
    }
}
