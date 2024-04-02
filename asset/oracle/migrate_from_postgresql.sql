ALTER TABLE my_table ADD (my_temp_column TIMESTAMP WITH TIME ZONE);
UPDATE my_table SET my_temp_column = FROM_TZ(CAST(my_timestamp_column AS TIMESTAMP), 'UTC');
ALTER TABLE my_table DROP COLUMN my_timestamp_column;
ALTER TABLE my_table RENAME COLUMN my_temp_column TO my_timestamp_column;