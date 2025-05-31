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

package org.jraf.resplit.receipt

import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ChatModel
import com.openai.models.responses.ResponseCreateParams
import com.openai.models.responses.ResponseInputImage
import com.openai.models.responses.ResponseInputItem
import com.openai.models.responses.StructuredResponseCreateParams
import java.io.File
import java.util.Base64

class ReceiptExtractor(
  openAiApiKey: String,
) {
  private val openAIClient = OpenAIOkHttpClient.builder()
    .apiKey(openAiApiKey)
    .build()

  fun extractFromFile(file: File): Result<Receipt> {
    val base64Url = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(file.readBytes())
    return extractFromUrl(base64Url)
  }

  fun extractFromUrl(url: String): Result<Receipt> {
    val responseInputImage: ResponseInputImage = ResponseInputImage.builder()
      .detail(ResponseInputImage.Detail.AUTO)
      .imageUrl(url)
      .build()

    val responseInputItem = ResponseInputItem.ofMessage(
      ResponseInputItem.Message.builder()
        .role(ResponseInputItem.Message.Role.USER)
        .addInputTextContent("Extract the data in this groceries receipt")
        .addContent(responseInputImage)
        .build(),
    )
    val responseCreateParams: StructuredResponseCreateParams<Receipt> = ResponseCreateParams.builder()
      .inputOfResponse(listOf(responseInputItem))
      .model(ChatModel.Companion.GPT_4_1_NANO)
      .text(Receipt::class.java)
      .build()

    return runCatching {
      openAIClient.responses().create(responseCreateParams).output()
        .map { it.message().get() }
        .flatMap { it.content() }
        .map { it.outputText().get() }
        .first()
    }
  }
}
