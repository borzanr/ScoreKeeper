package com.scorekeeper.app.ui

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Convite para avaliar o app, via In-App Review do Google.
 *
 * O momento escolhido é ao encerrar uma partida — o jogo acabou bem, a pessoa
 * não está no meio de lançar pontos.
 *
 * A própria Play limita quantas vezes o diálogo pode aparecer por usuário, e não
 * informa se ele foi de fato exibido nem se a pessoa avaliou. Por isso o código
 * nunca promete nada ao usuário nem premia quem avalia.
 */
object ReviewPrompt {

    private const val TAG = "ReviewPrompt"

    /** Falhas são silenciosas de propósito: avaliar é um extra. */
    fun request(activity: Activity, onFinished: () -> Unit) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fluxo de avaliação indisponível: ${task.exception?.message}")
                onFinished()
                return@addOnCompleteListener
            }
            manager.launchReviewFlow(activity, task.result)
                .addOnCompleteListener { onFinished() }
        }
    }
}
