package com.example.fitaware.Home


import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.IntentSender
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import com.example.fitaware.R
import com.example.fitaware.Home.Calendar.CalendarFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.location.places.Places
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.Intent
import com.example.fitaware.Communicator
import com.example.fitaware.MainActivity
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.result.DataSourcesResult
import com.google.firebase.database.*
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.PointsGraphSeries
import com.jjoe64.graphview.series.LineGraphSeries
import com.example.fitaware.User
import java.util.HashMap
import android.arch.lifecycle.Observer
import android.graphics.*
import android.os.Handler
import android.widget.*
import com.example.fitaware.Team.Member
import com.example.fitaware.Team.MemberAdapter
import kotlin.math.absoluteValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.sql.Timestamp

class HomeFragment : Fragment(){



    private val TAG = "HomeFragment"
    private var user_id: String = ""

    private var selected_id: String = "Display None"
    private var temp: String = ""

    private var periodical: String = ""
    private var my_steps: Long = 0
    private var teammate_steps: Long = 0
    private var team_steps: Long = 0

    private var my_goal: Long = 0
    private var teammate_goal: Long = 0
    private var team_goal: Long = 0

    private var mTimer: Timer? = null

    private lateinit var personalStepsGraph: GraphView
    private lateinit var groupStepsGraph: GraphView

    private var personalStepsSeries: LineGraphSeries<DataPoint>? = null
    private var groupStepsSeries: LineGraphSeries<DataPoint>? = null

    private var mDecoView4: DecoView? = null
    private var mBackIndex4: Int = 0
    private var mSeries4Index: Int = 0

    private var mDecoView5: DecoView? = null
    private var mBackIndex5: Int = 0
    private var mSeries5Index: Int = 0



    private lateinit var database: DatabaseReference

    private var mDecoView: DecoView? = null
    private var mDecoView2: DecoView? = null
    private var mDecoView3: DecoView? = null


    private var mBackIndex: Int = 0
    private var mBackIndex2: Int = 0
    private var mBackIndex3: Int = 0

    private var mSeries1Index: Int = 0
    private var mSeries2Index: Int = 0
    private var mSeries3Index: Int = 0
    private val mSeriesMax:Float = 6000f

    internal var percentFilled = 0f
    internal var remainingMins = 0f
    internal var remainingKm = 0f
    internal var remainingCals = 0f

    internal var activity_1 = 0f
    internal var activity_2 = 0f
    internal var activity_3 = 0f

    internal lateinit var textPercentage: TextView
    internal lateinit var textRemaining: TextView
    internal lateinit var textActivity1: TextView
    internal lateinit var textActivity2: TextView
    internal lateinit var textActivity3: TextView
    internal lateinit var rankActivity1: TextView
    internal lateinit var rankActivity2: TextView
    internal lateinit var rankActivity3: TextView

    internal lateinit var imageActivity1: ImageView
//    internal lateinit var demo1: Button
//    internal lateinit var demo2:Button
//    internal lateinit var clear:Button
    private var gridViewDecoViews: ExpandableHeightGridView? = null
    private var tempX : Long = 0L

