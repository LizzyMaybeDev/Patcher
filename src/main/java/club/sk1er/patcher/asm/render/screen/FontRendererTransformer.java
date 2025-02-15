package club.sk1er.patcher.asm.render.screen;

import club.sk1er.patcher.tweaker.ClassTransformer;
import club.sk1er.patcher.tweaker.transform.PatcherTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class FontRendererTransformer implements PatcherTransformer {

    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.gui.FontRenderer"};
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    @Override
    public void transform(ClassNode classNode, String name) {
        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "patcherFontRenderer", "L" + getHookClass("FontRendererHook;"), null, null));
        for (MethodNode methodNode : classNode.methods) {
            final String methodName = mapMethodName(classNode, methodNode);
            if (!ClassTransformer.smoothFontDetected) {
                if (methodNode.name.equals("<init>")) {
                    methodNode.instructions.insertBefore(methodNode.instructions.getLast().getPrevious(), fontRendererHookInit());
                } else if (methodName.equals("getStringWidth") || methodName.equals("func_78256_a")) {
                    clearInstructions(methodNode);
                    methodNode.instructions.insert(getStringWidthHook());
                } else if (methodName.equals("renderStringAtPos") || methodName.equals("func_78255_a")) {
                    methodNode.instructions.insert(renderStringAtPosHook());
                }
            }

            if ((methodName.equals("renderString") || methodName.equals("func_180455_b")) && methodNode.desc.equals("(Ljava/lang/String;FFIZ)I")) {
                methodNode.instructions.insert(getShadowHook());
            } else if ((methodName.equals("drawString") || methodName.equals("func_175065_a")) && methodNode.desc.equals("(Ljava/lang/String;FFIZ)I")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    final AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode && next.getOpcode() == Opcodes.INVOKESTATIC && ((MethodInsnNode) next).name.equals("max")) {
                        AbstractInsnNode previous = next;
                        for (int nodes = 0; nodes < 8; nodes++) {
                            previous = previous.getPrevious();
                        }

                        methodNode.instructions.insertBefore(previous, resetStyle());
                    }
                }

                iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    final AbstractInsnNode next = iterator.next();
                    if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.FLOAD && ((VarInsnNode) next).var == 2) {
                        methodNode.instructions.remove(next.getNext());
                        methodNode.instructions.remove(next.getNext());

                        methodNode.instructions.insertBefore(next, insertCleanShadowLabel());
                        methodNode.instructions.remove(next);
                    }
                }
            }
        }
    }

    private InsnList resetStyle() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/gui/FontRenderer", "func_78265_b", "()V", false));
        return list;
    }

    private InsnList insertCleanShadowLabel() {
        final InsnList list = new InsnList();
        list.add(getPatcherSetting("alternateTextShadow", "Z"));
        final LabelNode ifeq = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(new VarInsnNode(Opcodes.FLOAD, 2));
        final LabelNode gotoInsn = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.GOTO, gotoInsn));
        list.add(ifeq);
        list.add(new VarInsnNode(Opcodes.FLOAD, 2));
        list.add(new InsnNode(Opcodes.FCONST_1));
        list.add(new InsnNode(Opcodes.FADD));
        list.add(gotoInsn);
        return list;
    }

    private InsnList fontRendererHookInit() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new TypeInsnNode(Opcodes.NEW, getHookClass("FontRendererHook")));
        list.add(new InsnNode(Opcodes.DUP));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, getHookClass("FontRendererHook"), "<init>", "(Lnet/minecraft/client/gui/FontRenderer;)V", false));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "patcherFontRenderer", "L" + getHookClass("FontRendererHook;")));
        return list;
    }

    private InsnList renderStringAtPosHook() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "patcherFontRenderer", "L" + getHookClass("FontRendererHook;")));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ILOAD, 2));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, getHookClass("FontRendererHook"), "renderStringAtPos", "(Ljava/lang/String;Z)Z", false));
        final LabelNode labelNode = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
        list.add(new InsnNode(Opcodes.RETURN));
        list.add(labelNode);
        return list;
    }

    private InsnList getStringWidthHook() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "patcherFontRenderer", "L" + getHookClass("FontRendererHook;")));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, getHookClass("FontRendererHook"), "getStringWidth", "(Ljava/lang/String;)I", false));
        list.add(new InsnNode(Opcodes.IRETURN));
        return list;
    }

    private InsnList getShadowHook() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        LabelNode ifeq = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(getPatcherSetting("disableShadowedText", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(new LdcInsnNode(0));
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(ifeq);
        return list;
    }
}
