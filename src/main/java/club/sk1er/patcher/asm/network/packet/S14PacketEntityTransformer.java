package club.sk1er.patcher.asm.network.packet;

import club.sk1er.patcher.tweaker.transform.PatcherTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class S14PacketEntityTransformer implements PatcherTransformer {
    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.network.play.server.S14PacketEntity"};
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            String methodName = mapMethodName(classNode, methodNode);

            if (methodName.equals("getEntity") || methodName.equals("func_149065_a")) {
                clearInstructions(methodNode);
                methodNode.instructions.insert(getFixedEntity(
                    "net/minecraft/network/play/server/S14PacketEntity",
                    "field_149074_a")
                );
                break;
            }
        }
    }

    public static InsnList getFixedEntity(String owner, String entityId) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        LabelNode ifnull = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNULL, ifnull));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,
            owner,
            entityId,
            "I"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
            "net/minecraft/world/World",
            "func_73045_a", // getEntityByID
            "(I)Lnet/minecraft/entity/Entity;",
            false));
        LabelNode gotoInsn = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.GOTO, gotoInsn));
        list.add(ifnull);
        list.add(new InsnNode(Opcodes.ACONST_NULL));
        list.add(gotoInsn);
        list.add(new InsnNode(Opcodes.ARETURN));
        return list;
    }
}
