package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.IncludeProcessor
import org.asciidoctor.extension.PreprocessorReader
import java.io.File

class DocOpsInclude : IncludeProcessor() {
    override fun handles(target: String?): Boolean {
        return true
    }

    override fun process(
        document: Document,
        reader: PreprocessorReader,
        target: String,
        attributes: MutableMap<String, Any>
    ) {
        if(document.isBasebackend("html")) {
            document.attributes["docfile"]?.let {
                val f = File(it.toString())
                if(f.exists()) {
                    println("Document ${f.name} includes -> $target")
                }
            }
        }
        reader.pushInclude("",target,  File(".").absolutePath,
            1,
            attributes)
    }
}