/*
 * Copyright 2020 The DocOps Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.docops.asciidoctorj.extension.panels

import io.docops.asciidoc.buttons.service.ScriptLoader
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import org.asciidoctor.log.LogRecord
import org.asciidoctor.log.Severity
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.*
import java.util.zip.GZIPOutputStream


@Name("panels")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
open class PanelsBlockProcessor : BlockProcessor() {
    private var scriptLoader = ScriptLoader()
    private var server = "http://localhost:8010/extension"
    private var webserver = "http://localhost:8010/extension"
    private var localDebug = false
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val remoteServer = parent.document.attributes["panel-server"]
        if (remoteServer != null) {
            remoteServer as String
            server = remoteServer
        }
        val remoteWebserver = parent.document.attributes["panel-webserver"]
        remoteWebserver?.let {
            webserver = it as String
        }
        val content = subContent(reader, parent, localDebug)
        val debug = parent.document.attributes["local-debug"]
        if (debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }
        val format = attributes.getOrDefault("format", "dsl")
        var width = attributes.getOrDefault("width", "") as String
        val role = attributes.getOrDefault("role", "center") as String
        val backend = parent.document.getAttribute("backend") as String
        val idea = parent.document.getAttribute("env", "") as String
        val table = attributes.getOrDefault("table", "false") as String
        val filename = attributes.getOrDefault("name", "${System.currentTimeMillis()}_unk") as String
        if (serverPresent(server, parent, this, localDebug)) {
            log(LogRecord(Severity.DEBUG, parent.sourceLocation, "Server is present"))
            val payload: String = try {
                compressString(content)
            } catch (e: Exception) {
                log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
                ""
            }
            log(LogRecord(Severity.DEBUG, parent.sourceLocation, "payload compressed is $payload"))
            var isPdf = "HTML"
            var ext = "svg"
            var imageType = "svg+xml"
            if ("pdf" == backend) {
                isPdf = "PDF"
                ext = "png"
                imageType = "png"
            } else if ("idea" == idea) {
                isPdf = "IDEA"
            }
            var widthNum = 970
            if (width.isNotEmpty()) {
                val pct: BigDecimal
                if(width.contains("%")) {
                     pct = BigDecimal(width.substring(0, width.length - 1))

                } else {
                    pct = BigDecimal(width)
                }
                val fact = pct.divide(BigDecimal(100))
                widthNum = fact.multiply(BigDecimal(widthNum)).intValueExact()
            }
            val url = if ("csv" == format) {
                "$webserver/api/panel/csv?type=$isPdf&data=$payload&file=panel_${System.currentTimeMillis()}.$ext"
            } else {
                "$server/api/panel?width=$widthNum&type=${isPdf}&data=$payload&file=xyz.$ext"
            }
            log(LogRecord(Severity.DEBUG, parent.sourceLocation, "Url for request is $url"))
            if (localDebug) {
                println("Url for request is $url")
            }

            val argAttributes: MutableMap<String, Any> = HashMap()
            argAttributes["content_model"] = ":raw"

            if (table.toBoolean()) {
                val linesArray = mutableListOf<String>()
                // language=asciidoc
                linesArray.add("""[cols="1",role="$role",$width,frame="none"]""")
                linesArray.add("|===")
                linesArray.add("")
                linesArray.add("a|image::$url[format=$ext,width=\"$widthNum\",role=\"$role\",opts=\"inline\",align=\"$role\"]")
                linesArray.add("")
                linesArray.add("|===")
                val svgBlock = createBlock(parent, "open", "", HashMap(), HashMap<Any, Any>())
                var pdfBlock: Block? = null
                if ("PDF" == isPdf) {
                    val lines = dslToLines(dsl = payload, parent = parent)
                    pdfBlock = createBlock(parent, "open", lines)
                    parseContent(pdfBlock, lines)
                }
                parseContent(svgBlock, linesArray)
                val block: Block = createBlock(parent, "open", "")
                block.blocks.add(svgBlock)
                pdfBlock?.let {
                    block.blocks.add(it)
                }
                return block
            } else {
                val svgBlock: Block?
                var pdfBlock: Block? = null
                if ("PDF" == isPdf) {
                    val lines = dslToLines(dsl = payload, parent = parent)
                    pdfBlock = createBlock(parent, "open", lines)
                    parseContent(pdfBlock, lines)
                    svgBlock = produceBlock(url, filename, parent, widthNum.toString(), role, format = ext)
                } else {
                    val image = getContentFromServer(url, parent, this, debug = localDebug)
                    val dataUri = "data:image/$imageType;base64," + Base64.getEncoder()
                        .encodeToString(image.toByteArray())
                    svgBlock = createImageBlockFromString(parent, image, role)
                    //svgBlock = produceBlock(dataUri, filename, parent, widthNum.toString(), role, format = ext)
                }
                val block: Block = createBlock(parent, "open", "")
                block.blocks.add(svgBlock)
                pdfBlock?.let {
                    block.blocks.add(it)
                }
                return block
            }
        }
        return null
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

    private fun createImageBlockFromString(parent: StructuralNode, svg: String, role: String): Block {
        val svgMap = mutableMapOf<String, Any>(
            "role" to "center",
            "opts" to "inline",
            "align" to "center",
            "width" to "500",
            "alt" to "IMG not available",
        )
        val content: String = """
            <div class="openblock">
            <div class="content" align="$role">
            $svg
            </div>
            </div>
        """.trimIndent()


        return createBlock(parent, "pass", content, svgMap, HashMap())
    }

    /*private fun strToPanelButtons(str: String): MutableList<PanelButton> {
        val result = mutableListOf<PanelButton>()
        str.lines().forEach { line ->
            val items = line.split("|")
            val pb = PanelButton()
            pb.label = items[0].trim()
            pb.link = items[1].trim()
            if (items.size == 3) {
                pb.description = items[2]
            }
            result.add(pb)
        }
        return result
    }*/


    private fun dslToLines(dsl: String, parent: StructuralNode): List<String> {
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$server/api/panel/lines?data=$dsl&server=$webserver"))
            .timeout(Duration.ofSeconds(10))
            .build()
        try {
            val response = client.send(request, BodyHandlers.ofString())
            if (200 == response.statusCode()) {
                return response.body().lines()
            }
        } catch (e: Exception) {
            log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
            return emptyList()
        }
        return emptyList()
    }


}

