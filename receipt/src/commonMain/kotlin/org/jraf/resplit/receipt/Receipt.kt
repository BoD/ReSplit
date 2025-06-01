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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@SerialName("R")
data class Receipt(
  @get:JsonPropertyDescription("Total amount of the receipt, without currency, always use dot, e.g. \"123.45\"")
  @SerialName("t")
  val total: String,

  @SerialName("i")
  val items: List<ReceiptItem>,
) {
  companion object
}

@Serializable
@SerialName("I")
data class ReceiptItem(
  @get:JsonPropertyDescription("Label of the item, e.g. \"Milk\"")
  @SerialName("l")
  val label: String,

  @get:JsonPropertyDescription("Price of the item, without currency, always use dot, e.g. \"12.34\"")
  @SerialName("p")
  val price: String,
)

fun Receipt.toJsonString(): String {
  return Json.encodeToString(this)
}

fun Receipt.Companion.fromJson(json: String): Receipt {
  return Json.decodeFromString(json)
}
