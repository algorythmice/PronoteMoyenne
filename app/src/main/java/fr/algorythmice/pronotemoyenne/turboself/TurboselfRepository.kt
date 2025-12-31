package fr.algorythmice.pronotemoyenne.turboself

import android.content.Context
import fr.algorythmice.turboselfapi.TurbApi

class TurboselfRepository(
    private val context: Context
) {
    data class QrResult(
        val qrcode: String? = null,
        val error: String? = null
    )

    suspend fun fetchQrCode(): QrResult {
        val user = LoginTurboSelfStorage.getUser(context)
        val pass = LoginTurboSelfStorage.getPass(context)

        if (!fr.algorythmice.pronotemoyenne.Utils.isTurboSelfLoginComplete(user, pass)) {
            return QrResult(error = "Identifiants incomplets")
        }

        return try {
            val api = TurbApi(user, pass)
            api.initLogin()
            val qr = api.getQrPayload()
            api.close()
            TurboSelfCacheStorage.save(context, qr)
            QrResult(qrcode = qr)
        } catch (e: Exception) {
            QrResult(error = e.message ?: e.toString())
        }
    }

    fun getCachedQr(): String? = TurboSelfCacheStorage.getQRcodeNumber(context)
}
