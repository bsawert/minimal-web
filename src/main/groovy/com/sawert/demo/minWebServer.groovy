package com.sawert.demo

import com.sun.net.httpserver.HttpServer
import groovy.json.JsonOutput
import groovy.xml.XmlUtil

import java.text.DateFormat

def port = 8989
def context = "/ca"
def tempDir = System.properties.get('java.io.tmpdir')
def userHome = System.properties.get('user.home')
//def homeDir = System.getenv('HOME')
def logDir = "${userHome}/temp/logs/"
def contentXml = 'application/xml'
def contentJson = 'application/json'

HttpServer.create(new InetSocketAddress(port), 0).with {
    createContext(context) { http ->
        http.requestHeaders.each { header -> println header.toString() }
        def contentType = (http.requestHeaders.get('Content-type') ?: ['text/plain']).first()
        def echoToken = (http.requestHeaders.get('Echotoken') ?: ['None']).first()
        def requestText = http.requestBody.text
        def timeStamp = new Date().time.toString()
        def successMsg = 'success'

        if (requestText.length() > 0)  {
            if (contentType.contains(contentXml)) {
                // process xml
                def xmlNode = new XmlParser().parseText(requestText)
                def messageId = xmlNode.attribute('messageId')
                def xmlText = XmlUtil.serialize(xmlNode)
                println xmlText
                new File(logDir, "${timeStamp}.xml").withWriter { writer ->
                    writer.write(xmlText)
                    println "\nSaved to file ${timeStamp}.xml"
                }
                successMsg = '<response>success</response>'
            } else if (contentType.contains(contentJson)) {
                // process json
                def jsonText = JsonOutput.prettyPrint(requestText)
                println jsonText
                new File(logDir, "${timeStamp}.json").withWriter { writer ->
                    writer.write(jsonText)
                    println "\nSaved to file ${timeStamp}.json"
                }
                successMsg = '{"response": "success"}'
            } else {
                // process as plain text
                println requestText
                new File(logDir, "${timeStamp}.txt").withWriter { writer ->
                    writer.write(requestText)
                    println "\nSaved to file ${timeStamp}.txt"
                }
            }
        }

        http.responseHeaders.add("Content-type", "text/plain")
        http.sendResponseHeaders(200, 0)
        http.responseBody.withWriter { out ->
            out << successMsg
        }
    }
    start()

    println "Listening on http://localhost:${port}${context}"
}
