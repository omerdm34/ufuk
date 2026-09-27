package com.kampplus.ufuk.core.ui.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Barlow'un derece işareti büyük puntoda rakam kadar iri durur. Bu yüzden sıcaklıklarda derece işareti
 * sistem yazısıyla çizilir; orada küçük ve rakamın üst hizasındadır (termometre kadranlarındaki gibi).
 */
fun degreesText(text: String): AnnotatedString = buildAnnotatedString {
    text.forEach { char ->
        if (char == DEGREE) {
            withStyle(SpanStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Light)) { append(char) }
        } else {
            append(char)
        }
    }
}

private const val DEGREE = '°'
