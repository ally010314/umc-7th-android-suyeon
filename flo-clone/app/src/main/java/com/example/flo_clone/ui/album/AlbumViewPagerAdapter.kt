package com.example.flo_clone.ui.album

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AlbumViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SongFragment()
            1 -> DetailFragment()
            2 -> VideoFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}