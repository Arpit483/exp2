

from flask import Flask, request, jsonify
from addFaces import add_new_face
from demo1 import recognize_and_mark_attendance
from flask_cors import CORS
import threading
import time
import globals  
import os
from flask import send_from_directory
from datetime import datetime


app = Flask(__name__)
CORS(app)


camera_thread_instance = None


def camera_thread(subject_name):
    globals.camera_active = True  
    while globals.camera_active:
        result = recognize_and_mark_attendance(subject_name)
        print(f"Attendance Updated: {result}")  
        time.sleep(2)  


@app.route("/add-face", methods=["POST"])
def api_add_face():
    data = request.json
    roll_no = data.get("roll_no")
    name = data.get("name")

    if not roll_no or not name:
        return jsonify({"error": "Roll number and name are required"}), 400

    result = add_new_face(roll_no, name)
    return jsonify(result)


@app.route("/camera-on", methods=["POST"])
def camera_on():
    global camera_thread_instance
    subject_name = request.json.get("subject")

    if not subject_name:
        return jsonify({"error": "Subject name is required"}), 400

    if globals.camera_active:
        return jsonify({"message": "Camera is already running."})

    globals.camera_active = True
    
    camera_thread_instance = threading.Thread(target=camera_thread, args=(subject_name,))
    camera_thread_instance.start()

    return jsonify({"message": "Camera started and face detection began."})


@app.route("/camera-off", methods=["POST"])
def camera_off():
    global camera_thread_instance

    if not globals.camera_active:
        return jsonify({"message": "Camera is not running."})

    globals.camera_active = False  

    if camera_thread_instance:
        camera_thread_instance.join()  

    return jsonify({"message": "Camera stopped and attendance updated."})


@app.route("/excel-files", methods=["GET"])
def list_excel_files():
    files = [f for f in os.listdir() if f.endswith(".xlsx") and f != "students.xlsx"]
    return jsonify({"files": files})



@app.route("/download/<filename>", methods=["GET"])
def download_excel_file(filename):
    if not filename.endswith(".xlsx"):
        return jsonify({"error": "Invalid file type"}), 400

    directory = os.getcwd()
    if not os.path.exists(os.path.join(directory, filename)):
        return jsonify({"error": "File not found"}), 404

    return send_from_directory(directory, filename, as_attachment=True)



import pandas as pd

@app.route("/student-attendance/<roll_no>", methods=["GET"])
def student_attendance(roll_no):
    attendance_data = []
    directory = os.getcwd()

    for file in os.listdir(directory):
        if file.endswith(".xlsx") and file != "students.xlsx":
            try:
                df = pd.read_excel(os.path.join(directory, file))
                
                if "Roll No." in df.columns and roll_no in df["Roll No."].astype(str).values:
                    student_row = df[df["Roll No."].astype(str) == str(roll_no)]
                    
                    present_count = (student_row == "Present").sum(axis=1).values[0]
                    total_classes = student_row.iloc[:, 2:].shape[1]  # excluding Roll No. and Name
                    attendance_percent = (present_count / total_classes) * 100 if total_classes else 0

                    attendance_data.append({
                        "subject": file.replace(".xlsx", ""),
                        "present_days": int(present_count),
                        "total_classes": total_classes,
                        "attendance_percent": round(attendance_percent, 2)
                    })

            except Exception as e:
                print(f"Error reading {file}: {e}")
                continue

    if not attendance_data:
        return jsonify({"message": "No attendance data found for the given roll number."}), 404

    return jsonify({"roll_no": roll_no, "attendance_summary": attendance_data})





import json
from flask import Flask, request, jsonify
import os
import json
from werkzeug.utils import secure_filename
from datetime import datetime

