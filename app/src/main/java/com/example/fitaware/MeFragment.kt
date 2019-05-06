package com.example.fitaware


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.fitaware.Team.Member
import com.example.fitaware.Team.MemberAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MeFragment : Fragment() {
    private val TAG = "MeFragment"
    private var my_steps: Long = 0
    private var my_goal: Long = 0

    private var members = ArrayList<Member>(1)
    private lateinit var memberAdapter: MemberAdapter
    private var user_id: String = ""

    private var mDecoView: DecoView? = null
    private var mBackIndex: Int = 0
    private var mSeries1Index: Int = 0

    private var mTimer: Timer? = null
    private lateinit var personalStepsGraph: GraphView
    private var personalStepsSeries: LineGraphSeries<DataPoint>? = null
    private var tempX : Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_me, container,
            false)

        personalStepsGraph = view.findViewById(R.id.personalStepsGraph)

        val textRank = view.findViewById<TextView>(R.id.textRank)
        val textSteps= view.findViewById<TextView>(R.id.textSteps)
        mDecoView = view.findViewById<DecoView>(R.id.dynamicArcView)


        textSteps.text = String.format("%.0f steps", my_steps.toFloat())




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

            user_id = allSteps["user_id"]!!.toString()

            my_steps = allSteps["my_steps"]!!.toLong()
            my_goal = allSteps["my_goal"]!!.toLong()

            textSteps.text = String.format("%.0f steps", my_steps.toFloat())

            mDecoView!!.addEvent(
                DecoEvent.Builder(my_steps.toFloat())
                    .setIndex(mSeries1Index)
                    .setDuration(1000)
                    .setDelay(100)
                    .build()
            )

        }

        model.message.observe(activity!!, `object`)

        val myRef = FirebaseDatabase.getInstance().reference.child("User")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                Log.i(TAG, "myTeamMember: $my")
                var index = 1
                for((key, value) in my){
                    val details = value as Map<String, String>

                    members.add(Member(index.toString(), key, details.getValue("currentSteps").toInt(), details["goal"], "#000000"))

                    index++
                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")

                }
                Log.w(TAG, "members" + members.toString())

                val membersSort = members.sortedWith(compareByDescending(Member::getmSteps))
                members = ArrayList(membersSort)

                var indexM = 1
                for(member in members) {
                    if(member.getmName() == user_id){
                        textRank.text = "No. " + indexM
                    }
                    indexM++
                }
                Log.w(TAG, "members" + members.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

        personalStepsSeries = LineGraphSeries(arrayOf(DataPoint(0.0, my_steps.toDouble())))
        tempX = System.currentTimeMillis()/1000L

        mTimer = Timer()
        val delay = 1000 // delay for 0 sec.
        val period = 5000 // repeat 5 sec.

        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                updateUI()
            }
        }, delay.toLong(), period.toLong())


        createBackSeries(my_goal.toFloat())
        createDataSeries(my_goal.toFloat())
        createEvents()

        return view
    }

    private var second : Long = 0L

    private var unixTime : Long = 0L


    private fun updateUI() {

        // here you check the value of getActivity() and break up if needed
        if (activity != null) {
            activity!!.runOnUiThread {

                unixTime  = System.currentTimeMillis()/1000L

                second = unixTime - tempX


                personalStepsSeries!!.appendData(DataPoint(second.toDouble(), my_steps.toDouble()), true, 60)
                // set manual X bounds
                personalStepsGraph.viewport.isYAxisBoundsManual = true
                personalStepsGraph.viewport.setMinY(0.0)
                personalStepsGraph.viewport.setMaxY((my_steps + my_steps*0.3))

                personalStepsGraph.viewport.isXAxisBoundsManual = true
                personalStepsGraph.viewport.setMinX(0.0)
                personalStepsGraph.viewport.setMaxX(second + second*0.3)

                // enable scaling and scrolling
                personalStepsGraph.viewport.isScalable = true
                personalStepsGraph.viewport.setScalableY(true)
                personalStepsGraph.addSeries(personalStepsSeries)


                Log.w(TAG, "unixTime" + unixTime )
                Log.w(TAG, "second" + second.toDouble() )

            }
        }
    }


    private fun createBackSeries(goal: Float) {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, goal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex = mDecoView!!.addSeries(seriesItem)

    }

    private fun createDataSeries(goal: Float) {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#3ebfab")) //colorDis
            .setRange(-1f, goal, 0f)
            .setInitialVisibility(false)
            .build()

        mSeries1Index = mDecoView!!.addSeries(seriesItem)


    }

    private fun createEvents() {
        mDecoView!!.executeReset()

        mDecoView!!.addEvent(
            DecoEvent.Builder(my_goal.toFloat())
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
