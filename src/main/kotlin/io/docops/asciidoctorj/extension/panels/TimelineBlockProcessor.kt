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
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

@Name("timeline")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class TimelineBlockProcessor : BlockProcessor() {
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
        val role = attributes.getOrDefault("role", "center") as String
        val title = attributes.getOrDefault("title", "Title") as String
        val block: Block = createBlock(parent, "open", null as String?)
        if (serverPresent(server, parent, this, localDebug)) {
            var widthNum = 970
            if (width.isNotEmpty()) {
                val pct: Int
                if(width.contains("%")) {
                    pct = width.substring(0, width.length - 1).toInt()

                } else {
                    pct = width.toInt()
                }

                val fact = pct.toDouble().div(100)

                widthNum = fact.times(widthNum).toInt()
            }
            if ("pdf" == backend) {
                val payload: String = try {
                    compressString(content)
                } catch (e: Exception) {
                    log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
                    ""
                }
                val url = "$server/api/timeline/?payload=$payload&title=${title.encodeUrl()}"
                val lines =  getContentFromServer(url, parent,this, true)
                val pdfBlock = createBlock(parent, "open", lines)
                parseContent(pdfBlock, lines.lines())
                block.blocks.add(pdfBlock)
                return block
            }
            else {
                val url = "$server/api/timeline/?title=${title.encodeUrl()}"
                val resp = getContentFromServerPut(url, parent, this, localDebug, content)
                return createImageBlockFromString(parent, resp, role, width)
            }
        }
        return block
    }

    private fun createImageBlockFromString(parent: StructuralNode, svg: String, role: String, width: String): Block {

        val align = mutableMapOf(
            "right" to "margin-left: auto; margin-right: 0;",
            "left" to "",
            "center" to "margin: auto;"
        )
        val center = align[role.lowercase()]
        val content: String = """
            <div class="openblock">
            <div class="content" style="width: $width;padding: 10px;$center">
            $svg
            </div>
            </div>
        """.trimIndent()
        return createBlock(parent, "pass", content)
    }

    private fun getContentFromServerPut(url: String, parent: StructuralNode, pb: BlockProcessor, debug: Boolean = false, content: String): String {
        if (debug) {
            println("getting image from url $url")
        }
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(content))
            .timeout(Duration.ofMinutes(1))
            .build()
        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        } catch (e: Exception) {
            pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
            e.printStackTrace()
            ""
        }
    }

}
fun String.encodeUrl(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString());
}