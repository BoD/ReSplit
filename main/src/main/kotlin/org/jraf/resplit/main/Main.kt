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

package org.jraf.resplit.main

import org.jraf.klibnanolog.logd
import org.jraf.klibnanolog.logi
import org.jraf.resplit.receipt.ReceiptExtractor
import org.jraf.resplit.server.Server
import org.slf4j.simple.SimpleLogger


class Main {
  private val receiptExtractor =
    ReceiptExtractor(openAiApiKey = System.getenv("OPENAI_API_KEY") ?: error("OPENAI_API_KEY environment variable is not set"))

  private val publicBaseUrl = System.getenv("PUBLIC_BASE_URL")
    ?: error("PUBLIC_BASE_URL environment variable is not set. This is the URL that the server will be accessible at, e.g. https://example.com")

  fun start() {
    logi("Starting server")
    Server(
      publicBaseUrl = publicBaseUrl,
    ) { receiptSource ->
      logd("Extracting receipt from source: $receiptSource")
      when (receiptSource) {
        is Server.ReceiptSource.Url -> receiptExtractor.extractFromImageUrl(receiptSource.url)
        is Server.ReceiptSource.File -> receiptExtractor.extractFromPdfFile(receiptSource.file)
      }
//      receiptExtractor.extractFakeValues()
    }.start()
  }
}

fun main() {
  // This must be done before any logger is initialized
  System.setProperty(SimpleLogger.LOG_FILE_KEY, "System.out")
  System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "trace")
  System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true")
  System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "yyyy-MM-dd HH:mm:ss")

  Main().start()
}
