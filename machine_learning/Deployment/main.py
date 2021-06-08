from flask import Flask, render_template, request
import uuid
import tensorflow as tf
import numpy as np
import cv2
import os
import pyrebase
import json

firebaseConfig = {
    "apiKey": "<firebaseapiKey>",
    "authDomain": "rust-detector.firebaseapp.com",
    "databaseURL": "https://rust-detector-default-rtdb.asia-southeast1.firebasedatabase.app",
    "projectId": "rust-detector",
    "storageBucket": "rust-detector",
    "messagingSenderId": "567669336494",
    "appId": "1:567669336494:web:7e4942b1182c25e40bc03f",
    "measurementId": "G-HBR6ZEGPCR",
    "serviceAccount": "serviceAccountKey.json"
}

firebase = pyrebase.initialize_app(firebaseConfig)
storage = firebase.storage()

# Authentication
auth = firebase.auth()
# Credentials
email = "bangkitcapstone@gmail.com"
password = "bangkit1234"

# Sign in
user = auth.sign_in_with_email_and_password(email, password)


# Running flask app
app = Flask(__name__, template_folder='./templates')

@app.route("/")
def index():
  return render_template('./index.html')

@app.route("/predict", methods=['GET','POST'])
def predict():
  imagefile= request.files['imagefile']
  image_path = os.path.join("./images", imagefile.filename)
  imagefile.save(image_path)
  
  model = tf.keras.models.load_model('corrosion_segment_unet.hdf5')

  test_tensor = np.zeros((1, 256, 256, 3), dtype='float')

  test = cv2.imread(image_path)
  ori_image = test

  height, width, _ = test.shape
  test = cv2.resize(test, (256, 256))
  test = test / 255.0
  test = test.astype(np.float32)
  test = np.expand_dims(test, axis=0)
  test_tensor = test
    
  prediction = model.predict(test_tensor)[0]
  prediction = np.concatenate(
    [
        prediction,
        prediction,
        prediction
    ], axis=2)
  prediction = prediction * 255
  prediction = prediction.astype(np.float32)
  prediction = cv2.resize(prediction, (width, height))

  ori_image = ori_image.astype(np.float32)

  alpha = 0.8
  cv2.addWeighted(prediction, alpha, ori_image, 1-alpha, 0, ori_image)
  name = image_path.split("/")[-1]
  save_path = os.path.join("./save_images", name)
  cv2.imwrite(save_path, ori_image)

  # firebase path destination
  firebaseFolder = "images"
  # Unique file name
  randomFileName = str(uuid.uuid1())
  extensionFile = "jpg"
  path_on_cloud = firebaseFolder + "/" + randomFileName + "." + extensionFile

  # upload image to firebase
  storage.child(path_on_cloud).put(save_path)

  # Get image url
  url = storage.child(path_on_cloud).get_url(user['idToken'])
  
  prediction_url = url

  # a Python object (dict):
  json_obj = {
    "url": prediction_url
  }

  return json.dumps(json_obj)

if __name__ == '__main__':
    app.run()