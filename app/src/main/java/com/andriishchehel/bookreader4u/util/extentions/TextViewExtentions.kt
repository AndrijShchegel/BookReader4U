package com.andriishchehel.bookreader4u.util.extentions

import android.text.TextUtils
import android.widget.TextView

fun TextView.makeExpandable(collapsedMaxLines: Int = 4) {
    var isExpanded = false

    setOnClickListener {
        isExpanded = !isExpanded
        if (isExpanded) {
            maxLines = Integer.MAX_VALUE
            ellipsize = null
        } else {
            maxLines = collapsedMaxLines
            ellipsize = TextUtils.TruncateAt.END
        }
    }
}