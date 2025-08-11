package net.buildtheearth.terraminusminus.generator;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EarthGeneratorSettingsTest {

    @Test
    void testDefaults() {
        final EarthGeneratorSettings defaultSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.DEFAULT_SETTINGS);
        final EarthGeneratorSettings bteSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
        this.testNonNullFields(defaultSettings);
        this.testNonNullFields(bteSettings);
    }

    private void testNonNullFields(EarthGeneratorSettings settings) {
        assertNotNull(settings.projection());
        assertNotNull(settings.datasets());
        assertNotNull(settings.biomeProvider());
    }

}
