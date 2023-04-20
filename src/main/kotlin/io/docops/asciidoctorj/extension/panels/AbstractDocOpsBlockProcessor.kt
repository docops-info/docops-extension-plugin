package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor

abstract class AbstractDocOpsBlockProcessor: BlockProcessor() {
    protected fun imageBlock(
        env: String,
        pdf: String,
        parent: StructuralNode,
        imageUrl: String,
        fig: String
    ): Block {
        val block =
            when {
                "idea" == env && "pdf" != pdf -> {
                    createBlock(parent, "pass", "<img src='$imageUrl'></img>")
                }
                else -> {
                    val blockAttrs = mutableMapOf<String, Any>(
                        "role" to "docops.io.panels",
                        "target" to imageUrl,
                        "alt" to "IMG not available",
                        "title" to "Figure. $fig",
                        "interactive-option" to "",
                        "format" to "svg"
                    )
                    createBlock(parent, "image", ArrayList(), blockAttrs, HashMap())
                }
            }
        return block
    }
}