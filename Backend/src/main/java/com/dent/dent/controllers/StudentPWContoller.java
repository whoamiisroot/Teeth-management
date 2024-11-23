package com.dent.dent.controllers;

import com.dent.dent.entities.StudentPW;
import com.dent.dent.entities.StudentPWPK;
import com.dent.dent.services.StudentPWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studentpws")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentPWContoller {
    @Autowired
    StudentPWService studentPWService;

    @PostMapping("/add/{studentId}/{pwId}")
    public ResponseEntity<Object> create(@RequestParam long studentId,@RequestParam long pwId,@RequestBody StudentPW  studentPW){
        StudentPWPK studentPWPK = new StudentPWPK(studentId,pwId);
        studentPW.setId(studentPWPK);
        studentPWService.create(studentPW);
        return ResponseEntity.ok(studentPW);
    }


    @GetMapping("/all")
    public List<StudentPW> getAll(){
        return studentPWService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable long id){
        StudentPW studentPW = studentPWService.findById(id);
        if(studentPW == null)
            return new ResponseEntity<>(Map.of("message","studentPW does not exist"), HttpStatus.NOT_FOUND);
        else
            return ResponseEntity.ok(studentPW);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable long id){
        StudentPW studentPW = studentPWService.findById(id);
        if (studentPW == null)
            return new ResponseEntity<>(Map.of("message","studentPW does not exist"), HttpStatus.NOT_FOUND);
        else{
            studentPWService.delete(studentPW);
            return ResponseEntity.ok(Map.of("message","studentPW deleted successfully"));
        }
    }

    @PutMapping("/update/{studentId}/{pwId}")
    public ResponseEntity<Object> update(
            @PathVariable long studentId,
            @PathVariable long pwId,
            @RequestBody Map<String, Float> requestBody
            
    ) {
        try {
            // Fetch the existing StudentPW entity
            StudentPW existingStudentPW = studentPWService.findByIdPk(new StudentPWPK(studentId, pwId));

            if (existingStudentPW != null) {
                // Update the af1 value
                existingStudentPW.setAf1(requestBody.get("af1"));
                existingStudentPW.setAs1(requestBody.get("as1"));
                existingStudentPW.setAf2(requestBody.get("af2"));
                existingStudentPW.setAs2(requestBody.get("as2"));
                existingStudentPW.setBf1(requestBody.get("bf1"));
                existingStudentPW.setBf2(requestBody.get("bf2"));
                existingStudentPW.setBs1(requestBody.get("bs1"));
                existingStudentPW.setBs2(requestBody.get("bs2"));
                


                // Save the updated entity
                studentPWService.create(existingStudentPW);

                return ResponseEntity.ok(existingStudentPW);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Record not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error updating record"));
        }
    }
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Object> findPWByStudentId(@PathVariable Long studentId) {
        List<StudentPW> studentPWs = studentPWService.findByStudentId(studentId);

        if (studentPWs.isEmpty()) {
            return new ResponseEntity<>("No PW found for student with id: " + studentId, HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(studentPWs);
        }
    }
}





