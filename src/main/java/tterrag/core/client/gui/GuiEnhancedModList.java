package tterrag.core.client.gui;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.StringUtils;
import tterrag.core.TTCore;
import tterrag.core.common.config.ConfigHandler;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.client.GuiScrollingList;
import cpw.mods.fml.client.GuiSlotModList;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class GuiEnhancedModList extends GuiModList
{
    private enum SortType
    {
        NORMAL(24), A_TO_Z(25), Z_TO_A(26);

        private int buttonID;

        private SortType(int buttonID)
        {
            this.buttonID = buttonID;
        }

        public static SortType getTypeForButton(GuiButton button)
        {
            for (SortType t : values())
            {
                if (t.buttonID == button.id)
                {
                    return t;
                }
            }
            return null;
        }
    }

    private class InfoButton extends GuiButton
    {
        public InfoButton()
        {
            super(30, GuiEnhancedModList.this.width - 22, 2, 20, 20, "?");
        }

        @Override
        public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_)
        {
            if (this.field_146123_n)
            {
                ModContainer sel = GuiEnhancedModList.this.getSelectedMod();
                if (sel != null && sel.getName().equals(TTCore.NAME))
                {
                    this.displayString = TTCore.lang.localize("gui.modlistinfo2");
                }
                else
                {
                    this.displayString = TTCore.lang.localize("gui.modlistinfo1");
                }

                this.width = p_146112_1_.fontRenderer.getStringWidth(this.displayString) + 10;
                if (this.width % 2 != 0) // Fixes the button shifting to the left
                {
                    this.width++;
                }
                
                this.xPosition = GuiEnhancedModList.this.width - this.width - 2;
            }
            else
            {
                this.displayString = "?";
                this.width = 20;
                this.xPosition = GuiEnhancedModList.this.width - this.width - 2;
            }
            
            super.drawButton(p_146112_1_, p_146112_2_, p_146112_3_);
        }
    }

    private static Field _mods, _selected, _selectedMod;
    private static Field _modList, _modListRight, _modListBottom, _modListMods, _listWidth;
    static
    {
        try
        {
            _mods = GuiModList.class.getDeclaredField("mods");
            _mods.setAccessible(true);
            _selected = GuiModList.class.getDeclaredField("selected");
            _selected.setAccessible(true);
            _selectedMod = GuiModList.class.getDeclaredField("selectedMod");
            _selectedMod.setAccessible(true);
            _modList = GuiModList.class.getDeclaredField("modList");
            _modList.setAccessible(true);
            _modListRight = GuiScrollingList.class.getDeclaredField("right");
            _modListRight.setAccessible(true);
            _modListBottom = GuiScrollingList.class.getDeclaredField("bottom");
            _modListBottom.setAccessible(true);
            _modListMods = GuiSlotModList.class.getDeclaredField("mods");
            _modListMods.setAccessible(true);
            _listWidth = GuiModList.class.getDeclaredField("listWidth");
            _listWidth.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private int buttonMargin = 1;
    private int numButtons = SortType.values().length;

    private String lastFilterText = "";

    private GuiTextField search;
    private boolean sorted = false;
    private SortType sortType = SortType.values()[ConfigHandler.defaultModSort];

    public GuiEnhancedModList(GuiScreen mainMenu)
    {
        super(mainMenu);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui()
    {
        super.initGui();

        // Let's move some buttons
        for (GuiButton button : (List<GuiButton>) buttonList)
        {
            if (button.id == 6) // Done button
            {
                int min = getGuiModListRight();
                int max = width;
                button.xPosition = ((min + max) / 2) - (button.width / 2);
                button.yPosition += 10;
            }
            else if (button.id == 20 || button.id == 21) // Config/Disable
            {
                button.yPosition += 10;
            }
        }

        setGuiModListBottom(getGuiModListBottom() - 25);
        search = new GuiTextField(mc.fontRenderer, 12, getGuiModListBottom() + 17, getListWidth() - 4, 14);
        search.setFocused(true);
        search.setCanLoseFocus(true);

        int width = (getListWidth() / numButtons);
        int x = 10, y = 10;
        GuiButton normalSort = new GuiButton(SortType.NORMAL.buttonID, x, y, width - buttonMargin, 20, TTCore.lang.localize("gui.normal"));
        normalSort.enabled = false;
        buttonList.add(normalSort);
        x += width + buttonMargin;
        buttonList.add(new GuiButton(SortType.A_TO_Z.buttonID, x, y, width - buttonMargin, 20, "A-Z"));
        x += width + buttonMargin;
        buttonList.add(new GuiButton(SortType.Z_TO_A.buttonID, x, y, width - buttonMargin, 20, "Z-A"));

        buttonList.add(new InfoButton());
        
        reloadMods();
        disableButton();
    }

    @Override
    protected void mouseClicked(int x, int y, int button)
    {
        super.mouseClicked(x, y, button);
        search.mouseClicked(x, y, button);
        if (button == 1 && x >= search.xPosition && x < search.xPosition + this.width && y >= search.yPosition && y < search.yPosition + this.height)
        {
            search.setText("");
        }
    }

    @Override
    protected void keyTyped(char p_73869_1_, int p_73869_2_)
    {
        super.keyTyped(p_73869_1_, p_73869_2_);
        search.textboxKeyTyped(p_73869_1_, p_73869_2_);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        search.updateCursorCounter();

        if (!search.getText().equals(lastFilterText))
        {
            reloadMods();
            sorted = false;
        }

        if (!sorted)
        {
            switch (sortType)
            {
            case A_TO_Z:
                Collections.sort(getMods(), new Comparator<ModContainer>()
                {
                    @Override
                    public int compare(ModContainer o1, ModContainer o2)
                    {
                        return compareNames(o1, o1);
                    }
                });
                break;
            case Z_TO_A:
                Collections.sort(getMods(), new Comparator<ModContainer>()
                {
                    @Override
                    public int compare(ModContainer o1, ModContainer o2)
                    {
                        return compareNames(o2, o1);
                    }
                });
                break;
            default:
                reloadMods();
                break;
            }
            setMods();
            sorted = true;
        }
    }

    private int compareNames(ModContainer o1, ModContainer o2)
    {
        String name1 = StringUtils.stripControlCodes(o1.getName());
        String name2 = StringUtils.stripControlCodes(o2.getName());
        return name1.compareTo(name2);
    }

    private void reloadMods()
    {
        List<ModContainer> mods = getMods();
        mods.clear();
        for (ModContainer m : Loader.instance().getActiveModList())
        {
            if (m.getName().toLowerCase().contains(search.getText().toLowerCase()) && m.getMetadata().parentMod == null)
            {
                mods.add(m);
            }
        }
        setMods();
        lastFilterText = search.getText();
    }

    @Override
    public void drawScreen(int p_571_1_, int p_571_2_, float p_571_3_)
    {
        super.drawScreen(p_571_1_, p_571_2_, p_571_3_);

        String text = TTCore.lang.localize("gui.search");
        int x = ((10 + getGuiModListRight()) / 2) - (mc.fontRenderer.getStringWidth(text) / 2);
        mc.fontRenderer.drawString(text, x, getGuiModListBottom() + 5, 0xFFFFFF);
        search.drawTextBox();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id == 30)
        {
            search.setText("");
            reloadMods();
            for (ModContainer m : getMods())
            {
                if (m.getName().equals(TTCore.NAME))
                {
                    setSelectedMod(m);
                    setMods();
                }
            }
        }

        SortType type = SortType.getTypeForButton(button);

        if (type == null)
        {
            return;
        }
        else
        {
            sorted = false;
            sortType = type;
            disableButton();
        }

        setMods();
    }
    
    @SuppressWarnings("unchecked")
    private void disableButton()
    {
        for (GuiButton b : (List<GuiButton>) buttonList)
        {
            if (SortType.getTypeForButton(b) != null)
            {
                b.enabled = true;
            }
            if (b.id == sortType.buttonID)
            {
                b.enabled = false;
            }
        }
    }

    @SneakyThrows
    private void setMods()
    {
        List<ModContainer> mods = getMods();

        ModContainer sel = getSelectedMod();
        boolean found = false;
        for (int i = 0; !found && i < mods.size(); i++)
        {
            if (sel == mods.get(i))
            {
                _selected.setInt(this, i);
                found = true;
            }
        }
        if (!found)
        {
            _selected.setInt(this, -1);
            _selectedMod.set(this, null);
        }

        _mods.set(this, getMods());
    }

    @SneakyThrows
    private GuiSlotModList getGuiModList()
    {
        return (GuiSlotModList) _modList.get(this);
    }

    @SneakyThrows
    private int getGuiModListRight()
    {
        return _modListRight.getInt(getGuiModList());
    }

    @SneakyThrows
    private void setGuiModListBottom(int bottom)
    {
        _modListBottom.set(getGuiModList(), bottom);
    }

    @SneakyThrows
    private int getGuiModListBottom()
    {
        return _modListBottom.getInt(getGuiModList());
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private List<ModContainer> getMods()
    {
        return (List<ModContainer>) _modListMods.get(_modList.get(this));
    }

    @SneakyThrows
    private int getListWidth()
    {
        return _listWidth.getInt(this);
    }

    @SneakyThrows
    private ModContainer getSelectedMod()
    {
        return (ModContainer) _selectedMod.get(this);
    }

    @SneakyThrows
    private void setSelectedMod(ModContainer mod)
    {
        _selectedMod.set(this, mod);
    }
}
