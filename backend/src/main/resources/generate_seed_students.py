"""
Generate comprehensive student seed data: sys_user + student_profile + job_application + match_score
Designed to provide real data for all 4 enhancement phases.

Pre-requisites in DB:
  - sys_user id=1 is ADMIN
  - sys_user ids 2..101 are 100 ENTERPRISE users
  - enterprise_info ids 1..100 exist
  - job_position ids 1..100 exist
"""

import json
import random
import math

NUM_STUDENTS = 30
# enterprise users occupy ids 2..101, so first student user id = 102
FIRST_STUDENT_USER_ID = 102

# BCrypt hash of "password123"
BCRYPT_HASH = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9pFKUqBhLOr3JC"

# --------- Realistic Chinese data pools ---------
last_names = ['张', '王', '李', '赵', '陈', '刘', '杨', '周', '吴', '孙',
              '胡', '林', '黄', '郑', '高', '罗', '马', '朱', '谢', '韩',
              '唐', '冯', '许', '邓', '曹', '彭', '曾', '肖', '田', '董']

first_names = ['明', '浩', '子轩', '雨桐', '诗涵', '宇航', '梓涵', '欣怡',
               '博文', '思远', '嘉豪', '一诺', '语嫣', '子涵', '皓轩',
               '美琪', '俊杰', '雅婷', '文博', '佳怡', '浩然', '梦瑶',
               '泽宇', '雨萱', '致远', '诗琪', '鑫磊', '婉婷', '天佑', '芷若']

universities = [
    '北京大学', '清华大学', '浙江大学', '复旦大学', '上海交通大学',
    '南京大学', '武汉大学', '中山大学', '华中科技大学', '哈尔滨工业大学',
    '西安交通大学', '同济大学', '南开大学', '北京航空航天大学', '华南理工大学',
    '电子科技大学', '四川大学', '厦门大学', '中南大学', '重庆大学'
]

tech_majors = ['计算机科学与技术', '软件工程', '人工智能', '数据科学', '信息安全',
               '物联网工程', '网络工程', '电子信息工程']
non_tech_majors = ['工商管理', '市场营销', '新闻传播', '英语', '国际贸易',
                   '会计学', '人力资源管理', '广告学', '视觉传达设计', '金融学']

tech_skills = ['Java', 'Python', 'C++', 'Go', 'React', 'Vue', 'JavaScript',
               'Node.js', 'MySQL', 'Redis', 'Spring Boot', 'Docker', 'Linux',
               'HTML/CSS', 'TypeScript', 'Git']
non_tech_skills = ['沟通能力', '团队协作', '执行力', '抗压能力', '数据分析',
                   '逻辑思维', 'PPT', 'Excel', 'Axure', 'Figma', 'Photoshop',
                   '文案撰写', '活动策划', '新媒体运营']

locations = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安', '南京']
schedules = ['周一至周五 白天', '每周4天 可协商', '暑假全职', '弹性时间 远程可',
             '每周3-4天 工作日']
education_levels = ['本科', '硕士']

self_intro_templates = [
    '本人{university}{major}专业在读，熟练掌握{skill1}和{skill2}，曾在校内项目中担任核心开发，积极主动，学习能力强。',
    '我是{university}的{major}学生，对{skill1}有深入了解，具备良好的{skill2}能力，希望找到一份能发挥专业特长的实习工作。',
    '{university}{major}大三学生，参与过多个{skill1}相关项目，具有扎实的{skill2}基础，期待在实习中积累实战经验。',
    '热爱互联网行业的{university}在校生，专业方向{major}，擅长{skill1}、{skill2}等技术栈，有良好的代码规范意识和团队沟通能力。',
]

apply_messages = [
    '您好！我对贵公司的这个岗位非常感兴趣，我有相关的项目经验，希望能有机会加入团队学习成长。',
    '我是一名热爱技术的在校生，具备扎实的基础知识，期待在贵公司的实习中进一步提升自己。',
    '看到贵公司的招聘信息非常兴奋，我的技能和经历与岗位要求匹配度较高，希望能获得面试机会！',
    '我对这个职位充满热情，虽然经验尚浅但学习能力强，希望能够为团队贡献力量。',
    '您好，我目前正在寻找实习机会，贵公司的行业口碑和培训体系吸引了我，期待加入！',
    '仔细阅读了职位描述，我认为自己的技能储备能够胜任这份工作，期待进一步交流。',
]

