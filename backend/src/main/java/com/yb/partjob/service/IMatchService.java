package com.yb.partjob.service;

import com.yb.partjob.model.MatchScore;
import org.springframework.data.domain.Page;

public interface IMatchService {
    void computeMatchScores(Long studentId);

    Page<MatchScore> getTopMatches(Long studentId, int page, int size);
}
