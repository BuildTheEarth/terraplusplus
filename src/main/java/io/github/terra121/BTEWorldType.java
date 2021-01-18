package io.github.terra121;


import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BTEWorldType extends EarthWorldType {

	public static final String BTE_GENERATOR_SETTINGS = "{\"projection\":\"bteairocean\",\"orentation\":\"upright\",\"scaleX\":7318261.522857145,\"scaleY\":7318261.522857145,\"smoothblend\":true,\"roads\":true,\"customcubic\":\"\",\"dynamicbaseheight\":true,\"osmwater\":true,\"buildings\":true}";

	public static BTEWorldType create() {
		return new BTEWorldType();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onGuiActionPerformedPre(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		if(event.getGui() instanceof GuiCreateWorld) {
			GuiCreateWorld screen = (GuiCreateWorld) event.getGui();
			if(event.getButton().id == 0) { //This is the create world button
				
				//Get the selected world type index
				//The absence of any error handling here is voluntary,
				//we'd rather crash than let users create worlds with the wrong settings without them knowing
				int selectedIndex = ObfuscationReflectionHelper.getPrivateValue(GuiCreateWorld.class, screen, "field_146331_K");
				
				if(WorldType.WORLD_TYPES[selectedIndex] instanceof BTEWorldType) {
					screen.chunkProviderSettingsJson = BTE_GENERATOR_SETTINGS;
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTranslationKey() {
		return "generator.bte";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getInfoTranslationKey() {
		return "generator.bte.info";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasInfoNotice() {
		return true;
	}

	@Override
	public boolean isCustomizable() {
		return false;
	}

}
