import json
import random

# Generate SQL to update jobs 1-15 with realistic specific internship data
sql_statements = []

# 1. 暑期实习 (Summer Internships) - IDs 1 to 5
summer_jobs = [
    {
        "id": 1,
        "title": "2026届暑期实习-Golang后端开发工程师",
        "desc": "深入参与公司核心高并发业务的服务端开发工作。你将体验千万级DAU产品的技术架构，包括微服务拆分、缓存设计、数据库优化等。这是一个为期3个月的暑期技术夏令营，表现优异者可获得秋招SP/SSP提前批offer。",
        "reqs": "1. 2026届统招本科及以上学历，计算机相关专业；\n2. 扎实的计算机网络、操作系统、数据结构基础；\n3. 熟悉至少一门后端语言（Golang/Java/C++），有Golang经验加分；\n4. 具有较强的学习能力和抗压能力。",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["Golang", "MySQL", "Redis", "高并发", "微服务"], ensure_ascii=False),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["转正机会大", "大牛带队", "免费三餐", "住房补贴"], ensure_ascii=False),
        "salary_min": 200, "salary_max": 300,
        "duration": "全职 3个月"
    },
    {
        "id": 2,
        "title": "2026届暑期实习-前端开发工程师（核心大前端）",
        "desc": "负责公司亿级用户规模的移动端及PC端Web业务开发。参与下一代前端架构演进，体验React/Vue3/TypeScript等前沿技术的工程化落地。我们提供完善的新人培养体系",
        "reqs": "1. 2026届本硕，有前端项目经验；\n2. 深入理解HTML/CSS/JS，熟悉前端主流框架及原理；\n3. 对前后端分离有深入理解；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["React", "Vue3", "TypeScript", "Webpack"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["转正率80%", "六险一金", "弹性工作"]),
        "salary_min": 150, "salary_max": 250,
        "duration": "全职 3个月"
    },
    {
        "id": 3,
        "title": "2026届暑期实习-产品经理大拿训练营",
        "desc": "探索商业变现、用户增长的最前沿。你将独立负责一个子模块的迭代规划，从数据分析、用户调研到需求评审、项目验收，全链路体验产品经理的日常",
        "reqs": "1. 专业不限，极强的逻辑思维和数据分析能力；\n2. 同理心强，能敏锐洞察用户需求；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["产品设计", "数据分析", "Axure", "需求分析"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["S级项目", "导师一对一", "快速晋升"]),
        "salary_min": 180, "salary_max": 220,
        "duration": "全职 3个月"
    },
    {
        "id": 4,
        "title": "2026届暑期实习-大模型算法工程师",
        "desc": "加入AI Lab，深入参与千亿参数大模型（LLM）的预训练、SFT微调、RLHF阶段。在海量算力平台探索前沿AI技术落地",
        "reqs": "1. 硕士及以上学历，人工智能/计算机方向；\n2. 熟练使用PyTorch，有顶会论文者优先；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["PyTorch", "LLM", "NLP", "深度学习"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["前沿技术", "海量算力", "顶薪转正"]),
        "salary_min": 350, "salary_max": 500,
        "duration": "全职 3个月"
    },
    {
        "id": 5,
        "title": "2026届暑期实习-商业分析师（战略投资部）",
        "desc": "参与行业研究、竞品分析，为高管的投资并购和战略布局提供数据支撑。这是一次全方位了解互联网下半场商业版图的机会",
        "reqs": "1. 顶尖海内外名校背景，金融/经济/管理类优先；\n2. 熟练掌握Excel高阶数据透视及SQL；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["商业分析", "SQL", "行研报告", "数据洞察"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["直面高管", "深度行研", "精英圈层"]),
        "salary_min": 250, "salary_max": 400,
        "duration": "全职 3个月"
    }
]