    private var teammates = ArrayList<Teammates>(1)
    private var teammatesAdapter: TeammatesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_home, container,
            false)
        setHasOptionsMenu(true)

        personalStepsSeries = LineGraphSeries(arrayOf(DataPoint(0.0, my_steps.toDouble())))
        groupStepsSeries = LineGraphSeries(arrayOf(DataPoint(0.0, team_steps.toDouble())))
        tempX = System.currentTimeMillis()/1000L

        //updateUI
        mTimer = Timer()
        val delay = 1000 // delay for 0 sec.
        val period = 5000 // repeat 5 sec.

        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                updateUI()
            }
        }, delay.toLong(), period.toLong())

        personalStepsGraph = view.findViewById(R.id.personalStepsGraph)
        groupStepsGraph = view.findViewById(R.id.groupStepsGraph)


        val textDate = view.findViewById<TextView>(R.id.textDate)
        val textGridView = view.findViewById<TextView>(R.id.textGridView)

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM dd")
        val currentDate = simpleDateFormat.format(calendar.time)
        textDate.text = currentDate
        textGridView.text = "My Team"
        val calendar_frame = view.findViewById<FrameLayout>(R.id.calendar_frame)
        val dropDownButton = view.findViewById<ImageButton>(R.id.dropDownButton)
        val dropDownButtonOfGridView = view.findViewById<ImageButton>(R.id.dropDownButtonOfGridView)

        val calendarFragment = CalendarFragment()
        fragmentManager!!.beginTransaction().replace(R.id.calendar_frame, calendarFragment).addToBackStack(null).commit()

        gridViewDecoViews = view.findViewById(R.id.gridViewDecoViews)
        gridViewDecoViews!!.isExpanded = true

        calendar_frame.visibility = View.GONE;
        dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_down_black_24dp)
        dropDownButtonOfGridView.setImageResource(R.drawable. ic_keyboard_arrow_down_black_24dp)

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
        gridViewDecoViews!!.visibility = View.GONE

        dropDownButtonOfGridView.setOnClickListener(View.OnClickListener {
            if(gridViewDecoViews!!.visibility == View.GONE){
                gridViewDecoViews!!.visibility = View.VISIBLE
                dropDownButtonOfGridView.setImageResource(R.drawable. ic_keyboard_arrow_up_black_24dp)
            }
            else {
                gridViewDecoViews!!.visibility = View.GONE
                dropDownButtonOfGridView.setImageResource(R.drawable. ic_keyboard_arrow_down_black_24dp)
            }
        })

        mDecoView4 = view.findViewById<DecoView>(R.id.dynamicArcIcon)
        mDecoView5 = view.findViewById<DecoView>(R.id.dynamicArcIcon2)



        mDecoView = view.findViewById(R.id.dynamicArcView)
        mDecoView2 = view.findViewById(R.id.dynamicArcView2)
        mDecoView3 = view.findViewById(R.id.dynamicArcView3)

        textPercentage = view.findViewById(R.id.textPercentage)
        textRemaining = view.findViewById(R.id.textRemaining)
        textActivity1 = view.findViewById(R.id.textActivity1)
        textActivity2 = view.findViewById(R.id.textActivity2)
        textActivity3 = view.findViewById(R.id.textActivity3)

        rankActivity1 = view.findViewById(R.id.rankActivity1)
        rankActivity2 = view.findViewById(R.id.rankActivity2)
        rankActivity3 = view.findViewById(R.id.rankActivity3)

//        demo1 = view.findViewById(R.id.demo1)
//        demo2 = view.findViewById(R.id.demo2)
//        clear = view.findViewById(R.id.clear)

        mDecoView!!.visibility = View.GONE
        rankActivity1.visibility = View.GONE
        textActivity1.visibility = View.GONE

        imageActivity1 = view.findViewById(R.id.imageActivity1)


