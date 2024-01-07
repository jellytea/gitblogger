// Copyright 2023-2024 JetERA Creative
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0
// that can be found in the LICENSE file and https://mozilla.org/MPL/2.0/.

package com.github.jellytea.gitblogger

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.nio.file.Files
import java.util.*
import javax.swing.*
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.undo.UndoManager
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class BlogEditor(val blogManager: BlogManager) {
    val index = blogManager.index
    val log = Log()

    val attachments = Vector<String>()

    val w = JFrame()
    val markdownEditor = JTextArea()
    val htmlView = JTextPane()
    val undoManager = UndoManager()
    val topicsField = JTextField()
    val attachmentsModel = DefaultListModel<String>()
    val attachmentsList = JList(attachmentsModel)

    fun updateHtmlView() {
        htmlView.text = Markdown2Html(markdownEditor.text)
        scanAttachments()

        val lines = markdownEditor.text.lines()
        if (lines[0].isEmpty()) {
            w.title = "Please enter your title at the first line - GitBlogger"
        } else {
            w.title = lines[0].substringAfter("# ")
        }
    }


    fun publish() {
        if (markdownEditor.text.isEmpty()) {
            MessageDialog("Publish", "Empty content should not be published.", JOptionPane.WARNING_MESSAGE)
            return
        }
        if (markdownEditor.text.lines()[0].isEmpty()) {
            MessageDialog("Publish", "Title is not set.", JOptionPane.WARNING_MESSAGE)
            return
        }

        log.title = w.title

        var finalMarkdown = StringBuffer()

        for (line in markdownEditor.text.lines()) {
            val url = line.substringAfter("file:", "").substringBefore(')').substringBefore('"')
            if (url.isNotEmpty()) {
                finalMarkdown.appendLine(line.replace("file:$url", Path(url).fileName.toString()))
            } else {
                finalMarkdown.appendLine(line)
            }
        }

        val i = blogManager.index.logs.size

        val dir = Files.createDirectory(Path("${blogManager.basedir}/$i"))

        Files.createFile(Path(dir.absolutePathString() + "/content.md")).writeText(finalMarkdown.toString())

        for (file in attachments) {
            val path = Path(file)
            Files.copy(Path(file), Path("${dir.absolutePathString()}/${path.fileName}"))
        }

        blogManager.publish(log)
        w.dispose()
    }

    fun scanAttachments() {
        attachments.removeAllElements()
        for (line in markdownEditor.text.lines()) {
            val url = line.substringAfter("file:", "").substringBefore(')').substringBefore('"')
            if (Files.notExists(Path(url)) || Files.isDirectory(Path(url))) {
                continue
            }
            if (url.isNotEmpty()) {
                attachments.addElement(url)
            }
        }
        UpdateListModel(attachments, attachmentsModel)
    }

    fun attachLocalImage() {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(w) != 0) return
        val file = chooser.selectedFile

        val image = ImageIcon(file.canonicalPath).image

        val rawWidth = image.getWidth(null)
        val rawHeight = image.getHeight(null)

        val dialog = JDialog(w, "Attach Image from Filesystem")

        val altField = JTextField()

        var finalWidth = rawWidth
        var finalHeight = rawHeight

        val widthLabel = JLabel(" = $finalWidth")
        val heightLabel = JLabel(" = $finalHeight")

        val imageView = JTextPane()
        imageView.contentType = "text/html"
        imageView.isEditable = false

        var source = ""

        fun update() {
            source =
                "<img src=\"file:${file.canonicalPath}\" width=\"$finalWidth\" height=\"$finalHeight\" alt=\"${altField.text}\"/>"
            imageView.text = source
        }

        altField.document.addDocumentListener(NewDocumentListener {
            update()
        })

        val widthScaleSpinner = NewSpinner(SpinnerNumberModel(1, 1, 100, 1), fun(spinner: JSpinner) {
            finalWidth = rawWidth / (spinner.value as Int)
            widthLabel.text = " = $finalWidth"
            update()
        })
        val heightScaleSpinner = NewSpinner(SpinnerNumberModel(1, 1, 100, 1), fun(spinner: JSpinner) {
            finalHeight = rawHeight / (spinner.value as Int)
            heightLabel.text = " = $finalHeight"
            update()
        })

        dialog.layout = BorderLayout()

        dialog.add(NewButton("Attach") {
            markdownEditor.insert(source, markdownEditor.caretPosition)
            dialog.dispose()
        }, BorderLayout.SOUTH)

        dialog.add(
            NewVBox(
                NewHBox(
                    NewVBox(
                        JLabel("W: $rawWidth / "),
                        JLabel("H: $rawHeight / "),
                    ),
                    NewVBox(
                        widthScaleSpinner,
                        heightScaleSpinner,
                    ),
                    NewVBox(
                        widthLabel,
                        heightLabel,
                    ),
                ),
                NewHBox(JLabel("Alt: "), altField),
            ),
            BorderLayout.NORTH
        )
        dialog.add(JScrollPane(imageView), BorderLayout.CENTER)

        update()

        dialog.pack()
        dialog.size = Dimension(800, 600)
        ShowWindow(dialog)
    }

    fun fontSettings() {
        val dialog = JDialog(w, "Font Settings")
        dialog.contentPane = NewVBox(FontSettingBoxOf(markdownEditor), FontSettingBoxOf(htmlView))
        dialog.size = Dimension(600, 200)
        dialog.pack()
        ShowWindow(dialog)
    }

    init {
        markdownEditor.font = Font("Noto Sans Mono", Font.PLAIN, 13)
        htmlView.font = Font("Noto Sans", Font.PLAIN, 13)

        markdownEditor.document.addUndoableEditListener(undoManager)
        htmlView.contentType = "text/html"
        htmlView.isEditable = false
        htmlView.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)

        w.jMenuBar = NewMenuBar(
            NewMenu(
                "File",
                NewMenuItem("Publish") {
                    publish()
                }
            ),
            NewMenu(
                "Edit",
                NewMenuItem("Undo", KeyStroke.getKeyStroke("ctrl pressed Z")) {
                    undoManager.undo()
                },
                NewMenuItem("Redo", KeyStroke.getKeyStroke("ctrl shift pressed Z")) {
                    undoManager.redo()
                },
                JSeparator(),
                NewMenuItem("Attach image from filesystem") {
                    attachLocalImage()
                }
            ),
            NewMenu(
                "View",
                NewMenuItem("Font Settings") {
                    fontSettings()
                },
                JSeparator(),
                NewMenuItem("Switch to HTML view") {
                    htmlView.contentType = "text/html"
                    updateHtmlView()
                },
                NewMenuItem("Switch to raw view") {
                    htmlView.contentType = ""
                    updateHtmlView()
                }
            )
        )

        markdownEditor.document.addDocumentListener(NewDocumentListener {
            updateHtmlView()
        })

        w.layout = BorderLayout()

        w.add(NewHBox(JLabel("Topics: "), topicsField), BorderLayout.NORTH)

        w.add(
            JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                JScrollPane(markdownEditor),
                JScrollPane(htmlView)
            ), BorderLayout.CENTER
        )
        w.add(JScrollPane(attachmentsList), BorderLayout.SOUTH)

        w.size = Dimension(800, 600)
        w.defaultCloseOperation = DISPOSE_ON_CLOSE

        updateHtmlView()

        ShowWindow(w)
    }
}
