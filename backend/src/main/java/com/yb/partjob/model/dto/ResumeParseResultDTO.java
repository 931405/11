package com.yb.partjob.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeParseResultDTO {
    private String university;
    private List<String> skills;
    private String selfIntro;
    private String fileName;
    private String fileUrl;
}
