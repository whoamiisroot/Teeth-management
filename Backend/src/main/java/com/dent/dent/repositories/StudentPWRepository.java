package com.dent.dent.repositories;

import com.dent.dent.entities.StudentPW;
import com.dent.dent.entities.StudentPWPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPWRepository extends JpaRepository<StudentPW,Long> {
    List<StudentPW> findByStudentId(Long studentId);

    Optional<StudentPW> findById(StudentPWPK studentPWPK);



}
