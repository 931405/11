import json
import random

jobs = []
categories = [
    # (categoryId, categoryName, is_tech)
    (1, '产品', False), 
    (2, '设计', False),
    (3, '研发', True),
    (4, '运营', False),
    (5, '市场', False),
    (6, '销售', False)
]

locations = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安']
educations = ['本科', '硕士', '大专', '不限']
durations = ['4天/周 3个月', '5天/周 6个月', '3天/周 3个月', '5天/周 3个月', '4天/周 6个月']

tech_tags = [
    ['就近租房补贴', '餐饮及下午茶', '年度体检', '节日礼品'],
    ['免费三餐', '租房补贴', '交通补助', '定期团建'],
    ['14薪', '周末双休', '弹性工作', '扁平管理'],
    ['大牛带队', '带薪年假', '免费健身房', '补充医疗保险']
]

non_tech_tags = [
    ['团队氛围好', '领导nice', '下午茶', '节日福利'],
    ['绩效奖金', '五险一金', '周末双休', '年底双薪'],
    ['实习证明', '转正机会', '专业培训', '定期体检'],
    ['交通补助', '弹性工作', '扁平管理', '不打卡']
]

tech_titles = [
    'Java开发实习生', '前端开发实习生', '测试开发实习生', '算法实习生', '数据分析实习生',
    'C++开发实习生', 'Python开发实习生', 'Android开发实习生', 'iOS开发实习生', '运维实习生'
]

non_tech_titles = [
    '产品经理实习生', '新媒体运营实习', '用户运营实习生', 'HR实习生', '市场营销实习生',
    '短视频编导', 'UI设计实习生', '交互设计实习生', '平面设计实习生', '电商运营实习生'
]

byte_description = """职位描述
ByteIntern：面向2026届毕业生（2025年9月-2026年8月期间毕业），为符合岗位要求的同学提供转正机会。
团队介绍：产品覆盖今日头条、头条极速版等各类产品，为上亿用户持续提供优质的资讯、视频等服务。我们通过建立良好的内容生态，鼓励优质的原创内容，为创作者提供优质的服务和创作体验，促进创作和交流，同时我们致力于通过尽可能丰富的内容体裁和尽可能多的分发方式，连接人与信息，丰富大家的精神生活，让人们看到更大的世界。

1、主要负责客户端/服务端产品质量相关或业务线测试与工具开发工作；
2、深度参与产品研发项目，协同产品和研发团队高质量交付产品；
3、日常项目线下测试与线上质量分析；
4、参与质量体系规划和建设；
5、参与开发效率工具和保证技术项目质量。

职位要求
1、2026届本科及以上学历在读，计算机等相关专业；
2、热爱互联网，对互联网相关业务和技术充满好奇及热情；
3、具备快速的产品及业务学习能力、敏捷而周到的逻辑思维能力以及良好的沟通协作能力；
4、有移动互联网测试经验，有自动化或白盒测试经验优先考虑；
5、了解常用客户端、服务端开发及前沿技术，如自动化框架、压测工具、大数据处理，AI大模型等优先；
6、每周至少工作4天，实习时间3个月及以上。
"""

general_description = """职位描述
团队介绍：我们是一支年轻且充满活力的团队，核心成员来自一线互联网大厂，致力于为用户提供创新的产品体验。团队氛围轻松活跃，有着完善的培养体系，导师一对一帮带，帮助实习生快速成长。在这里你不仅能接触到核心业务，还能参与到产品从0到1的建设过程。

工作职责：
1、协助团队完成日常项目跟进与落地，确保进度与质量；
2、对接内部各业务部门，协调资源，推进跨团队合作；
3、参与相关业务数据的统计分析，输出数据报告并提出优化建议；
4、完成主管交办的其他相关支持性工作，如会议纪要、文档整理等。

任职要求：
1、统招本科及以上学历在读，专业不限（相关专业优先）；
2、具备良好的逻辑思维能力和沟通表达能力，工作细致耐心；
3、熟练使用常用办公软件（Word/Excel/PPT等）；
4、具备较强的抗压能力和责任心，学习能力强，能快速融入团队；
5、每周可实习4天以上，连续实习3个月以上者优先考​​虑。
"""

skills_pool = ['Java', 'Python', 'C++', 'Go', 'React', 'Vue', 'HTML/CSS', 'JavaScript', 'Node.js', 'MySQL', 'Redis', 'Spring Boot', '沟通能力', '团队协作', '执行力', '抗压能力', '数据分析', '逻辑思维', 'PPT', 'Excel', 'Axure', 'Figma', 'Photoshop']

sql_insert = "INSERT INTO job_position (enterprise_id, title, category_id, description, requirements, skills_required, salary_min, salary_max, work_location, work_schedule, education_requirement, duration_requirement, job_tags, headcount, status, view_count, apply_count) VALUES "

with open('seed_jobs_new.sql', 'w', encoding='utf-8') as f:
    f.write("-- Auto-generated 100 job records with rich simulated data\n\nUSE partjob;\n\n")
    
    for i in range(1, 101):
        # random enterprise 1-100
        enterprise_id = random.randint(1, 100)
        
        # choose tech or non-tech
        is_tech = random.choice([True, False])
        
        if is_tech:
            title = random.choice(tech_titles) + f" (编号{i})"
            category_id = 3
            desc = byte_description if random.random() > 0.5 else general_description
            tags = random.choice(tech_tags)
            skills = random.sample(skills_pool[:12], random.randint(3, 5))
            salary_min = random.randint(150, 250)
            salary_max = salary_min + random.randint(50, 150)
        else:
            title = random.choice(non_tech_titles) + f" (编号{i})"
            category_id = random.choice([1, 2, 4, 5, 6])
            desc = general_description
            tags = random.choice(non_tech_tags)
            skills = random.sample(skills_pool[12:], random.randint(2, 4))
            salary_min = random.randint(100, 150)
            salary_max = salary_min + random.randint(30, 80)
            
        location = random.choice(locations)
        if random.random() > 0.8:
            location = "全国" # remote or cross-country
            
        work_schedule = "周一至周五 白天"
        education_req = random.choice(educations)
        duration_req = random.choice(durations)
        headcount = random.randint(1, 10)
        view_count = random.randint(10, 500)
        apply_count = random.randint(0, 50)
        
        # Format requirements as just text
        reqs = "详情请参阅职位描述中的任职要求。"
        
        skills_json = json.dumps(skills, ensure_ascii=False)
        tags_json = json.dumps(tags, ensure_ascii=False)
        
        sql = f"{sql_insert} ({enterprise_id}, '{title}', {category_id}, '{desc}', '{reqs}', '{skills_json}', {salary_min}, {salary_max}, '{location}', '{work_schedule}', '{education_req}', '{duration_req}', '{tags_json}', {headcount}, 'OPEN', {view_count}, {apply_count});\n"
        
        f.write(sql)
        
print("Successfully generated seed_jobs_new.sql")
