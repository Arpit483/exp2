

import os 
import cv2
import json
import time
import numpy as np
import pandas as pd
import psycopg2
import face_recognition
from datetime import datetime
import globals  # Import the shared global state

def recognize_and_mark_attendance(subject_name):
    subject_filename = f"{subject_name}_attendance.xlsx"

    # 📌 Load existing attendance sheet or create a new one from "students.xlsx"
    if os.path.exists(subject_filename):
        df_students = pd.read_excel(subject_filename, engine="openpyxl")
    else:
        students_file = "students.xlsx"
        df_students = pd.read_excel(students_file, engine="openpyxl")
        df_students.columns = df_students.columns.str.strip()

    # 📌 Connect to PostgreSQL Database
    conn = psycopg2.connect(
        dbname="project",
        user="postgres",
        password="pratik115",
        host="localhost",
        port="5432"
    )
    cursor = conn.cursor()

    # 📌 Load stored face encodings
    known_faces = []
    known_names = []
    cursor.execute("SELECT name, encoding FROM faces")
    rows = cursor.fetchall()

    for name, encoding_str in rows:
        encoding = np.array(json.loads(encoding_str), dtype=np.float32)
        known_faces.append(encoding)
        known_names.append(name)

    print(f"✅ Loaded {len(known_faces)} faces from database.")

    # 📌 Start Camera for Face Recognition
    video_capture = cv2.VideoCapture(0)
    time.sleep(2)

    if not video_capture.isOpened():
        return {"error": "Could not open camera"}

    present_students = set()

    try:
        while globals.camera_active:  # ✅ Read shared variable from globals.py
            ret, frame = video_capture.read()
            if not ret:
                continue  

            # 📌 Resize frame for faster processing
            small_frame = cv2.resize(frame, (0, 0), fx=0.5, fy=0.5)
            rgb_small_frame = cv2.cvtColor(small_frame, cv2.COLOR_BGR2RGB)

            # 📌 Detect faces
            face_locations = face_recognition.face_locations(rgb_small_frame)
            face_encodings = face_recognition.face_encodings(rgb_small_frame, face_locations)

            for face_encoding, face_location in zip(face_encodings, face_locations):
                matches = face_recognition.compare_faces(known_faces, face_encoding, tolerance=0.5)
                name = "Unknown"

                face_distances = face_recognition.face_distance(known_faces, face_encoding)
                if len(face_distances) > 0:
                    best_match_index = np.argmin(face_distances)
                    if matches[best_match_index]:
                        name = known_names[best_match_index]
                        present_students.add(name)  # ✅ Store recognized students

                # 📌 Draw rectangle & label on detected face
                top, right, bottom, left = [v * 2 for v in face_location]  # Scale back after resizing
                cv2.rectangle(frame, (left, top), (right, bottom), (0, 255, 0), 2)
                cv2.putText(frame, name, (left, top - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)

            # 📌 Show live video with recognized faces
            cv2.imshow('Face Recognition', frame)

            if cv2.waitKey(1) & 0xFF == ord('q'):  # Press 'q' to stop manually
                break

    except Exception as e:
        return {"error": str(e)}

    finally:
        # 📌 Mark attendance for detected students
        date_column = datetime.now().strftime("%Y-%m-%d")
        df_students[date_column] = df_students["Name"].apply(lambda x: "Present" if x in present_students else "Absent")

        # 📌 Save subject-wise attendance file
        df_students.to_excel(subject_filename, index=False)
        print(f"📤 Attendance data saved in {subject_filename}")

        # ✅ Release camera & close resources
        video_capture.release()
        cv2.destroyAllWindows()
        cursor.close()
        conn.close()

    return {"recognized_students": list(present_students)}


