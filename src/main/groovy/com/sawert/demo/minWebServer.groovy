package com.sawert.demo

import com.sun.net.httpserver.HttpServer
import groovy.xml.XmlUtil

def port = 8989
def context = "/pm"
def tempDir = System.properties.get('java.io.tmpdir')
def userHome = System.properties.get('user.home')
//def homeDir = System.getenv('HOME')
def logDir = "${userHome}/temp/logs/"

HttpServer.create(new InetSocketAddress(port), 0).with {
    createContext(context) { http ->
        http.requestHeaders.each { header -> println header.toString() }
        def echoToken = http.requestHeaders.get('Echotoken').first()
        def requestText = http.requestBody.text
        if (requestText.length() > 0)  {
            def xmlNode = new XmlParser().parseText(requestText)
            def messageId = xmlNode.attribute('messageId')
            def fileName = messageId ?: echoToken
            XmlUtil.serialize(xmlNode, System.out)
            XmlUtil.serialize(xmlNode, new FileWriter("${logDir}/${fileName}.xml"))
            println "\nSaved to file ${fileName}.xml"
        }
        http.responseHeaders.add("Content-type", "text/plain")
        http.sendResponseHeaders(200, 0)
        http.responseBody.withWriter { out ->
            out << "SUCCESS"
        }
    }
    start()

    println "Listening on http://localhost:${port}${context}"
}
