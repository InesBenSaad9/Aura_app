import whisper
import sys

audio_file = sys.argv[1]
model = whisper.load_model("tiny")
result = model.transcribe(audio_file, fp16=False, language="fr")
print(result["text"])