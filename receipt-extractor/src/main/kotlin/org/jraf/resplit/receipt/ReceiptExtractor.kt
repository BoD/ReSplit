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
import com.openai.models.responses.ResponseInputContent
import com.openai.models.responses.ResponseInputFile
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

  fun extractFromPdfFile(file: File): Result<Receipt> {
    val base64Url = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(file.readBytes())
    val responseInputFile: ResponseInputFile = ResponseInputFile.builder()
      .fileData(base64Url)
      .filename("receipt.pdf")
      .build()
    return extract(ResponseInputContent.ofInputFile(responseInputFile))
  }

  fun extractFromImageUrl(url: String): Result<Receipt> {
    val responseInputImage: ResponseInputImage = ResponseInputImage.builder()
      .detail(ResponseInputImage.Detail.AUTO)
      .imageUrl(url)
      .build()
    return extract(ResponseInputContent.ofInputImage(responseInputImage))
  }

  private fun extract(responseInputImage: ResponseInputContent): Result<Receipt> {
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

  fun fakeExtractFromUrl(url: String): Result<Receipt> {
    return Result.success(
      Receipt(
        total = "79.12",
        items = listOf(
          ReceiptItem(label = "SAN PELLEGRINO LIMONATA 6x33CL", price = "3.75"),
          ReceiptItem(label = "PUR JUS CLEMENTINE U PET 1L", price = "2.41"),
          ReceiptItem(label = "SPEC.MUESLI FRUIT. JORDONS 750G", price = "4.16"),
          ReceiptItem(label = "MAYO FINE QUAL. TRUIT. KATL. 2376", price = "3.01"),
          ReceiptItem(label = "MAIS S/SURE AJOUTE U X3 2.50", price = "2.55"),
          ReceiptItem(label = "FLAGEOLETS EXTRA-FINS U 14 7 74", price = "2.42"),
          ReceiptItem(label = "PIN'S FONDANT ORANGE LU 1500", price = "1.75"),
          ReceiptItem(label = "FIGOLU 192G", price = "1.59"),
          ReceiptItem(label = "ST. NORET NATURE 17.6G 300G", price = "4.07"),
          ReceiptItem(label = "PLAIS. ALPES FRI'S JAIN 6X125G", price = "3.66"),
          ReceiptItem(label = "DEUF PL.AR TOP LA.ROGE LOUE X6", price = "3.01"),
          ReceiptItem(label = "MARONSUI'S LA LAITIERE 4X690", price = "2.13"),
          ReceiptItem(label = "DANETTE CHOCOLAT 4X125G", price = "1.74"),
          ReceiptItem(label = "LAIT ECREME UHT BBC U 1L", price = "1.32"),
          ReceiptItem(label = "CITRON VERNA 0.198 KG X 5.39 €/kg", price = "1.19"),
          ReceiptItem(label = "POMME DE TERRE AGATA 0.680 KG x 3.69 €/kg", price = "2.51"),
          ReceiptItem(label = "BANANE CAVENDISH FRANCE 0.772 KG x 2.30 €/kg", price = "1.78"),
          ReceiptItem(label = "PAVE SAUMON OSEILLE RIZ U 270G", price = "9.96"),
          ReceiptItem(label = "BOB LISTERINE DENTIGENCE 500ML", price = "6.13"),
          ReceiptItem(label = "DEO STICK PROTECTOR SANEX 65ML", price = "3.37"),
          ReceiptItem(label = "HACHIS PARN.EMEN.GRAI. FM 300G", price = "4.17"),
          ReceiptItem(label = "ESCALOP MILAN. SPAGH. FL. M 300G", price = "4.00"),
          ReceiptItem(label = "DESODORISANT CHAUSSUR. U 100ML", price = "2.66"),
          ReceiptItem(label = "RCH.NET.TRIP.ACT. AJAX 750ML", price = "2.06"),
          ReceiptItem(label = "COLLE PATTEX CONTACT GEL 50G", price = "3.70"),
        ),
      ),
    )
  }
}
