package com.example.fitaware


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import java.util.HashMap


class MeFragment : Fragment() {
    private val TAG = "MeFragment"
    private var my_steps: Long = 0


    private var mDecoView: DecoView? = null
    private var mBackIndex: Int = 0
    private var mSeries1Index: Int = 0


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
        textSteps.text = String.format("%.0f steps", my_steps.toFloat())

        createBackSeries()
        createDataSeries()
        createEvents()


        val model = ViewModelProviders.of(activity!!).get(Communicator::class.java)
        val `object` = Observer<Any> { o ->
            // Update the UI

            Log.w(TAG, "allSteps" + o!!.toString())

            val value = o.toString().substring(1, o.toString().length - 1)
            val keyValuePairs = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val allSteps = HashMap<String, String>()

            for (pair in keyValuePairs) {
                val entry = pair.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                allSteps[entry[0].trim { it <= ' ' }] = entry[1].trim { it <= ' ' }
            }

            Log.w(TAG, "my_steps" + allSteps["my_steps"]!!)

            my_steps = allSteps["my_steps"]!!.toLong()

            textSteps.text = my_steps.toString()

            mDecoView!!.addEvent(
                DecoEvent.Builder(my_steps.toFloat())
                    .setIndex(mSeries1Index)
                    .setDuration(1000)
                    .setDelay(100)
                    .build()
            )
        }

        model.message.observe(activity!!, `object`)


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
            DecoEvent.Builder(my_steps.toFloat())
                .setIndex(mSeries1Index)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )
    }

}
