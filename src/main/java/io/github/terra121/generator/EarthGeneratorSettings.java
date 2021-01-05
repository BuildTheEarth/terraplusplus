package io.github.terra121.generator;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.api.DeserializationException;
import io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.api.SyntaxError;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.PresetLoadError;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.transform.ScaleProjection;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class EarthGeneratorSettings {

    public JsonSettings settings;
    private Gson gson;

    public EarthGeneratorSettings(String generatorSettings) {
        if (!TerraConfig.reducedConsoleMessages) {
            TerraMod.LOGGER.info(generatorSettings);
        }

        this.gson = new GsonBuilder().create();

        if (generatorSettings.isEmpty()) { //blank string means default
            this.settings = new JsonSettings();
        } else {
            try {
                this.settings = this.gson.fromJson(generatorSettings, JsonSettings.class);
            } catch (JsonSyntaxException e) {
                TerraMod.LOGGER.error("Invalid Earth Generator Settings, using default settings");
                this.settings = new JsonSettings();
            }
        }
    }

    @Override
    public String toString() {
        return this.gson.toJson(this.settings, JsonSettings.class);
    }

    public CustomGeneratorSettings getCustomCubic() {
        if (Strings.isNullOrEmpty(this.settings.customcubic)) { //use new minimal defaults
            CustomGeneratorSettings cfg = new CustomGeneratorSettings();
            cfg.mineshafts = cfg.caves = cfg.strongholds = cfg.dungeons = cfg.ravines = false;
            cfg.lakes.clear();
            cfg.waterLevel = 0;
            return cfg;
        } else {
            return this.customCubicFromJson(this.settings.customcubic);
        }
    }

    //Crappy attempt to coerce custom cubic settings
    private CustomGeneratorSettings customCubicFromJson(String jsonString) {
        try {
            return CustomGenSettingsSerialization.jankson().fromJsonCarefully(jsonString, CustomGeneratorSettings.class);
        } catch (PresetLoadError | DeserializationException err) {
            throw new RuntimeException(err);
        } catch (SyntaxError err) {
            String message = err.getMessage() + '\n' + err.getLineMessage();
            throw new RuntimeException(message, err);
        }
    }

    public GeographicProjection getProjection() {
        GeographicProjection p = GeographicProjection.orientProjection(
                GeographicProjection.projections.get(this.settings.projection), this.settings.orentation);

        if (this.settings.scaleX == 1 && this.settings.scaleY == 1) {
            FMLCommonHandler.instance().exitJava(-1, false);
        }

        return new ScaleProjection(p, this.settings.scaleX, this.settings.scaleY);
    }

    public GeographicProjection getNormalizedProjection() {
        return GeographicProjection.orientProjection(
                GeographicProjection.projections.get(this.settings.projection), GeographicProjection.Orientation.upright);
    }

    //json template to be filled by Gson
    public static class JsonSettings {
        public String projection = "equirectangular";
        public GeographicProjection.Orientation orentation = GeographicProjection.Orientation.none;
        public double scaleX = 100000.0d;
        public double scaleY = 100000.0d;
        public boolean smoothblend = true;
        public boolean roads = true;
        public String customcubic = "";
        public boolean dynamicbaseheight = true;
        @Deprecated
        public boolean osmwater = true;
        public boolean buildings = true;
    }
}