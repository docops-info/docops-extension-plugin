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

package io.docops.extension.plugin

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.junit.jupiter.api.Test
import java.io.File

internal class PanelsBlockProcessorTest {
    @Test
    fun testPanel() {
        val attrs = Attributes.builder()
            .sourceHighlighter("highlightjs")
            .allowUriRead(true)
            .dataUri(true)
            .copyCss(true)
            .noFooter(true)
            .attribute("highlightjs-theme", "dark")
            .attribute("rouge-css", "style")
            .attribute("coderay-css", "class")
            .attribute("coderay-linenums-mode", "inline")
            .attribute("feedback")
            .attribute("tocbot")
            .attribute("local-debug", "true")
            .attribute("panel-webserver", "http://localhost:8010/extension")
            .build()

        val asciidoctor = Asciidoctor.Factory.create()
        val src = File("src/main/docs/panel.adoc")
        val build = File("docs/")
        build.mkdirs()
        val target = File(build, "panel.html")
        if(target.exists()) {
            target.delete()
        }
        val options = Options.builder()
            .backend("html5")
            .toDir(build)
            .attributes(attrs)
            .safe(SafeMode.UNSAFE)
            .build()
        try {
            asciidoctor.convertFile(src, options)

            assert(target.exists())
            val images = File(src.parent,"images")
            images.deleteRecursively()
            //target.deleteOnExit()
            makePdf(src)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun makePdf(filepath: File) {
        val attrs = Attributes.builder()
            .sourceHighlighter("rouge")
            .showTitle(true)
            .allowUriRead(true)
            .dataUri(true)
            .copyCss(true)
            .noFooter(true)
            .attribute("rouge-css", "style")
            .attribute("coderay-css", "class")
            .attribute("coderay-linenums-mode", "inline")
            .attribute("tocbot")
            .attribute("panel-webserver", "http://localhost:8010/extension")
            .build()
        val asciidoctor = Asciidoctor.Factory.create()
        val src = filepath
        val build = File("docs/")
        build.mkdirs()
        val targetName = filepath.name.replace(".adoc", ".pdf")
        val target = File(build, targetName)

        if(target.exists()) {
            target.delete()
        }
        val options = Options.builder()
            .backend("pdf")
            .toDir(build)
            .attributes(attrs)
            .safe(SafeMode.UNSAFE)
            .build()

        asciidoctor.convertFile(src, options)

    }


}
