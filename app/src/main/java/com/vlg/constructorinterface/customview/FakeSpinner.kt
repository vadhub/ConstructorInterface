package com.vlg.constructorinterface.customview

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.vlg.constructorinterface.R

class FakeSpinner(context: Context) : AppCompatTextView(context) {

    init {
        setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context,R.drawable.outline_arrow_drop_down_24), null)
    }
}