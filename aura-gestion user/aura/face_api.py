from flask import Flask, request, jsonify
from transformers import AutoImageProcessor, AutoModel
from PIL import Image
import io
import torch

app = Flask(__name__)

# Load the model once when the server starts
print("⏳ Downloading and loading the AI model... (This may take a minute the first time)")
processor = AutoImageProcessor.from_pretrained('facebook/dinov2-small')
model = AutoModel.from_pretrained('facebook/dinov2-small')
print("✅ Model loaded successfully! Server is ready.")

@app.route('/api/embeddings', methods=['POST'])
def get_embeddings():
    try:
        # Read the raw image bytes sent from Java
        image = Image.open(io.BytesIO(request.data)).convert('RGB')
        
        # Process image and get features
        inputs = processor(images=image, return_tensors="pt")
        with torch.no_grad():
            outputs = model(**inputs)
        
        # Extract the global image feature vector (CLS token)
        # Convert it to a standard Python list
        embedding = outputs.last_hidden_state[0][0].numpy().tolist()
        
        # Wrap in a double array to match the old Hugging Face format: [[...]]
        return jsonify([[embedding]])
        
    except Exception as e:
        print(f"Error processing image: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    print("🚀 Starting Local AI Server on http://localhost:5050")
    app.run(port=5050, debug=False)
