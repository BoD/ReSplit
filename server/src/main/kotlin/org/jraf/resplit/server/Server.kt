/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2025-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jraf.resplit.server

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.json.Json
import org.jraf.klibnanolog.logd
import org.jraf.klibnanolog.logw
import org.jraf.resplit.receipt.Receipt
import org.jraf.resplit.receipt.toJsonString
import org.slf4j.event.Level
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val ENV_PORT = "PORT"
private const val PORT_DEFAULT = 8042

class Server(
  publicBaseUrl: String,
  private val extractReceipt: (suspend (receiptSource: ReceiptSource) -> Result<Receipt>),
) {
  sealed interface ReceiptSource {
    data class File(val file: java.io.File) : ReceiptSource
    data class Url(val url: String) : ReceiptSource
  }

  private val publicBaseUrl: String = publicBaseUrl.trimEnd('/')

  private val receiptsDir: File = File(System.getProperty("java.io.tmpdir") + "/resplit").apply {
    mkdirs()
  }

  fun start() {
    val listenPort = System.getenv(ENV_PORT)?.toInt() ?: PORT_DEFAULT
    embeddedServer(
      factory = Netty,
      configure = {
        connectors.add(
          EngineConnectorBuilder().apply {
            port = listenPort
          },
        )
      },
      module = { mainModule() },
    ).start(wait = true)
  }

  private fun Application.mainModule() {
    install(DefaultHeaders)

    install(CallLogging) {
      level = Level.DEBUG
      format { call ->
        val host = call.request.origin.remoteHost
        val httpMethod = call.request.httpMethod.value
        val path = call.request.path()
        val headers = call.request.headers
        val status = call.response.status()
        """
          |
          |-----------------------------------
          |Host: $host
          |Method: $httpMethod
          |Path: $path
          |Headers:
          ${headers.entries().joinToString("\n") { "|- ${it.key}: ${it.value.joinToString()}" }}
          |Status: $status
          |-----------------------------------
          |
        """.trimMargin()
      }
    }

    install(StatusPages)

    install(ContentNegotiation) {
      json(
        Json {
          prettyPrint = true
        },
      )
    }

    routing()
  }

  private fun Application.routing() {
    routing {
      staticResources("/", "")
      staticFiles("/receipts", receiptsDir)

      post("/receipt") {
        var extension: String? = null
        var file: File? = null
        val multipartData = call.receiveMultipart(formFieldLimit = 10 * 1024 * 1024)
        multipartData.forEachPart { part ->
          when (part) {
            is PartData.FileItem -> {
              extension = part.contentType?.contentSubtype ?: part.originalFileName!!.substringAfterLast('.', "pdf")
              val fileName = @OptIn(ExperimentalUuidApi::class) Uuid.random().toString() + ".$extension"
              file = File(receiptsDir, fileName)
              part.provider().copyAndClose(file.writeChannel())
              return@forEachPart
            }

            else -> {}
          }
          part.dispose()
        }
        logd("File written to ${file!!}")
        extractReceipt(
          when (extension) {
            "pdf" -> ReceiptSource.File(file)
            else -> ReceiptSource.Url("${publicBaseUrl}/receipts/${file.name}")
          },
        )
          .onFailure { e ->
            logw(e, "Failed to extract receipt")
            call.respondText("Failed to extract receipt: ${e.message}", status = HttpStatusCode.InternalServerError)
          }
          .onSuccess { receipt ->
            logd("Receipt extracted successfully: $receipt")
            call.respondRedirect("$publicBaseUrl/split.html?receipt=" + receipt.toJsonString().encodeURLParameter(), permanent = false)
          }

        // Keep the file in debug mode
        if (System.getenv("DEBUG_KEEP_TEMP_FILES") != "true") {
          file.delete()
        }
      }
    }
  }
}
