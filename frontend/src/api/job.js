import request from '@/utils/request'

// Mock Jobs for promoted companies
const MOCK_JOBS = [
    { id: 1001, enterpriseId: 1, title: '前端实习生', salaryMin: 400, salaryMax: 600, workLocation: '北京', workSchedule: '每周5天', matchScore: 90, companyName: '字节跳动', categoryName: '研发', applyCount: 156, requirements: '熟悉Vue/React，热爱前端技术。' },
    { id: 1002, enterpriseId: 1, title: '后端开发实习', salaryMin: 400, salaryMax: 600, workLocation: '北京', workSchedule: '每周5天', matchScore: 85, companyName: '字节跳动', categoryName: '研发', applyCount: 231, requirements: 'Java基础扎实，了解Spring框架。' },
    { id: 1003, enterpriseId: 1, title: '产品经理助理', salaryMin: 300, salaryMax: 500, workLocation: '北京', workSchedule: '每周4天', matchScore: 78, companyName: '字节跳动', categoryName: '产品', applyCount: 345, requirements: '逻辑清晰，会使用Axure等工具。' },
    { id: 1004, enterpriseId: 1, title: '运营实习生', salaryMin: 200, salaryMax: 400, workLocation: '北京', workSchedule: '每周4天', matchScore: 70, companyName: '字节跳动', categoryName: '运营', applyCount: 421, requirements: '网感好，熟悉各种社交媒体玩法。' },
    { id: 1005, enterpriseId: 1, title: '算法工程师', salaryMin: 600, salaryMax: 800, workLocation: '北京', workSchedule: '每周5天', matchScore: 95, companyName: '字节跳动', categoryName: '算法', applyCount: 89, requirements: '有顶会论文者优先，熟悉计算机视觉或自然语言处理。' },
    { id: 2001, enterpriseId: 2, title: '产品策划实习生', salaryMin: 300, salaryMax: 500, workLocation: '深圳', workSchedule: '每周5天', matchScore: 82, companyName: '腾讯', categoryName: '产品', applyCount: 567, requirements: '负责产品功能的策划与设计。' },
    { id: 2002, enterpriseId: 2, title: 'iOS开发实习', salaryMin: 400, salaryMax: 600, workLocation: '深圳', workSchedule: '每周5天', matchScore: 88, companyName: '腾讯', categoryName: '研发', applyCount: 123, requirements: '熟悉Objective-C/Swift编程。' },
    { id: 2003, enterpriseId: 2, title: '游戏运营实习', salaryMin: 250, salaryMax: 400, workLocation: '深圳', workSchedule: '每周4天', matchScore: 75, companyName: '腾讯', categoryName: '运营', applyCount: 678, requirements: '骨灰级游戏玩家优先，需对数据敏感。' },
    { id: 2004, enterpriseId: 2, title: '交互设计', salaryMin: 350, salaryMax: 500, workLocation: '深圳', workSchedule: '每周5天', matchScore: 80, companyName: '腾讯', categoryName: '设计', applyCount: 234, requirements: '能产出高质量的交互稿，心理学/设计背景优先。' },
    { id: 2005, enterpriseId: 2, title: '数据分析实习', salaryMin: 350, salaryMax: 600, workLocation: '深圳', workSchedule: '每周4天', matchScore: 85, companyName: '腾讯', categoryName: '报表', applyCount: 312, requirements: '精通SQL_Python分析，数理统计能力强。' }
]

export const searchJobs = async (params) => {
    // Inject mock content if matching our promoted data
    let useMock = false;
    let filteredMocks = MOCK_JOBS;

    if (params.enterpriseId && (params.enterpriseId == 1 || params.enterpriseId == 2)) {
        useMock = true;
        filteredMocks = filteredMocks.filter(j => j.enterpriseId == params.enterpriseId);
    }
    if (params.keyword && (params.keyword.includes('腾讯') || params.keyword.includes('字节跳动'))) {
        useMock = true;
        filteredMocks = filteredMocks.filter(j => 
            j.title.includes(params.keyword.replace('腾讯', '').replace('字节跳动', '').trim()) ||
            j.companyName.includes(params.keyword.replace(j.title, '').trim())
        );
    }

    if (useMock) {
        return {
            content: filteredMocks,
            totalElements: filteredMocks.length,
            totalPages: 1
        };
    }

    return request({
        url: '/api/jobs',
        method: 'get',
        params
    })
}

export const getJobDetail = async (jobId) => {
    const mockDbJob = MOCK_JOBS.find(j => j.id == jobId);
    if (mockDbJob) {
        return {
            ...mockDbJob,
            companyDesc: mockDbJob.enterpriseId === 1 ? '字节跳动全球科技公司' : '腾讯互联网科技',
            address: mockDbJob.workLocation,
            contactEmail: 'hr@example.com'
        }
    }

    return request({
        url: `/api/jobs/${jobId}`,
        method: 'get'
    })
}

export const getCategories = () => {
    return request({
        url: '/api/jobs/categories',
        method: 'get'
    })
}
