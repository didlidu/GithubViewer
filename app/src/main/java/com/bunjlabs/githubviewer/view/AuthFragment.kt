package com.bunjlabs.githubviewer.view

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.bunjlabs.githubviewer.R
import com.bunjlabs.githubviewer.hideKeyboard
import com.bunjlabs.githubviewer.model.entity.AuthEntity
import com.bunjlabs.githubviewer.model.entity.RepositoryState
import com.bunjlabs.githubviewer.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.auth_fragment.*



class AuthFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var onFragmentInteractionListener: OnFragmentInteractionListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.auth_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)

        loginEditText.setText(viewModel.cachedLogin)
        passwordEditText.setText(viewModel.cachedPassword)

        loginEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.cachedLogin = loginEditText.text.toString()
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.cachedPassword = passwordEditText.text.toString()
            }
        })

        progressBar.visibility = View.GONE
        subscribeAuthEntity(viewModel.getAuthEntity())
        subscribeRepositoryState(viewModel.getRepositoryState())

        enterButton.setOnClickListener {
            loginEditText.error = null
            passwordEditText.error = null
            this.hideKeyboard()
            val login = loginEditText.text.trim().toString()
            val password = passwordEditText.text.trim().toString()
            if (login.isEmpty() || password.isEmpty()) {
                if (login.isEmpty()) {
                    loginEditText.error = getString(R.string.field_required_error)
                }
                if (password.isEmpty()) {
                    passwordEditText.error = getString(R.string.field_required_error)
                }
                return@setOnClickListener
            }
            viewModel.auth(login, password)
        }

        continueAsAnonymousButton.setOnClickListener {
            this.hideKeyboard()
            viewModel.continueAsAnonymous()
            onFragmentInteractionListener.changeFragment(OnFragmentInteractionListener.FRAGMENT_SEARCH)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            onFragmentInteractionListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    private fun subscribeAuthEntity(observable: LiveData<AuthEntity>) {
        observable.observe(viewLifecycleOwner, Observer { authEntity ->
            if (authEntity != null) {
                onFragmentInteractionListener.changeFragment(OnFragmentInteractionListener.FRAGMENT_SEARCH)
            }
        })
    }

    private fun subscribeRepositoryState(observable: LiveData<RepositoryState>) {
        observable.observe(viewLifecycleOwner, Observer { repositoryState ->
            progressBar.visibility = if (repositoryState == RepositoryState.LOADING) View.VISIBLE else View.GONE
            if (repositoryState?.isError() == true) {
                Snackbar.make(progressBar, repositoryState.getErrorMessageResId(), Snackbar.LENGTH_LONG).show()
            }
        })
    }

    companion object {
        const val TAG = "AuthFragment"
    }

}
