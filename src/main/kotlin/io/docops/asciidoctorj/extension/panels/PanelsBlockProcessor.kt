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

import io.docops.asciidoc.buttons.dsl.FontWeight
import io.docops.asciidoc.buttons.dsl.PanelButton
import io.docops.asciidoc.buttons.dsl.Panels
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
import java.io.File


@Name("panels")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class PanelsBlockProcessor : BlockProcessor() {
    private var scriptLoader = ScriptLoader()
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any {
        var filename = attributes.getOrDefault("2", "${System.currentTimeMillis()}_unk") as String
        val format = attributes.getOrDefault("format", "dsl")
        val content = reader.read()
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
                        weight = FontWeight.bold
                    }
                }
            }

        } else {
            val source = """
            import io.docops.asciidoc.buttons.dsl.*
            import io.docops.asciidoc.buttons.models.*
            import io.docops.asciidoc.buttons.theme.*
            import io.docops.asciidoc.buttons.*
            
            $content
        """.trimIndent()
            panels = scriptLoader.parseKotlinScript(source = source)
        }
        val backend = parent.document.getAttribute("backend")
        val panelService = PanelService()
        var pdfBlock: Block? = null
        if ("pdf" == backend) {
            panels.isPdf = true
            filename += "_pdf"
            val lines = panelService.toLines(filename, panels)
            pdfBlock = createBlock(parent, "open", lines)
        }

        val imgSrc = panelService.fromPanelToSvg(panels)
        val imgDir = parent.document.getAttribute("imagesdir")
        var target = "images/${filename}.svg"
        if (imgDir != null) {
            target = "${filename}.svg"
        }
        val svg = File("${reader.dir}/images/${filename}.svg")
        val p = svg.parentFile
        if(!p.exists()) {
            p.mkdirs()
        }
        svg.writeBytes(imgSrc.toByteArray())
        val blockAttrs = mutableMapOf<String, Any>(
            "role" to "docops.io.panels",
            "target" to target,
            "alt" to "IMG not available",
            "title" to "Figure. $filename",
            "interactive-option" to "",
            "opts" to "inline",
            "format" to "svg"

        )
        val argAttributes: MutableMap<String, Any> = HashMap()
        argAttributes["content_model"] = ":raw"
        val block: Block = createBlock(parent, "open", "", argAttributes, HashMap<Any, Any>())
        block.blocks.add(createBlock(parent, "image", ArrayList(), blockAttrs, HashMap()))
        pdfBlock?.let {
            parseContent(block, pdfBlock.lines)
        }
        return block
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
}