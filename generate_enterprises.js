const fs = require('fs');

const industries = ['Technology', 'Education', 'Marketing', 'Design', 'Administration', 'Service', 'Finance', 'Healthcare'];
const sizes = ['SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE'];
// reusing the admin password hash from schema.sql for simplicity
const passwordHash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9pFKUqBhLOr3JC';

let sql = '-- Auto-generated 100 enterprise records\n\n';
sql += 'USE partjob;\n\n';

for (let i = 1; i <= 100; i++) {
    const username = `enterprise_${i}`;
    const realName = `HR Director ${i}`;
    const companyName = `Global Tech Solutions ${i}`;
    const industry = industries[Math.floor(Math.random() * industries.length)];
    const size = sizes[Math.floor(Math.random() * sizes.length)];
    const contact = `Agent ${i}`;
    const phone = `1390000${i.toString().padStart(4, '0')}`;
    const address = `Innovation Park Bldg ${i}, Capital City`;

    sql += `INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES ('${username}', '${passwordHash}', '${realName}', '${phone}', 'ENTERPRISE', 1);\n`;
    sql += `SET @ent_uid_${i} = LAST_INSERT_ID();\n`;
    sql += `INSERT INTO enterprise_info (user_id, company_name, industry, company_size, contact_person, contact_phone, company_address, certification_status) VALUES (@ent_uid_${i}, '${companyName}', '${industry}', '${size}', '${contact}', '${phone}', '${address}', 'APPROVED');\n\n`;
}

fs.writeFileSync('d:/OneDrive/Desktop/test/11/backend/src/main/resources/seed_enterprises.sql', sql);
console.log('Successfully generated 100 enterprise seed records at backend/src/main/resources/seed_enterprises.sql');
