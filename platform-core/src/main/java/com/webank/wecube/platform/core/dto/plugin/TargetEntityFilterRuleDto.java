package com.webank.wecube.platform.core.dto.plugin;

public class TargetEntityFilterRuleDto {
    private String targetEntityFilterRule;
    private String entityName;
    private String pkgName;
    
    private String taskCategory;

    public String getTargetEntityFilterRule() {
        return targetEntityFilterRule;
    }

    public void setTargetEntityFilterRule(String targetEntityFilterRule) {
        this.targetEntityFilterRule = targetEntityFilterRule;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }
    
    
}
