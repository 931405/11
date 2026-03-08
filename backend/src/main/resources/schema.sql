-- Part-time Job Matching Platform Database Schema
CREATE DATABASE IF NOT EXISTS partjob DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE partjob;

-- 1. System User Table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Login username',
    password VARCHAR(255) NOT NULL COMMENT 'Encrypted password',
    real_name VARCHAR(50) COMMENT 'Real name',
    phone VARCHAR(20) COMMENT 'Phone number',
    email VARCHAR(100) COMMENT 'Email address',
    avatar VARCHAR(500) COMMENT 'Avatar URL',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT 'Role: STUDENT, ENTERPRISE, ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled, 1-active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='System users';

-- 2. Student Profile Table
CREATE TABLE IF NOT EXISTS student_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT 'FK to sys_user',
    university VARCHAR(100) COMMENT 'University name',
    major VARCHAR(100) COMMENT 'Major / Discipline',
    education_level VARCHAR(20) COMMENT 'Degree level: BACHELOR, MASTER, PHD',
    enrollment_year INT COMMENT 'Enrollment year',
    skills JSON COMMENT 'Skill tags as JSON array',
    self_intro TEXT COMMENT 'Self introduction',
    expected_salary_min DECIMAL(10, 2) COMMENT 'Min expected salary (per hour)',
    expected_salary_max DECIMAL(10, 2) COMMENT 'Max expected salary (per hour)',
    expected_location VARCHAR(200) COMMENT 'Preferred work locations',
    available_schedule VARCHAR(500) COMMENT 'Available time slots',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Student profiles / resumes';

-- 3. Enterprise Info Table
CREATE TABLE IF NOT EXISTS enterprise_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT 'FK to sys_user',
    company_name VARCHAR(200) NOT NULL COMMENT 'Company name',
    industry VARCHAR(100) COMMENT 'Industry category',
    company_size VARCHAR(50) COMMENT 'Company size range',
    contact_person VARCHAR(50) COMMENT 'Contact person name',
    contact_phone VARCHAR(20) COMMENT 'Contact phone',
    company_address VARCHAR(500) COMMENT 'Company address',
    business_license VARCHAR(500) COMMENT 'Business license image URL',
    description TEXT COMMENT 'Company description',
    certification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
    certification_remark VARCHAR(500) COMMENT 'Admin remark for certification',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    INDEX idx_certification (certification_status)
) ENGINE=InnoDB COMMENT='Enterprise information';

-- 4. Job Category Table
CREATE TABLE IF NOT EXISTS job_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Category name',
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent category id, 0 = root',
    sort_order INT DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled, 1-active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB COMMENT='Job category taxonomy';

-- 5. Job Position Table
CREATE TABLE IF NOT EXISTS job_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enterprise_id BIGINT NOT NULL COMMENT 'FK to enterprise_info',
    title VARCHAR(200) NOT NULL COMMENT 'Job title',
    category_id BIGINT COMMENT 'FK to job_category',
    description TEXT COMMENT 'Job description',
    requirements TEXT COMMENT 'Job requirements',
    skills_required JSON COMMENT 'Required skills as JSON array',
    salary_min DECIMAL(10, 2) COMMENT 'Min salary (per hour)',
    salary_max DECIMAL(10, 2) COMMENT 'Max salary (per hour)',
    work_location VARCHAR(200) COMMENT 'Work location',
    work_schedule VARCHAR(500) COMMENT 'Work schedule description',
    education_requirement VARCHAR(50) COMMENT 'Education requirement',
    duration_requirement VARCHAR(100) COMMENT 'Duration requirement',
    job_tags JSON COMMENT 'Job tags as JSON array',
    headcount INT DEFAULT 1 COMMENT 'Number of positions',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, CLOSED, DRAFT',
    view_count INT DEFAULT 0 COMMENT 'View count',
    apply_count INT DEFAULT 0 COMMENT 'Application count',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise_info(id) ON DELETE CASCADE,
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB COMMENT='Job positions';

