package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
        block: StructuralNode
    ): String {
        return "image::$webserver/api/timeline/?payload=$payload&scale=$scale&title=${title.encodeUrl()}&type=SVG&filename=def.svg[format=svg,opts=inline,float=\"$role\",align='$role']"


    }

}
fun String.encodeUrl(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString());
}