UPLOAD_FOLDER = "uploads"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route("/mark-attendance-request", methods=["POST"])
def request_attendance():
    roll_no = request.form.get("roll_no")
    name = request.form.get("name")
    subject = request.form.get("subject")
    date = request.form.get("date")
    reason = request.form.get("reason")
    letter_file = request.files.get("letter")

    required_fields = [roll_no, name, subject, date, reason]
    if not all(required_fields):
        return jsonify({"error": "Missing required fields"}), 400
    
    # try:
    #     parsed_date = datetime.strptime(date, "%Y-%m-%d")
    #     date = parsed_date.strftime("%d-%m-%Y")
    # except ValueError:
    #     return jsonify({"error": "Invalid date format. Please use DD-MM-YYYY."}), 400

    # Save the uploaded file
    letter_filename = None
    if letter_file:
        filename = secure_filename(f"{roll_no}_{subject}_{date}_{letter_file.filename}")
        letter_path = os.path.join(UPLOAD_FOLDER, filename)
        letter_file.save(letter_path)
        letter_filename = letter_path
    else:
        return jsonify({"error": "Verification letter image is required"}), 400

    # Store request data
    request_data = {
        "roll_no": roll_no,
        "name": name,
        "subject": subject,
        "date": date,
        "reason": reason,
        "status": "Pending",
        "letter_path": letter_filename
    }

    request_file = "attendance_requests.json"
    try:
        if os.path.exists(request_file):
            with open(request_file, "r") as f:
                existing_data = json.load(f)
        else:
            existing_data = []

        existing_data.append(request_data)

        with open(request_file, "w") as f:
            json.dump(existing_data, f, indent=4)

        return jsonify({"message": "Attendance request with letter submitted successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500



@app.route('/uploads/<filename>', methods=['GET'])  ## For HOD to view letters
def get_uploaded_file(filename):
    filename = secure_filename(filename)
    return send_from_directory(UPLOAD_FOLDER, filename)


@app.route("/attendance-requests", methods=["GET"])
def get_attendance_requests():
    request_file = "attendance_requests.json"
    
    if not os.path.exists(request_file):
        return jsonify({"message": "No attendance requests found"}), 404

    try:
        with open(request_file, "r") as f:
            data = json.load(f)

        # Update each record to include letter download URL
        for req in data:
            if req.get("letter_path"):
                filename = os.path.basename(req["letter_path"])
                req["letter_url"] = request.host_url + "uploads/" + filename

        return jsonify(data), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500
    

    
import pandas as pd
from datetime import datetime
import pandas as pd
from datetime import datetime

@app.route("/attendance-requests/<roll_no>/<date>", methods=["PATCH"])
def update_attendance_request_status(roll_no, date):
    status = request.json.get("status")
    request_file = "attendance_requests.json"

    if status not in ["Approved", "Rejected"]:
        return jsonify({"error": "Invalid status. Must be 'Approved' or 'Rejected'."}), 400

    if not os.path.exists(request_file):
        return jsonify({"error": "No attendance requests found."}), 404

    try:
        with open(request_file, "r") as f:
            requests = json.load(f)

        updated = False
        for req in requests:
            if req["roll_no"] == roll_no and req["date"] == date:
                req["status"] = status
                updated = True

            
                if status == "Approved":
                    subject_file = req["subject"] + "_attendance.xlsx"
                    if os.path.exists(subject_file):
                        df = pd.read_excel(subject_file)

                        # Ensure the date format is matched in column headers (YYYY-MM-DD)
                        formatted_date = pd.to_datetime(date).strftime("%Y-%m-%d")  # Convert to "YYYY-MM-DD" format
                        
                        # Check if the column exists and update the attendance for the given roll_no
                        if "Roll No." in df.columns and formatted_date in df.columns:
                            df.loc[df["Roll No."].astype(str) == str(roll_no), formatted_date] = "Present"
                            df.to_excel(subject_file, index=False)
                        else:
                            return jsonify({"error": f"Roll No. or {formatted_date} column not found in Excel"}), 400
                    else:
                        return jsonify({"error": "Subject Excel file not found"}), 404

                break

        if not updated:
            return jsonify({"error": "Attendance request not found."}), 404

        with open(request_file, "w") as f:
            json.dump(requests, f, indent=4)

        return jsonify({"message": f"Request status updated to '{status}'"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# @app.route("/attendance-requests/<roll_no>/<date>", methods=["PATCH"])
# def update_attendance_request_status(roll_no, date):
#     status = request.json.get("status")
#     request_file = "attendance_requests.json"

#     if status not in ["Approved", "Rejected"]:
#         return jsonify({"error": "Invalid status. Must be 'Approved' or 'Rejected'."}), 400

#     if not os.path.exists(request_file):
#         return jsonify({"error": "No attendance requests found."}), 404

#     try:
#         with open(request_file, "r") as f:
#             requests = json.load(f)

#         updated = False
#         for req in requests:
#             if req["roll_no"] == roll_no and req["date"] == date:
#                 req["status"] = status
#                 updated = True

#                 if status == "Approved":
#                     subject_file = req["subject"] + "_attendance.xlsx"
#                     if os.path.exists(subject_file):
#                         df = pd.read_excel(subject_file)

#                         # Ensure the date format is matched in column headers (YYYY-MM-DD)
#                         formatted_date = pd.to_datetime(date).strftime("%Y-%m-%d")  # Convert to "YYYY-MM-DD" format

#                         # Search for the column that matches the date (only the date part, no session number)
#                         column_found = False
#                         for column in df.columns:
#                             if column.startswith(formatted_date):  # Check if column matches the date
#                                 column_found = True
#                                 break

#                         if column_found:
#                             # Update attendance for the given roll_no for the matched date
#                             if "Roll No." in df.columns:
#                                 df.loc[df["Roll No."].astype(str) == str(roll_no), column] = "Present"
#                                 df.to_excel(subject_file, index=False)
#                             else:
#                                 return jsonify({"error": "Roll No. column not found in Excel"}), 400
#                         else:
#                             return jsonify({"error": f"No column found for {formatted_date}"}), 400
#                     else:
#                         return jsonify({"error": "Subject Excel file not found"}), 404

#                 break

#         if not updated:
#             return jsonify({"error": "Attendance request not found."}), 404

#         with open(request_file, "w") as f:
#             json.dump(requests, f, indent=4)

#         return jsonify({"message": f"Request status updated to '{status}'"}), 200

#     except Exception as e:
#         return jsonify({"error": str(e)}), 500






if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)






















