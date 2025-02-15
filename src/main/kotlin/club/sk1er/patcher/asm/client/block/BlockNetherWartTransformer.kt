package club.sk1er.patcher.asm.client.block

import club.sk1er.patcher.tweaker.transform.PatcherTransformer
import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.insns.jvm.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class BlockNetherWartTransformer : PatcherTransformer {
    override fun getClassName() = arrayOf(
        "net.minecraft.block.BlockNetherWart"
    )

    override fun transform(classNode: ClassNode, name: String) {
        val getSelectedBoundingBox = MethodNode(
            Opcodes.ACC_PUBLIC,
            "func_180646_a",
            "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)Lnet/minecraft/util/AxisAlignedBB;",
            null,
            null
        )
        getSelectedBoundingBox.instructions.add(createGetSelectedBoundingBox().first)
        classNode.methods.add(getSelectedBoundingBox)

        val collisionRayTrace = MethodNode(
            Opcodes.ACC_PUBLIC,
            "func_180636_a",
            "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;",
            null,
            null
        )
        collisionRayTrace.instructions.add(createCollisionRayTrace().first)
        classNode.methods.add(collisionRayTrace)
    }

    private fun createCollisionRayTrace() = assembleBlock {
        aload_1
        aload_2
        aload_1
        aload_2
        invokevirtual(
            "net/minecraft/world/World",
            "func_180495_p",
            "net/minecraft/block/state/IBlockState",
            "net/minecraft/util/BlockPos"
        )
        invokeinterface("net/minecraft/block/state/IBlockState", "func_177230_c", "net/minecraft/block/Block")
        invokestatic(
            getHookClass("FarmHook"),
            "updateWartMaxY",
            void,
            "net/minecraft/world/World",
            "net/minecraft/util/BlockPos",
            "net/minecraft/block/Block"
        )
        aload_0
        aload_1
        aload_2
        aload_3
        aload(4)
        invokespecial(
            "net/minecraft/block/BlockBush",
            "func_180636_a",
            "net/minecraft/util/MovingObjectPosition",
            "net/minecraft/world/World",
            "net/minecraft/util/BlockPos",
            "net/minecraft/util/Vec3",
            "net/minecraft/util/Vec3"
        )
        areturn
    }

    private fun createGetSelectedBoundingBox() = assembleBlock {
        aload_1
        aload_2
        aload_1
        aload_2
        invokevirtual(
            "net/minecraft/world/World",
            "func_180495_p",
            "net/minecraft/block/state/IBlockState",
            "net/minecraft/util/BlockPos"
        )
        invokeinterface("net/minecraft/block/state/IBlockState", "func_177230_c", "net/minecraft/block/Block")
        invokestatic(
            getHookClass("FarmHook"),
            "updateWartMaxY",
            void,
            "net/minecraft/world/World",
            "net/minecraft/util/BlockPos",
            "net/minecraft/block/Block"
        )
        aload_0
        aload_1
        aload_2
        invokespecial(
            "net/minecraft/block/BlockBush",
            "func_180646_a",
            "net/minecraft/util/AxisAlignedBB",
            "net/minecraft/world/World",
            "net/minecraft/util/BlockPos"
        )
        areturn
    }
}