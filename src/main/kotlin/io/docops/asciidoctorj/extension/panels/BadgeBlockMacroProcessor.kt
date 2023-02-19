package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name

@Name("badge")
class BadgeBlockMacroProcessor: BlockMacroProcessor() {
    private var server = "http://localhost:8010/extension"
    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): Any? {
        val content = subs(target, parent)
        val remoteServer = parent.document.attributes["panel-server"]
        if (remoteServer != null) {
            remoteServer as String
            server = remoteServer
        }
        val backend = parent.document.getAttribute("backend") as String
        val lines = mutableListOf<String>()

        content.lines().forEach {
                line ->
            val payload = compressString(" $line")
            var imageLink = "image:"
            if ("pdf" == backend) {
                imageLink = "image::"
            }
            val split = line.split("|")
            var link = ""
            if(split.size>2 ) {
                link = ",link=\"${split[2]}\""
            }
            val str = "$imageLink$server/api/badge/item?payload=$payload&finalname=abc.svg[format=svg $link] "
            lines.add(str)
        }
        parseContent(parent, lines)
        return null
    }
}