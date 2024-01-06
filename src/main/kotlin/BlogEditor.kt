// Copyright 2023-2024 JetERA Creative
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0
// that can be found in the LICENSE file and https://mozilla.org/MPL/2.0/.

package com.github.jellytea.gitblogger

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.io.File
import java.nio.file.Files
import java.util.*
import javax.swing.*
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.undo.UndoManager
import kotlin.io.path.Path
import kotlin.io.path.pathString

class BlogEditor(val blogManager: BlogManager) {
    val index = blogManager.index
    val log = Log()

    val dir = Files.createTempDirectory("GitBlog-")
    val attachments = Vector<File>()

    val w = JFrame()
    val markdownEditor = JTextArea()
    val htmlView = JTextPane()
    val undoManager = UndoManager()
    val topicsField = JTextField()
    val attachmentsModel = DefaultListModel<String>()
    val attachmentsList = JList(attachmentsModel)

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

        blogManager.publish(log)

        w.dispose()
    }

    fun attachFile() {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(w) != 0) return
        val file = chooser.selectedFile

        Files.createSymbolicLink(Path(dir.pathString + "/" + file.name), Path(file.canonicalPath))

        attachments.add(file)

        attachmentsModel.addElement(file.canonicalPath)
    }

    fun fontSettings() {
        val dialog = JDialog(w, "Font Settings")
        dialog.contentPane = NewVBox(FontSettingBoxOf(markdownEditor), FontSettingBoxOf(htmlView))
        dialog.size = Dimension(600, 200)
        dialog.pack()
        ShowWindow(dialog)
    }

    fun updateHtmlView() {
        htmlView.text = Markdown2Html(markdownEditor.text)

        val lines = markdownEditor.text.lines()
        if (lines[0].isEmpty()) {
            w.title = "Please enter your title at the first line - GitBlogger"
        } else {
            w.title = lines[0].substringAfter("# ")
        }
    }

    fun updateAttachmentsView() {
        val model = attachmentsList.model as DefaultListModel<String>
        model.removeAllElements()
        for (file in attachments) {
            model.addElement(file.canonicalPath)
        }
        attachmentsList.ensureIndexIsVisible(model.size)
    }

    init {
        println(dir.toAbsolutePath())

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
                NewMenuItem("Attach", KeyStroke.getKeyStroke("ctrl shift pressed A")) {
                    attachFile()
                }
            ),
            NewMenu(
                "View",
                NewMenuItem("Font Settings") {
                    fontSettings()
                },
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

        markdownEditor.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(p0: DocumentEvent?) {
                updateHtmlView()
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                updateHtmlView()
            }

            override fun changedUpdate(p0: DocumentEvent?) {
                updateHtmlView()
            }
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
