package tterrag.core.common.command;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import org.apache.commons.lang3.ArrayUtils;

import tterrag.core.TTCore;
import tterrag.core.common.event.ConfigFileChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandReloadConfigs extends CommandBase
{
    public static final CommandReloadConfigs SERVER = new CommandReloadConfigs(Side.SERVER);
    public static final CommandReloadConfigs CLIENT = new CommandReloadConfigs(Side.CLIENT);

    private static List<String> validModIDs = new ArrayList<String>();
    
    private final Side side;

    static
    {
        TTCore.logger.info("Sending dummy event to all mods");

        for (ModContainer mod : Loader.instance().getActiveModList())
        {
            ConfigFileChangedEvent event = new ConfigFileChangedEvent(mod.getModId());
            FMLCommonHandler.instance().bus().post(event);

            if (event.isSuccessful())
            {
                validModIDs.add(mod.getModId());
            }
        }
    }

    @Override
    public String getCommandName()
    {
        return side == Side.SERVER ? "reloadServerConfigs" : "reloadConfigs";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "/" + getCommandName() + " <modid> (<modid2> <modid3> ...)";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender player)
    {
        return player.getEntityWorld().isRemote || super.canCommandSenderUseCommand(player);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender player, String[] args)
    {
        if (args.length >= 1)
        {
            String[] avail = validModIDs.toArray(new String[validModIDs.size()]);

            for (int i = 0; i < args.length - 1; i++)
            {
                avail = ArrayUtils.removeElement(avail, args[i]);
            }

            return getListOfStringsMatchingLastWord(args, avail);
        }

        return null;
    }

    @Override
    public void processCommand(ICommandSender player, String[] args)
    {
        if (side == Side.CLIENT == player.getEntityWorld().isRemote)
        for (String s : args)
        {
            boolean validModid = false;
            for (ModContainer mod : Loader.instance().getModObjectList().keySet())
            {
                if (mod.getModId().equals(s))
                {
                    validModid = true;
                }
            }

            if (validModid)
            {
                ConfigFileChangedEvent event = new ConfigFileChangedEvent(s);
                FMLCommonHandler.instance().bus().post(event);

                if (event.isSuccessful())
                {
                    sendResult(player, s, "success");
                }
                else
                {
                    sendResult(player, s, "fail");
                }
            }
            else
            {
                sendResult(player, s, "invalid");
            }
        }
    }

    private void sendResult(ICommandSender player, String modid, String result)
    {
        player.addChatMessage(new ChatComponentText(TTCore.lang.localize("command.config.result." + result, modid)));
    }
}
