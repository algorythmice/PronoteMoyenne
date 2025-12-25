package fr.algorythmice.pronotemoyenne.turboself

import android.content.Context
import com.chaquo.python.Python
import fr.algorythmice.pronotemoyenne.Utils.isTurboSelfLoginComplete

object TurboselfUtils {

    /* ------------------ CALL API ------------------ */
    data class FetchQRcodeResult(
        val qrcode: String = "",
        val error: String? = null
    )


    fun fetchQRCode(context: Context):FetchQRcodeResult {
        val user = LoginTurboSelfStorage.getUser(context)
        val pass = LoginTurboSelfStorage.getPass(context)

        if (!isTurboSelfLoginComplete(user, pass)) {
            return FetchQRcodeResult(error = "Identifiants incomplets")
        }

        return try {
            val py = Python.getInstance()
            val module = py.getModule("turboself_fetch")

            val result = module.callAttr(
                "get_qr_code",
                user,
                pass,
                context.filesDir.absolutePath
            )

            TurboSelfCacheStorage.save(context, result.toString())

            FetchQRcodeResult(qrcode = result.toString())


        } catch (e: Exception) {
            FetchQRcodeResult(error = e.toString())
        }
    }
}