import whisper
import sys

# Récupère le chemin du fichier audio depuis Java
audio_file = sys.argv[1]

# Charge le modèle Whisper (tiny = rapide et léger)
result = model.transcribe(audio_file, fp16=False)

# Transcrit
result = model.transcribe(audio_file)

# Affiche juste le texte — Java va le lire
print(result["text"])