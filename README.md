# AutoAttend: Manage Attendance Effortlessly

![AutoAttend Logo](https://github.com/Arpit483/exp2/blob/master/app/src/main/res/drawable/immigration.png)

## Overview

AutoAttend is an AI-powered attendance management system that uses facial recognition to automate and streamline the process of tracking student attendance. Traditional methods are time-consuming and error-prone, often allowing proxy entries and inaccurate records. AutoAttend ensures real-time detection, high accuracy, and secure data logging.

Developed by:
- Pratik Shinde ([@pratikshinde](https://github.com/pratikshinde))
- Arpit Deosthale ([@Arpit483](https://github.com/Arpit483))
- Aditya Kotewar
- Ajit Chavan

Under guidance of: Prof. Zarina Shaikh, Dr. D. Y. Patil Institute of Technology, Pune

## Features

- **Facial Recognition**: Automated attendance marking using real-time face detection
- **Multiple User Roles**: Separate interfaces for students, teachers, and club administrators
- **Club Attendance Management**: Special feature for tracking extracurricular participation
- **Secure Authentication**: Role-based access control system
- **Attendance Analytics**: Visual reports and statistics on attendance patterns
- **Request System**: Students can submit attendance requests for missed classes
- **Export Functionality**: Download attendance reports in Excel format

## Technology Stack

- **Backend**: Python, Flask, OpenCV, face_recognition
- **Database**: PostgreSQL, Firebase Realtime Database
- **Frontend**: Java Android application with Material UI
- **Data Processing**: pandas for Excel sheet management

## Installation & Setup

### Prerequisites
- Android Studio
- Python 3.7+ with pip
- PostgreSQL database
- Firebase account for authentication

### Android App Setup
1. Clone the repository:
```bash
git clone https://github.com/Arpit483/exp2.git
```

2. Open the project in Android Studio

3. Connect to Firebase:
   - Create a new Firebase project
   - Add your `google-services.json` to the app directory
   - Follow Firebase setup instructions for Android

4. Build and run the application on your device or emulator

### Backend Server Setup
1. Navigate to the server directory:
```bash
cd backend
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Configure database connection in `config.py`

4. Run the Flask server:
```bash
python app.py
```

## Usage Guide

### For Teachers
1. Sign in with your credentials
2. Navigate to the subject list
3. Select a subject to begin attendance
4. Activate the camera to start facial recognition
5. Students present in the frame will be automatically marked as present
6. Download reports or add new student faces as needed

### For Students
1. Sign in with your credentials
2. View your attendance statistics across all subjects
3. Submit attendance requests with proper documentation for missed classes
4. Track request status

### For Club Administrators
1. Sign in with your credentials
2. Review pending attendance requests
3. Approve or reject requests with comments
4. Track club participation statistics

## API Endpoints

The system provides the following API endpoints:
- `/camera-on` & `/camera-off` (POST)
- `/add-face` (POST)
- `/student-attendance/<roll_no>` (GET)
- `/excel-files` (GET)
- `/download/<filename>` (GET)
- `/mark-attendance-request` (POST)
- `/attendance-requests` (GET for faculty)
- `/attendance-requests/<roll_no>/<date>` (PATCH for approval/rejection)

## Screenshots

![Login Screen](screenshots/login.png)
![Teacher Dashboard](screenshots/teacher_dashboard.png)
![Face Recognition](screenshots/recognition.png)
![Student Dashboard](screenshots/student_view.png)

## Contributions

We welcome contributions to this project. Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Dr. D. Y. Patil Institute of Technology for supporting this project
- Our project guide Prof. Zarina Shaikh for her valuable guidance
- OpenCV and face_recognition libraries for providing the foundational tools

## Future Scope

- Mobile application for iOS
- Integration with institutional management systems
- Cloud-based storage for improved accessibility
- Advanced analytics and reporting features
