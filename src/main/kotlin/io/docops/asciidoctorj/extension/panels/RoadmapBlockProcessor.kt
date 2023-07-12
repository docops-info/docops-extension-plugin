package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import org.asciidoctor.log.LogRecord
import org.asciidoctor.log.Severity

@Name("roadmap")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class RoadmapBlockProcessor : BlockProcessor(){
    private var server = "http://localhost:8010/extension"
    private var webserver = "http://localhost:8010/extension"
    private var localDebug = false
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val debug = parent.document.attributes["local-debug"]
        if (debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }
        val content = subContent(reader, parent, localDebug)
        val remoteServer = parent.document.attributes["panel-server"]
        remoteServer?.let {
            server = remoteServer as String
        }
        val remoteWebserver = parent.document.attributes["panel-webserver"]
        remoteWebserver?.let {
            webserver = it as String
        }
        val backend = parent.document.getAttribute("backend") as String
        val idea = parent.document.getAttribute("env", "") as String
        val width = attributes.getOrDefault("width", "") as String
        val scale = attributes.getOrDefault("scale", "1.0") as String
        val block: Block = createBlock(parent, "open", null as String?)
        if (serverPresent(server, parent, this, localDebug)) {
            var widthNum = 970
            if (width.isNotEmpty()) {
                val pct: Int
                pct = if(width.contains("%")) {
                    width.substring(0, width.length - 1).toInt()

                } else {
                    width.toInt()
                }
                val fact = pct.toDouble().div(100)
                widthNum = fact.times(widthNum).toInt()
            }

            val payload: String = try {
                compressString(content)
            } catch (e: Exception) {
                log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
                ""
            }
            val lines = mutableListOf<String>()
            val url = "image::$webserver/api/roadmap/?payload=$payload&type=SVG&scale=$scale&filename=ghi.svg[format=svg,opts=inline,width=$widthNum]"
            if(localDebug) {
                println(url)
            }
            lines.add(url)
            //lines.add("$webserver/api/release/?payload=$payload&type=XLS&filename=def.xls[Excel]")
            parseContent(block, lines)
        }
        return block
    }
}