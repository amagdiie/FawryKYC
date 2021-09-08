import cv2
import face_recognition
from os.path import join
from PIL import Image, ExifTags
import numpy as np
import math

def faceComparison(imgReal, imgDetect):

    encodeReal = checkImage(imgReal)

    encodeTest = checkImage(imgDetect)

    errorMSG = "error"

    if encodeReal == errorMSG or encodeTest == errorMSG:
        if encodeReal == errorMSG:
            return errorMSG+" in ID image"
        else:
            return errorMSG+" in Face image"
    else:
        results = face_recognition.compare_faces([encodeReal],encodeTest)
        faceDis = face_recognition.face_distance([encodeReal],encodeTest)
        accuracy = face_distance_to_conf(faceDis)
        print("accuracy: " + str((accuracy * 100)))
        return str(results)

def rotateImage(imagePath):

    img_path = imagePath
    pil_image = Image.open(img_path).convert("RGB")
    img_exif = pil_image.getexif()
    ret = {}
    orientation  = 0
    if img_exif:
        for tag, value in img_exif.items():
            decoded = ExifTags.TAGS.get(tag, tag)
            ret[decoded] = value
        orientation  = ret["Orientation"]

    # fix orientation
    # if 8, 90 degree
    # if 3, 180 degree
    # if 6, 270 degree
    if orientation == 8:
        pil_image = pil_image.rotate(90, Image.NEAREST, expand=1)
    elif orientation == 3:
        pil_image = pil_image.rotate(180, Image.NEAREST, expand=1)
    elif orientation == 6:
        pil_image = pil_image.rotate(270, Image.NEAREST, expand=1)

    return np.array(pil_image)

def face_distance_to_conf(face_distance, face_match_threshold=0.6):
    if face_distance > face_match_threshold:
        range = (1.0 - face_match_threshold)
        linear_val = (1.0 - face_distance) / (range * 2.0)
        return linear_val
    else:
        range = face_match_threshold
        linear_val = 1.0 - (face_distance / (range * 2.0))
        return linear_val + ((1.0 - linear_val) * math.pow((linear_val - 0.5) * 2, 0.2))

def checkImage(image):
    img = join(r"/storage/emulated/0/Android/data/fawry.kyc.lib/files/Pictures/", image)
    img = rotateImage(img)
    img = cv2.resize(img, (300,300), interpolation = cv2.INTER_AREA)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    faceLoc = face_recognition.face_locations(img)
    encodeReal = face_recognition.face_encodings(img)
    if (len(encodeReal) > 0):
        return encodeReal[0]
    else:
        return "error"


def openVideo(videoName, encodeImage):
    videoPath = join(r"/storage/emulated/0/Android/data/fawry.kyc.lib/files/Pictures/", videoName)
    vid_capture = cv2.VideoCapture(videoPath)
    check = 0

    if (vid_capture.isOpened() == False):
        print("Error opening the video file")
        # Read fps and frame count
    else:
        fps = vid_capture.get(5)
        print('Frames per second : ', fps,'FPS')
        frame_count = vid_capture.get(7)
        print('Frame count : ', frame_count)

    while(vid_capture.isOpened()):
        success, img = vid_capture.read()
        imgS = rotateImage(img)
        imgS = cv2.resize(imgS, (0,0), None, 0.25, 0.25)
        imgS = cv2.cvtColor(imgS, cv2.COLOR_BGR2RGB)

        faceCurFrame = face_recognition.face_locations(imgS)
        encodeCurFrame = face_recognition.face_encodings(imgS, faceCurFrame)

        for encodeFace, faceLoc in zip(encodeCurFrame, faceCurFrame):
            matches = face_recognition.compare_faces(encodeImage, encodeFace)
            faceDis = face_recognition.face_distance(encodeImage, encodeFace)
            print(faceDis)
            matchIndex = np.argmin(faceDis)

            if (matches[matchIndex] and (check < 5)):
                check = check + 1
            else:
                return str(match)









