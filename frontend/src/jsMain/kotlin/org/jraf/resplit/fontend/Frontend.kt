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
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.jetbrains.compose.web.renderComposable
import org.jraf.resplit.fontend.split.Attribution
import org.jraf.resplit.fontend.split.SplitReceipt
import org.jraf.resplit.fontend.split.SplitReceiptItem
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

  SplitSection(
    splitReceipt = splitReceipt,
    onSplitChange = { newSplitReceipt ->
      splitReceipt = newSplitReceipt
    },
  )

  WhoPaidSection(
    splitReceipt = splitReceipt,
    onSelect = { selectedAttribution ->
      splitReceipt = splitReceipt.copy(whoPaid = selectedAttribution)
    },
  )

  WhoOwesSection(splitReceipt = splitReceipt)
}

@Composable
private fun SplitSection(
  splitReceipt: SplitReceipt,
  onSplitChange: (SplitReceipt) -> Unit,
) {
  Ul {
    for (splitReceiptItem in splitReceipt.items) {
      Li {
        ReceiptItemAndPrice(splitReceiptItem)

        AttributionSelector(
          currentAttribution = splitReceiptItem.forWho,
          additionalClass = "vertical",
          onSelect = { selectedAttribution ->
            onSplitChange(
              splitReceipt.copy(
                items = splitReceipt.items.map { item ->
                  if (item === splitReceiptItem) {
                    splitReceiptItem.copy(forWho = selectedAttribution)
                  } else {
                    item
                  }
                },
              ),
            )
          },
        )
      }
    }
  }
}

@Composable
private fun ReceiptItemAndPrice(splitReceiptItem: SplitReceiptItem) {
  Div(
    attrs = {
      classes("receipt-item-label-and-price")
    },
  ) {
    Div(
      attrs = {
        classes("receipt-item-label")
      },
    ) {
      Text(splitReceiptItem.label.lowercase())
    }

    Div(
      attrs = {
        classes("receipt-item-price")
      },
    ) {
      Text(splitReceiptItem.price.formattedPrice())
    }
  }
}

@Composable
private fun WhoPaidSection(
  splitReceipt: SplitReceipt,
  onSelect: (Attribution) -> Unit,
) {
  Div(
    attrs = {
      classes("who-paid")
    },
  ) {
    H2 {
      Text("💸 Who paid?")
    }
    AttributionSelector(
      currentAttribution = splitReceipt.whoPaid,
      additionalClass = "horizontal",
      onSelect = onSelect,
    )
  }
}

@Composable
private fun WhoOwesSection(splitReceipt: SplitReceipt) {
  val (attribution, amount) = splitReceipt.whoOwesHowMuch
  H2(
    attrs = {
      classes("net-debt")
      onClick {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
          navigator.clipboard.writeText(amount.toStringExpanded()).await()
        }

      }
    },
  ) {
    Text("💰 ${attribution.formattedName()} owes ${amount.formattedPrice()}")
  }
}

@Composable
private fun AttributionSelector(
  currentAttribution: Attribution,
  additionalClass: String,
  onSelect: (Attribution) -> Unit,
) {
  Div(
    attrs = {
      classes("attribution-selector", additionalClass)
    },
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
}

@Composable
private fun AttributionSelectorItem(
  currentAttribution: Attribution,
  attributionChoice: Attribution,
  onClick: () -> Unit,
) {
  Button(
    attrs = {
      classes(
        buildList {
          add("button")
          if (attributionChoice == currentAttribution) {
            when (attributionChoice) {
              Attribution.PERSON_1 -> add("button-person-1")
              Attribution.PERSON_2 -> add("button-person-2")
              Attribution.BOTH -> add("button-neutral")
            }
          } else {
            add("button-unselected")
          }
        },
      )
      if (attributionChoice != currentAttribution) {
        onClick { onClick() }
      }
    },
  ) {
    Text(
      attributionChoice.formattedName(),
    )
  }
}

private fun Attribution.formattedName(): String = when (this) {
  Attribution.PERSON_1 -> "BoD"
  Attribution.PERSON_2 -> "Carm"
  Attribution.BOTH -> "Both"
}

private fun BigDecimal.formattedPrice(): String {
  val expanded = toStringExpanded()
  val decimalPart = expanded.substringAfterLast('.', "")
  val formatted =
    when {
      decimalPart.isEmpty() -> {
        "$expanded.00"
      }

      decimalPart.length < 2 -> {
        "$expanded${"0".repeat(2 - decimalPart.length)}"
      }

      else -> {
        expanded
      }
    }
  return "$formatted\u00A0€"
}
