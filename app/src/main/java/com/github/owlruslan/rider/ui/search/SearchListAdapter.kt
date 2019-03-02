package com.github.owlruslan.rider.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.owlruslan.rider.R
import com.google.android.libraries.places.api.model.AutocompletePrediction
import java.util.ArrayList

class SearchListAdapter(private val searchList: ArrayList<AutocompletePrediction>) :
    RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder>() {

    class SearchListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textView: TextView
        private val fullTextView: TextView

        init {
            textView = itemView.findViewById(R.id.searchListItemTitle)
            fullTextView = itemView.findViewById(R.id.searchListItemTitleBottom)
        }

        fun bind(prediction: AutocompletePrediction) {
            textView.setText(prediction.getPrimaryText(null))
            fullTextView.setText(prediction.getFullText(null))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): SearchListAdapter.SearchListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.searchlist_item_view, parent, false)

        return SearchListViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        holder.bind(searchList.get(position))
    }

    override fun getItemCount() = searchList.size
}