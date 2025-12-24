package com.vlg.constructorinterface

import androidx.fragment.app.Fragment

interface Navigator {
    fun startFragment(fragment: Fragment)
}