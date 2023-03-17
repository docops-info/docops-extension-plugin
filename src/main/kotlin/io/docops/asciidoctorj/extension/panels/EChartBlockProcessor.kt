package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.*
import org.asciidoctor.log.LogRecord
import org.asciidoctor.log.Severity

@Name("echart-bar")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
open class EChartBlockProcessor : BlockProcessor() {
    private var server = "http://localhost:8010/extension"
    private var localDebug = false
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        return processRequest(parent, reader, "bar")
    }

    protected fun processRequest(
        parent: StructuralNode,
        reader: Reader,
        type: String
    ): Any? {
        val backend = parent.document.getAttribute("backend") as String
        val debug = parent.document.attributes["local-debug"]
        if(debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }
        if ("html5".equals(backend, true)) {
            val remoteServer = parent.document.attributes["panel-server"]
            if (remoteServer != null) {
                remoteServer as String
                server = remoteServer
            }
            val content = reader.read()
            if (serverPresent(server, parent, this, localDebug)) {
                val payload: String = try {
                    compressString(content)
                } catch (e: Exception) {
                    log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
                    ""
                }
                log(LogRecord(Severity.DEBUG, parent.sourceLocation, "payload compressed is $payload"))
                val url = "$server/api/echart?type=$type&data=$payload"
                val source = getContentFromServer(url, parent, this, localDebug)
                return createBlock(parent, "pass", source)
            }
        }
        return null
    }
}

@Name("echart-stack-bar")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class EChartStackBarBlockProcessor : EChartBlockProcessor() {
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        return processRequest(parent, reader, "stackbar")
    }
}

@Name("echart-tree")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class EChartTreeBlockProcessor : EChartBlockProcessor() {
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        return processRequest(parent, reader, "tree")
    }
}

@Name("echart-custom")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class EChartCustomBlockProcessor : EChartBlockProcessor() {
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        return processRequest(parent, reader, "custom")
    }
}