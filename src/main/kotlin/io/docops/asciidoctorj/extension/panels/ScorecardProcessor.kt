package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name


@Name("scorecard")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class ScorecardProcessor : AbstractDocOpsBlockProcessor() {
    override fun buildUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode,
        opts: String,
        attributes: MutableMap<String, Any>,
        useDark: Boolean
    ): String {
        return """image::$webserver/api/scorecard/?payload=$payload&type=$type&useDark=$useDark&filename=ghi.svg[$opts]"""
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
        useDark: Boolean
    ): String {
        return """$webserver/api/scorecard/?payload=$payload&type=$type&useDark=$useDark&filename=ghi.svg"""
    }
}