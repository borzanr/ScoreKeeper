package com.scorekeeper.app.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.core.net.toUri
import com.scorekeeper.app.R

/** Pacote do LuckyDraw na Play, para a promoção cruzada entre os dois apps. */
const val LUCKYDRAW_PACKAGE = "com.borzanti.luckydraw"

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun Context.shareText(text: String, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, chooserTitle))
}

/** Abre a página do app na Play; sem o app da Play, cai no navegador. */
fun Context.openPlayStore(packageName: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.play_store_url, packageName).toUri()))
    }
}
