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
    @Test
    fun testRelease() {
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
        val src = File("src/main/docs/strategy.adoc")
        val build = File("docs/")
        build.mkdirs()
        val target = File(build, "strategy.html")
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

    @Test
    fun testConnector() {
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
        val src = File("src/main/docs/connector.adoc")
        val build = File("docs/")
        build.mkdirs()
        val target = File(build, "connector.html")
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

    @Test
    fun testApiDocs () {
        val attrs = Attributes.builder()
            .sourceHighlighter("highlightjs")
            .allowUriRead(true)
            .dataUri(true)
            .copyCss(true)
            .noFooter(true)
            .attribute("highlightjs-theme", "idea")
            .attribute("rouge-css", "style")
            .attribute("coderay-css", "class")
            .attribute("coderay-linenums-mode", "inline")
            .attribute("tocbot")
            .attribute("local-debug", "true")
            .attribute("panel-webserver", "https://roach.gy/extension")
            .build()

        val asciidoctor = Asciidoctor.Factory.create()
        val src = File("/Users/steveroach/IdeaProjects/apidocs/target/generated-snippets/index.adoc")
        val build = File("docs/")
        build.mkdirs()
        val target = File(build, "index.html")
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

    @Test
    fun testLikeDislike() {
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
        val src = File("src/main/docs/likeDislike.adoc")
        val build = File("docs/")
        build.mkdirs()
        val target = File(build, "likeDislike.html")
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

    @Test
    fun testPieChart() {
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
            .build()

        val asciidoctor = Asciidoctor.Factory.create()
        val src = File("src/main/docs/piechart.adoc")
        val build = File("docs/")
        build.mkdirs()
        val target = File(build, "piechart.html")
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
            if (images.exists()) {
                images.deleteRecursively()
            }
            makePdf(src)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun testTreeChart() {
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
            .build()

        val asciidoctor = Asciidoctor.Factory.create()
        val src = File("src/main/docs/treechart.adoc")
        val build = File("docs/")
        build.mkdirs()
        val target = File(build, "treechart.html")
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
            if (images.exists()) {
                images.deleteRecursively()
            }
            makePdf(src)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
