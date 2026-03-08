USE partjob;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE job_application;
TRUNCATE TABLE job_favorite;
TRUNCATE TABLE match_score;
TRUNCATE TABLE job_position;
SET FOREIGN_KEY_CHECKS = 1;

-- Add new columns if they do not exist
DROP PROCEDURE IF EXISTS upgrade_columns;
DELIMITER $$
CREATE PROCEDURE upgrade_columns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'partjob' AND TABLE_NAME = 'job_position' AND COLUMN_NAME = 'education_requirement') THEN
        ALTER TABLE job_position ADD COLUMN education_requirement VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'partjob' AND TABLE_NAME = 'job_position' AND COLUMN_NAME = 'duration_requirement') THEN
        ALTER TABLE job_position ADD COLUMN duration_requirement VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'partjob' AND TABLE_NAME = 'job_position' AND COLUMN_NAME = 'job_tags') THEN
        ALTER TABLE job_position ADD COLUMN job_tags JSON;
    END IF;
END $$
DELIMITER ;
CALL upgrade_columns();
DROP PROCEDURE upgrade_columns;
