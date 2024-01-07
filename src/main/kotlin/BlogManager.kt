// Copyright 2023-2024 JetERA Creative
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0
// that can be found in the LICENSE file and https://mozilla.org/MPL/2.0/.

package com.github.jellytea.gitblogger

import com.google.gson.Gson
import java.awt.Dimension
import java.io.File
import java.time.Instant
import javax.swing.*
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.table.DefaultTableModel

class BlogManager(val file: File) {
    val basedir = file.parent
    val index = Gson().fromJson(file.readText(), Index::class.java)

    val w = JFrame("Blog Manager [${file.canonicalPath}] - GitBlogger")
    val listModel = DefaultListModel<String>()
    val listView = JList(listModel)
    val detailView = JTable()
    val markdownView = JTextPane()

    fun publish(log: Log) {
        log.publishTime = Instant.now().epochSecond

        index.logs.add(log)

        file.writeText(Gson().toJson(index))

        listModel.insertElementAt(log.title, 0)
    }

    fun updateDetailView() {
        val i = index.logs.size - listView.selectedIndex - 1

        val log = index.logs[i]

        val model = DefaultTableModel(0, 2)
        model.addRow(arrayOf("Index", i))
        model.addRow(
            arrayOf("Publish", Instant.ofEpochSecond(log.publishTime).toString())
        )
        model.addRow(arrayOf("Title", log.title))
        var str = ""
        for (topic in log.topics) {
            str += "$topic, "
        }
        model.addRow(arrayOf("Topics", str))

        detailView.model = model

        markdownView.text = Markdown2Html(File("$basedir/$i/content.md").readText())
    }

    init {
        for (log in index.logs.reversed()) {
            listModel.addElement(log.title)
        }

        w.jMenuBar = NewMenuBar(
            NewMenu("File",
                NewMenuItem("New log") {
                    BlogEditor(this)
                }
            )
        )

        w.add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JScrollPane(listView), JScrollPane(detailView)))

        listView.addListSelectionListener { updateDetailView() }

        w.defaultCloseOperation = DISPOSE_ON_CLOSE
        w.size = Dimension(800, 600)
        ShowWindow(w)
    }
}
