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
import org.asciidoctor.jruby.AsciiDocDirectoryWalker
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class PanelsBlockProcessorTest {
    @Test
    fun testPanel() {
        val attrs = Attributes.builder()
            .sourceHighlighter("coderay")
            .allowUriRead(true)
            .dataUri(true)
            .copyCss(true)
            .noFooter(true)
            .attribute("coderay-css", "class")
            .attribute("coderay-linenums-mode", "inline")
            .attribute("feedback")
            .attribute("tocbot")
            .build()

        val asciidoctor = Asciidoctor.Factory.create()
        val src = File("src/main/docs/panel.adoc")
        val build = File("build/docs/")
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
        val str = asciidoctor.convertFile(src, options)

        assert(target.exists())
        val images = File(src.parent,"images")
        images.deleteRecursively()
        //target.deleteOnExit()
       // makePdf()
    }
    fun makePdf() {
        val attrs = Attributes.builder()
            .sourceHighlighter("coderay")
            .allowUriRead(true)
            .dataUri(true)
            .copyCss(true)
            .noFooter(true)
            .attribute("coderay-css", "class")
            .attribute("coderay-linenums-mode", "inline")
            .attribute("feedback")
            .attribute("tocbot")
            .build()
        val asciidoctor = Asciidoctor.Factory.create()
        val src = File("src/main/docs/panel.adoc")
        val build = File("build/docs/")
        build.mkdirs()
        val target = File(build, "panel.pdf")

        if(target.exists()) {
            target.delete()
        }
        val options = Options.builder()
            .backend("pdf")
            .toDir(build)
            .attributes(attrs)
            .safe(SafeMode.UNSAFE)
            .build()

        val str = asciidoctor.convertFile(src, options)

    }
}