# 2. 远程实习 (Remote Internships) - IDs 6 to 10
remote_jobs = [
    {
        "id": 6,
        "title": "【可远程】小红书图文运营实习生",
        "desc": "寻找网感极佳的Z世代！主要负责公司官方小红书账号的日常更新，包括选题策划、文案撰写、简单的图片排版。全程云办公，时间自由支配！",
        "reqs": "1. 5G冲浪选手，深谙小红书爆款文案套路；\n2. 会使用剪映、Canva、PS等基础排版工具；\n3. 每天能保证2-3小时输出时间，接受周末碎片化对接",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["小红书运营", "文案撰写", "内容策划", "图片排版"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["时间自由", "全票据报销", "无需打卡", "云端团队"]),
        "salary_min": 60, "salary_max": 100,
        "duration": "碎片化时间"
    },
    {
        "id": 7,
        "title": "兼职远程-英法双语字幕翻译",
        "desc": "负责公司引入的海外纪录片、课程视频的本地化字幕翻译及校对工作。按照视频时长结算，多劳多得",
        "reqs": "1. 英语专八或同等水平，熟悉各类本土文化梗；\n2. 熟练使用ArcTime等字幕工具；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["字幕翻译", "英语专八", "本地化", "ArcTime"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["按件计费", "项目制", "时间灵活"]),
        "salary_min": 80, "salary_max": 150,
        "duration": "按件交付"
    },
    {
        "id": 8,
        "title": "远程插画师（日系二次元风格）",
        "desc": "参与新研发二次元卡牌手游的立绘、CG等美术资产外包工作。根据主美需提供的设定独立完成上色及细化",
        "reqs": "1. 有成熟的二次元日系画风作品（投递需附带Pixiv等作品链接）；\n2. 熟悉人体结构、光影，具有良好的色彩感；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["原画设计", "二次元风格", "SAI/PS", "人物立绘"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["云端审稿", "接单自由", "版权署名"]),
        "salary_min": 150, "salary_max": 300,
        "duration": "按画幅结算"
    },
    {
        "id": 9,
        "title": "【线上/远程】社群用户运营实习生",
        "desc": "负责付费用户微信社群的活跃引导、答疑解惑、活动统筹。你不需要来办公室，只要有一台手机和稳定的网速即可",
        "reqs": "1. 性格外向，喜欢在微信里聊天活跃气氛；\n2. 有极强的耐心，解答用户各种基础问题；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["社群运营", "用户维系", "活动策划", "沟通能力"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["一部手机搞定", "月度奖金", "工作不设限"]),
        "salary_min": 50, "salary_max": 80,
        "duration": "轮班制"
    },
    {
        "id": 10,
        "title": "海外电商独立站运营助手 (Remote)",
        "desc": "协助主管完成Shopify独立站的日常上新、商品描述本地化（英文）、竞品价格追踪。适合想要了解跨境电商又不想坐班的同学",
        "reqs": "1. 英语四六级以上，能无障碍浏览海外网站；\n2. 踏实细心，有较强的数据对比能力；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["跨境电商", "Shopify", "英语读写", "竞品分析"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["跨境风口", "老带新", "时间灵活"]),
        "salary_min": 70, "salary_max": 120,
        "duration": "日均3小时"
    }
]

