package com.kampplus.ufuk.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.kampplus.ufuk.R

/** Yerlerime ekle / çıkar. Durum, erişilebilirlikte "açık/kapalı" olarak da okunur. */
@Composable
fun SaveToggleButton(isSaved: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconToggleButton(checked = isSaved, onCheckedChange = { onClick() }, modifier = modifier) {
        Icon(
            imageVector = if (isSaved) Icons.Filled.Star else StarOutline,
            contentDescription = stringResource(if (isSaved) R.string.action_remove_place else R.string.action_save_place),
            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Material "StarBorder" ikonu. Yalnızca çekirdek ikon seti bağımlılıkta olduğu için (genişletilmiş set
 * birkaç MB) bu tek ikon burada tanımlanır.
 */
private val StarOutline: ImageVector = materialIcon(name = "Ufuk.StarOutline") {
    materialPath {
        moveTo(22f, 9.24f)
        lineToRelative(-7.19f, -0.62f)
        lineTo(12f, 2f)
        lineTo(9.19f, 8.63f)
        lineTo(2f, 9.24f)
        lineToRelative(5.46f, 4.73f)
        lineTo(5.82f, 21f)
        lineTo(12f, 17.27f)
        lineTo(18.18f, 21f)
        lineToRelative(-1.63f, -7.03f)
        lineTo(22f, 9.24f)
        close()
        moveTo(12f, 15.4f)
        lineToRelative(-3.76f, 2.27f)
        lineToRelative(1f, -4.28f)
        lineToRelative(-3.32f, -2.88f)
        lineToRelative(4.38f, -0.38f)
        lineTo(12f, 6.1f)
        lineToRelative(1.71f, 4.04f)
        lineToRelative(4.38f, 0.38f)
        lineToRelative(-3.32f, 2.88f)
        lineToRelative(1f, 4.28f)
        lineTo(12f, 15.4f)
        close()
    }
}
