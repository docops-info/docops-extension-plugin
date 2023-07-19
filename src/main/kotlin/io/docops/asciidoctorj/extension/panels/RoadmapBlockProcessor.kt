package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name

@Name("roadmap")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class RoadmapBlockProcessor : AbstractDocOpsBlockProcessor(){


    override fun buildUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode,
        idea: String
    ): String {
        var opts = "format=svg,opts=inline,float=\"$role\",align='$role'"
        if("idea".equals(idea, true))
        {
            opts=""
        }
        val fname = System.currentTimeMillis()
        return "image::$webserver/api/roadmap/?payload=$payload&type=SVG&scale=$scale&title=$title&filename=ghi$fname.svg[$opts]"
    }
}