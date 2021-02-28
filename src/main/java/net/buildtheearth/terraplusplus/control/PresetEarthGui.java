package net.buildtheearth.terraplusplus.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.util.Strings;
import org.lwjgl.input.Keyboard;

import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * The basic world preset GUI.
 * 
 * @author SmylerMC
 */
public class PresetEarthGui extends GuiScreen {

	public static final Map<String, String> DEFAULT_PRESETS = new HashMap<String, String>();

	static {
		// We parse then toString to remove all whitespaces
		DEFAULT_PRESETS.put("default", EarthGeneratorSettings.parse(EarthGeneratorSettings.DEFAULT_SETTINGS).toString());
		DEFAULT_PRESETS.put("bte", EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).toString());
	}

	protected GuiScreen parentScreen;
	protected Consumer<String> whenDone;
	protected String settings;
	protected List<Preset> presets = new ArrayList<>();
	protected GuiButton doneButton;
	protected GuiButton cancelButton;
	protected GuiTextField presetTextField;

	public static final int OPTION_IMG_WIDTH = 50;
	public static final int OPTION_IMG_HEIGHT = OPTION_IMG_WIDTH;
	public static final int OPTION_FOOTER_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 10;
	public static final int PADDING = 40;

	public PresetEarthGui(GuiScreen parentScreen, String settings, Consumer<String> whenDone) {
		this.parentScreen = parentScreen;
		this.whenDone = whenDone;
		this.settings = Strings.isBlank(settings) ? EarthGeneratorSettings.DEFAULT_SETTINGS: settings;
	}

	@Override
	public void initGui() {
		super.initGui();

		Keyboard.enableRepeatEvents(true);

		this.buttonList.clear();
		this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
		this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

		this.presets.clear();
		boolean foundPreset = false;
		int entryCount = DEFAULT_PRESETS.size() + 1;
		int x = (this.width - entryCount * OPTION_IMG_WIDTH - (entryCount - 1) * PADDING) / 2;
		int y = (this.height - OPTION_IMG_HEIGHT - OPTION_FOOTER_HEIGHT) / 2;
		for(String preset: DEFAULT_PRESETS.keySet()) {
			DefaultPreset p = new DefaultPreset(x, y, preset, DEFAULT_PRESETS.get(preset));
			if(p.presetSettings.equals(this.settings)) {
				p.selected = true;
				foundPreset = true;
			}
			this.presets.add(p);
			x += OPTION_IMG_WIDTH + PADDING;
		}
		AdvancedPreset advanced = new AdvancedPreset(x, y);
		if(!foundPreset) advanced.selected = true;
		this.presets.add(advanced);

		this.presetTextField = new GuiTextField(0, this.fontRenderer, this.width / 2 - 100, 40, 200, 20);
		this.presetTextField.setMaxStringLength(Integer.MAX_VALUE);
		this.setSettingsJson(this.settings);

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRenderer, I18n.format(TerraConstants.MODID + ".presetscreen.title"), this.width / 2, 15, 0xFFFFFFFF);
		this.presetTextField.drawTextBox();
		for(Preset preset: this.presets) {
			preset.draw(this.isMouseOverEntry(mouseX, mouseY, preset));
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.presetTextField.updateCursorCounter();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		String before = this.presetTextField.getText();
		this.presetTextField.textboxKeyTyped(typedChar, keyCode);
		String newText = this.presetTextField.getText();
		if(!before.equals(newText)) {
			try {
				EarthGeneratorSettings.parseUncached(newText);
				this.presetTextField.setTextColor(0xFFFFFFFF);
				this.doneButton.enabled = true;
			} catch(Exception e) {
				this.presetTextField.setTextColor(0xFFFF6060);
				this.doneButton.enabled = false;
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.presetTextField.mouseClicked(mouseX, mouseY, mouseButton);
		for(Preset preset: this.presets) {
			if(this.isMouseOverEntry(mouseX, mouseY, preset)) {
				for(Preset p: PresetEarthGui.this.presets) {
					p.selected  = false;
				}
				preset.selected = true;
				preset.onClick();
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button == this.cancelButton) {
			Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
		} else if(button == this.doneButton) {
			this.whenDone.accept(this.settings);
			Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
		}
	}

	private boolean isMouseOverEntry(int x, int y, Preset preset) {
		return preset.x < x && x < preset.x+ OPTION_IMG_WIDTH && preset.y < y && y < preset.y + OPTION_IMG_HEIGHT;
	}

	private void setSettingsJson(String settings) {
		this.settings = settings;
		this.presetTextField.setText(settings);
		this.presetTextField.setTextColor(0xFFFFFFFF);
		this.presetTextField.setCursorPosition(0);
		this.doneButton.enabled = true;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}



	private abstract class Preset {

		protected ResourceLocation texture;
		protected String langKey;
		protected int x, y;
		protected boolean selected = false;

		public Preset(int x, int y, String name) {
			this.x = x;
			this.y = y;
			this.langKey = TerraConstants.MODID + ".preset." + name;
			this.texture = new ResourceLocation(TerraConstants.MODID, "textures/presets/" + name + ".png");
		}

		public void draw(boolean hover) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
			if(this.selected) {
				Gui.drawRect(this.x - 5, this.y - 4, this.x - 4, this.y + OPTION_IMG_HEIGHT + 4, 0x60FFFFFF);
				Gui.drawRect(this.x - 4, this.y + OPTION_IMG_HEIGHT + 5, this.x + OPTION_IMG_WIDTH + 4, this.y + OPTION_IMG_HEIGHT + 4, 0x60FFFFFF);
				Gui.drawRect(this.x + OPTION_IMG_WIDTH + 4, this.y + OPTION_IMG_HEIGHT + 4, this.x + OPTION_IMG_WIDTH + 5, this.y - 4, 0x60FFFFFF);
				Gui.drawRect(this.x - 4, this.y - 4, this.x + OPTION_IMG_WIDTH + 4, this.y - 5, 0x60FFFFFF);
				Gui.drawRect(this.x - 4, this.y - 4, this.x + OPTION_IMG_WIDTH + 4, this.y + OPTION_IMG_HEIGHT + 4, 0x80000000);
			}
			GlStateManager.color(1f, 1f, 1f);
			int x, y, width, height, textColor;
			if(hover) {
				x = this.x - 3;
				y = this.y - 3;
				width = OPTION_IMG_WIDTH + 6;
				height = OPTION_IMG_HEIGHT + 6;
				textColor = 0xFF6666FF;
			} else {
				x = this.x;
				y = this.y;
				width = OPTION_IMG_WIDTH;
				height = OPTION_IMG_HEIGHT;
				textColor = 0xFFFFFFFF;
			}
			Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
			PresetEarthGui.this.drawCenteredString(Minecraft.getMinecraft().fontRenderer, I18n.format(this.langKey), this.x + OPTION_IMG_WIDTH / 2, this.y + OPTION_IMG_HEIGHT + 10, textColor);
		}

		public abstract void onClick();

	}

	private class DefaultPreset extends Preset {

		protected String presetSettings;

		public DefaultPreset(int x, int y, String name, String settings) {
			super(x, y, name);
			this.presetSettings = settings;
		}

		@Override
		public void onClick() {
			PresetEarthGui.this.setSettingsJson(this.presetSettings);
		}

	}

	private class AdvancedPreset extends Preset {

		public AdvancedPreset(int x, int y) {
			super(x, y, "advanced");
		}

		@Override
		public void onClick() {
			EarthGeneratorSettings genSettings = EarthGeneratorSettings.parse(PresetEarthGui.this.settings);
			Minecraft.getMinecraft().displayGuiScreen(new AdvancedEarthGui(PresetEarthGui.this, genSettings, PresetEarthGui.this::setSettingsJson));
		}

	}

}
