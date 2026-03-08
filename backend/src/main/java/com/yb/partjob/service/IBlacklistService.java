package com.yb.partjob.service;

import com.yb.partjob.model.vo.BlacklistVO;
import java.util.List;

public interface IBlacklistService {
    void addBlacklist(Long studentId, Long enterpriseId);

    void removeBlacklist(Long studentId, Long enterpriseId);

    List<BlacklistVO> getBlacklist(Long studentId);
}
