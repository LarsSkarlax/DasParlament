package com.lrs.dasparlament

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

@Serializable
data class Ausgabe(
    val datum: String,
    val nummer: String,
    val titel: String,
    val jahr: String,
    val browserLink: String,
    val pdfLink: String,
    val vorschauBild: String
)

fun fetchAusgabenUndSpeichernAlsJson(url: String, outputFile: String) {
    val doc: Document = Jsoup.connect(url).get()

    val ausgaben = doc.select("div.epaper").map { epaper ->
        val headline = epaper.selectFirst("div.epaper__headline")?.text()?.trim() ?: ""
        val text = epaper.selectFirst("div.epaper__text")?.text()?.trim() ?: ""

        val (datum, nummer) = parseDatumUndNummer(headline)
        val jahr = datum.takeLast(4)

        val links = epaper.select("a.epaper__link")
        val browserLink = "https://www.das-parlament.de" + (links.firstOrNull()?.attr("href") ?: "")
        val pdfLink = "https://www.das-parlament.de" + (links.getOrNull(1)?.attr("href") ?: "")

        val bildPfad = epaper.selectFirst("div.epaper__image img")?.attr("src") ?: ""
        val vorschauBild = "https://www.das-parlament.de$bildPfad"

        Ausgabe(
            datum = datum,
            nummer = nummer,
            titel = text,
            jahr = jahr,
            browserLink = browserLink,
            pdfLink = pdfLink,
            vorschauBild = vorschauBild
        )
    }

    val json = Json { prettyPrint = true }.encodeToString(ausgaben)
    File(outputFile).writeText(json)

    println("✅ ${ausgaben.size} Ausgaben erfolgreich als JSON gespeichert in: $outputFile")
}

fun parseDatumUndNummer(headline: String): Pair<String, String> {
    val parts = headline.split("|")
    val datum = parts.getOrNull(0)?.trim() ?: "Unbekannt"
    val nummer = parts.getOrNull(1)?.trim()?.removePrefix("Ausgabe-Nr.")?.trim() ?: "?"
    return datum to nummer
}

fun main() {
    val url = "https://www.das-parlament.de/e-paper"
    val outputFile = "@assets/ausgaben.json"
    fetchAusgabenUndSpeichernAlsJson(url, outputFile)
}
