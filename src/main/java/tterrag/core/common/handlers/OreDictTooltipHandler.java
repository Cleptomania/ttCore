package tterrag.core.common.handlers;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.oredict.OreDictionary;
import tterrag.core.TTCore;
import tterrag.core.common.Handlers.Handler;
import tterrag.core.common.Handlers.Handler.HandlerType;
import tterrag.core.common.config.ConfigHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Handler(types = HandlerType.FORGE)
public class OreDictTooltipHandler
{
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event)
    {
        if (ConfigHandler.showOredictTooltips)
        {
            int[] ids = OreDictionary.getOreIDs(event.itemStack);

            if (ids.length > 0)
            {
                event.toolTip.add(TTCore.lang.localize("tooltip.oreDictNames"));
                for (int i : ids)
                {
                    event.toolTip.add("  - " + OreDictionary.getOreName(i));
                }
            }
        }
    }
}
