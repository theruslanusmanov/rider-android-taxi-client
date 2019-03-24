package com.github.owlruslan.rider.ui.modes.passanger.ride

import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.github.owlruslan.rider.R

class ViewPagerAdapter(private val mContext: Context, private val mListData: List<String>) : PagerAdapter() {

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return mListData.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Title $position"
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.viewpager_item, container, false) as ViewGroup

        val textView = view.findViewById<TextView>(R.id.textView)
        textView.text = mListData[position]

        container.addView(view)
        return view
    }
}