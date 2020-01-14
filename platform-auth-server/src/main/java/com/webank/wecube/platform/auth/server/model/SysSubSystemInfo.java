package com.webank.wecube.platform.auth.server.model;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class SysSubSystemInfo {
    private String id;

    private String name;

    private String systemCode;

    private String apiKey;

    private String pubApiKey;

    private Boolean active;

    private Boolean blocked;

    private Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPubApiKey() {
        return pubApiKey;
    }

    public void setPubApiKey(String pubApiKey) {
        this.pubApiKey = pubApiKey;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
    
    public SysSubSystemInfo addAuthorities(Collection<GrantedAuthority> authorities) {
        if(authorities == null){
            return this;
        }
        
        authorities.forEach( e -> {
            if(e != null){
                this.authorities.add(e);
            }
        });
        
        return this;
    }
    
    

}
