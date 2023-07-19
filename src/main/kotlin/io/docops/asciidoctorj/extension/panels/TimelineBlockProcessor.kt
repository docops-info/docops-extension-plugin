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
        idea: String
    ): String {
        var opts = "format=svg,opts=inline,float=\"$role\",align='$role'"
        if("idea".equals(idea, true))
        {
            opts=""
        }
        return "image::$webserver/api/timeline/?payload=$payload&scale=$scale&title=${title.encodeUrl()}&type=SVG&filename=def.svg[$opts]"


    }

}
