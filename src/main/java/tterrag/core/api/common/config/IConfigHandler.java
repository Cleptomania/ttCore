package tterrag.core.api.common.config;

import java.io.File;
import java.util.List;

import net.minecraftforge.common.config.ConfigCategory;
import tterrag.core.common.config.AbstractConfigHandler.Section;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public interface IConfigHandler
{
    void initialize(File cfg);

    List<Section> getSections();

    ConfigCategory getCategory(String name);

    String getModID();
    
    /**
     * A hook for the {@link FMLInitializationEvent}, also called during config reloads depending on
     * {@link #shouldHookOnReload()}
     */
    void initHook();
    
    /**
     * A hook for the {@link FMLPostInitializationEvent}, also called during config reloads
     * depending on {@link #shouldHookOnReload()}
     */
    void postInitHook();
}
