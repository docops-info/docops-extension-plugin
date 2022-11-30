package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader

@Name("badge")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class BadgeBlockProcessor : BlockProcessor() {

    private var server = "http://localhost:8010/extension"
    //image:http://localhost:8010/extension/api/badge/item?label=ABC&message=512&color=RED&fname=abc.svg[]
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val content = reader.read()
        val remoteServer = parent.document.attributes["panel-server"]
        if (remoteServer != null) {
            remoteServer as String
            server = remoteServer
        }
        val backend = parent.document.getAttribute("backend") as String
        val lines = mutableListOf<String>()
        content.lines().forEach { line ->
            val split = line.split("|")
            var imageLink = "image:"
            if ("pdf" == backend) {
                imageLink = "image::"
            }
            if(split.size>=2) {
                var str = "$imageLink$server/api/badge/item?label=${split[0]}&amp;message=${split[1]}"
                var link = ""
                if(split.size>2 ) {
                    link += ",link=\"${split[2]}\""
                }
                if(split.size >3) {
                    str += "&amp;color=${split[3]}"
                }
                str += "&amp;filename=abc.svg[format=svg $link]"

                lines.add(str)
            }
        }
        parseContent(parent, lines)
        return null
    }
}