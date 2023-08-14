package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name

@Name("buttons")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class ButtonsBlockProcessor : AbstractDocOpsBlockProcessor() {
    override fun buildUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode,
        opts: String,
        attributes: MutableMap<String, Any>
    ): String {
        return """image::$webserver/api/buttons?payload=$payload&type=$type&filename=ghi.svg[$opts]"""
    }

    override fun getUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode,
        opts: String,
        attributes: MutableMap<String, Any>
    ): String {
        return """image::$webserver/api/buttons?payload=$payload&type=$type&filename=ghi.svg[$opts]"""
    }
}