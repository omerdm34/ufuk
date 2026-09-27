package com.kampplus.ufuk.core.ui.text

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

/**
 * ViewModel'in Context'e ihtiyaç duymadan metin üretebilmesi için soyutlama.
 * Argümanlar da [UiText] olabilir; iç içe metinler çözülürken önce onlar çözülür.
 */
sealed interface UiText {
    data class Dynamic(
        val value: String
    ) : UiText

    class Resource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UiText {
        override fun equals(other: Any?): Boolean = other is Resource && other.resId == resId && other.args.contentEquals(args)

        override fun hashCode(): Int = 31 * resId + args.contentHashCode()

        override fun toString(): String = "Resource($resId, ${args.toList()})"
    }

    class Plural(
        @param:PluralsRes val resId: Int,
        val count: Int,
        vararg val args: Any
    ) : UiText {
        override fun equals(other: Any?): Boolean =
            other is Plural && other.resId == resId && other.count == count && other.args.contentEquals(args)

        override fun hashCode(): Int = (31 * resId + count) * 31 + args.contentHashCode()

        override fun toString(): String = "Plural($resId, $count, ${args.toList()})"
    }

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> stringResource(resId, *args.map { it.resolve() }.toTypedArray())
        is Plural -> pluralStringResource(resId, count, *args.map { it.resolve() }.toTypedArray())
    }

    /** Compose dışında (ör. paylaşım Intent'i, erişilebilirlik metni) metni çözmek için. */
    fun asString(resources: Resources): String = when (this) {
        is Dynamic -> value
        is Resource -> resources.getString(resId, *args.map { it.resolve(resources) }.toTypedArray())
        is Plural -> resources.getQuantityString(resId, count, *args.map { it.resolve(resources) }.toTypedArray())
    }
}

@Composable
private fun Any.resolve(): Any = if (this is UiText) asString() else this

private fun Any.resolve(resources: Resources): Any = if (this is UiText) asString(resources) else this
