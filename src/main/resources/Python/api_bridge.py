import sys
import os
import numpy as np
from tensorflow.keras.models import load_model

# --- PATH FIX ---
# This finds the folder where THIS script is actually located
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# This builds the full path to your model file
MODEL_PATH = os.path.join(BASE_DIR, 'lay_htu_model.h5')

# Load model using the full path
# We do this outside the function so it only loads ONCE when the app starts
try:
    model = load_model(MODEL_PATH)
except Exception as e:
    print(f"Error loading model at {MODEL_PATH}: {e}")
    sys.exit(1)

def predict(data_string):
    try:
        # Convert comma-separated string from Java to numpy array
        # Reshaping to (1, 24, 1) for your LSTM timesteps
        input_data = np.array([float(x) for x in data_string.split(',')]).reshape(1, 24, 1)

        prediction = model.predict(input_data, verbose=0)

        # This is what your Java 'BufferedReader' will catch
        print(prediction[0][0])
    except Exception as e:
        print(f"Prediction Error: {e}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        predict(sys.argv[1])
    else:
        print("No data provided from Java!")