enterprise_remarks_by_status = {
    'REVIEWING': [None],  # No remark yet
    'INTERVIEW': [
        '请于本周五下午2:00到公司参加面试，地址：创新大厦5楼，联系人：HR小李 13800001234',
        '恭喜您通过简历筛选！请加微信 hr_zhaopin 预约面试时间。',
        '欢迎参加线上面试，腾讯会议链接另行发送，时间：周三上午10:00。',
    ],
    'ACCEPTED': [
        '恭喜您通过面试！请于下周一到公司报到，携带身份证和学生证。',
        '欢迎加入我们的团队！入职手续请联系HR办理。',
    ],
    'REJECTED': [
        '感谢您的投递，目前岗位已招满，欢迎关注我们的其他机会。',
        '您的背景暂时与当前岗位需求不太匹配，建议补充相关项目经验后再投递。',
        None,  # No remark
    ],
}

# Application statuses with rough distribution
status_weights = [
    ('APPLIED', 25),
    ('REVIEWING', 20),
    ('INTERVIEW', 15),
    ('ACCEPTED', 10),
    ('REJECTED', 15),
]
status_pool = []
for s, w in status_weights:
    status_pool.extend([s] * w)


def escape_sql(s):
    """Escape single quotes for SQL."""
    if s is None:
        return 'NULL'
    return "'" + s.replace("'", "\\'").replace("\\", "\\\\") + "'"


