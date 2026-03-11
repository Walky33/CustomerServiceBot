package com.atome.bot.service;

import com.atome.bot.model.Configuration;
import com.atome.bot.repositories.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @InjectMocks
    private ConfigService service;

    @BeforeEach
    void setUp() {
        service = new ConfigService(configRepository);
        reset(configRepository);
    }

    @Test
    void constructor_insertsDefaultsWhenKeysAreMissing() {
        reset(configRepository);
        when(configRepository.findById("kb_url")).thenReturn(Optional.empty());
        when(configRepository.findById("additional_guidelines")).thenReturn(Optional.empty());

        service = new ConfigService(configRepository);

        verify(configRepository, times(2)).save(any(Configuration.class));
    }

    @Test
    void constructor_doesNotInsertDefaultsWhenKeysAlreadyExist() {
        reset(configRepository);
        when(configRepository.findById("kb_url")).thenReturn(Optional.of(new Configuration("kb_url", "x")));
        when(configRepository.findById("additional_guidelines")).thenReturn(Optional.of(new Configuration("additional_guidelines", "y")));

        service = new ConfigService(configRepository);

        verify(configRepository, never()).save(any(Configuration.class));
    }

    @Test
    void getAll_returnsKeyValueMap() {
        when(configRepository.findAll()).thenReturn(List.of(
                new Configuration("kb_url", "u"),
                new Configuration("additional_guidelines", "g")
        ));

        var result = service.getAll();

        assertEquals("u", result.get("kb_url"));
        assertEquals("g", result.get("additional_guidelines"));
    }

    @Test
    void get_returnsValueWhenPresent() {
        when(configRepository.findById("kb_url")).thenReturn(Optional.of(new Configuration("kb_url", "u")));
        assertEquals("u", service.get("kb_url"));
    }

    @Test
    void get_returnsNullWhenMissing() {
        when(configRepository.findById("missing")).thenReturn(Optional.empty());
        assertNull(service.get("missing"));
    }

    @Test
    void set_savesConfiguration() {
        service.set("kb_url", "new-value");

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(configRepository).save(captor.capture());
        assertEquals("kb_url", captor.getValue().getConfigKey());
        assertEquals("new-value", captor.getValue().getConfigValue());
    }
}
