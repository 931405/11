package com.yb.partjob.service;

import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.JobCategory;
import com.yb.partjob.model.vo.ApplicationVO;
import com.yb.partjob.model.vo.JobVO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IJobService {
    Page<JobVO> searchJobs(String keyword, Long categoryId, String location, String jobType, Long enterpriseId,
            int page, int size, Long currentUserId);

    JobVO getJobDetail(Long jobId, Long currentUserId);

    JobApplication applyJob(Long userId, Long jobId, String message);

    Page<ApplicationVO> getMyApplications(Long userId, int page, int size);

    void favoriteJob(Long userId, Long jobId);

    void unfavoriteJob(Long userId, Long jobId);

    Page<JobVO> getMyFavorites(Long userId, int page, int size);

    List<JobCategory> getCategoryTree();
}
