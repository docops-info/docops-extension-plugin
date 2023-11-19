package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name

@Name("connector")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class DocOpsBlockProcessor: AbstractDocOpsBlockProcessor() {
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
        return "image::$webserver/api/connector/?payload=$payload&scale=$scale&type=SVG&useDark=$useDark&filename=lmn.svg[$opts]"
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
        return "$webserver/api/connector/?payload=$payload&scale=$scale&type=SVG&useDark=$useDark&filename=lmn.svg"
    }
}