import com.sap.it.script.v2.api.Message
import groovy.xml.XmlSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {
    def xml = new XmlSlurper().parse(message.getBody(java.io.Reader))

    def result = [ (xml.name()): convert(xml) ]

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
    return message
}

def convert(node) {
    def attrs    = node.attributes()
    def children = node.children()

    // Leaf without attributes -> plain value
    if (children.size() == 0 && attrs.isEmpty()) {
        return node.text()
    }

    def map = [:]
    attrs.each { k, v -> map["@" + k] = v }

    if (children.size() == 0) {
        map["#text"] = node.text()      // leaf with attributes
        return map
    }

    children.each { child ->
        def name  = child.name()
        def value = convert(child)
        if (map.containsKey(name)) {
            // repeated element -> turn into an array
            if (!(map[name] instanceof List)) map[name] = [map[name]]
            map[name] << value
        } else {
            map[name] = value
        }
    }
    return map
}
