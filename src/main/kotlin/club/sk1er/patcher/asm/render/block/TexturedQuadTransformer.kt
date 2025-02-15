package club.sk1er.patcher.asm.render.block

import club.sk1er.hookinjection.injectInstructions
import club.sk1er.patcher.hooks.TexturedQuadHook
import club.sk1er.patcher.tweaker.transform.PatcherTransformer
import org.objectweb.asm.tree.ClassNode

class TexturedQuadTransformer : PatcherTransformer {
    override fun getClassName() = arrayOf("net.minecraft.client.model.TexturedQuad")

    override fun transform(classNode: ClassNode, name: String) {
        classNode.methods.first {
            val methodName = mapMethodName(classNode, it)
            methodName == "draw" || methodName == "func_178765_a"
        }?.apply {
            clearInstructions(this)
            injectInstructions {
                of(TexturedQuadHook::draw)
                into(this@apply)
                params(0, 1, 2)
                keepReturns
            }
        }
    }
}