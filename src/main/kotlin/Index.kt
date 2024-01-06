// Copyright 2023-2024 JetERA Creative
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0
// that can be found in the LICENSE file and https://mozilla.org/MPL/2.0/.

package com.github.jellytea.gitblogger

import java.util.*

class Log {
    var publishTime = 0L

    var title = ""

    var revision = 0

    var topics = arrayOf<String>()
}

class Index {
    var logs = Vector<Log>()
}
