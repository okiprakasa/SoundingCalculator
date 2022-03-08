@file:Suppress("DEPRECATION")

package bc.okimatra.soundingcalculator

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.cuberto.bubbleicontabbarandroid.TabBubbleAnimator
import com.google.android.material.tabs.TabLayout
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val mFragmentList: MutableList<Fragment> = ArrayList()
    private var tabBubbleAnimator: TabBubbleAnimator? = null
    private val titles = arrayOf("Calculator", "Data", "User", "Company")
    //private val colors = intArrayOf(R.color.login, R.color.login, R.color.login)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFragmentList.add(TabFragment(titles[0]))
        mFragmentList.add(TabFragment(titles[1]))
        mFragmentList.add(TabFragment(titles[2]))
        mFragmentList.add(TabFragment(titles[3]))
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val adapter: FragmentStatePagerAdapter = object : FragmentStatePagerAdapter(
            supportFragmentManager
        ) {
            override fun getItem(position: Int): Fragment {
                return mFragmentList[position]
            }
            override fun getCount(): Int {
                return mFragmentList.size
            }
        }
        viewPager.adapter = adapter
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.setupWithViewPager(viewPager)
        tabBubbleAnimator = TabBubbleAnimator(tabLayout)
        tabBubbleAnimator!!.addTabItem(titles[0], R.drawable.ic_calculator, R.color.login)
        tabBubbleAnimator!!.addTabItem(titles[1], R.drawable.ic_files, R.color.login)
        tabBubbleAnimator!!.addTabItem(titles[2], R.drawable.ic_user, R.color.login)
        tabBubbleAnimator!!.addTabItem(titles[3], R.drawable.ic_company, R.color.login)
        tabBubbleAnimator!!.setUnselectedColorId(Color.BLACK)
        tabBubbleAnimator!!.highLightTab(0)
        viewPager.addOnPageChangeListener(tabBubbleAnimator!!)
    }

    override fun onStart() {
        super.onStart()
        tabBubbleAnimator!!.onStart(findViewById<View>(R.id.tabLayout) as TabLayout)
    }

    override fun onStop() {
        super.onStop()
        tabBubbleAnimator!!.onStop()
    }
}