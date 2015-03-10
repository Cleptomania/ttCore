package tterrag.core.common.transform;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class TTCoreTransformerClient extends TTCoreTransformer
{
    private static final String scrollingListClass = "cpw.mods.fml.client.GuiScrollingList";
    private static final ObfSafeName drawScreen = new ObfSafeName("func_73863_a", "drawScreen");

    private static final String slotModListClass = "cpw.mods.fml.client.GuiSlotModList";
    private static final ObfSafeName drawBackground = new ObfSafeName("drawBackground", "drawBackground");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (transformedName.equals(scrollingListClass))
        {
            basicClass = transform(basicClass, scrollingListClass, drawScreen, new Transform()
            {
                @Override
                void transform(Iterator<MethodNode> methods)
                {
                    while (methods.hasNext())
                    {
                        MethodNode m = methods.next();
                        if (drawScreen.equals(m.name))
                        {
                            for (int i = 0; i < m.instructions.size(); i++)
                            {
                                AbstractInsnNode n = m.instructions.get(i);
                                if (n instanceof InsnNode && n.getOpcode() == ICONST_4)
                                {
                                    AbstractInsnNode next = m.instructions.get(++i);
                                    if (next instanceof VarInsnNode && next.getOpcode() == ISTORE && ((VarInsnNode) next).var == 15)
                                    {
                                        i += 2;
                                        AbstractInsnNode insertPoint = m.instructions.get(i++);
                                        m.instructions.remove(m.instructions.get(i));
                                        m.instructions.remove(m.instructions.get(i));
                                        m.instructions.remove(m.instructions.get(i));
                                        m.instructions.remove(m.instructions.get(i));
                                        m.instructions.insert(insertPoint, new InsnNode(ICONST_1));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            });
        }
        else if (transformedName.equals(slotModListClass))
        {
            basicClass = transform(basicClass, slotModListClass, drawBackground, new Transform()
            {
                @SuppressWarnings("deprecation")
                @Override
                void transform(Iterator<MethodNode> methods)
                {
                    while (methods.hasNext())
                    {
                        MethodNode m = methods.next();
                        if (drawBackground.equals(m.name))
                        {
                            m.instructions.clear();
                            ObfSafeName drawBackground = new ObfSafeName("drawBackground", "func_148123_a");
                            String fieldOwner = "cpw/mods/fml/client/GuiSlotModList";
                            String fieldName = "parent";
                            String fieldType = "Lcpw/mods/fml/client/GuiModList;";

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, fieldOwner, fieldName, fieldType));
                            list.add(new InsnNode(ICONST_0));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "cpw/mods/fml/client/GuiModList", drawBackground.getName(), "(I)V"));
                            list.add(new InsnNode(RETURN));

                            m.instructions.insert(list);
                            break;
                        }
                    }
                }
            });
        }
        return basicClass;
    }
}