//        demo1.visibility = View.GONE
//        demo2.visibility = View.GONE
//        clear.visibility = View.GONE
//        demo2.text = "DEMO"
//
//        demo1.setOnClickListener { demo1(1500f, 1300f, 4000f) }
//
//        demo2.setOnClickListener {
//            if (demo2.text === "DEMO") {
//                demo1.visibility = View.VISIBLE
//                clear.visibility = View.VISIBLE
//                demo2.text = "100%"
//            } else if (demo2.text === "100%") {
//                demo2()
//                demo1.isEnabled = false
//            }
//        }
//
//        clear.setOnClickListener {
//            clear()
//            demo1.visibility = View.GONE
//            clear.visibility = View.GONE
//            demo2.text = "DEMO"
//            demo1.isEnabled = true
//        }




        val model = ViewModelProviders.of(activity!!).get(Communicator::class.java)
        val `object` = Observer<Any> { o ->
            // Update the UI

            Log.w(TAG, "allSteps" + o!!.toString())

            val value = o.toString().substring(1, o.toString().length - 1)
            val keyValuePairs = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val allSteps = java.util.HashMap<String, String>()

            for (pair in keyValuePairs) {
                val entry = pair.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                allSteps[entry[0].trim { it <= ' ' }] = entry[1].trim { it <= ' ' }
            }

            Log.w(TAG, "my_steps" + allSteps["my_steps"]!!)

            user_id = allSteps["user_id"]!!.toString()

            my_goal = allSteps["my_goal"]!!.toLong()
            teammate_goal = allSteps["teammate_goal"]!!.toLong()
            team_goal = allSteps["team_goal"]!!.toLong()

            my_steps = allSteps["my_steps"]!!.toLong()
            teammate_steps = allSteps["teammate_steps"]!!.toLong()
            team_steps = allSteps["team_steps"]!!.toLong()

            selected_id = allSteps["selected_id"]!!.toString()


            if (selected_id != "Display None") {
                if(temp != selected_id) {
                    temp = selected_id
                    imageActivity1.setImageResource(R.drawable.ic_person_black_24dp)
                    mDecoView!!.visibility = View.VISIBLE
                    rankActivity1.visibility = View.VISIBLE
                    textActivity1.visibility = View.VISIBLE
                    createBackSeriesTeammate(teammate_goal.toFloat())
                    createDataSeriesTeammate(teammate_goal.toFloat())
                    createEventsTeammate()
                    }
                } else {
                temp = selected_id
                imageActivity1.setImageResource(R.drawable.ic_add_black_24dp)
                mDecoView!!.visibility = View.GONE
                rankActivity1.visibility = View.GONE
                textActivity1.visibility = View.GONE
            }


            refreshEvents(teammate_steps.toFloat(), my_steps.toFloat(), team_steps.toFloat())

        }


        model.message.observe(activity!!, `object`)



        val myRef = FirebaseDatabase.getInstance().reference.child("User")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                teammates.clear()
                Log.i(TAG, "myTeamMember: $my")
                var index = 1
                for((key, value) in my){
                    val details = value as Map<String, String>


                    if(key == user_id) {
                        Log.i(TAG, "3ebfab: $key")
                        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.shuail8)
                        bitmap = getCroppedBitmap(bitmap)
                        teammates.add(Teammates(bitmap, key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], "#3ebfab"))
                    }
                    else{
                        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.shuail8)
                        bitmap = getCroppedBitmap(bitmap)
                        teammates.add(Teammates(bitmap, key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], "#000000"))

                    }



                    index++
                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")

                }
                Log.w(TAG, "teammates" + teammates.toString())
                val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getSteps))
                teammates = ArrayList(teammatesSort)
                var indexM = 1
                for(teammate in teammates) {
                    teammate.rank = indexM.toString()
                    indexM++
                }
                Log.w(TAG, "teammates" + teammates.toString())


                teammatesAdapter = TeammatesAdapter(
                    activity,
                    R.layout.decoviews,
                    teammates
                )
                gridViewDecoViews!!.adapter = teammatesAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

        gridViewDecoViews!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

        }

        val handler = Handler()
        handler.postDelayed({
            createBackSeries(teammate_goal.toFloat(), my_goal.toFloat(), team_goal.toFloat())
            createDataSeries(teammate_goal.toFloat(), my_goal.toFloat(), team_goal.toFloat())
            createEvents()
        }, 1000)


        imageActivity1.setOnClickListener {
            val teamMemberListFragment = TeamMemberListFragment()
            teamMemberListFragment.show(activity!!.supportFragmentManager, DecoviewDialogFragment.TAG)
        }


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


                groupStepsSeries!!.appendData(DataPoint(second.toDouble(), team_steps.toDouble()), true, 60)
                // set manual X bounds
                groupStepsGraph.viewport.isYAxisBoundsManual = true
                groupStepsGraph.viewport.setMinY(0.0)
                groupStepsGraph.viewport.setMaxY((team_steps + team_steps*0.3))

                groupStepsGraph.viewport.isXAxisBoundsManual = true
                groupStepsGraph.viewport.setMinX(0.0)
                groupStepsGraph.viewport.setMaxX(second + second*0.3)

                // enable scaling and scrolling
                groupStepsGraph.viewport.isScalable = true
                groupStepsGraph.viewport.setScalableY(true)
                groupStepsGraph.addSeries(groupStepsSeries)

                Log.w(TAG, "unixTime" + unixTime )
                Log.w(TAG, "second" + second.toDouble() )

            }
        }
    }

    fun getCroppedBitmap(bitmap:Bitmap ):Bitmap {
        var output  = Bitmap.createBitmap(bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888);
        var  canvas:Canvas = Canvas(output)

        var color:Int = 0xff424242.toInt()
        var paint:Paint = Paint()
        var rect:Rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true;
        canvas.drawARGB(0, 0, 0, 0);
        paint.color = color
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(), paint);
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
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

    private fun createBackSeriesTeammate(teammateGoal:Float) {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, teammateGoal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex = mDecoView!!.addSeries(seriesItem)
    }

    private fun createDataSeriesTeammate(teammateGoal:Float) {

        val seriesItem = SeriesItem.Builder(Color.parseColor("#77e6f1")) //colorActivity1
            .setRange(-1f, teammateGoal, 0f)
            .setInitialVisibility(false)
            .build()

        seriesItem.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                rankActivity1.text = "No. 1"
                textActivity1.text = String.format("%.0f / %.0f", currentPosition, teammateGoal)
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })

        mSeries1Index = mDecoView!!.addSeries(seriesItem)
    }

    private fun createEventsTeammate() {
        mDecoView!!.executeReset()

        mDecoView!!.addEvent(
            DecoEvent.Builder(teammate_goal.toFloat())
                .setIndex(mBackIndex)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )
    }

    private fun createBackSeries(teammateGoal:Float, myGoal: Float, teamGoal: Float) {
//        val seriesItem = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
//            .setRange(-1f, teammateGoal, 0f)
//            .setInitialVisibility(true)
//            .build()
//
//        mBackIndex = mDecoView!!.addSeries(seriesItem)

        val seriesItem2 = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, myGoal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex2 = mDecoView2!!.addSeries(seriesItem2)

        val seriesItem3 = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, teamGoal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex3 = mDecoView3!!.addSeries(seriesItem3)

        val seriesItem4 = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, myGoal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex4 = mDecoView4!!.addSeries(seriesItem4)

        val seriesItem5 = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, teamGoal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex5 = mDecoView5!!.addSeries(seriesItem5)
    }

    private fun createDataSeries(teammateGoal:Float, myGoal: Float, teamGoal: Float) {

//        val seriesItem = SeriesItem.Builder(Color.parseColor("#77e6f1")) //colorActivity1
//            .setRange(-1f, teammateGoal, 0f)
//            .setInitialVisibility(false)
//            .build()
//
//        seriesItem.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
//            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
//                rankActivity1.text = "No. 1"
//                textActivity1.text = String.format("%.0f / %.0f", currentPosition, teammateGoal)
//            }
//
//            override fun onSeriesItemDisplayProgress(percentComplete: Float) {
//
//            }
//        })
//
//        mSeries1Index = mDecoView!!.addSeries(seriesItem)

        val seriesItem2 = SeriesItem.Builder(Color.parseColor("#3ebfab")) //colorActivity2
            .setRange(-1f, myGoal, 0f)
            .setInitialVisibility(false)
            .build()


        seriesItem2.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                rankActivity2.text = "No. 2"
                textActivity2.text = String.format("%.0f / %.0f", currentPosition, myGoal)
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })


        seriesItem2.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                percentFilled = (currentPosition - seriesItem2.minValue) / (seriesItem2.maxValue - seriesItem2.minValue)
                textPercentage.text = String.format("%.0f%%", percentFilled * 100f)
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })


        seriesItem2.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                remainingKm = myGoal - currentPosition

                textRemaining.text = String.format("%.0f steps to goal", remainingKm.absoluteValue)

            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })

        mSeries2Index = mDecoView2!!.addSeries(seriesItem2)

        val seriesItem3 = SeriesItem.Builder(Color.parseColor("#ff6347")) //colorActivity3
            .setRange(-1f, teamGoal, 0f)
            .setInitialVisibility(false)
            .build()


        seriesItem3.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                rankActivity3.text = "No. 2"
                textActivity3.text = String.format("%.0f / %.0f", currentPosition, teamGoal)
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })

        mSeries3Index = mDecoView3!!.addSeries(seriesItem3)



        val seriesItem4 = SeriesItem.Builder(Color.parseColor("#3ebfab")) //colorDis
            .setRange(-1f, myGoal, 0f)
            .setInitialVisibility(false)
            .build()

        mSeries4Index = mDecoView4!!.addSeries(seriesItem4)

        val seriesItem5 = SeriesItem.Builder(Color.parseColor("#ff6347")) //colorTime
            .setRange(-1f, teamGoal, 0f)
            .setInitialVisibility(false)
            .build()

        mSeries5Index = mDecoView5!!.addSeries(seriesItem5)

    }


    private fun createEvents() {
        mDecoView2!!.executeReset()

//        mDecoView!!.addEvent(
//            DecoEvent.Builder(teammate_goal.toFloat())
//                .setIndex(mBackIndex)
//                .setDuration(1000)
//                .setDelay(100)
//                .build()
//        )
        mDecoView2!!.addEvent(
            DecoEvent.Builder(my_goal.toFloat())
                .setIndex(mBackIndex2)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )
        mDecoView3!!.addEvent(
            DecoEvent.Builder(team_goal.toFloat())
                .setIndex(mBackIndex3)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )


//        mDecoView!!.addEvent(
//            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
//                .setIndex(mSeries1Index)
//                .setDuration(1000)
//                .setDelay(300)
//                .build()
//        )
        mDecoView2!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries2Index)
                .setDuration(1000)
                .setDelay(300)
                .build()
        )
        mDecoView3!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries3Index)
                .setDuration(1000)
                .setDelay(300)
                .build()
        )

        mDecoView4!!.executeReset()

        mDecoView4!!.addEvent(
            DecoEvent.Builder(my_goal.toFloat())
                .setIndex(mBackIndex4)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView4!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries4Index)
                .setDuration(1000)
                .setDelay(300)
                .build()
        )


        mDecoView5!!.addEvent(
            DecoEvent.Builder(team_goal.toFloat())
                .setIndex(mBackIndex5)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView5!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries5Index)
                .setDuration(1000)
                .setDelay(300)
                .build()
        )
    }

    private fun refreshEvents(teammateSteps: Float, mySteps: Float, teamSteps: Float) {

        mDecoView!!.addEvent(
            DecoEvent.Builder(teammateSteps)
                .setIndex(mSeries1Index)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView2!!.addEvent(
            DecoEvent.Builder(mySteps)
                .setIndex(mSeries2Index)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView3!!.addEvent(
            DecoEvent.Builder(teamSteps)
                .setIndex(mSeries3Index)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView4!!.addEvent(
            DecoEvent.Builder(mySteps)
                .setIndex(mSeries4Index)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoView5!!.addEvent(
            DecoEvent.Builder(teamSteps)
                .setIndex(mSeries5Index)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )
    }





//    private fun demo1(teammateSteps: Float, mySteps: Float, teamSteps: Float) {
//
//        mDecoView!!.addEvent(
//            DecoEvent.Builder(teammateSteps)
//                .setIndex(mSeries1Index)
//                .setDuration(1000)
//                .setDelay(100)
//                .build()
//        )
//        mDecoView2!!.addEvent(
//            DecoEvent.Builder(mySteps)
//                .setIndex(mSeries2Index)
//                .setDuration(1000)
//                .setDelay(100)
//                .build()
//        )
//        mDecoView3!!.addEvent(
//            DecoEvent.Builder(teamSteps)
//                .setIndex(mSeries3Index)
//                .setDuration(1000)
//                .setDelay(100)
//                .build()
//        )
//    }
//
//    private fun demo2() {
//
//        mDecoView!!.addEvent(DecoEvent.Builder(2000f).setIndex(mSeries1Index).setDelay(100).setDuration(1000).build())
//
//        mDecoView2!!.addEvent(DecoEvent.Builder(2000f).setIndex(mSeries2Index).setDelay(100).setDuration(1000).build())
//
//        mDecoView3!!.addEvent(
//            DecoEvent.Builder(mSeriesMax)
//                .setIndex(mSeries3Index)
//                .setDelay(100)
//                .setDuration(1000)
//                .build()
//        )
//
//        mDecoView!!.addEvent(
//            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE)
//                .setIndex(mSeries1Index)
//                .setDelay(1500)
//                .setDuration(2000)
//                .build()
//        )
//
//        mDecoView2!!.addEvent(
//            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE)
//                .setIndex(mSeries2Index)
//                .setDelay(1500)
//                .setDuration(2000)
//                .build()
//        )
//
//
//        mDecoView3!!.addEvent(
//            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE)
//                .setIndex(mSeries3Index)
//                .setDelay(1500)
//                .setDuration(2000)
//                .setDisplayText("Nice Job!")
//                .setListener(object : DecoEvent.ExecuteEventListener {
//                    override fun onEventStart(decoEvent: DecoEvent) {
//                        resetText()
//                    }
//
//                    override fun onEventEnd(decoEvent: DecoEvent) {
//                        mDecoView!!.addEvent(
//                            DecoEvent.Builder(mSeriesMax)
//                                .setIndex(mBackIndex)
//                                .setDuration(2000)
//                                .setDelay(100)
//                                .build()
//                        )
//                        mDecoView2!!.addEvent(
//                            DecoEvent.Builder(mSeriesMax)
//                                .setIndex(mBackIndex2)
//                                .setDuration(2000)
//                                .setDelay(100)
//                                .build()
//                        )
//                        mDecoView3!!.addEvent(
//                            DecoEvent.Builder(mSeriesMax)
//                                .setIndex(mBackIndex3)
//                                .setDuration(2000)
//                                .setDelay(100)
//                                .build()
//                        )
//
//                        mDecoView!!.addEvent(
//                            DecoEvent.Builder(2000f)
//                                .setIndex(mSeries1Index)
//                                .setDelay(110)
//                                .build()
//                        )
//                        mDecoView2!!.addEvent(
//                            DecoEvent.Builder(2000f)
//                                .setIndex(mSeries2Index)
//                                .setDelay(120)
//                                .build()
//                        )
//                        mDecoView3!!.addEvent(
//                            DecoEvent.Builder(mSeriesMax)
//                                .setIndex(mSeries3Index)
//                                .setDelay(130)
//                                .build()
//                        )
//
//                    }
//                })
//                .build()
//        )
//    }
//
//    private fun clear() {
//        mDecoView!!.addEvent(
//            DecoEvent.Builder(0f)
//                .setIndex(mSeries1Index)
//                .setDelay(0)
//                .setDuration(0)
//                .build()
//        )
//        mDecoView2!!.addEvent(
//            DecoEvent.Builder(0f)
//                .setIndex(mSeries2Index)
//                .setDelay(0)
//                .setDuration(0)
//                .build()
//        )
//        mDecoView3!!.addEvent(
//            DecoEvent.Builder(0f)
//                .setIndex(mSeries3Index)
//                .setDelay(0)
//                .setDuration(0)
//                .build()
//        )
//    }
//
//    private fun resetText() {
//        //        textActivity1.setText("");
//        //        textActivity2.setText("");
//        //        textActivity3.setText("");
//        textPercentage.text = ""
//        textRemaining.text = ""
//
//        rankActivity1.text = ""
//        rankActivity2.text = ""
//        rankActivity3.text = ""
//
//    }

}
