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
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


@Name("badge")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class BadgeBlockProcessor : BlockProcessor() {

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
        val block: Block = createBlock(parent, "open", null as String?)
        val lines: MutableList<String> = if ("pdf" == backend) {
            makeContentForPdf(content, localDebug)
        } else {
            makeContentForHtml(content, localDebug)
        }
        parseContent(block, lines)
        return block
    }

    private fun makeContentForHtml(content: String, debug: Boolean): MutableList<String> {
        val lines = mutableListOf<String>()
        content.lines().forEach { line ->
            val payload = compressString(" $line")
            val imageLink = "image:"
            val split = line.split("|")
            var link = ""
            if (split.size > 2) {
                link = ",link=\"${split[2]}\""
            }
            val str =
                "$imageLink$webserver/api/badge/item?payload=$payload&type=SVG&finalname=abc_${System.currentTimeMillis()}.svg[format=svg$link] "
            lines.add(str)
        }
        if(debug) {
            lines.forEach { println(it) }
        }
        return lines
    }

    private fun produceBlock(
        dataSrc: String,
        filename: String,
        parent: StructuralNode,
        width: String,
        role: Any,
        format: String
    ): Block {

        val svgMap = mutableMapOf<String, Any>(
            "role" to "center",
            "opts" to "inline",
            "align" to "$role",
            "width" to width,
            "target" to dataSrc,
            "alt" to "IMG not available",
            "title" to "Figure. $filename",
            "format" to format
        )
        return this.createBlock(parent, "image", "", svgMap, HashMap())
    }
    private fun makeContentForPdf(content: String, debug: Boolean): MutableList<String> {
        val lines = mutableListOf<String>()
        lines.add("""[cols="1,1,1,1", grid=none, frame=none, role="center",width="90%"]""")
        lines.add("|===")
        content.lines().forEachIndexed { index, line ->
            val payload = compressString(" $line")
            val imageLink = "image::"
            val split = line.split("|")
            var link = ""
            if (split.size > 2) {
                link = ",link=\"${split[2]}\""
            }
            val str =
                "$imageLink$webserver/api/badge/item?payload=$payload&type=PDF&finalname=abc_${System.currentTimeMillis()}.png[format=png$link] "
            var direction = ">"
            if(index >0) {
                direction = "<"
            }
            lines.add("${direction}a|$str")
        }
        val size = content.lines().size
        val rem = size % 4
        if(rem == 1) {
            lines.add("|")
            lines.add("|")
        } else if(rem == 2) {
            lines.add("|")
        }
        lines.add("|===")
        if(debug) {
            lines.forEach { println(it) }
        }
        return lines
    }
    private fun getSvgContentFromServer(content: String, parent: StructuralNode) : String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$webserver/api/badges?type=SVG"))
            .POST(HttpRequest.BodyPublishers.ofString(content))
            .build()
        val client: HttpClient = HttpClient.newHttpClient()
        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (200 == response.statusCode()) {
                return response.body()
            }
        } catch (e: Exception) {
            log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
            return ""
        }
        return ""
    }

}