package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name

@Name("timeline")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class TimelineBlockProcessor : AbstractDocOpsBlockProcessor() {

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
        val numChars = getCharLength(attributes, 24)
        return "image::$webserver/api/timeline/?payload=$payload&scale=$scale&title=${title.encodeUrl()}&numChars=$numChars&type=SVG&filename=def.svg[$opts]"
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
        val numChars = getCharLength(attributes, 24)
        return "$webserver/api/timeline/?payload=$payload&scale=$scale&title=${title.encodeUrl()}&numChars=$numChars&type=SVG&filename=def.svg"

    }
}
