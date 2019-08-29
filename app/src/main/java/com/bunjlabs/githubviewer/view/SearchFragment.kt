package com.bunjlabs.githubviewer.view

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment

import com.bunjlabs.githubviewer.R
import com.bunjlabs.githubviewer.viewmodel.SearchViewModel
import android.view.*
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bunjlabs.githubviewer.GlideApp
import com.bunjlabs.githubviewer.databinding.LoadingItemBinding
import com.bunjlabs.githubviewer.databinding.SearchItemBinding
import com.bunjlabs.githubviewer.model.entity.AuthEntity
import com.bunjlabs.githubviewer.model.entity.RepositoryState
import com.bunjlabs.githubviewer.viewmodel.AuthViewModel
import com.bunjlabs.githubviewer.viewmodel.RepositoryListItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.search_fragment.*
import androidx.core.view.isEmpty


class SearchFragment : Fragment(), SearchView.OnQueryTextListener{

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var authViewModel: AuthViewModel

    private lateinit var adapter: SearchAdapter
    private lateinit var onFragmentInteractionListener: OnFragmentInteractionListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        authViewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)

        adapter = SearchAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.isEmpty() && !recyclerView.canScrollVertically(1)) {
                    searchViewModel.searchNext()
                }
            }
        })

        subscribeAuthEntity(authViewModel.getAuthEntity())
        subscribeRepositoryState(searchViewModel.getRepositoryState())
        subscribeRepositories(searchViewModel.getRepositories())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val loginItem = menu.findItem(R.id.login)
        val logoutItem = menu.findItem(R.id.logout)
        loginItem.isVisible = authViewModel.getAuthEntity().value?.isAnonymous != false
        logoutItem.isVisible = authViewModel.getAuthEntity().value?.isAnonymous == false

        val searchView = (menu.findItem(R.id.search).actionView as SearchView)
        searchView.setQuery(searchViewModel.getQuery(), false)
        searchView.queryHint = getString(R.string.query_hint)
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.login || item?.itemId == R.id.logout){
            authViewModel.logout()
        }

        return super.onOptionsItemSelected(item)
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
            if (authEntity == null) {
                onFragmentInteractionListener.changeFragment(OnFragmentInteractionListener.FRAGMENT_AUTH)
            }
        })
    }

    private fun subscribeRepositoryState(observable: LiveData<RepositoryState>) {
        observable.observe(viewLifecycleOwner, Observer { repositoryState ->
            if (repositoryState?.isError() == true) {
                Snackbar.make(mainLayout, repositoryState.getErrorMessageResId(), Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun subscribeRepositories(observable: LiveData<List<RepositoryListItem>>) {
        observable.observe(viewLifecycleOwner, Observer { repositories ->
            if (repositories != null) {
                adapter.setList(repositories)
            } else {
                adapter.setList(ArrayList())
            }
            if (repositories == null || repositories.isEmpty()) {
                noItemsImage.visibility = View.VISIBLE
                noItemsText.visibility = View.VISIBLE
            } else {
                noItemsImage.visibility = View.INVISIBLE
                noItemsText.visibility = View.INVISIBLE
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return if (query != null && query.trim().isNotEmpty()) {
            searchViewModel.search(query)
            true
        } else {
            false
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    companion object {
        const val TAG = "SearchFragment"
    }

}

class SearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList: List<RepositoryListItem>? = null

    inner class ItemViewHolder(
        val binding: SearchItemBinding
    ): RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(
        val binding: LoadingItemBinding
    ): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                val binding = DataBindingUtil.inflate<SearchItemBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.search_item, parent, false)
                ItemViewHolder(binding)
            }
            TYPE_LOADING -> {
                val binding = DataBindingUtil.inflate<LoadingItemBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.loading_item, parent, false)
                LoadingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mList!![position]
        when (holder) {
            is ItemViewHolder -> {
                GlideApp.with(holder.binding.avatar).load(item.repository!!.owner.avatarUrl).circleCrop()
                    .placeholder(R.drawable.ic_github).error(R.drawable.ic_github).into(holder.binding.avatar)
                holder.binding.fullName.text = item.repository.fullName
                if (item.repository.description.isNullOrBlank()) {
                    holder.binding.description.setText(R.string.no_description)
                } else {
                    holder.binding.description.text = item.repository.description
                }
            }
            is LoadingViewHolder -> {}
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = mList!![position]
        return when {
            item.repository != null -> TYPE_ITEM
            item.isLoadingItem -> TYPE_LOADING
            else -> throw IllegalArgumentException("Invalid item at position $position")
        }
    }

    override fun getItemCount(): Int = mList?.size ?: 0

    fun setList(list: List<RepositoryListItem>) {
        mList = list
        notifyDataSetChanged()
        if (mList == null) {
            mList = list
            notifyItemRangeInserted(0, mList!!.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return mList?.size ?: 0
                }

                override fun getNewListSize(): Int {
                    return list.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return mList!![oldItemPosition].repository?.id == list[newItemPosition].repository?.id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val newItem = list[newItemPosition]
                    val oldItem = mList!![oldItemPosition]
                    return (newItem.repository?.id == oldItem.repository?.id
                            && newItem.repository?.name == oldItem.repository?.name
                            && newItem.repository?.fullName == oldItem.repository?.fullName
                            && newItem.repository?.description === oldItem.repository?.description)
                }
            })
            mList = list
            result.dispatchUpdatesTo(this)
        }
    }

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }
}
