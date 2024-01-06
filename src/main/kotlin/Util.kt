// Copyright 2023-2024 JetERA Creative
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0
// that can be found in the LICENSE file and https://mozilla.org/MPL/2.0/.

package com.github.jellytea.gitblogger

import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.Box
import javax.swing.JComboBox
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.text.JTextComponent

fun FontSettingBoxOf(textComponent: JTextComponent): Box {
    val fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames

    val fontCombo = NewComboBox(fontNames, fun(combo: JComboBox<String>) {
        textComponent.font = Font(combo.selectedItem as String, Font.PLAIN, textComponent.font.size)
    })

    val fontSizeSpinner = NewSpinner(SpinnerNumberModel(textComponent.font.size, 10, 100, 1), fun(s: JSpinner) {
        textComponent.font = Font(textComponent.font.family, Font.PLAIN, s.value as Int)
    })

    fontCombo.selectedItem = textComponent.font.family

    return NewHBox(fontCombo, fontSizeSpinner)
}
