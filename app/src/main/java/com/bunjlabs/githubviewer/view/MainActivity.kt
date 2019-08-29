package com.bunjlabs.githubviewer.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bunjlabs.githubviewer.R

class MainActivity : AppCompatActivity(), OnFragmentInteractionListener {

    override fun changeFragment(fragmentType: Int) {
        when (fragmentType) {
            OnFragmentInteractionListener.FRAGMENT_AUTH -> {
                Log.d("Debug", "AuthFragment")
                supportFragmentManager.beginTransaction().replace(
                    R.id.flContent, AuthFragment()
                ).commit()
            }
            OnFragmentInteractionListener.FRAGMENT_SEARCH -> {
                Log.d("Debug", "SearchFragment")
                supportFragmentManager.beginTransaction().replace(
                    R.id.flContent, SearchFragment()
                ).commit()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.flContent, AuthFragment()
            ).commit()
        }
    }
}
