import request from '@/utils/request'

// Mock Data
const MOCK_ENTERPRISES = {
    '1': {
        id: 1,
        companyName: '字节跳动',
        industry: '互联网/科技',
        companySize: '10000人以上',
        companyAddress: '北京市海淀区紫金数码园',
        description: '全球领先的科技公司，提供丰富的技术、产品和运营实习岗位机会。',
        certificationStatus: 'APPROVED'
    },
    '2': {
        id: 2,
        companyName: '腾讯',
        industry: '互联网/科技',
        companySize: '10000人以上',
        companyAddress: '深圳市南山区腾讯滨海大厦',
        description: '覆盖产品、研发、设计等方向，适合希望进入头部互联网企业的同学。',
        certificationStatus: 'APPROVED'
    }
}

export const getEnterprisePublicInfo = async (id) => {
    if (MOCK_ENTERPRISES[id]) return MOCK_ENTERPRISES[id];
    return request({
        url: `/api/enterprises/${id}`,
        method: 'get'
    })
}

export const getEnterpriseJobCount = async (id) => {
    if (MOCK_ENTERPRISES[id]) return id == 1 ? 5 : 5;
    return request({
        url: `/api/enterprises/${id}/job-count`,
        method: 'get'
    })
}
