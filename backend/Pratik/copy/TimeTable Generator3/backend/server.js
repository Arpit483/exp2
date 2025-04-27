

import express from "express";
import cors from "cors";
import pg from "pg";
import bodyParser from "body-parser";

let app = express();
app.use(cors());
app.use(bodyParser.json());
let port = 5000;

const db = new pg.Client({
  user: "postgres",
  host: "localhost",
  database: "TimeTable",
  password: "pratik115",
  port: 5432,
});

db.connect();

app.post("/timetable", async (req, res) => {
  const {
    subject_name,
    professor_name,
    classroom_number,
    day,
    division,
    start_time,
    end_time,
    start,
    till,
    batchDetails = {}, // New addition for batch-wise data
  } = req.body;

  try {
    let result;
    if (subject_name === "practical") {
      result = await db.query(
        `INSERT INTO timetable (subject_name, professor_name, classroom_number, day, division, start_time, end_time, start, till, 
          batch1_professor, batch1_classroom, batch1_practical_subject, batch2_professor, batch2_classroom, batch2_practical_subject, batch3_professor, batch3_classroom, batch3_practical_subject)
          VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18) RETURNING *`,
        [
          subject_name,
          professor_name,
          classroom_number,
          day,
          division,
          start_time,
          end_time,
          start,
          till,
          batchDetails.batch1_professor,
          batchDetails.batch1_classroom,
          batchDetails.batch1_practical_subject,
          batchDetails.batch2_professor,
          batchDetails.batch2_classroom,
          batchDetails.batch2_practical_subject,
          batchDetails.batch3_professor,
          batchDetails.batch3_classroom,
          batchDetails.batch3_practical_subject,
        ]
      );
    } else {
      result = await db.query(
        `INSERT INTO timetable (subject_name, professor_name, classroom_number, day, division, start_time, end_time, start, till)
          VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) RETURNING *`,
        [
          subject_name,
          professor_name,
          classroom_number,
          day,
          division,
          start_time,
          end_time,
          start,
          till,
        ]
      );
    }

    res.json({ data: result.rows[0] });
  } catch (error) {
    console.log(error);
    res.status(500).send("Error inserting timetable entry");
  }
});



app.post("/timetable", async (req, res) => {
  const {
    subject_name,
    professor_name,
    classroom_number,
    day,
    start_time,
    end_time,
    division,
    batchDetails = {},
  } = req.body;

  try {
    // Check for existing overlapping schedules (professor or classroom)
    const conflictCheck = await db.query(
      `SELECT * FROM timetable 
       WHERE day = $1 AND (
         (professor_name = $2 AND ((start_time, end_time) OVERLAPS ($3, $4))) OR
         (classroom_number = $5 AND ((start_time, end_time) OVERLAPS ($3, $4)))
       )`,
      [day, professor_name, start_time, end_time, classroom_number]
    );

    if (conflictCheck.rows.length > 0) {
      return res.status(400).json({
        message: "⚠️ Conflict detected!",
        conflict: conflictCheck.rows,
      });
    }

    // Proceed with insertion if no conflict
    const result = await db.query(
      `INSERT INTO timetable (subject_name, professor_name, classroom_number, day, division, start_time, end_time)
       VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
      [subject_name, professor_name, classroom_number, day, division, start_time, end_time]
    );

    res.json({ data: result.rows[0] });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "❌ Error inserting timetable entry" });
  }
});






app.put("/timetable/:id", async (req, res) => {
  const { id } = req.params;
  const updates = req.body;

  try {
    // Fetch existing entry from DB
    const existingEntry = await db.query("SELECT * FROM timetable WHERE id = $1", [id]);

    if (!existingEntry.rows.length) {
      return res.status(404).json({ message: "Timetable entry not found" });
    }

    const oldEntry = existingEntry.rows[0];

    // Ensure existing values are preserved if new ones are missing
    const updatedEntry = {
      subject_name: updates.subject_name ?? oldEntry.subject_name,
      division: updates.division ?? oldEntry.division,
      day: updates.day ?? oldEntry.day,
      start_time: updates.start_time ?? oldEntry.start_time,
      end_time: updates.end_time ?? oldEntry.end_time,
      professor_name : updates. professor_name  ?? oldEntry. professor_name ,
      classroom_number : updates. classroom_number  ?? oldEntry. classroom_number ,
      batch1_practical_subject: updates.batch1_practical_subject ?? oldEntry.batch1_practical_subject,
      batch2_practical_subject: updates.batch2_practical_subject ?? oldEntry.batch2_practical_subject,
      batch3_practical_subject: updates.batch3_practical_subject ?? oldEntry.batch3_practical_subject,
      batch1_professor: updates.batch1_professor ?? oldEntry.batch1_professor,
      batch2_professor: updates.batch2_professor ?? oldEntry.batch2_professor,
      batch3_professor: updates.batch3_professor ?? oldEntry.batch3_professor,
      batch1_classroom: updates.batch1_classroom ?? oldEntry.batch1_classroom,
      batch2_classroom: updates.batch2_classroom ?? oldEntry.batch2_classroom,
      batch3_classroom: updates.batch3_classroom ?? oldEntry.batch3_classroom,
    };

    // Update DB
    await db.query(
      `UPDATE timetable SET 
    subject_name = $1, division = $2, day = $3, start_time = $4, end_time = $5, professor_name = $6, classroom_number = $7,
    batch1_practical_subject = $8, batch2_practical_subject = $9, batch3_practical_subject = $10,
    batch1_professor = $11, batch2_professor = $12, batch3_professor = $13,
    batch1_classroom = $14, batch2_classroom = $15, batch3_classroom = $16
WHERE id = $17
`,
      [
        updatedEntry.subject_name,
        updatedEntry.division,
        updatedEntry.day,
        updatedEntry.start_time,
        updatedEntry.end_time,
        updatedEntry.professor_name,
        updatedEntry.classroom_number,
        updatedEntry.batch1_practical_subject,
        updatedEntry.batch2_practical_subject,
        updatedEntry.batch3_practical_subject,
        updatedEntry.batch1_professor,
        updatedEntry.batch2_professor,
        updatedEntry.batch3_professor,
        updatedEntry.batch1_classroom,
        updatedEntry.batch2_classroom,
        updatedEntry.batch3_classroom,
        id
      ]
    );

    res.json({ message: "Timetable updated successfully", updatedEntry });
  } catch (error) {
    console.error("Error updating timetable:", error);
    res.status(500).json({ message: "Server error while updating timetable" });
  }
});



app.get("/timetable", async (req, res) => {
  try {
    let sender = await db.query(`SELECT * FROM timetable ORDER BY id ASC`);
    if (sender.rows.length === 0) {
      return res.status(404).send("No timetable entries found.");
    }
    res.json({ data: sender.rows });
  } catch (error) {
    console.log(error);
    res.status(500).send("Error fetching timetable data");
  }
});



app.delete('/timetable/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await db.query('DELETE FROM timetable WHERE id = $1', [id]);
    res.status(200).json({ message: 'Deleted successfully' });
  } catch (error) {
    console.error('Error deleting:', error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

app.listen(port, () => {
  console.log(`Listening at port ${port}`);
});



