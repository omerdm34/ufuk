package com.kampplus.ufuk.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kampplus.ufuk.R

/** Yerlerime ekle / çıkar. Durum, erişilebilirlikte "açık/kapalı" olarak da okunur. */
@Composable
fun SaveToggleButton(isSaved: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconToggleButton(checked = isSaved, onCheckedChange = { onClick() }, modifier = modifier) {
        Icon(
            imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = stringResource(if (isSaved) R.string.action_remove_place else R.string.action_save_place),
            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
