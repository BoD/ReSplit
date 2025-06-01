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

package org.jraf.resplit.fontend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.jetbrains.compose.web.renderComposable
import org.jraf.resplit.fontend.split.Attribution
import org.jraf.resplit.fontend.split.SplitReceipt
import org.jraf.resplit.receipt.Receipt
import org.jraf.resplit.receipt.fromJson
import org.w3c.dom.url.URLSearchParams

class Frontend {
  fun start() {
    val urlParams = URLSearchParams(window.location.search)
    val receiptJson = urlParams.get("receipt")
    val receipt = receiptJson?.let { Receipt.fromJson(it) }

    renderComposable(rootElementId = "root") {
      MainScreen(receipt)
    }
  }
}


@Composable
private fun MainScreen(receipt: Receipt?) {
  if (receipt == null) {
    Text("No receipt found")
    return
  }
  var splitReceipt by remember { mutableStateOf(SplitReceipt(receipt)) }

  Ul {
    for (splitReceiptItem in splitReceipt.items) {
      Li {
        Text("${splitReceiptItem.label} - ${splitReceiptItem.price}")
        AttributionSelector(
          currentAttribution = splitReceiptItem.forWho,
          onSelect = { selectedAttribution ->
            splitReceipt = splitReceipt.copy(
              items = splitReceipt.items.map { item ->
                if (item === splitReceiptItem) {
                  splitReceiptItem.copy(forWho = selectedAttribution)
                } else {
                  item
                }
              },
            )
          },
        )
      }
    }
  }

  Div {
    Text("Who paid?")
    AttributionSelector(
      currentAttribution = splitReceipt.whoPaid,
      onSelect = { selectedAttribution ->
        splitReceipt = splitReceipt.copy(whoPaid = selectedAttribution)
      },
    )
  }

  Div {
    val (attribution, amount) = splitReceipt.whoOwesHowMuch
    Text("${attribution} owes ${amount.toPlainString()} €")
  }
}

@Composable
private fun AttributionSelector(
  currentAttribution: Attribution,
  onSelect: (Attribution) -> Unit,
) {
  AttributionSelectorItem(currentAttribution, Attribution.PERSON_1) {
    onSelect(Attribution.PERSON_1)
  }
  AttributionSelectorItem(currentAttribution, Attribution.PERSON_2) {
    onSelect(Attribution.PERSON_2)
  }
  AttributionSelectorItem(currentAttribution, Attribution.BOTH) {
    onSelect(Attribution.BOTH)
  }
}


@Composable
private fun AttributionSelectorItem(
  currentAttribution: Attribution,
  attributionChoice: Attribution,
  onClick: () -> Unit,
) {
  A(
    attrs = {
      if (attributionChoice == currentAttribution) {
        classes("selected")
      } else {
        onClick {
          onClick()
        }
      }
    },
  ) {
    Text(attributionChoice.toString())
  }
}