with open('seed_students.sql', 'w', encoding='utf-8') as f:
    f.write("-- Auto-generated student seed data\n\nUSE partjob;\n\n")
    f.write("-- Clear existing student-related data\n")
    f.write("SET FOREIGN_KEY_CHECKS = 0;\n")
    f.write("DELETE FROM match_score;\n")
    f.write("DELETE FROM job_application;\n")
    f.write("DELETE FROM job_favorite;\n")
    f.write("DELETE FROM student_profile;\n")
    f.write("DELETE FROM sys_user WHERE role = 'STUDENT';\n")
    f.write("SET FOREIGN_KEY_CHECKS = 1;\n\n")

    # ---------- Generate students ----------
    for i in range(1, NUM_STUDENTS + 1):
        real_name = random.choice(last_names) + random.choice(first_names)
        username = f'student_{i}'
        phone = f'1380000{200 + i:04d}'
        is_tech = random.random() < 0.6

        university = random.choice(universities)
        major = random.choice(tech_majors if is_tech else non_tech_majors)
        edu_level = random.choice(education_levels)
        enrollment_year = random.choice([2021, 2022, 2023, 2024])

        if is_tech:
            skills = random.sample(tech_skills, random.randint(3, 6))
        else:
            skills = random.sample(non_tech_skills, random.randint(3, 5))
        skills_json = json.dumps(skills, ensure_ascii=False)

        s1, s2 = skills[0], skills[1]
        intro_tmpl = random.choice(self_intro_templates)
        self_intro = intro_tmpl.format(university=university, major=major, skill1=s1, skill2=s2)

        salary_min = random.randint(100, 200)
        salary_max = salary_min + random.randint(50, 150)
        expected_loc = random.choice(locations)
        schedule = random.choice(schedules)

        # Insert sys_user
        f.write(f"INSERT INTO sys_user (username, password, real_name, phone, role, status) "
                f"VALUES ('{username}', '{BCRYPT_HASH}', '{real_name}', '{phone}', 'STUDENT', 1);\n")
        f.write(f"SET @stu_uid_{i} = LAST_INSERT_ID();\n")

        # Insert student_profile
        f.write(f"INSERT INTO student_profile (user_id, university, major, education_level, "
                f"enrollment_year, skills, self_intro, expected_salary_min, expected_salary_max, "
                f"expected_location, available_schedule) VALUES ("
                f"@stu_uid_{i}, '{university}', '{major}', '{edu_level}', {enrollment_year}, "
                f"'{skills_json}', {escape_sql(self_intro)}, {salary_min}, {salary_max}, "
                f"'{expected_loc}', '{schedule}');\n")
        f.write(f"SET @stu_pid_{i} = LAST_INSERT_ID();\n\n")

    # ---------- Generate job applications ----------
    f.write("\n-- ========= Job Applications =========\n\n")

    # Each student applies to 3-8 random jobs
    application_count = 0
    student_job_pairs = set()

    for student_i in range(1, NUM_STUDENTS + 1):
        num_apps = random.randint(3, 8)
        applied_jobs = random.sample(range(1, 101), num_apps)

        for job_id in applied_jobs:
            pair = (student_i, job_id)
            if pair in student_job_pairs:
                continue
            student_job_pairs.add(pair)

            status = random.choice(status_pool)
            message = random.choice(apply_messages)

            # Pick remark based on status
            if status in enterprise_remarks_by_status:
                remark = random.choice(enterprise_remarks_by_status[status])
            else:
                remark = None

            remark_sql = escape_sql(remark)

            # Vary the created_at dates (random days in last 30 days)
            days_ago = random.randint(0, 30)
            hours_ago = random.randint(0, 23)

            f.write(f"INSERT INTO job_application (student_id, job_id, status, apply_message, "
                    f"enterprise_remark, created_at, updated_at) VALUES ("
                    f"@stu_pid_{student_i}, {job_id}, '{status}', {escape_sql(message)}, "
                    f"{remark_sql}, "
                    f"DATE_SUB(NOW(), INTERVAL {days_ago * 24 + hours_ago} HOUR), "
                    f"DATE_SUB(NOW(), INTERVAL {max(0, days_ago * 24 + hours_ago - random.randint(1, 48))} HOUR));\n")
            application_count += 1

    f.write(f"\n-- Total applications generated: {application_count}\n\n")

    # ---------- Generate match scores ----------
    f.write("\n-- ========= Match Scores (pre-computed) =========\n")
    f.write("-- These will be recomputed by the backend when a student updates their profile.\n")
    f.write("-- Seeding them here so the recommendation engine works immediately.\n\n")

    for student_i in range(1, NUM_STUDENTS + 1):
        # Generate scores for 20-40 random jobs per student
        num_scores = random.randint(20, 40)
        scored_jobs = random.sample(range(1, 101), num_scores)

        for job_id in scored_jobs:
            skill_score = round(random.uniform(20, 100), 2)
            salary_score = round(random.uniform(30, 100), 2)
            location_score = round(random.choice([20, 50, 100]), 2)
            schedule_score = round(random.choice([30, 50, 100]), 2)

            total_score = round(
                skill_score * 0.40 +
                salary_score * 0.25 +
                location_score * 0.20 +
                schedule_score * 0.15, 2
            )

            f.write(f"INSERT INTO match_score (student_id, job_id, total_score, skill_score, "
                    f"salary_score, location_score, schedule_score) VALUES ("
                    f"@stu_pid_{student_i}, {job_id}, {total_score}, {skill_score}, "
                    f"{salary_score}, {location_score}, {schedule_score});\n")

    # ---------- Generate a few favorites ----------
    f.write("\n\n-- ========= Job Favorites =========\n\n")
    fav_pairs = set()
    for student_i in range(1, NUM_STUDENTS + 1):
        num_favs = random.randint(1, 5)
        fav_jobs = random.sample(range(1, 101), num_favs)
        for job_id in fav_jobs:
            pair = (student_i, job_id)
            if pair in fav_pairs:
                continue
            fav_pairs.add(pair)
            f.write(f"INSERT INTO job_favorite (student_id, job_id) VALUES "
                    f"(@stu_pid_{student_i}, {job_id});\n")

    f.write("\n-- Done! Seed data generation complete.\n")

print(f"Successfully generated seed_students.sql")
print(f"  - {NUM_STUDENTS} student users + profiles")
print(f"  - {application_count} job applications")
print(f"  - Match scores for recommendations")
print(f"  - Job favorites")
