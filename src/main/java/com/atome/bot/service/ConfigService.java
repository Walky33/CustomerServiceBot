package com.atome.bot.service;

import com.atome.bot.model.Configuration;
import com.atome.bot.repositories.ConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ConfigService {
    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
        ensureDefaults();
    }

    private void ensureDefaults() {
        saveIfNotExists("kb_url","https://help.atome.ph/hc/en-gb/categories/4439682039065-Atome-Card");
        saveIfNotExists("additional_guidelines",
                "Be concise and polite.\nPrefer knowledge base citations.\nIf unsure, ask a clarifying question.");
    }

    private void saveIfNotExists(String key, String value) {
        if (configRepository.findById(key).isEmpty())
            configRepository.save(new Configuration(key, value));
    }

    public Map<String, String> getAll() {
        return configRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(Configuration::getConfigKey,
                Configuration::getConfigValue));
    }

    public String get(String key) {
        return configRepository.findById(key).map(Configuration::getConfigValue).orElse(null);
    }

    public void set(String key, String value) {
        configRepository.save(new Configuration(key, value));
    }
}
