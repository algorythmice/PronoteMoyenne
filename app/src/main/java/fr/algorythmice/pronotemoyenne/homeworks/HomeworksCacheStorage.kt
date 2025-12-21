package fr.algorythmice.pronotemoyenne.homeworks

import android.content.Context
import androidx.core.content.edit

object HomeworksCacheStorage {

    private const val PREF_NAME = "homeworks_cache_prefs"
    private const val KEY_HOMEWORKS = "cached_homeworks"
    private const val KEY_LAST_UPDATE = "last_update"

    fun saveHomeworks(context: Context, homeworks: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_HOMEWORKS, homeworks)
            putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
        }
    }

    fun loadHomeworks(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HOMEWORKS, null)
    }

    fun getLastUpdate(context: Context): Long =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_UPDATE, 0L)
}


