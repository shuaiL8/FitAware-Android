package com.example.fitaware.Home


import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.example.fitaware.R
import com.example.fitaware.Home.Calendar.CalendarFragment
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {


    private var mDecoView: DecoView? = null
    private var mBackIndex: Int = 0
    private var mSeries1Index: Int = 0

    private var mDecoView2: DecoView? = null
    private var mBackIndex2: Int = 0
    private var mSeries1Index2: Int = 0

    private var personSteps: Float = 1200f
    private var groupSteps: Float = 4100f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_home, container,
            false)
        setHasOptionsMenu(true)


        val decoviewFragment = DecoviewFragment()
        fragmentManager!!.beginTransaction().replace(R.id.decoView_frame, decoviewFragment).addToBackStack(null).commit()

        val textDate = view.findViewById<TextView>(R.id.textDate)

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("YYYY-MMMM-dd")
        val currentDate = simpleDateFormat.format(calendar.time)
        textDate.text = currentDate

        val calendar_frame = view.findViewById<FrameLayout>(R.id.calendar_frame)
        val dropDownButton = view.findViewById<ImageButton>(R.id.dropDownButton)

        val calendarFragment = CalendarFragment()
        fragmentManager!!.beginTransaction().replace(R.id.calendar_frame, calendarFragment).addToBackStack(null).commit()

        calendar_frame.visibility = View.GONE;
        dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_down_black_24dp)

        dropDownButton.setOnClickListener(View.OnClickListener {
            if(calendar_frame.visibility == View.GONE){
                calendar_frame.visibility = View.VISIBLE
                dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_up_black_24dp)
            }
            else {
                calendar_frame.visibility = View.GONE
                dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_down_black_24dp)
            }
        })

        mDecoView = view.findViewById<DecoView>(R.id.dynamicArcIcon)
        mDecoView2 = view.findViewById<DecoView>(R.id.dynamicArcIcon2)

        createBackSeries()
        createDataSeries()
        createEvents()


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tool_bar, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.share -> {
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun createBackSeries() {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(0f, 2000f, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex = mDecoView!!.addSeries(seriesItem)

        val seriesItem2 = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(0f, 6000f, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex2 = mDecoView2!!.addSeries(seriesItem2)
    }

    private fun createDataSeries() {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#6a5acd")) //colorDis
            .setRange(0f, 2000f, 0f)
            .setInitialVisibility(false)
            .build()

        mSeries1Index = mDecoView!!.addSeries(seriesItem)

        val seriesItem2 = SeriesItem.Builder(Color.parseColor("#ff6347")) //colorTime
            .setRange(0f, 6000f, 0f)
            .setInitialVisibility(false)
            .build()

        mSeries1Index2 = mDecoView2!!.addSeries(seriesItem2)

    }

    private fun createEvents() {
        mDecoView!!.executeReset()

        mDecoView!!.addEvent(
            DecoEvent.Builder(2000f)
                .setIndex(mBackIndex)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries1Index)
                .setDuration(1000)
                .setDelay(300)
                .build()
        )

         mDecoView!!.addEvent(
             DecoEvent.Builder(personSteps)
                .setIndex(mSeries1Index)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView2!!.addEvent(
            DecoEvent.Builder(6000f)
                .setIndex(mBackIndex2)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView2!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries1Index2)
                .setDuration(1000)
                .setDelay(300)
                .build()
        )

        mDecoView2!!.addEvent(
            DecoEvent.Builder(groupSteps)
                .setIndex(mSeries1Index2)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )
    }

}