fun getContentFromServer(url: String, parent: StructuralNode, pb: BlockProcessor, debug: Boolean = false): String {
    if (debug) {
        println("getting image from url $url")
    }
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofMinutes(1))
        .build()
    return try {
        val response = client.send(request, BodyHandlers.ofString())
        response.body()
    } catch (e: Exception) {
        pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
        e.printStackTrace()
        ""
    }
}

fun serverPresent(server: String, parent: StructuralNode, pb: BlockProcessor, debug: Boolean = false): Boolean {
    if (debug) {
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
        val response = client.send(request, BodyHandlers.ofString())
        (200 == response.statusCode())
    } catch (e: Exception) {
        pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
        e.printStackTrace()
        false
    }
}

fun compressString(body: String): String {
    val baos = ByteArrayOutputStream()
    val zos = GZIPOutputStream(baos)
    zos.use { z ->
        z.write(body.toByteArray())
    }
    val bytes = baos.toByteArray()
    return Base64.getUrlEncoder().encodeToString(bytes)
}

fun subContent(reader: Reader, parent: StructuralNode, debug: Boolean = false): String {
    val content = reader.read()
    return subs(content, parent, debug)
}

fun subs(content: String, parent: StructuralNode, debug: Boolean = false): String {
    val pattern = """#\[.*?]""".toRegex()
    val res = pattern.findAll(content)
    var localContent = content
    res.forEach {
        val subValue = parent.document.attributes[it.value.replace("#[", "").replace("]", "").lowercase()]
        val key = it.value
        if (debug) {
            println("Text Substitution for $key & value to replace $subValue")
        }
        if (subValue != null) {
            subValue as String
            localContent = localContent.replace(key, subValue)
            if (debug) {
                println("content after substituting $key -> $localContent")
            }
        }
    }
    return localContent
}

@Name("panel")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class PanelBlockProcessor : PanelsBlockProcessor() {

}