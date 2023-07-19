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
open class PanelsBlockProcessor : AbstractDocOpsBlockProcessor() {

    override fun buildUrl(
        payload: String,
        scale: String,
        title: String,
        type: String,
        role: String,
        block: StructuralNode,
        opts: String,
        attributes: MutableMap<String, Any>
    ): String {
        return "image::$webserver/api/panel?type=SVG&data=$payload&filename=def.svg[$opts]"
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
class PanelBlockProcessor : PanelsBlockProcessor()