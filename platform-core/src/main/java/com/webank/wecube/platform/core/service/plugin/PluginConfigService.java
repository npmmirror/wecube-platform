package com.webank.wecube.platform.core.service.plugin;


import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.*;

@Service
@Transactional
public class PluginConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PluginPackageRepository pluginPackageRepository;
    @Autowired
    private PluginConfigRepository pluginConfigRepository;
    @Autowired
    private PluginPackageEntityRepository pluginPackageEntityRepository;

    public List<PluginConfigInterface> getPluginConfigInterfaces(int pluginConfigId) {
        return pluginConfigRepository.findAllPluginConfigInterfacesByConfigIdAndFetchParameters(pluginConfigId);
    }

    public PluginConfig savePluginConfig(PluginConfig pluginConfig) throws WecubeCoreException {
        ensurePluginConfigIsValid(pluginConfig);
        Integer packageId = pluginConfig.getPluginPackageId();
        pluginPackageRepository.findById(packageId).ifPresent(pluginConfig::setPluginPackage);
        PluginConfig pluginConfigFromDatabase = pluginConfigRepository.findById(pluginConfig.getId()).get();
        if (REGISTERED.equals(pluginConfigFromDatabase.getStatus())) {
            throw new WecubeCoreException("Not allow to update plugin with status: REGISTERED");
        }
        pluginConfig.setStatus(UNREGISTERED);

        return pluginConfigRepository.save(pluginConfig);
    }

    private void ensurePluginConfigIsValid(PluginConfig pluginConfig) {
        if (null == pluginConfig.getId() || pluginConfig.getId() < 1) {
            throw new WecubeCoreException("Invalid pluginConfig with id: " + pluginConfig.getId());
        }
        if (!pluginConfigRepository.existsById(pluginConfig.getId())) {
            throw new WecubeCoreException("PluginConfig not found for id: " + pluginConfig.getId());
        }

        Integer entityId = pluginConfig.getEntityId();
        if (null == entityId && entityId.intValue() < 1) {
            throw new WecubeCoreException("Invalid entity Id: " + entityId);
        }
        Optional<PluginPackageEntity> pluginPackageEntityOptional = pluginPackageEntityRepository.findById(entityId);
        if (!pluginPackageEntityOptional.isPresent()) {
            throw new WecubeCoreException("PluginPackageEntity not found for id: " + entityId);
        }
    }

    public PluginConfig registerPlugin(int pluginConfigId) {
        if (!pluginConfigRepository.existsById(pluginConfigId)) {
            throw new WecubeCoreException("PluginConfig not found for id: " + pluginConfigId);
        }

        Optional<PluginConfig> pluginConfigOptional = pluginConfigRepository.findById(pluginConfigId);
        PluginConfig pluginConfig = pluginConfigOptional.get();

        ensurePluginConfigIsValid(pluginConfig);
        if (!UNREGISTERED.equals(pluginConfig.getStatus())) {
            throw new WecubeCoreException("Not allow to register pluginConfig with status: " + pluginConfig.getStatus());
        }
        pluginConfig.setStatus(REGISTERED);
        return pluginConfigRepository.save(pluginConfig);
    }

    public void deletePlugin(int pluginConfigId) {
        if (!pluginConfigRepository.existsById(pluginConfigId)) {
            throw new WecubeCoreException("PluginConfig not found for id: " + pluginConfigId);
        }

        PluginConfig pluginConfig = pluginConfigRepository.findById(pluginConfigId).get();

        pluginConfig.setStatus(DECOMMISSIONED);
        pluginConfigRepository.save(pluginConfig);
    }
}
