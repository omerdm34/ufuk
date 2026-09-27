package com.kampplus.ufuk.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.os.ConfigurationCompat
import java.util.Locale

/**
 * Alet üzerine kazınmış gibi duran küçük etiket: dar yazı, açık harf aralığı, büyük harf.
 * Büyük harfe çevirme cihaz diliyle yapılır; Türkçede "i" doğru olarak "İ" olur.
 */
@Composable
fun EngravedLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.labelMedium
) {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
    Text(
        text = text.uppercase(locale),
        modifier = modifier,
        color = color,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
