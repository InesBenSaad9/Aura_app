package Interface;

public interface Analyzable {
    String analyzeVoiceMood();
    double calculateEnergy(byte[] buffer);
    String getAudioFilePath();
}