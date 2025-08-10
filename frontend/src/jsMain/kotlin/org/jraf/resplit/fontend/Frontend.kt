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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import navigator.clipboard
import org.jetbrains.compose.web.attributes.autoFocus
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.dom.Ul
import org.jetbrains.compose.web.renderComposable
import org.jraf.resplit.fontend.split.Attribution
import org.jraf.resplit.fontend.split.SplitReceipt
import org.jraf.resplit.fontend.split.SplitReceiptItem
import org.jraf.resplit.fontend.split.canonicalLabel
import org.jraf.resplit.fontend.split.toBigDecimalOrNull
import org.jraf.resplit.fontend.split.withPrice
import org.jraf.resplit.receipt.Receipt
import org.jraf.resplit.receipt.fromJson
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.set
import org.w3c.dom.url.URLSearchParams
import kotlin.js.Date

class Frontend {
  private val snackBarSpec: MutableStateFlow<SnackBarSpec?> = MutableStateFlow(null)

  fun start() {
    val urlParams = URLSearchParams(window.location.search)
    val receiptJson = urlParams.get("receipt")
    val receipt = receiptJson?.let { Receipt.fromJson(it) }

    renderComposable(rootElementId = "root") {
      val snackBarSpec: SnackBarSpec? by snackBarSpec.collectAsState()
      MainScreen(receipt)

      if (snackBarSpec != null) {
        SnackBar(snackBarSpec!!)
      }
    }
  }

  @Composable
  private fun MainScreen(receipt: Receipt?) {
    if (receipt == null) {
      Text("No receipt found")
      return
    }
    var splitReceipt by remember { mutableStateOf(SplitReceipt(receipt, window.localStorage)) }

    TotalAndNewSection(
      splitReceipt = splitReceipt,
      onNewClick = {
        // Navigate to <current url minus the path>
        window.location.href = window.location.toString().substringBeforeLast('/')
      },
    )

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
  private fun TotalAndNewSection(
    splitReceipt: SplitReceipt,
    onNewClick: () -> Unit,
  ) {
    Div(
      attrs = {
        classes("total-and-new")
      },
    ) {
      H2(
        attrs = {
          classes("total")
        },
      ) {

        Text("💸 Total: ${splitReceipt.total.formattedPrice()}")
      }
      Button(
        attrs = {
          classes("button", "button-small", "button-neutral")
          onClick { onNewClick() }
        },
      ) {
        Text("📃 New")
      }
    }
  }

  @Composable
  private fun SplitSection(
    splitReceipt: SplitReceipt,
    onSplitChange: (SplitReceipt) -> Unit,
  ) {
    Ul {
      for ((index, splitReceiptItem) in splitReceipt.items.withIndex()) {
        Li {
          ReceiptItemAndPrice(
            splitReceiptItem = splitReceiptItem,
            onFocusOut = { input ->
              val newPrice = input.toBigDecimalOrNull()
              if (newPrice != null) {
                onSplitChange(
                  splitReceipt.withPrice(index, newPrice),
                )
              } else {
                // Invalid input, do nothing
              }
            },
          )

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

              window.localStorage[splitReceiptItem.canonicalLabel()] = selectedAttribution.name
            },
          )
        }
      }
    }
  }

  @Composable
  private fun ReceiptItemAndPrice(
    splitReceiptItem: SplitReceiptItem,
    onFocusOut: (String) -> Unit,
  ) {
    var isBeingEdited by remember { mutableStateOf(false) }
    var editedValue by remember { mutableStateOf(splitReceiptItem.price.toStringExpanded()) }
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
        if (isBeingEdited) {
          TextInput(
            value = editedValue,
          ) {
            inputMode("decimal")
            id("receiptItemInput")
            autoFocus() // <- useless on iOS
//            onFocusIn {
//              (it.target as HTMLInputElement).select()
//            }
            onFocusOut {
              isBeingEdited = false
              onFocusOut(editedValue)
            }
            onInput {
              editedValue = it.value.replace(",", ".")
            }
            onKeyUp {
              when (it.key) {
                "Enter" -> {
                  isBeingEdited = false
                  onFocusOut(editedValue)
                }
              }
            }
          }

          LaunchedEffect(Unit) {
            // Focus the input when it is created (doesn't work on iOS ¯\_(ツ)_/¯)
            (document.getElementById("receiptItemInput") as? HTMLInputElement)?.focus()
          }

        } else {
          Span(
            attrs = {
              onClick { isBeingEdited = true }
            },
          ) {
            Text(value = splitReceiptItem.price.formattedPrice())
          }
        }
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
        Text("💰 Who paid?")
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
          snackBarSpec.value = SnackBarSpec("Copied to clipboard!")
          @OptIn(DelicateCoroutinesApi::class)
          GlobalScope.launch {
            clipboard.writeText(amount.toStringExpanded()).await()
          }
        }
      },
    ) {
      Text("🤝 ${attribution.formattedName()} owes ${amount.formattedPrice()}")
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

  private class SnackBarSpec(
    val message: String,
  ) {
    override fun equals(other: Any?): Boolean {
      return false
    }

    override fun hashCode(): Int {
      return Date().getMilliseconds()
    }
  }

  @Composable
  private fun SnackBar(snackBarSpec: SnackBarSpec) {
    Div(
      attrs = {
        classes("snackBar")
      },
    ) {
      LaunchedEffect(snackBarSpec) {
        delay(4000)
        this@Frontend.snackBarSpec.value = null
      }
      Text(snackBarSpec.message)
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
}
