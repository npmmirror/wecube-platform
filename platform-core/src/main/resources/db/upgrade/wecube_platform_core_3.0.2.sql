
ALTER TABLE `plugin_config_interface_parameters` ADD COLUMN `multiple` VARCHAR(10) NULL;
ALTER TABLE `plugin_config_interface_parameters` ADD COLUMN `ref_object_name` VARCHAR(60) NULL;

ALTER TABLE `plugin_object_property_meta` ADD COLUMN `ref_object_name` VARCHAR(10) NULL;
ALTER TABLE `plugin_object_property_meta` ADD COLUMN `multiple` VARCHAR(10) NULL;

ALTER TABLE `plugin_object_property_var` RENAME TO `core_object_property_var`;
ALTER TABLE `plugin_object_var` RENAME TO `core_object_var`;

ALTER TABLE `core_re_task_node_def_info` ADD COLUMN `prev_ctx_node_ids` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL;
ALTER TABLE `core_re_proc_def_info` ADD COLUMN `tags` VARCHAR(255) NULL;

ALTER TABLE `core_ru_task_node_exec_param` ADD COLUMN `multiple` VARCHAR(45) NULL;
ALTER TABLE `core_ru_task_node_exec_param` ADD COLUMN `param_def_id` VARCHAR(45) NULL;
ALTER TABLE `core_ru_task_node_exec_param` ADD COLUMN `mapping_type` VARCHAR(45) NULL;
ALTER TABLE `core_ru_task_node_exec_param` ADD COLUMN `callback_id` VARCHAR(60) NULL;

ALTER TABLE `core_ru_task_node_exec_req` ADD COLUMN `context_data_flag` VARCHAR(45) NULL;
ALTER TABLE `core_ru_task_node_exec_req` ADD COLUMN `req_object_amount` INT NULL;
ALTER TABLE `core_ru_task_node_exec_req` ADD COLUMN `resp_object_amount` INT NULL;

ALTER TABLE `plugin_package_attributes` ADD COLUMN `multiple` VARCHAR(6) NULL DEFAULT 'N';

CREATE TABLE `plugin_certification` (
  `id` VARCHAR(60) NOT NULL,
  `created_by` VARCHAR(45) NULL,
  `created_time` DATETIME NULL,
  `updated_by` VARCHAR(45) NULL,
  `updated_time` DATETIME NULL,
  `plugin` VARCHAR(45) NULL,
  `lpk` TEXT NULL DEFAULT NULL,
  `encrypt_data` TEXT NULL DEFAULT NULL,
  `signature` TEXT NULL DEFAULT NULL,
  `description` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

delete from `menu_items` where id = 'ADMIN_CERTIFICATION';
delete from `role_menu` where id = 'sEZloDCk2Cpb';
INSERT INTO `menu_items` (`id`,`parent_code`,`code`,`source`,`description`,`local_display_name`,`menu_order`) VALUES ('ADMIN_CERTIFICATION','ADMIN','ADMIN_CERTIFICATION','SYSTEM',NULL,'授权管理',17);
INSERT INTO `role_menu` (`id`,`role_name`,`menu_code`) VALUES ('sEZloDCk2Cpb','SUPER_ADMIN','ADMIN_CERTIFICATION');

ALTER TABLE `execution_job_parameters` ADD COLUMN `ref_object_name` VARCHAR(100) NULL;
ALTER TABLE `execution_job_parameters` ADD COLUMN `multiple` VARCHAR(10) NULL;
