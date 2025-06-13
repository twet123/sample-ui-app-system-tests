#!/usr/bin/env kotlin

@file:DependsOn("com.google.code.gson:gson:2.10.1")

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class TestOccurrence(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("details") val details: String,
)

val basePrompt = """
    System tests have been failing due to an element change.
    Here is the information about the failure: __INFO_PLACEHOLDER__
    Update the related element throughout the affected test files.
    Potential file candidates, based on the stack trace are:
    __FILE_CANDIDATES_PLACEHOLDER__
    Also here is the difference in affected html pages that caused the failure:
    Removed:
    __DIFF_REMOVED_PLACEHOLDER__
    Added:
    __DIFF_ADDED_PLACEHOLDER__
    After you are done create a pull request and add me as a reviewer.
""".trimIndent()

data class LineDiffResult(
    val added: List<String>,
    val removed: List<String>
)

fun fetchTestOccurrences(buildId: String, token: String): List<TestOccurrence> {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8111/app/rest/testOccurrences?locator=build:(id:$buildId),status:FAILURE&fields=testOccurrence(id,name,status,details)"))
        .header(
            "Authorization",
            "Bearer $token"
        ).header("Accept", "application/json").GET().build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() != 200) {
        throw RuntimeException("Failed to get test occurrences. Status: ${response.statusCode()}")
    }

    val json = JsonParser.parseString(response.body()).asJsonObject.getAsJsonArray("testOccurrence").asJsonArray
    val gson = Gson()

    return json.map { it -> gson.fromJson(it, TestOccurrence::class.java) }
}

fun getLastSuccessfulBuildId(buildTypeId: String, token: String): String {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8111/app/rest/builds?locator=buildType:$buildTypeId,status:SUCCESS,count:1&fields=build(id)"))
        .header(
            "Authorization",
            "Bearer $token"
        ).header("Accept", "application/json").GET().build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return JsonParser.parseString(response.body()).asJsonObject.getAsJsonArray("build").asJsonArray.first().asJsonObject.get(
        "id"
    ).asString
}

fun downloadArtifact(buildId: String, token: String, artifactName: String): String {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8111/app/rest/builds/id:$buildId/artifacts/content/$artifactName"))
        .header(
            "Authorization",
            "Bearer $token"
        ).header("Accept", "text/html").GET().build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun getLineDiff(str1: String, str2: String): List<Pair<String?, String?>> {
    val lines1 = str1.split("\n")
    val lines2 = str2.split("\n")
    val diffs = mutableListOf<Pair<String?, String?>>()

    lines1.zip(lines2).forEach { (line1, line2) ->
        if (line1 != line2) {
            diffs.add(line1 to line2)
        }
    }

    if (lines1.size > lines2.size) {
        lines1.subList(lines2.size, lines1.size).forEach {
            diffs.add(it to null)
        }
    } else if (lines2.size > lines1.size) {
        lines2.subList(lines1.size, lines2.size).forEach {
            diffs.add(null to it)
        }
    }

    return diffs
}

fun generateHtmlDiff(buildId: String, buildTypeId: String, token: String, testName: String): LineDiffResult {
    val lastSuccessfulBuildId = getLastSuccessfulBuildId(buildTypeId, token)

    val lastSuccessfulHtml = downloadArtifact(lastSuccessfulBuildId, token, "$testName-passed.html")
    val failedHtml = downloadArtifact(buildId, token, "$testName-failed.html")

    val lineDiff = getLineDiff(lastSuccessfulHtml, failedHtml)
    val oldLines = lineDiff.mapNotNull { it.first }.joinToString("\n")
    val newLines = lineDiff.mapNotNull { it.second }.joinToString("\n")
    return LineDiffResult(newLines.lines(), oldLines.lines())
}

fun generatePrompts(buildId: String, buildTypeId: String, token: String, testOccurrences: List<TestOccurrence>): List<String> {
    return testOccurrences.mapNotNull { it ->
        val elementNotFound = it.details.lines().firstOrNull { line -> line.contains("Element not found") }
        val fileCandidates = it.details.lines().filter { line -> line.contains(Regex("\\(\\w+\\.kt:\\d+\\)")) }
        val htmlDiff = generateHtmlDiff(buildId, buildTypeId, token, it.name.substringAfterLast("."))

        if (elementNotFound != null) {
            basePrompt.replace("__INFO_PLACEHOLDER__", elementNotFound)
                .replace("__FILE_CANDIDATES_PLACEHOLDER__", fileCandidates.joinToString("\n"))
                .replace("__DIFF_REMOVED_PLACEHOLDER__", htmlDiff.removed.joinToString("\n"))
                .replace("__DIFF_ADDED_PLACEHOLDER__", htmlDiff.added.joinToString("\n"))
        } else {
            null
        }
    }
}

fun main(args: Array<String>) {
    val authToken = args[0]
    val buildTypeId = args[1]
    val buildId = args[2]

    val testOccurrences = fetchTestOccurrences(buildId, authToken)
    println(generatePrompts(buildId, buildTypeId, authToken, testOccurrences))
}

main(args)