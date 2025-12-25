package fr.algorythmice.pronotemoyenne.pronote

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.chaquo.python.Python
import fr.algorythmice.pronotemoyenne.LoginStorage
import fr.algorythmice.pronotemoyenne.Utils.isLoginComplete
import fr.algorythmice.pronotemoyenne.grades.GradesCacheStorage
import fr.algorythmice.pronotemoyenne.homeworks.HomeworksCacheStorage
import fr.algorythmice.pronotemoyenne.infos.InfosCacheStorage

object PronoteUtils {

    /* ------------------ CALL API ------------------ */
    data class NotesResult(
        val notes: Map<String, List<Pair<Double, Double>>>,
        val homework: Map<String, Map<String, List<String>>> = emptyMap(),
        val error: String? = null
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun syncPronoteData(context: Context): NotesResult {

        val user = LoginStorage.getUser(context)
        val pass = LoginStorage.getPass(context)
        val ent = LoginStorage.getEnt(context)
        val pronoteUrl = LoginStorage.getUrlPronote(context)

        if (!isLoginComplete(user, pass, ent, pronoteUrl)) {
            return NotesResult(emptyMap(), error = "Identifiants incomplets")
        }

        return try {
            val py = Python.getInstance()
            val module = py.getModule("pronote_fetch")

            val result = module.callAttr(
                "get_notes",
                pronoteUrl,
                user,
                pass,
                ent
            )

            val resultList = result.asList()

            val rawGrades = resultList[0].toString()
            val className = resultList[1].toString()
            val establishment = resultList[2].toString()
            val studentName = resultList[3].toString()
            val rawHomeworks = resultList[4].toString()

            val parsedNotes = parseAndComputeNotes(rawGrades)
            val parsedHomeworks = parseHomeworks(rawHomeworks)

            GradesCacheStorage.saveNotes(context, parsedNotes)
            HomeworksCacheStorage.saveHomeworks(context, rawHomeworks)
            InfosCacheStorage.save(context, className, establishment, studentName)

            NotesResult(
                notes = parsedNotes,
                homework = parsedHomeworks
            )

        } catch (e: Exception) {
            NotesResult(emptyMap(), error = e.toString())
        }
    }

    /* ------------------ PARSE DATA ------------------ */
    private fun parseAndComputeNotes(raw: String): Map<String, List<Pair<Double, Double>>> {
        val result = mutableMapOf<String, MutableList<Pair<Double, Double>>>()
        val lines = raw.lines()
        var currentSubject = ""
        var notes = mutableListOf<Pair<Double, Double>>()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("Matière :")) {
                if (notes.isNotEmpty()) {
                    result[currentSubject] = notes
                    notes = mutableListOf()
                }
                currentSubject = trimmed.removePrefix("Matière :").trim()

            } else if (trimmed.isNotEmpty() && !trimmed.contains("abs", true)) {

                val match =
                    Regex("""([\d.,]+)/(\d+)\s*\(coef:\s*([\d.,]+)\)""")
                        .find(trimmed)

                if (match != null) {
                    val (noteStr, surStr, coefStr) = match.destructured
                    val note = noteStr.replace(",", ".").toDouble()
                    val sur = surStr.toDouble()
                    val coef = coefStr.replace(",", ".").toDouble()

                    val note20 = if (sur != 20.0) note * 20 / sur else note
                    val coefFinal = if (sur != 20.0) coef * sur / 20 else coef

                    notes.add(note20 to coefFinal)
                }
            }
        }

        if (notes.isNotEmpty()) {
            result[currentSubject] = notes
        }

        return result
    }

    fun parseHomeworks(
        raw: String
    ): Map<String, Map<String, List<String>>> {

        val result = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

        var currentDate = ""
        var currentSubject = ""

        raw.lines().forEach { line ->
            val trimmed = line.trim()

            when {
                trimmed.startsWith("Date :") -> {
                    currentDate = trimmed.removePrefix("Date :").trim()
                    result.putIfAbsent(currentDate, mutableMapOf())
                }

                trimmed.startsWith("Matière :") -> {
                    currentSubject = trimmed.removePrefix("Matière :").trim()
                    result[currentDate]?.putIfAbsent(currentSubject, mutableListOf())
                }

                trimmed.isNotEmpty() -> {
                    result[currentDate]?.get(currentSubject)?.add(trimmed)
                }
            }
        }

        return result
    }

}