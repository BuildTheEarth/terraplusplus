package net.buildtheearth.terraminusminus.generator;

import org.junit.Test;

import static org.junit.Assert.*;

public class EarthGeneratorSettingsTest {

    @Test
    public void testDefaults() {
        final EarthGeneratorSettings defaultSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.DEFAULT_SETTINGS);
        final EarthGeneratorSettings bteSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
        this.testNonNullFields(defaultSettings);
        this.testNonNullFields(bteSettings);
    }

    public void testNonNullFields(EarthGeneratorSettings settings) {
        assertNotNull(settings.projection());
        assertNotNull(settings.datasets());
        assertNotNull(settings.biomeProvider());
    }

}
