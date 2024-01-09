// Copyright 2023-2024 JetERA Creative
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0
// that can be found in the LICENSE file and https://mozilla.org/MPL/2.0/.

package com.github.jellytea.gitblogger

import com.google.gson.Gson
import java.awt.Dimension
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.WindowConstants.EXIT_ON_CLOSE

fun main() {
    System.setProperty("awt.useSystemAAFontSettings", "on")
    System.setProperty("swing.aatext", "true")

    val w = JFrame("GitBlogger")

    w.jMenuBar = NewMenuBar(
        NewMenu(
            "File",
            NewMenuItem("Create blog index") {
                try {
                    val chooser = JFileChooser()
                    chooser.selectedFile = File("index.json")

                    if (chooser.showSaveDialog(w) != 0) return@NewMenuItem

                    val file = chooser.selectedFile
                    file.writeText(Gson().toJson(Index()))

                    MessageDialog(
                        "Created Index",
                        "Initial index has been created successfully at ${file.canonicalPath}",
                    )
                } catch (e: Exception) {
                    ExceptionDialog(e)
                }
            },
            NewMenuItem("Open blog index") {
                try {
                    val chooser = JFileChooser()
                    if (chooser.showOpenDialog(w) != 0) return@NewMenuItem
                    BlogManager(chooser.selectedFile)
                } catch (e: Exception) {
                    ExceptionDialog(e)
                }
            },
        ),
        NewMenu(
            "About",
            NewMenuItem("Author") {
                MessageDialog("Author", "Copyright (c) 2023-2024 JetERA Creative\n" +
                        "https://github.com/jetera-creative/gitblogger\n\n" +
                        "Use of this software and the source code is governed under the MPL v2.0 that can be found at:\n" +
                        "https://mozilla.org/MPL/2.0/")
            }
        )
    )

    w.size = Dimension(400, 150)
    w.defaultCloseOperation = EXIT_ON_CLOSE

    ShowWindow(w)
}