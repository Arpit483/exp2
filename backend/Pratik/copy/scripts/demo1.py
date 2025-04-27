import cv2
import face_recognition
import numpy as np
import time
import psycopg2
import json
import pandas as pd
import os
from datetime import datetime


subject_name = input("Enter subject name: ").strip().replace(" ", "_")  
subject_filename = f"{subject_name}_attendance.xlsx"

#
if os.path.exists(subject_filename):
    df_students = pd.read_excel(subject_filename, engine="openpyxl")
else:
    students_file = "students.xlsx"
    df_students = pd.read_excel(students_file, engine="openpyxl")
    df_students.columns = df_students.columns.str.strip()

conn = psycopg2.connect(
    dbname="project",
    user="postgres",
    password="pratik115",
    host="localhost",
    port="5432"
)
cursor = conn.cursor()


known_faces = []
known_names = []

cursor.execute("SELECT name, encoding FROM faces")
rows = cursor.fetchall()
for name, encoding_str in rows:
    encoding = np.array(json.loads(encoding_str), dtype=np.float32)
    known_faces.append(encoding)
    known_names.append(name)

print(f"✅ Loaded {len(known_faces)} faces from database.")


video_capture = cv2.VideoCapture(0)
time.sleep(2)

if not video_capture.isOpened():
    print("Error: Could not open camera.")
    video_capture.release()
    exit()

present_students = set()

try:
    while True:
        ret, frame = video_capture.read()
        if not ret:
            continue  

        small_frame = cv2.resize(frame, (0, 0), fx=0.5, fy=0.5)  
        rgb_small_frame = cv2.cvtColor(small_frame, cv2.COLOR_BGR2RGB)
        
        face_locations = face_recognition.face_locations(rgb_small_frame)
        face_encodings = face_recognition.face_encodings(rgb_small_frame, face_locations) if face_locations else []

        for face_encoding, face_location in zip(face_encodings, face_locations):
            matches = face_recognition.compare_faces(known_faces, face_encoding, tolerance=0.5)
            name = "Unknown"

            face_distances = face_recognition.face_distance(known_faces, face_encoding)
            if len(face_distances) > 0:
                best_match_index = np.argmin(face_distances)
                if matches[best_match_index]:
                    name = known_names[best_match_index]
                    present_students.add(name)  # Store recognized student name

            # Draw rectangle around face
            top, right, bottom, left = [v * 2 for v in face_location]  # Scale back after resizing
            cv2.rectangle(frame, (left, top), (right, bottom), (0, 255, 0), 2)
            cv2.putText(frame, name, (left, top - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)

        cv2.imshow('Face Recognition', frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

except Exception as e:
    print(f"❌ Error: {e}")

finally:
    # 📌 Mark attendance for the subject
    date_column = datetime.now().strftime("%Y-%m-%d %H:%M:%S")  
    df_students[date_column] = df_students["Name"].apply(lambda x: "Present" if x in present_students else "Absent")

    # 📌 Save subject-wise attendance file
    df_students.to_excel(subject_filename, index=False)
    print(f"📤 Attendance data saved in {subject_filename}")

    # ✅ Close resources
    video_capture.release()
    cv2.destroyAllWindows()
    cursor.close()
    conn.close()
