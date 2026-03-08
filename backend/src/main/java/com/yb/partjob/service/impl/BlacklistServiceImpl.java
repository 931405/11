package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.EnterpriseBlacklist;
import com.yb.partjob.model.vo.BlacklistVO;
import com.yb.partjob.repository.EnterpriseBlacklistRepository;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.service.IBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlacklistServiceImpl implements IBlacklistService {

    @Autowired
    private EnterpriseBlacklistRepository blacklistRepository;

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Override
    @Transactional
    public void addBlacklist(Long studentId, Long enterpriseId) {
        if (blacklistRepository.existsByStudentIdAndEnterpriseId(studentId, enterpriseId)) {
            throw new BusinessException("已屏蔽该企业");
        }

        EnterpriseBlacklist blacklist = EnterpriseBlacklist.builder()
                .studentId(studentId)
                .enterpriseId(enterpriseId)
                .build();
        blacklistRepository.save(blacklist);
    }

    @Override
    @Transactional
    public void removeBlacklist(Long studentId, Long enterpriseId) {
        EnterpriseBlacklist blacklist = blacklistRepository.findByStudentIdAndEnterpriseId(studentId, enterpriseId)
                .orElseThrow(() -> new BusinessException("未找到屏蔽记录"));
        blacklistRepository.delete(blacklist);
    }

    @Override
    public List<BlacklistVO> getBlacklist(Long studentId) {
        List<EnterpriseBlacklist> records = blacklistRepository.findByStudentId(studentId);

        return records.stream().map(record -> {
            BlacklistVO vo = new BlacklistVO();
            vo.setEnterpriseId(record.getEnterpriseId());
            vo.setBlacklistedAt(record.getCreatedAt());

            enterpriseInfoRepository.findByUserId(record.getEnterpriseId()).ifPresent(info -> {
                vo.setCompanyName(info.getCompanyName());
                vo.setIndustry(info.getIndustry());
            });
            return vo;
        }).collect(Collectors.toList());
    }
}
