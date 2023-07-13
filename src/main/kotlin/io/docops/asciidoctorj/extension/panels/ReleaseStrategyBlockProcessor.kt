package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name

@Name("release")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class ReleaseStrategyBlockProcessor : AbstractDocOpsBlockProcessor(){



    override fun buildUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode
    ): String {
        return """
image::$webserver/api/release/?payload=$payload&type=SVG&filename=def.svg[format=svg,opts=inline,float="$role",align='$role']

link:$webserver/api/release/?payload=$payload&type=XLS&filename=def.xls[Excel]
""".trimIndent()
    }

}