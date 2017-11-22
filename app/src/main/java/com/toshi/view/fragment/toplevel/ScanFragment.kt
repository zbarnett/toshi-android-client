package com.toshi.view.fragment.toplevel

import android.support.v4.app.Fragment

// Only used to open ScanActivity. It's never used as a view
class ScanFragment : Fragment(), TopLevelFragment {

    companion object {
        private const val TAG = "ScanFragment"
    }

    override fun getFragmentTag() = TAG
}