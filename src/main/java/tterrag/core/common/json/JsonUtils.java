package tterrag.core.common.json;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.oredict.OreDictionary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils
{
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Object parseStringIntoRecipeItem(String string)
    {
        return parseStringIntoRecipeItem(string, false);
    }

    public static Object parseStringIntoRecipeItem(String string, boolean forceItemStack)
    {
        if (OreDictionary.getOres(string).isEmpty())
        {
            ItemStack stack = null;

            String[] info = string.split(";");
            Object temp = null;
            int damage = OreDictionary.WILDCARD_VALUE;
            temp = Item.itemRegistry.getObject(info[0]);
            if (info.length > 1)
            {
                damage = Integer.parseInt(info[1]);
            }

            if (temp instanceof Item)
            {
                stack = new ItemStack((Item) temp, 1, damage);
            }
            else if (temp instanceof Block)
            {
                stack = new ItemStack((Block) temp, 1, damage);
            }
            else if (temp instanceof ItemStack)
            {
                ((ItemStack) temp).setItemDamage(damage);
            }
            else
            {
                throw new IllegalArgumentException(string
                        + " is not a vaild string. Strings should be either an oredict name, or in the format objectname;damage (damage is optional)");
            }

            return stack;
        }
        else if (forceItemStack)
        {
            return OreDictionary.getOres(string).get(0);
        }
        else
        {
            return string;
        }
    }

    public static ItemStack parseStringIntoItemStack(String string)
    {
        int size = 1;
        int idx = string.indexOf('#');

        if (idx != -1)
        {
            String num = string.substring(idx + 1);

            try
            {
                size = Integer.parseInt(num);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(num + " is not a valid stack size");
            }

            string = string.substring(0, idx);
        }

        ItemStack stack = (ItemStack) parseStringIntoRecipeItem(string, true);
        stack.stackSize = MathHelper.clamp_int(size, 1, stack.getMaxStackSize());
        return stack;
    }
}
