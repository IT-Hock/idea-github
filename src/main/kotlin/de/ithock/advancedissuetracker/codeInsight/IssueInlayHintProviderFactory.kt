@file:Suppress("UnstableApiUsage")

package de.ithock.advancedissuetracker.codeInsight

import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsProviderFactory
import com.intellij.codeInsight.hints.ProviderInfo
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.tree.TokenSet

class IssueInlayHintProviderFactory : InlayHintsProviderFactory {
    override fun getLanguages(): Iterable<Language> {
        val languagesSupported = ArrayList<Language>()
        val languages = Language.getRegisteredLanguages()
        for (language in languages){
            if(isLanguageSupported(language)) {
                languagesSupported.add(language)
            }
        }

        return languagesSupported
    }

    override fun getProvidersInfo(): List<ProviderInfo<out Any>> {
        return emptyList()
        return listOf(
            ProviderInfo(Language.ANY, IssueInlayHintProvider(Language.ANY))
        )
    }

    override fun getProvidersInfoForLanguage(language: Language): List<InlayHintsProvider<out Any>> {
        return listOf(
            IssueInlayHintProvider(language)
        )
    }

    private fun isLanguageSupported(language: Language) : Boolean {
        LanguageParserDefinitions.INSTANCE.forLanguage(language)?.let { parserDefinition ->
            val commentTokens = parserDefinition.commentTokens
            if (commentTokens != TokenSet.EMPTY) {
                return true
            }
        }
        return false
    }
}