# 3. 应届实习 (Graduate Internships) - IDs 11 to 15
grad_jobs = [
    {
        "id": 11,
        "title": "2025/2026届-财务管培生实习（可转正）",
        "desc": "进入集团财务中心，轮岗于总账、AP/AR、资金管理、税务规划四大模块。这不仅是实习，更是寻找未来财务管理者的培养计划。",
        "reqs": "1. 25或26届统招本科/硕士，财务/会计类相关专业；\n2. 拥有CPA/ACCA通过部分科目者优先；\n3. 实习满4个月可提前发放秋招录用转正Offer。",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["财务管理", "各类报表", "审计支持", "CPA优先"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["管培生计划", "全留用指标", "年底分红制", "完善培训"]),
        "salary_min": 150, "salary_max": 200,
        "duration": "每周4天 4个月"
    },
    {
        "id": 12,
        "title": "研发工程师（应届生留用专岗）- 新一代OS部",
        "desc": "加入国产智能汽车操作系统OS核心研发团队，参与车载底层通信架构、微内核进程调度的开发。实习期即被视为正式员工培养，共享内部最新技术文档。",
        "reqs": "1. 2025届毕业生，计算机/软件/通信工程专业；\n2. 精通C/C++，对Linux内核有深刻理解；\n3. 需要能够全职实习至毕业。",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["C/C++", "Linux内核", "操作系统", "车载OS"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["硬核技术", "100%转正", "购车补贴", "六险二金"]),
        "salary_min": 250, "salary_max": 400,
        "duration": "每周5天 至毕业"
    },
    {
        "id": 13,
        "title": "大客户销售（B2B SaaS）- 应届生直聘",
        "desc": "作为大客户经理储备军，你将负责国内Top500企业的线索挖掘与初步对接。百万年薪的起点，这绝不是一个打杂的岗位，而是真正的“拿业绩说话”。",
        "reqs": "1. 本科及以上，专业不限，极度渴望成功；\n2. 抗压能力强，有极强的自驱力和表达能力；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["TO B销售", "陌拜", "商务谈判", "SaaS软件"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["高底薪高提成", "狼性文化", "销冠带教"]),
        "salary_min": 150, "salary_max": 200,
        "duration": "每周5天 3个月"
    },
    {
        "id": 14,
        "title": "【可转正】海外市场PR营销实习生（出海业务）",
        "desc": "随着公司全球化战略加速，我们需要你协助对接欧美、东南亚的KOL/KOC，策划TikTok上的病毒营销事件，提升品牌国际影响力。",
        "reqs": "1. 海归留学生或具备海外生活经验者优先，英语听说读写接近母语水平；\n2. 深度TikTok重度用户，了解各类网红生态；",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["海外PR", "KOL商务", "TikTok营销", "英语流利"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["全球出海", "外企氛围", "直通转正"]),
        "salary_min": 120, "salary_max": 180,
        "duration": "每周4天 5个月"
    },
    {
        "id": 15,
        "title": "人力资源实习生（招聘方向/直通秋招）",
        "desc": "深度参与公司级别的校招/社招全流程。从简历筛选、寻访(Mapping)、安排笔面试到后期的Offer沟通。你将站在HR视角看透招聘本质。",
        "reqs": "1. 2025届应届生，人力资源/心理学相关专业优先；\n2. 踏实细心，共情力强；\n3. 通过实习期考核100%留用进入HR总办。",
        "skills": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["招聘交付", "简历筛选", "面试安排", "人才Mapping"]),
        "tags": json.dumps(..., ensure_ascii=False).replace('..., ensure_ascii=False', 'ensure_ascii=False')(["100%留用", "团队氛围好", "扁平管理"]),
        "salary_min": 100, "salary_max": 150,
        "duration": "每周4天 3个月"
    }
]

# Write UPDATE statements
for job in summer_jobs:
    sql = f"UPDATE job_position SET title='{job['title']}', description='{job['desc']}', requirements='{job['reqs']}', skills_required='{job['skills']}', job_tags='{job['tags']}', salary_min={job['salary_min']}, salary_max={job['salary_max']}, duration_requirement='{job['duration']}', job_type='暑期实习' WHERE id={job['id']};"
    sql_statements.append(sql)

for job in remote_jobs:
    sql = f"UPDATE job_position SET title='{job['title']}', description='{job['desc']}', requirements='{job['reqs']}', skills_required='{job['skills']}', job_tags='{job['tags']}', salary_min={job['salary_min']}, salary_max={job['salary_max']}, duration_requirement='{job['duration']}', job_type='远程实习' WHERE id={job['id']};"
    sql_statements.append(sql)

for job in grad_jobs:
    sql = f"UPDATE job_position SET title='{job['title']}', description='{job['desc']}', requirements='{job['reqs']}', skills_required='{job['skills']}', job_tags='{job['tags']}', salary_min={job['salary_min']}, salary_max={job['salary_max']}, duration_requirement='{job['duration']}', job_type='应届实习' WHERE id={job['id']};"
    sql_statements.append(sql)

# Also update some remaining jobs to realistic "综合" jobs
mix_jobs = [
    {"id":16, "t":"Java业务开发实习生", "d":"参与内部CRM系统的日常迭代开发"},
    {"id":17, "t":"人事行政实习生", "d":"前台接待、物品采购及快递收发"},
    {"id":18, "t":"测试实习生 (QA)", "d":"编写测试用例，执行手动及自动化测试"}
]
for job in mix_jobs:
    sql = f"UPDATE job_position SET title='{job['t']}', description='{job['d']}', job_type='综合' WHERE id={job['id']};"
    sql_statements.append(sql)


with open("update_realistic_jobs.sql", "w", encoding="utf-8") as f:
    for sql in sql_statements:
        f.write(sql + "\n")
print("Realistic update SQL generated successfully.")
