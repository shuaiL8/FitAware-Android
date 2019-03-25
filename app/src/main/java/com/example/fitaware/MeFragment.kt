package com.example.fitaware


import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent


class MeFragment : Fragment() {

    private var mDecoView: DecoView? = null
    private var mBackIndex: Int = 0
    private var mSeries1Index: Int = 0

    private var personSteps: Float = 1200f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_me, container,
            false)


        val textRank = view.findViewById<TextView>(R.id.textRank)
        val textSteps= view.findViewById<TextView>(R.id.textSteps)
        mDecoView = view.findViewById<DecoView>(R.id.dynamicArcView)


        textRank.text = "No. 3"
        textSteps.text = String.format("%.0f steps", personSteps)

        createBackSeries()
        createDataSeries()
        createEvents()


        return view
    }


    private fun createBackSeries() {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(0f, 2000f, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex = mDecoView!!.addSeries(seriesItem)

    }

    private fun createDataSeries() {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#3ebfab")) //colorDis
            .setRange(0f, 2000f, 0f)
            .setInitialVisibility(false)
            .build()

        mSeries1Index = mDecoView!!.addSeries(seriesItem)


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
    }

}
