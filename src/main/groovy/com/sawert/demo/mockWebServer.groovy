package com.sawert.demo

import com.sun.net.httpserver.HttpServer
@picocli.CommandLine.Command(sortOptions = false)
@picocli.groovy.PicocliScript

import groovy.transform.Field

//@Grapes([
//    @Grab('info.picocli:picocli:3.9.1')
//])

import picocli.CommandLine
import picocli.CommandLine.Option

def tempDir = System.properties.get('java.io.tmpdir')
def userHome = System.properties.get('user.home')
def envHome = System.getenv("HOME")

@Option(names = ["-h", "--help"], usageHelp = true, description = "Show this help message and exit.")
@Field boolean helpRequested

@Option(names = ["-p", "--port"], description = "Port number (default: 8080)")
@Field int port = 8080

@Option(names = ["-c", "--context"], description = "Context path (default: /)", converter = SlashPrefixConverter)
@Field String context = '/'

// meta class to add decoding
String.metaClass.decodeURL = {
    URLDecoder.decode(delegate, 'ISO-8859-1')
}

// meta class to add prefix
String.metaClass.prefixWith = { String prefixStr ->
    delegate.startsWith(prefixStr) ? delegate : prefixStr + delegate
}

HttpServer.create(new InetSocketAddress(port), 0).with {
    createContext(context) { http ->
        println "\nRequest Headers:"
        println "\t${http.requestMethod}"
        def headers = http.requestHeaders
        headers.each { header -> println "\t${header.toString()}" }

        println "\nRequest Path:"
        def path = http.requestURI.path
        println "\t${path}"

        println "\nQuery Parameters:"
        def query = http.requestURI.rawQuery
        query?.split('&').each { param -> println "\t${param.decodeURL()}" }

        println "\nRequest Body:"
        def contentType = headers.get('Content-type')
        def requestText = http.requestBody.text
        if (contentType.contains('application/x-www-form-urlencoded')) {
            requestText?.split('&').each { param -> println "\t${param.decodeURL()}" }
        } else {
            println "\t${requestText}"
        }

        http.responseHeaders.add("Content-type", "text/plain")
        http.sendResponseHeaders(200, 0)
        http.responseBody.withWriter { out ->
            out << "SUCCESS\n"
        }
    }
    start()

    println "Listening on http://localhost:${port}${context}"
}

// could make this a metaclass function instead
class SlashPrefixConverter implements CommandLine.ITypeConverter<String> {
    public String convert(String value) throws Exception {
        return value.startsWith('/') ? value : "/" + value
    }
}