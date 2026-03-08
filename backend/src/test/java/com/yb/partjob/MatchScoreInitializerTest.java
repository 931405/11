package com.yb.partjob;

import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.service.IMatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MatchScoreInitializerTest {

    @Autowired
    private IMatchService matchService;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Test
    public void generateMatchScores() {
        List<StudentProfile> students = studentProfileRepository.findAll();
        System.out.println("Found " + students.size() + " students.");
        for (StudentProfile student : students) {
            System.out.println("Computing match scores for student ID: " + student.getId());
            matchService.computeMatchScores(student.getId());
        }
        System.out.println("Match scores generation completed.");
    }
}
