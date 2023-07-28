package tterrag.core.client.gui;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;

import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lombok.SneakyThrows;
import tterrag.core.common.Handlers.Handler;
import tterrag.core.common.Handlers.Handler.HandlerType;

@Handler(HandlerType.FORGE)
public class GuiEventHandler {

    private static Field _mainMenu, _parentScreen;

    static {
        try {
            _mainMenu = GuiModList.class.getDeclaredField("mainMenu");
            _mainMenu.setAccessible(true);
            _parentScreen = GuiIngameModOptions.class.getDeclaredField("parentScreen");
            _parentScreen.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    @SneakyThrows
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null) {
            return;
        }
        if (event.gui.getClass() == GuiModList.class) {
            event.setCanceled(true);
            Minecraft.getMinecraft()
                .displayGuiScreen(new GuiEnhancedModList((GuiScreen) _mainMenu.get(event.gui)));
        }
        if (event.gui.getClass() == GuiIngameModOptions.class) {
            event.setCanceled(true);
            Minecraft.getMinecraft()
                .displayGuiScreen(new GuiEnhancedModList((GuiScreen) _parentScreen.get(event.gui)));
        }
    }
}
