package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.PositionalAttributes
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

@Name("docops")
@PositionalAttributes(value = ["kind"])
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class DocOpsBlockProcessor: BlockProcessor()  {
    protected var server = "http://localhost:8010/extension"
    protected var webserver = "http://localhost:8010/extension"
    protected var localDebug = false

    override fun process(
        parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>
    ): Any? {
        debugOnOff(parent)
        val content = subContent(reader, parent, debug = localDebug)
        setupServers(parent)
        val block: Block = createBlock(parent, "open", null as String?)

        if(serverAvailable(parent = parent, pb = this)) {
            var type = getType(parent = parent)

            val payload = getCompressedPayload(parent = parent, content = content)
            val role = attributes.getOrDefault("role", "center") as String
            var opts = "format=svg,opts=inline,align='$role'"
            val kind = attributes.get("kind") as String
            if(kind.isEmpty()) {
                parseContent(parent, mutableListOf<String>("Parameter Error: Missing 'kind' block parameter, example -> [\"docops\", kind=\"buttons\"] 😵"))
            }
            val dark = attributes.getOrDefault("useDark", "false") as String
            val useDark : Boolean = "true".equals(dark, true)
            val scale = attributes.getOrDefault("scale", "1.0") as String
            val outlineColor = attributes.getOrDefault("outlineColor", "#7149c6") as String
            val title = attributes.getOrDefault("title", "Title") as String
            val numChars = getCharLength(attributes, 24)
            if(isIdeaOn(parent = parent)) {
                opts = ""
                val url = """$webserver/api/docops/svg?kind=$kind&payload=$payload&type=$type&useDark=$useDark&title=${title.encodeUrl()}&filename=ghi.svg"""
                val image = getContentFromServer(url, parent, this, debug = localDebug)
                return createImageBlockFromString(parent, image, role)
            } else {
                val lines = mutableListOf<String>()
                val url = if("PDF" == type) {
                    val iopts = "format=png,opts=inline,align='$role'"
                    """image::$webserver/api/docops/png?kind=$kind&payload=$payload&scale=$scale&outlineColor=${outlineColor.encodeUrl()}&title=${title.encodeUrl()}&numChars=$numChars&type=SVG&useDark=$useDark&filename=docops.png[$iopts]"""
                } else {
                    """image::$webserver/api/docops/svg?kind=$kind&payload=$payload&scale=$scale&outlineColor=${outlineColor.encodeUrl()}&title=${title.encodeUrl()}&numChars=$numChars&type=SVG&useDark=$useDark&filename=docops.svg[$opts]"""
                }
                if(localDebug) {
                    println(url)
                }
                lines.addAll(url.lines())
                parseContent(block, lines)
            }

        } else {
            parseContent(parent, mutableListOf<String>("DocOps Server Unavailable! 😵"))
        }
        return block
    }

    fun debugOnOff(parent: StructuralNode) {
        val debug = parent.document.attributes["local-debug"]
        if (debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }
    }

    fun setupServers(parent: StructuralNode) {
        val remoteServer = parent.document.attributes["panel-server"]
        remoteServer?.let {
            server = remoteServer as String
        }
        val remoteWebserver = parent.document.attributes["panel-webserver"]
        remoteWebserver?.let {
            webserver = it as String
        }
    }

    fun getCompressedPayload(parent: StructuralNode, content: String) : String {
        val payload: String = try {
            compressString(content)
        } catch (e: Exception) {
            log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
            ""
        }
        return payload
    }
    fun isIdeaOn(parent: StructuralNode): Boolean {
        val idea = parent.document.getAttribute("env", "") as String
        return "idea".equals(idea, true)
    }
    fun getType(parent: StructuralNode): String {
        var type ="SVG"
        val backend = parent.document.getAttribute("backend") as String
        if("pdf" == backend) {
            type = "PDF"
        }
        return type
    }

    private fun createImageBlockFromString(
        parent: StructuralNode,
        svg: String,
        role: String,
        width: String = "970"
    ): Block {

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
    fun serverAvailable(parent: StructuralNode, pb: BlockProcessor) : Boolean {
        if (localDebug) {
            println("Checking if server is present ${server}/api/ping")
        }
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$server/api/ping"))
            .timeout(Duration.ofMinutes(1))
            .build()
        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            (200 == response.statusCode())
        } catch (e: Exception) {
            pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
            e.printStackTrace()
            false
        }
    }
    protected fun String.encodeUrl(): String {
        return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
    }

    protected fun getCharLength(attributes: MutableMap<String, Any>, defaultLength: Int) : String {
        return attributes.getOrDefault("numChars", "$defaultLength") as String
    }
}