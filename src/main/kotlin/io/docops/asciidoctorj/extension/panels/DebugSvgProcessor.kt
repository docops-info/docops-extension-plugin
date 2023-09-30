package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name

@Name("debug")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class DebugSvgProcessor : AbstractDocOpsBlockProcessor() {

    override fun buildUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode,
        opts: String,
        attributes: MutableMap<String, Any>,
        useDark: Boolean,
        outlineColor: String
    ): String {
        return """image::$webserver/api/svg/debug?payload=$payload&type=$type&useDark=$useDark&filename=ghi.svg[$opts]"""
    }

    override fun getUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode,
        opts: String,
        attributes: MutableMap<String, Any>,
        useDark: Boolean,
        outlineColor: String
    ): String {
        return """$webserver/api/svg/debug?payload=$payload&type=$type&useDark=$useDark&filename=ghi.svg"""
    }
}