-- 6. Job Application Table
CREATE TABLE IF NOT EXISTS job_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT 'FK to student_profile',
    job_id BIGINT NOT NULL COMMENT 'FK to job_position',
    status VARCHAR(20) NOT NULL DEFAULT 'APPLIED' COMMENT 'APPLIED, REVIEWING, INTERVIEW, ACCEPTED, REJECTED',
    apply_message TEXT COMMENT 'Application message from student',
    enterprise_remark VARCHAR(500) COMMENT 'Enterprise remark',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_position(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_job (student_id, job_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='Job applications';

-- 7. Job Favorite Table
CREATE TABLE IF NOT EXISTS job_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT 'FK to student_profile',
    job_id BIGINT NOT NULL COMMENT 'FK to job_position',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_position(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_job (student_id, job_id)
) ENGINE=InnoDB COMMENT='Student job favorites';

-- 8. Feedback Table
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'FK to sys_user',
    type VARCHAR(20) NOT NULL DEFAULT 'FEEDBACK' COMMENT 'FEEDBACK, COMPLAINT, SUGGESTION',
    title VARCHAR(200) NOT NULL COMMENT 'Feedback title',
    content TEXT NOT NULL COMMENT 'Feedback content',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSING, RESOLVED',
    admin_reply TEXT COMMENT 'Admin reply',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='User feedback';

-- 9. Match Score Table
CREATE TABLE IF NOT EXISTS match_score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT 'FK to student_profile',
    job_id BIGINT NOT NULL COMMENT 'FK to job_position',
    total_score DECIMAL(5, 2) NOT NULL COMMENT 'Overall match score 0-100',
    skill_score DECIMAL(5, 2) DEFAULT 0 COMMENT 'Skill match score',
    salary_score DECIMAL(5, 2) DEFAULT 0 COMMENT 'Salary match score',
    location_score DECIMAL(5, 2) DEFAULT 0 COMMENT 'Location match score',
    schedule_score DECIMAL(5, 2) DEFAULT 0 COMMENT 'Schedule match score',
    computed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_position(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_job (student_id, job_id),
    INDEX idx_total_score (total_score DESC)
) ENGINE=InnoDB COMMENT='Pre-computed matching scores';

-- 10. Data Dictionary Table
CREATE TABLE IF NOT EXISTS data_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL COMMENT 'Dictionary type',
    dict_key VARCHAR(50) NOT NULL COMMENT 'Key',
    dict_value VARCHAR(200) NOT NULL COMMENT 'Value',
    sort_order INT DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled, 1-active',
    INDEX idx_type (dict_type)
) ENGINE=InnoDB COMMENT='Data dictionary';

-- Insert default data
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9pFKUqBhLOr3JC', 'System Admin', 'ADMIN', 1);

INSERT INTO job_category (name, parent_id, sort_order) VALUES
('Technology', 0, 1),
('Education', 0, 2),
('Marketing', 0, 3),
('Design', 0, 4),
('Administration', 0, 5),
('Service', 0, 6);

INSERT INTO data_dict (dict_type, dict_key, dict_value, sort_order) VALUES
('education_level', 'BACHELOR', 'Bachelor', 1),
('education_level', 'MASTER', 'Master', 2),
('education_level', 'PHD', 'PhD', 3),
('company_size', 'SMALL', 'Less than 50', 1),
('company_size', 'MEDIUM', '50-200', 2),
('company_size', 'LARGE', '200-1000', 3),
('company_size', 'ENTERPRISE', 'More than 1000', 4);

-- 11. System Config Table
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT 'Configuration Key',
    config_value VARCHAR(500) NOT NULL COMMENT 'Configuration Value',
    description VARCHAR(200) COMMENT 'Description',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='System Configuration';

INSERT IGNORE INTO sys_config (config_key, config_value, description) VALUES
('skill_weight', '0.40', 'Skill match weight'),
('salary_weight', '0.25', 'Salary match weight'),
('location_weight', '0.20', 'Location match weight'),
('schedule_weight', '0.15', 'Schedule match weight');

-- 12. System Log Table
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) COMMENT 'Operator Username',
    action VARCHAR(100) NOT NULL COMMENT 'Action Description',
    method VARCHAR(200) COMMENT 'Method Name',
    params TEXT COMMENT 'Request Parameters',
    ip VARCHAR(50) COMMENT 'IP Address',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-Success, 0-Fail',
    error_msg TEXT COMMENT 'Error Message',
    time_cost BIGINT COMMENT 'Time Cost in ms',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB COMMENT='System Operation Logs';

-- 13. User Behavior Table
CREATE TABLE IF NOT EXISTS user_behavior (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT 'FK to student_profile',
    job_id BIGINT NOT NULL COMMENT 'FK to job_position',
    action_type VARCHAR(50) NOT NULL COMMENT 'e.g. VIEW_JOB',
    dwell_time INT DEFAULT 0 COMMENT 'Time spent in seconds',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_position(id) ON DELETE CASCADE,
    INDEX idx_student_behavior (student_id)
) ENGINE=InnoDB COMMENT='Student behavior tracking';

-- Interview Invitation Table
CREATE TABLE IF NOT EXISTS `interview_invitation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `application_id` BIGINT NOT NULL COMMENT 'Related job application ID',
  `enterprise_id` BIGINT NOT NULL COMMENT 'Enterprise user ID',
  `student_id` BIGINT NOT NULL COMMENT 'Student user ID',
  `job_id` BIGINT NOT NULL COMMENT 'Job Position ID',
  `interview_time` DATETIME NOT NULL COMMENT 'Scheduled interview time',
  `location` VARCHAR(255) NOT NULL COMMENT 'Interview location/link',
  `contact` VARCHAR(100) NOT NULL COMMENT 'Contact person and phone',
  `message` VARCHAR(500) COMMENT 'Additional message from HR',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, ACCEPTED, REJECTED, CANCELLED',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_application` (`application_id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_enterprise` (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
