

import cv2
import face_recognition
import numpy as np
import psycopg2
import json
import pandas as pd
import os

# 📌 Connect to the PostgreSQL Database
conn = psycopg2.connect(
    dbname="project",
    user="postgres",
    password="pratik115",
    host="localhost",
    port="5432"
)
cursor = conn.cursor()

# 📌 Get Student Details
roll_number = input("Enter student's Roll No.: ").strip()
student_name = input("Enter the student's full name: ").strip()

# 📌 Capture Face from Webcam
video_capture = cv2.VideoCapture(0)

if not video_capture.isOpened():
    print("Error: Could not open camera.")
    video_capture.release()
    exit()

print("📸 Capturing face... Look at the camera.")
face_encoding = None

while True:
    ret, frame = video_capture.read()
    if not ret:
        continue

    # Convert frame to RGB
    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

    # Detect face
    face_locations = face_recognition.face_locations(rgb_frame)
    face_encodings = face_recognition.face_encodings(rgb_frame, face_locations)

    if face_encodings:
        face_encoding = face_encodings[0]
        print("✅ Face captured!")
        break

    cv2.imshow("Capture Face", frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        print("❌ Face capture canceled.")
        exit()

video_capture.release()
cv2.destroyAllWindows()

# 📌 Store Encoded Face in Database
if face_encoding is not None:
    face_encoding_list = face_encoding.tolist()
    face_encoding_json = json.dumps(face_encoding_list)

    cursor.execute("INSERT INTO faces (roll_no, name, encoding) VALUES (%s, %s, %s)", 
                   (roll_number, student_name, face_encoding_json))
    conn.commit()
    print(f"✅ {student_name} (Roll No. {roll_number}) added to the database!")

# 📌 Update `students.xlsx`
students_file = "students.xlsx"

# Load existing student list or create a new one
if os.path.exists(students_file):
    df_students = pd.read_excel(students_file, engine="openpyxl")
else:
    df_students = pd.DataFrame(columns=["Roll No.", "Name"])

# Ensure no duplicate entries
if roll_number not in df_students["Roll No."].astype(str).values:
    new_student = pd.DataFrame({"Roll No.": [roll_number], "Name": [student_name]})
    df_students = pd.concat([df_students, new_student], ignore_index=True)

    # Save back to Excel
    df_students.to_excel(students_file, index=False)
    print(f"✅ {student_name} (Roll No. {roll_number}) added to {students_file}!")

cursor.close()
conn.close()
