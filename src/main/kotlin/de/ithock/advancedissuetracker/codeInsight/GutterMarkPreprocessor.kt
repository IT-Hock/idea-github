package de.ithock.advancedissuetracker.codeInsight

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.openapi.editor.GutterMarkPreprocessor

class IssueGutterMarkPreprocessor : GutterMarkPreprocessor {
    override fun processMarkers(list: MutableList<GutterMark>): MutableList<GutterMark> {
        return list.filterIsInstance<IssueGutterMark>().toMutableList()
    }
}