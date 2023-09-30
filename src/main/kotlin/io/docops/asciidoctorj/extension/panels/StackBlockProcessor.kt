package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader

@Name("stack")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class StackBlockProcessor : AbstractDocOpsBlockProcessor() {
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val title = attributes.getOrDefault("3", "") as String
        val fig = attributes.getOrDefault("2", "") as String
        val content = reader.read()
        val backend = parent.document.getAttribute("backend")
        val env = parent.document.getAttribute("env") as String
        var pdf = "html"
        if ("pdf" == backend) {
            pdf = "pdf"
        }
        val encoded = compressString(content)
        val imageUrl = "http://localhost:7001/imageserver/api/stacked?encoded=$encoded&title=$title&type=$pdf"
        return imageBlock(env, pdf, parent, imageUrl, fig)
    }

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
        return "http://localhost:7001/imageserver/api/stacked?encoded=$payload&title=$title&useDark=$useDark&type=$type"
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
        TODO("Not yet implemented")
    }

}