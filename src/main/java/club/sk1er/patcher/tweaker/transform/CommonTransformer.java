package club.sk1er.patcher.tweaker.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * Used to string across multiple transformers that all do the same thing
 * without having to use duplicated code if it contains a {@link PatcherTransformer} method,
 * as those cannot be made a static method.
 */
public interface CommonTransformer extends PatcherTransformer {
    default void makeNametagTransparent(MethodNode methodNode) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        LabelNode afterDraw = new LabelNode();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                String nodeName = mapMethodNameFromNode(node);
                if (nodeName.equals("begin") || nodeName.equals("func_181668_a")) {
                    AbstractInsnNode prevNode = node.getPrevious().getPrevious().getPrevious();
                    methodNode.instructions.insertBefore(prevNode, getPatcherSetting("disableNametagBoxes", "Z"));
                    methodNode.instructions.insertBefore(prevNode, new JumpInsnNode(Opcodes.IFNE, afterDraw));
                } else if (nodeName.equals("draw") || nodeName.equals("func_78381_a")) {
                    methodNode.instructions.insert(node, afterDraw);
                    break;
                }
            }
        }
    }

    default void makeNametagShadowed(MethodNode methodNode) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                String nodeName = mapMethodNameFromNode(node);
                if (nodeName.equals("drawString") || nodeName.equals("func_78276_b")) {
                    methodNode.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        getHookClass("NameTagRenderingHooks"),
                        "drawNametagText",
                        "(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)I",
                        false));
                }
            }
        }
    }

    default void changeChatComponentHeight(MethodNode methodNode) {
        Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            if (node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKESTATIC) {
                String methodInsnName = mapMethodNameFromNode(node);

                if (methodInsnName.equals("floor_float") || methodInsnName.equals("func_76141_d")) {
                    for (int i = 0; i < 4; ++i) {
                        node = node.getPrevious();
                    }

                    methodNode.instructions.insertBefore(node, minus12());
                    break;
                }
            }
        }
    }

    default void cachePlayerHoverEvents(MethodNode methodNode) {
        methodNode.instructions.insert(getCacheTime());
        final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            final AbstractInsnNode next = iterator.next();
            if (next instanceof MethodInsnNode && next.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                final String methodInsnName = mapMethodNameFromNode(next);
                if (methodInsnName.equals("setChatHoverEvent") || methodInsnName.equals("func_150209_a")) {
                    iterator.next();
                    iterator.remove();
                    for (int i = 0; i < 5; i++) {
                        iterator.previous();
                        iterator.remove();
                    }
                    break;
                }
            }
        }

        methodNode.instructions.insertBefore(methodNode.instructions.getLast().getPrevious(), setCacheTime());
    }

    default InsnList setCacheTime() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "displayNameCache", "Lnet/minecraft/util/IChatComponent;"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "displayNameCacheTime", "J"));
        return list;
    }

    default InsnList getCacheTime() {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "displayNameCacheTime", "J"));
        list.add(new InsnNode(Opcodes.LSUB));
        list.add(new LdcInsnNode(50));
        list.add(new InsnNode(Opcodes.I2L));
        list.add(new InsnNode(Opcodes.LCMP));
        LabelNode ifge = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFGE, ifge));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "displayNameCache", "Lnet/minecraft/util/IChatComponent;"));
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(ifge);
        return list;
    }

    default InsnList minus12() {
        InsnList list = new InsnList();
        LabelNode afterSub = new LabelNode();
        list.add(getPatcherSetting("chatPosition", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, afterSub));
        list.add(new IincInsnNode(7, -12));
        list.add(afterSub);
        return list;
    }

    default InsnList modifyNametagRenderState(boolean voidReturnType) {
        InsnList list = new InsnList();
        list.add(getPatcherSetting("betterHideGui", "Z"));
        LabelNode ifeq = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/Minecraft", isDevelopment() ? "getMinecraft" : "func_71410_x", "()Lnet/minecraft/client/Minecraft;", false));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", isDevelopment() ? "gameSettings" : "field_71474_y", "Lnet/minecraft/client/settings/GameSettings;"));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/settings/GameSettings", isDevelopment() ? "hideGUI" : "field_74319_N", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        if (!voidReturnType) {
            list.add(new InsnNode(Opcodes.ICONST_0));
        }
        list.add(new InsnNode(voidReturnType ? Opcodes.RETURN : Opcodes.IRETURN));
        list.add(ifeq);
        return list;
    }
}
