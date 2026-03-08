const fs = require('fs');

const categories = [1, 2, 3, 4, 5, 6];
const locations = ['海淀区', '朝阳区', '东城区', '西城区', '丰台区', '科技园', '大学城'];
const schedules = ['周一至周五 白天', '周末双休', '弹性工作时间', '周二周四下午', '周末兼职'];
const skillPool = ['Java', 'Python', 'Vue', 'React', '设计', '文案', '沟通', 'Excel', '英语', '翻译', '服务', '销售', 'C++', 'Node.js', '产品策划'];
const titles = ['Java实习生', '前端开发兼职', '运营助理', '设计助理', '英语助教', '周末兼职店员', '校园大使', '数据标注员', '新媒体运营实习', '产品助理实习生'];

let sql = '-- Auto-generated 100 job records\n\n';
sql += 'USE partjob;\n\n';

for (let i = 1; i <= 100; i++) {
    // Random enterprise from the 100 we generated earlier
    const enterpriseId = Math.floor(Math.random() * 100) + 1;
    const title = titles[Math.floor(Math.random() * titles.length)] + ` (编号${i})`;
    const categoryId = categories[Math.floor(Math.random() * categories.length)];
    const description = `这是一份非常有前景的工作...负责相关业务支持...`;
    const requirements = `具有强烈的责任心，每周至少到岗3天...`;

    // Random skills
    const skillsCount = Math.floor(Math.random() * 4) + 1; // 1 to 4 skills
    let shuffledSkills = skillPool.sort(() => 0.5 - Math.random());
    let skillsRequired = JSON.stringify(shuffledSkills.slice(0, skillsCount));

    // Random salary range between 1500 and 8000
    const baseSalary = Math.floor(Math.random() * 50) + 100; // 100 to 149
    const salaryMin = baseSalary;
    const salaryMax = baseSalary + Math.floor(Math.random() * 50) + 20;

    const location = locations[Math.floor(Math.random() * locations.length)];
    const schedule = schedules[Math.floor(Math.random() * schedules.length)];
    const headcount = Math.floor(Math.random() * 5) + 1;

    sql += `INSERT INTO job_position (enterprise_id, title, category_id, description, requirements, skills_required, salary_min, salary_max, work_location, work_schedule, headcount, status, view_count, apply_count) 
  VALUES (${enterpriseId}, '${title}', ${categoryId}, '${description}', '${requirements}', '${skillsRequired}', ${salaryMin}, ${salaryMax}, '${location}', '${schedule}', ${headcount}, 'OPEN', ${Math.floor(Math.random() * 100)}, ${Math.floor(Math.random() * 20)});\n`;
}

fs.writeFileSync('d:/OneDrive/Desktop/test/11/backend/src/main/resources/seed_jobs.sql', sql);
console.log('Successfully generated 100 job seed records at backend/src/main/resources/seed_jobs.sql');
