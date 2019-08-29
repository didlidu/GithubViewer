package com.bunjlabs.githubviewer.view

interface OnFragmentInteractionListener {

    fun changeFragment(fragmentType: Int)

    companion object {
        const val FRAGMENT_AUTH = 0
        const val FRAGMENT_SEARCH = 1
    }
}