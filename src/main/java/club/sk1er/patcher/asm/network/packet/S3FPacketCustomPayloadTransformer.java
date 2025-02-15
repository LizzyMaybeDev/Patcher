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

public class S3FPacketCustomPayloadTransformer implements PatcherTransformer {
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.network.play.server.S3FPacketCustomPayload"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode method : classNode.methods) {
            String methodName = mapMethodName(classNode, method);

            if (methodName.equals("processPacket") || methodName.equals("func_148833_a")) {
                method.instructions.insertBefore(method.instructions.getLast().getPrevious(), releaseData());
                break;
            }
        }
    }

    private InsnList releaseData() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/play/server/S3FPacketCustomPayload", "field_149171_b", "Lnet/minecraft/network/PacketBuffer;"));
        LabelNode ifnull = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNULL, ifnull));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/play/server/S3FPacketCustomPayload", "field_149171_b", "Lnet/minecraft/network/PacketBuffer;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "release", "()Z", false));
        list.add(new InsnNode(Opcodes.POP));
        list.add(ifnull);
        return list;
    }
}
