package com.kampplus.ufuk.architecture

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Clean Architecture bağımlılık kurallarını kaynak kod üzerinden denetler.
 * Tek modüllü yapıda katman ve özellik sınırlarını derleyici değil bu test korur.
 */
class LayerDependencyTest {

    private val sourceRoot = File("src/main/java/com/kampplus/ufuk")

    private val frameworkImports = listOf(
        "import android.",
        "import androidx.",
        "import retrofit2.",
        "import okhttp3.",
        "import kotlinx.serialization.",
        "import dagger.hilt.android."
    )

    @Test
    fun `source root is reachable`() {
        assertTrue("Kaynak dizini bulunamadı: ${sourceRoot.absolutePath}", sourceRoot.isDirectory)
    }

    @Test
    fun `domain layer and shared model do not depend on frameworks`() {
        val violations = (importsIn("/domain/") + importsIn("/core/model/")).filter { (_, line) ->
            frameworkImports.any { line.startsWith(it) }
        }
        assertNoViolations("domain katmanı framework'e bağımlı olamaz", violations)
    }

    @Test
    fun `domain layer does not depend on data or presentation`() {
        val violations = importsIn("/domain/").filter { (_, line) ->
            line.contains(".data.") || line.contains(".presentation.")
        }
        assertNoViolations("domain katmanı dış katmanları bilemez", violations)
    }

    @Test
    fun `presentation layer does not depend on data layer`() {
        val violations = importsIn("/presentation/").filter { (_, line) -> line.contains(".data.") }
        assertNoViolations("presentation katmanı data katmanını bilemez", violations)
    }

    @Test
    fun `features talk to each other only through domain`() {
        val violations = importsIn("/feature/").filter { (path, line) ->
            val owner = featureOf(path)
            val target = FEATURE_IMPORT.find(line)?.groupValues
            target != null && target[1] != owner && target[2] != "domain"
        }
        assertNoViolations("bir özellik başka bir özelliğin yalnızca domain katmanını kullanabilir", violations)
    }

    @Test
    fun `shared core does not know features`() {
        val violations = listOf("/core/common/", "/core/model/", "/core/ui/")
            .flatMap(::importsIn)
            .filter { (_, line) -> line.contains(".feature.") }
        assertNoViolations("core/common, core/model ve core/ui özellikleri bilemez", violations)
    }

    private fun featureOf(path: String): String = path.substringAfter("/feature/").substringBefore('/')

    private fun importsIn(segment: String): List<Pair<String, String>> = sourceRoot.walkTopDown()
        .filter { it.isFile && it.extension == "kt" && segment in it.invariantSeparatorsPath }
        .flatMap { file ->
            file.readLines()
                .map(String::trim)
                .filter { it.startsWith("import ") }
                .map { file.invariantSeparatorsPath to it }
        }
        .toList()

    private fun assertNoViolations(rule: String, violations: List<Pair<String, String>>) {
        assertTrue(
            "$rule:\n" + violations.joinToString("\n") { (file, line) -> "  ${file.substringAfterLast('/')} → $line" },
            violations.isEmpty()
        )
    }

    private companion object {
        val FEATURE_IMPORT = Regex("""com\.kampplus\.ufuk\.feature\.(\w+)\.(\w+)""")
    }
}
