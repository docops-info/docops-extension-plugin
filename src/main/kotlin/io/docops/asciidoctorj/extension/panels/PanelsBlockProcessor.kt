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

import io.docops.asciidoc.buttons.dsl.PanelButton
import io.docops.asciidoc.buttons.dsl.Panels
import io.docops.asciidoc.buttons.dsl.font
import io.docops.asciidoc.buttons.dsl.panels
import io.docops.asciidoc.buttons.service.PanelService
import io.docops.asciidoc.buttons.service.ScriptLoader
import io.docops.asciidoc.buttons.theme.Grouping
import io.docops.asciidoc.buttons.theme.GroupingOrder
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
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any {
        val remoteServer = parent.document.attributes["panel-server"]
        if (remoteServer != null) {
            remoteServer as String
            server = remoteServer
        }
        var content = subContent(reader, parent)

        val format = attributes.getOrDefault("format", "dsl")
        var filename = attributes.getOrDefault("2", "${System.currentTimeMillis()}_unk") as String
        val backend = parent.document.getAttribute("backend") as String
        val idea = parent.document.getAttribute("env", "") as String
        if (serverPresent(server,parent, this)) {
            log(LogRecord(Severity.DEBUG, parent.sourceLocation, "Server is present"))
            val payload: String = try {
                compressString(content)
            } catch (e: Exception) {
                log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
                ""
            }
            log(LogRecord(Severity.DEBUG, parent.sourceLocation, "payload compressed is $payload"))
            var isPdf = "HTML"
            if ("pdf" == backend) {
                isPdf = "PDF"
            } else if("idea" == idea) {
                isPdf = "IDEA"
            }
            val url = if ("csv" == format) {
                "$server/api/panel/csv?type=$isPdf&data=$payload"
            } else {
                "$server/api/panel?type=$isPdf&data=$payload"
            }
            log(LogRecord(Severity.DEBUG, parent.sourceLocation, "Url for request is $url"))
            val svgBlock: Block = if ("html5".equals(backend, true) || "idea" == idea) {
                // language=html
                val imageStr = getContentFromServer(url, parent, this)
                createBlock(parent, "pass", imageStr)
            }
            else {
                produceBlock(url = url, filename = filename, parent = parent)
            }
            var pdfBlock: Block? = null
            if ("PDF" == isPdf) {
                val lines = dslToLines(dsl = payload, parent = parent)
                pdfBlock = createBlock(parent, "open", lines)
            }
            val argAttributes: MutableMap<String, Any> = HashMap()
            argAttributes["content_model"] = ":raw"
            val block: Block = createBlock(parent, "open", "", argAttributes, HashMap<Any, Any>())
            block.blocks.add(svgBlock)

            pdfBlock?.let {
                parseContent(block, it.lines)
            }
            return block
        } else {

            val panels: Panels
            if ("csv" == format) {
                panels = panels {
                    columns = 3
                    panelButtons = strToPanelButtons(content)
                    theme {
                        layout {
                            columns = 2
                            groupBy = Grouping.TITLE
                            groupOrder = GroupingOrder.ASCENDING
                        }
                        font {
                            color = "#000000"
                            bold = true
                        }
                    }
                }

            } else {
                // language=KTS
                val source = """
            import io.docops.asciidoc.buttons.dsl.*
            import io.docops.asciidoc.buttons.models.*
            import io.docops.asciidoc.buttons.theme.*
            import io.docops.asciidoc.buttons.*
            
            $content
        """.trimIndent()
                panels = scriptLoader.parseKotlinScript(source = source)
            }
            val panelService = PanelService()
            var pdfBlock: Block? = null
            if ("pdf" == backend) {
                panels.isPdf = true
                filename += "_pdf"
                val lines = panelService.toLines(filename, panels, server)
                pdfBlock = createBlock(parent, "open", lines)
            }

            val imgSrc = panelService.fromPanelToSvg(panels)
            val argAttributes: MutableMap<String, Any> = HashMap()
            argAttributes["content_model"] = ":raw"
            val block: Block = createBlock(parent, "open", "", argAttributes, HashMap<Any, Any>())
            val imgBlock = createImageBlockFromString(parent = parent, svg = imgSrc)
            block.blocks.add(imgBlock)
            pdfBlock?.let {
                parseContent(block, pdfBlock.lines)
            }
            return block
        }
    }

    private fun produceBlock(url: String, filename: String, parent: StructuralNode): Block {

        val svgMap = mutableMapOf<String, Any>(
            "role" to "docops.io.panels",
            "target" to url,
            "alt" to "IMG not available",
            "title" to "Figure. $filename",
            "opts" to "interactive",
            "format" to "svg"
        )
        return this.createBlock(parent, "image", ArrayList(), svgMap, HashMap())
    }

    private fun createImageBlockFromString(parent: StructuralNode, svg: String): Block {
        return createBlock(parent, "pass", svg)
    }

    private fun strToPanelButtons(str: String): MutableList<PanelButton> {
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
    }


    private fun dslToLines(dsl: String, parent: StructuralNode): List<String> {
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$server/api/panel/lines?data=$dsl&server=$server"))
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
fun getContentFromServer(url: String, parent: StructuralNode, pb: BlockProcessor): String {
    println("getting image from url $url")
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
fun serverPresent(server: String, parent: StructuralNode, pb: BlockProcessor): Boolean {
    println("Checking if server is present ${server}/api/ping")
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
fun subContent(reader: Reader, parent: StructuralNode): String {
    val content = reader.read()
    return subs(content, parent)
}
fun subs(content: String, parent: StructuralNode): String {
    val pattern = """\#\[.*?\]""".toRegex()
    val res = pattern.findAll(content)
    var localContent = content
    res.forEach {
        val subValue = parent.document.attributes[it.value.replace("#[", "").replace("]","").lowercase()]
        val key = it.value
        if (subValue != null) {
            subValue as String
            localContent = localContent.replace(key, subValue)
        }
    }
    return localContent
}
@Name("panel")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class  PanelBlockProcessor : PanelsBlockProcessor() {

}