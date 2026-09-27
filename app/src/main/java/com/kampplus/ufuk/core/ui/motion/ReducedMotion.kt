package com.kampplus.ufuk.core.ui.motion

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Sistemde "Animasyonları kaldır" açıksa (animator süre ölçeği 0) true döner.
 * İbre salınımı gibi hareketler bu durumda doğrudan son konuma atlar.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
