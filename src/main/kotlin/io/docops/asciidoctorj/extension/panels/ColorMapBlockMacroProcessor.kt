package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name

@Name("colorMap")
class ColorMapBlockMacroProcessor : BlockMacroProcessor() {

    override fun process(parent: StructuralNode, target: String, attributes: MutableMap<String, Any>): StructuralNode {
        // language=Html
        val str =  """
            <div class='exampleblock'>
                <div class='content'>
                    <div class='literalblock'>
                        <div class='content' id="colormap"></div>
                    </div>
                </div>
            </div>
            <button name="Generate" value="Generate" onclick="genColors();">Create Color Palette</button>
            <input type="number" id="points" name="points" value="20" step="1">
            <script>
            function getColor(){
              return hslToHex(360 * Math.random(), (25 + 70 * Math.random()) , (75 + 10 * Math.random()) );
            }
            function hslToHex(h, s, l) {
              l /= 100;
              const a = s * Math.min(l, 1 - l) / 100;
              const f = n => {
                const k = (n + h / 30) % 12;
                const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1);
                return Math.round(255 * color).toString(16).padStart(2, '0');   // convert to Hex and prefix "0" if needed
              };
              return `#${'$'}{f(0)}${'$'}{f(8)}${'$'}{f(4)}`;
            }
            function genColors() {
                let str = "colorMap {\n";
                let elem = document.getElementById("colormap");
                elem.innerHTML = '';
                var pts = document.getElementById("points");
                var count = pts.value;
                for( let i = count; i--;) {
                  let item = document.createElement('div');
                  var color = document.createElement('div');
                  let clr = getColor();
                  item.style.cssText = `
                    display:inline-block;
                    padding: 2em;
                    margin:5px;
                    border-radius:50%;
                    background: ${'$'}{clr};
                  `
                    item.innerText = clr;
                    str +=  '      color(\"'+ clr + '\")\n';
                    elem.appendChild(item);
                }
                str += "}"
                color.innerText = str
                var ppre = document.createElement("pre");
                var pcode = document.createElement("code");
                pcode.className = 'language-kotlin';
                ppre.appendChild(pcode);
                pcode.innerText = str;
                
                elem.appendChild(ppre);
            }
            </script>
        """.trimIndent()
        return createBlock(parent, "pass", str)
    }

}