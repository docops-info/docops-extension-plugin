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
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry

class PanelsRegistry : ExtensionRegistry {
    override fun register(asciidoctor: Asciidoctor) {
        val registry = asciidoctor.javaExtensionRegistry()
        registry.block(PanelsBlockProcessor::class.java)
        //registry.docinfoProcessor(FeedbackDocinfoProcessor::class.java)
        // registry.docinfoProcessor(FeedbackDivDocInfoProcessor::class.java)
        registry.blockMacro(ColorMapBlockMacroProcessor::class.java)
    }

}