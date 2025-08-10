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

package org.jraf.resplit.fontend.split

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import org.jraf.resplit.receipt.Receipt
import org.jraf.resplit.receipt.ReceiptItem
import org.w3c.dom.Storage
import org.w3c.dom.get

private val decimalMode = DecimalMode(scale = 2, roundingMode = RoundingMode.ROUND_HALF_TO_EVEN)
private val two = 2.toBigDecimal(decimalMode = decimalMode)

enum class Attribution {
  PERSON_1,
  PERSON_2,
  BOTH,
}

data class SplitReceipt(
  val items: List<SplitReceiptItem>,
  val whoPaid: Attribution,
) {
  constructor(receipt: Receipt, storage: Storage) : this(
    items = receipt.items.map { receiptItem ->
      val savedAttribution = storage[receiptItem.canonicalLabel()]?.let { Attribution.valueOf(it) } ?: Attribution.PERSON_1
      SplitReceiptItem(receiptItem, savedAttribution)
    },
    whoPaid = Attribution.PERSON_1,
  )

  val total: BigDecimal
    get() = items.sumBy { it.price }

  val whoOwesHowMuch: Pair<Attribution, BigDecimal>
    get() {
      return when (whoPaid) {
        Attribution.PERSON_1 -> {
          val person2Owes = items.filter { it.forWho == Attribution.PERSON_2 }.sumBy { it.price } +
            items.filter { it.forWho == Attribution.BOTH }.sumBy { it.price } / two
          Attribution.PERSON_2 to person2Owes
        }

        Attribution.PERSON_2 -> {
          val person1Owes = items.filter { it.forWho == Attribution.PERSON_1 }.sumBy { it.price } +
            items.filter { it.forWho == Attribution.BOTH }.sumBy { it.price } / two
          Attribution.PERSON_1 to person1Owes
        }

        Attribution.BOTH -> {
          val spentForPerson1 = items.filter { it.forWho == Attribution.PERSON_1 }.sumBy { it.price }
          val spentForPerson2 = items.filter { it.forWho == Attribution.PERSON_2 }.sumBy { it.price }
          if (spentForPerson1 > spentForPerson2) {
            Attribution.PERSON_1 to (spentForPerson1 - spentForPerson2) / two
          } else {
            Attribution.PERSON_2 to (spentForPerson2 - spentForPerson1) / two
          }
        }
      }
    }
}

private fun <T> Iterable<T>.sumBy(selector: (T) -> BigDecimal): BigDecimal {
  return fold(BigDecimal.ZERO.copy(decimalMode = decimalMode)) { acc, item ->
    acc + selector(item)
  }
}

data class SplitReceiptItem(
  val label: String,
  val price: BigDecimal,
  val forWho: Attribution,
) {
  constructor(item: ReceiptItem, forWho: Attribution) : this(
    label = item.label,
    price = item.price.toBigDecimal(decimalMode = decimalMode),
    forWho = forWho,
  )
}

fun SplitReceiptItem.canonicalLabel(): String {
  return label.canonical()
}

fun ReceiptItem.canonicalLabel(): String {
  return label.canonical()
}

private fun String.canonical(): String = lowercase().replace(Regex("[^a-z]"), "").take(10)

fun String.toBigDecimalOrNull(): BigDecimal? {
  return try {
    toBigDecimal(decimalMode = decimalMode)
  } catch (_: Exception) {
    null
  }
}

fun SplitReceipt.withPrice(
  itemIndex: Int,
  newPrice: BigDecimal,
): SplitReceipt = copy(
  items = items.mapIndexed { i, item ->
    if (i == itemIndex) {
      item.copy(price = newPrice)
    } else {
      item
    }
  },
)
