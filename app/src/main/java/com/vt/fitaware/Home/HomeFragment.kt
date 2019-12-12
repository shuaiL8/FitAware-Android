package com.vt.fitaware.Home


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import com.vt.fitaware.R
import com.vt.fitaware.Home.Calendar.CalendarFragment
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import java.text.SimpleDateFormat
import java.util.*
import com.vt.fitaware.Communicator
import com.google.firebase.database.*
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.*
import androidx.navigation.Navigation
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.collections.ArrayList


class HomeFragment : Fragment(){



    private val TAG = "HomeFragment"
    private var user_id: String = "none"

    private var team: String = "none"
    private var captain: String = "none"
    private var periodical: String = "none"

    private var teamRank: String = "0"


    private var my_steps: Long = 0
    private var my_duration: Long = 0
    private var my_heartPoints: Long = 0
    private var my_distance: Long = 0
    private var my_calories: Long = 0
    private var teammate_steps: Long = 0
    private var team_steps: Long = 0

    private var token: String = "none"


    private var newSelected: String = "none"
    private var newGraph: String = "none"


    private var my_goal: Long = 0
    private var teammate_goal: Long = 0
    private var team_goal: Long = 0

    private var mDecoViewTeam: DecoView? = null
    private var mBackIndexTeam: Int = 0
    private var mSeriesIndexTeam: Int = 0

    internal lateinit var rankOfTeam: TextView
    internal lateinit var teamID: TextView
    internal lateinit var stepsOfGoal: TextView

    private var mDecoView: DecoView? = null
    private var mDecoView2: DecoView? = null
    private var mDecoView3: DecoView? = null


    private var mBackIndex: Int = 0
    private var mBackIndex2: Int = 0
    private var mBackIndex3: Int = 0

    private var mSeries1Index: Int = 0
    private var mSeries2Index: Int = 0
    private var mSeries3Index: Int = 0

    internal var percentFilled = 0f
    internal var remainingKm = 0f

    internal lateinit var imageActivity1: ImageView
    internal lateinit var imageActivity2: ImageView
    internal lateinit var imageActivity3: ImageView
    internal lateinit var imageActivity4: ImageView
    internal lateinit var imageActivity5: ImageView
    internal lateinit var imageActivity6: ImageView

    internal lateinit var textName: TextView
    internal lateinit var textRank: TextView
    internal lateinit var textPercentage: TextView
    internal lateinit var textRemaining: TextView
    internal lateinit var textActivity1: TextView
    internal lateinit var textActivity2: TextView
    internal lateinit var textActivity3: TextView
    internal lateinit var rankActivity1: TextView
    internal lateinit var rankActivity2: TextView
    internal lateinit var rankActivity3: TextView

    internal lateinit var textActivity4: TextView
    internal lateinit var textActivity5: TextView
    internal lateinit var textActivity6: TextView
    internal lateinit var rankActivity4: TextView
    internal lateinit var rankActivity5: TextView
    internal lateinit var rankActivity6: TextView

    private var gridViewDecoViews: ExpandableHeightGridView? = null

    private var teammates = ArrayList<Teammates>(1)
    private var teammatesAdapter: TeammatesAdapter? = null

    private var sharedPreferences: SharedPreferences? = null

    private var mStorageRef: StorageReference? = null

    private var calendar_frameVisibility: String = "none"

    private var imageView_teamIcon:ImageView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_home, container,
            false)
        setHasOptionsMenu(true)
        initSharedPreferences()

        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = "FitAware"


        user_id = sharedPreferences!!.getString("user_id", "none")
        captain = sharedPreferences!!.getString("captain", "none")
        team = sharedPreferences!!.getString("team", "none")
        my_goal = sharedPreferences!!.getString("my_goal", "0").toLong()
        periodical = sharedPreferences!!.getString("periodical", "none")
        my_steps = sharedPreferences!!.getString("currentSteps", "0").toLong()
        my_duration = sharedPreferences!!.getString("duration", "0").toLong()
        my_heartPoints = sharedPreferences!!.getString("heartPoints", "0").toLong()
        my_distance = sharedPreferences!!.getString("distance", "0").toLong()
        my_calories = sharedPreferences!!.getString("calories", "0").toLong()
        token = sharedPreferences!!.getString("token", "none")
        calendar_frameVisibility = sharedPreferences!!.getString("calendar_frameVisibility", "none")

        val textDate = view.findViewById<TextView>(R.id.textDate)

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM dd")
        val currentDate = simpleDateFormat.format(calendar.time)
        textDate.text = currentDate
        val calendar_frame = view.findViewById<FrameLayout>(R.id.calendar_frame)
        val decoView_frame = view.findViewById<FrameLayout>(R.id.decoView_frame)
        val team_frame = view.findViewById<FrameLayout>(R.id.team_frame)
        val team_pie_frame = view.findViewById<FrameLayout>(R.id.team_pie_frame)
        val pie_chart_view = view.findViewById<AnyChartView>(R.id.pie_chart_view)
        val switchButton = view.findViewById<ImageView>(R.id.switchButton)
        val switchToTeam = view.findViewById<ImageView>(R.id.switchToTeam)
        val switchToPie = view.findViewById<ImageView>(R.id.switchToPie)

        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)

        val dropDownButton = view.findViewById<ImageButton>(R.id.dropDownButton)

        team_pie_frame.visibility = View.GONE


        mDecoViewTeam = view.findViewById<DecoView>(R.id.dynamicArcViewsTeam)
        imageView_teamIcon = view.findViewById<ImageView>(R.id.imageView_teamIcon)

        imageView_teamIcon!!.setOnClickListener {
            if(captain == user_id) {

                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder
                    .setMessage("Do you want to change the Team icon?")
                    .setPositiveButton("Choose from Gallery", DialogInterface.OnClickListener {
                            dialog, id ->

                        val pickPhoto = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(pickPhoto, 0)

                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                alert.show()



            }
            else {
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder
                    .setMessage("Only team captain can change the Team icon")
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                alert.show()
            }

        }


        mStorageRef = FirebaseStorage.getInstance().reference

        val iconRef = mStorageRef!!.child("team_icon/$team/icon.jpg")

        iconRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadIconUrl = task.result

                Picasso.get().load(downloadIconUrl).into(imageView_teamIcon)

            } else {
                var bMap = BitmapFactory.decodeResource(resources, R.drawable.teamwork)
                bMap = getCroppedBitmap(bMap)
                imageView_teamIcon!!.setImageBitmap(bMap)
            }
        }



        rankOfTeam = view.findViewById<TextView>(R.id.rankOfTeam)
        teamID = view.findViewById<TextView>(R.id.teamID)
        stepsOfGoal = view.findViewById<TextView>(R.id.stepsOfGoal)


        val calendarFragment = CalendarFragment()
        fragmentManager!!.beginTransaction().replace(R.id.calendar_frame, calendarFragment).addToBackStack(null).commit()

        gridViewDecoViews = view.findViewById(R.id.gridViewDecoViews)
        gridViewDecoViews!!.isExpanded = true

        if(calendar_frameVisibility == "none") {
            calendar_frame.visibility = View.GONE
            dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_down_black_24dp)

        }
        else{
            calendar_frame.visibility = View.VISIBLE
            dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_up_black_24dp)

        }

        dropDownButton.setOnClickListener(View.OnClickListener {
            if(calendar_frame.visibility == View.GONE){
                calendar_frame.visibility = View.VISIBLE
                dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_up_black_24dp)

                val editor = sharedPreferences?.edit()
                editor!!.putString("calendar_frameVisibility", "VISIBLE")

                editor.commit()
            }
            else {
                calendar_frame.visibility = View.GONE
                dropDownButton.setImageResource(R.drawable. ic_keyboard_arrow_down_black_24dp)
                val editor = sharedPreferences?.edit()
                editor!!.putString("calendar_frameVisibility", "none")

                editor.commit()
            }
        })

        val tabLayoutHome = view.findViewById<TabLayout>(R.id.tabLayoutHome)

        val duration = tabLayoutHome.newTab() // Create a new Tab names "First Tab"
        duration.setIcon(R.drawable.ic_duration)
        tabLayoutHome.addTab(duration, 0)

        val steps = tabLayoutHome.newTab() // Create a new Tab names "First Tab"
        steps.setIcon(R.drawable.ic_steps)
        tabLayoutHome.addTab(steps, 1)

        val heartPoints = tabLayoutHome.newTab() // Create a new Tab names "First Tab"
        heartPoints.setIcon(R.drawable.ic_heartpoints)
        tabLayoutHome.addTab(heartPoints, 2)

        tabLayoutHome.getTabAt(1)!!.select()

        mDecoView = view.findViewById(R.id.dynamicArcView)
        mDecoView2 = view.findViewById(R.id.dynamicArcView2)
        mDecoView3 = view.findViewById(R.id.dynamicArcView3)

        textName = view.findViewById(R.id.textName)
        textRank = view.findViewById(R.id.textRank)

        textPercentage = view.findViewById(R.id.textPercentage)
        textRemaining = view.findViewById(R.id.textRemaining)

        imageActivity1 = view.findViewById(R.id.imageActivity1)
        imageActivity2 = view.findViewById(R.id.imageActivity2)
        imageActivity3 = view.findViewById(R.id.imageActivity3)
        imageActivity4 = view.findViewById(R.id.imageActivity4)
        imageActivity5 = view.findViewById(R.id.imageActivity5)
        imageActivity6 = view.findViewById(R.id.imageActivity6)

        textActivity1 = view.findViewById(R.id.textActivity1)
        textActivity2 = view.findViewById(R.id.textActivity2)
        textActivity3 = view.findViewById(R.id.textActivity3)
        textActivity4 = view.findViewById(R.id.textActivity4)
        textActivity5 = view.findViewById(R.id.textActivity5)
        textActivity6 = view.findViewById(R.id.textActivity6)

        rankActivity1 = view.findViewById(R.id.rankActivity1)
        rankActivity2 = view.findViewById(R.id.rankActivity2)
        rankActivity3 = view.findViewById(R.id.rankActivity3)
        rankActivity4 = view.findViewById(R.id.rankActivity4)
        rankActivity5 = view.findViewById(R.id.rankActivity5)
        rankActivity6 = view.findViewById(R.id.rankActivity6)

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
            team = allSteps["team"]!!.toString()

            my_steps = allSteps["my_steps"]!!.toLong()
            my_heartPoints = allSteps["my_heartPoints"]!!.toLong()
            my_duration = allSteps["my_duration"]!!.toLong()
            my_distance = allSteps["my_distance"]!!.toLong()
            my_calories = allSteps["my_calories"]!!.toLong()

            teammate_steps = allSteps["teammate_steps"]!!.toLong()
            team_steps = allSteps["team_steps"]!!.toLong()

            my_goal = allSteps["my_goal"]!!.toLong()
            periodical = allSteps["periodical"]!!.toString()
            teammate_goal = allSteps["teammate_goal"]!!.toLong()
            team_goal = allSteps["team_goal"]!!.toLong()
            teamRank = allSteps["teamRank"]!!.toString()


            if(team != "none") {
                refreshEventsTeam(team_steps.toFloat(), teamRank)
            }

            if(newSelected != "none") {
                for(id in teammates) {
                    if(id.name == newSelected ) {
                        refreshEvents(id.name, "No. "+id.rank, id.duration.toFloat(), id.steps.toFloat(), id.heartPoints.toFloat())
                        textActivity4.text = id.distance.toString()
                        textActivity5.text = id.calories.toString()
                        textActivity6.text = "0"
                    }

                }
            }
            else {
                refreshEvents("","" , my_duration.toFloat(), my_steps.toFloat(), my_heartPoints.toFloat())
                textActivity4.text = my_distance.toString()
                textActivity5.text = my_calories.toString()
                textActivity6.text = "0"
            }


        }
        model.message.observe(activity!!, `object`)


//        val calendarNY = Calendar.getInstance()
//        val mdformatNY = SimpleDateFormat("yyyy-MM-dd")
////        mdformatNY.timeZone = TimeZone.getTimeZone("America/New_York")
//        val strDate = mdformatNY.format(calendarNY.time)
//
//        initDaily(
//            user_id,
//            strDate,
//            my_calories.toString(),
//            my_goal.toString(),
//            my_heartPoints.toString(),
//            my_duration.toString(),
//            my_distance.toString(),
//            my_steps.toString(),
//            token)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.homeFragment)

//            initDaily(
//                user_id,
//                strDate,
//                my_calories.toString(),
//                my_goal.toString(),
//                my_heartPoints.toString(),
//                my_duration.toString(),
//                my_distance.toString(),
//                my_steps.toString(),
//                token)

        }


        if(tabLayoutHome.selectedTabPosition == 1) {
            val myRef = FirebaseDatabase.getInstance().reference.child("User")

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val my = dataSnapshot.value as Map<String, Any>

                    teammates.clear()
                    Log.i(TAG, "myTeamMember: $my")

                    var iniTeamSteps = 0L

                    var index = 1
                    for((key, value) in my){
                        val details = value as Map<String, String>

                        val myRefDailyRecord = FirebaseDatabase.getInstance().reference.child("DailyRecord/$key")
                        val postListenerDailyRecord = object : ValueEventListener {
                            override fun onDataChange(dataSnapshotDR: DataSnapshot) {
                                // Get Post object and use the values to update the UI

                                if(dataSnapshotDR.value != null) {
                                    val myDR = dataSnapshotDR.value as Map<String, Any>

                                    val calendar = Calendar.getInstance()
                                    val mdformat = SimpleDateFormat("yyyy-MM-dd")
//                                    mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
                                    val strDate = mdformat.format(calendar.time)

                                    if (!myDR.containsKey(strDate)) {
                                        resetPost(
                                            key,
                                            "0",
                                            "0",
                                            "0",
                                            "0",
                                            "0"
                                        )

                                        Log.i(TAG, "reset DailyRecord: $key")

                                    }
                                }
                            }

                            override fun onCancelled(databaseErrorDR: DatabaseError) {
                                // Getting Post failed, log a message
                                Log.w(TAG, "loadPost:onCancelled", databaseErrorDR.toException())
                                // ...
                            }
                        }
                        myRefDailyRecord.addValueEventListener(postListenerDailyRecord)

                        if(key == user_id) {
                            team_goal = details["teamGoal"].toString().toLong()

                            Log.i(TAG, "3ebfab: $key")
                            teammates.add(Teammates("1", key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(),"#008577"))
                            captain = details.getValue("captain")
                            team = details.getValue("team")
                            periodical = details["periodical"].toString()

                            iniTeamSteps += details["currentSteps"].toString().toLong()
                        }
                        else {

                            if(details["team"].toString() == team && details["team"].toString() != "none"){
                                teammates.add(Teammates("1",  key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#3ebfab"))
                                iniTeamSteps += details["currentSteps"].toString().toLong()
                            }

                        }

                        index++
                        Log.i(TAG, "iniTeamSteps: $iniTeamSteps")

                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    team_steps = iniTeamSteps


                    Log.w(TAG, "teammates" + teammates.toString())
                    val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getSteps))
                    teammates = ArrayList(teammatesSort)
                    var indexM = 1
                    for(teammate in teammates) {
                        teammate.rank = indexM.toString()
                        indexM++
                    }
                    Log.w(TAG, "teammates" + teammates.toString())

                    if (activity !=null){
                        teammatesAdapter = TeammatesAdapter(
                            activity,
                            R.layout.decoviews,
                            teammates
                        )
                        gridViewDecoViews!!.adapter = teammatesAdapter
                    }


                    Log.w(TAG, "test teammates: $teammates")
                    val pie = AnyChart.pie()
                    val pieData = ArrayList<DataEntry>()
                    for (teammate in teammates) {
                        pieData.add(ValueDataEntry(teammate.name, teammate.steps))
                    }

                    pie.data(pieData)
                    pie_chart_view.setChart(pie)


                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }
            myRef.addValueEventListener(postListener)
        }



        tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // called when tab selected
                if (tab.position == 0) {

                    val myRef = FirebaseDatabase.getInstance().reference.child("User")

                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            val my = dataSnapshot.value as Map<String, Any>

                            teammates.clear()
                            Log.i(TAG, "myTeamMember: $my")

                            var iniTeamSteps = 0L

                            var index = 1
                            for((key, value) in my){
                                val details = value as Map<String, String>


                                if(key == user_id) {
                                    team_goal = details["teamGoal"].toString().toLong()

                                    Log.i(TAG, "3ebfab: $key")
                                    teammates.add(Teammates("0", key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(),"#008577"))
                                    captain = details.getValue("captain")
                                    team = details.getValue("team")

                                    iniTeamSteps += details["currentSteps"].toString().toLong()
                                }
                                else {

                                    if(details["team"].toString() == team && details["team"].toString() != "none"){
                                        teammates.add(Teammates("0",  key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#77e6f1"))
                                        iniTeamSteps += details["currentSteps"].toString().toLong()
                                    }

                                }


                                index++
                                Log.i(TAG, "iniTeamSteps: $iniTeamSteps")

                                Log.i(TAG, "$key: $value")
                                Log.i(TAG, "details: $details")

                            }

                            team_steps = iniTeamSteps


                            Log.w(TAG, "teammates" + teammates.toString())
                            val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getDuration))
                            teammates = ArrayList(teammatesSort)
                            var indexM = 1
                            for(teammate in teammates) {
                                teammate.rank = indexM.toString()
                                indexM++
                            }
                            Log.w(TAG, "teammates" + teammates.toString())

                            if (activity !=null){
                                teammatesAdapter = TeammatesAdapter(
                                    activity,
                                    R.layout.decoviews,
                                    teammates
                                )
                                gridViewDecoViews!!.adapter = teammatesAdapter
                            }


                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            // ...
                        }
                    }
                    myRef.addValueEventListener(postListener)

                }
                else if (tab.position == 1) {
                    val myRef = FirebaseDatabase.getInstance().reference.child("User")

                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            val my = dataSnapshot.value as Map<String, Any>

                            teammates.clear()
                            Log.i(TAG, "myTeamMember: $my")

                            var iniTeamSteps = 0L

                            var index = 1
                            for((key, value) in my){
                                val details = value as Map<String, String>


                                if(key == user_id) {
                                    team_goal = details["teamGoal"].toString().toLong()

                                    Log.i(TAG, "3ebfab: $key")
                                    teammates.add(Teammates("1", key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(),"#008577"))
                                    captain = details.getValue("captain")
                                    team = details.getValue("team")

                                    iniTeamSteps += details["currentSteps"].toString().toLong()
                                }
                                else {

                                    if(details["team"].toString() == team && details["team"].toString() != "none"){
                                        teammates.add(Teammates("1",  key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#3ebfab"))
                                        iniTeamSteps += details["currentSteps"].toString().toLong()
                                    }

                                }



                                index++
                                Log.i(TAG, "iniTeamSteps: $iniTeamSteps")

                                Log.i(TAG, "$key: $value")
                                Log.i(TAG, "details: $details")

                            }

                            team_steps = iniTeamSteps


                            Log.w(TAG, "teammates" + teammates.toString())
                            val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getSteps))
                            teammates = ArrayList(teammatesSort)
                            var indexM = 1
                            for(teammate in teammates) {
                                teammate.rank = indexM.toString()
                                indexM++
                            }
                            Log.w(TAG, "teammates" + teammates.toString())

                            if (activity !=null){
                                teammatesAdapter = TeammatesAdapter(
                                    activity,
                                    R.layout.decoviews,
                                    teammates
                                )
                                gridViewDecoViews!!.adapter = teammatesAdapter
                            }


                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            // ...
                        }
                    }
                    myRef.addValueEventListener(postListener)

                }
                else {
                    val myRef = FirebaseDatabase.getInstance().reference.child("User")

                    val postListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            val my = dataSnapshot.value as Map<String, Any>

                            teammates.clear()
                            Log.i(TAG, "myTeamMember: $my")

                            var iniTeamSteps = 0L

                            var index = 1
                            for((key, value) in my){
                                val details = value as Map<String, String>


                                if(key == user_id) {
                                    team_goal = details["teamGoal"].toString().toLong()

                                    Log.i(TAG, "3ebfab: $key")
                                    teammates.add(Teammates("2", key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(),"#008577"))
                                    captain = details.getValue("captain")
                                    team = details.getValue("team")

                                    iniTeamSteps += details["currentSteps"].toString().toLong()
                                }
                                else {

                                    if(details["team"].toString() == team && details["team"].toString() != "none"){
                                        teammates.add(Teammates("2",  key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#ff6347"))
                                        iniTeamSteps += details["currentSteps"].toString().toLong()
                                    }

                                }


                                index++
                                Log.i(TAG, "iniTeamSteps: $iniTeamSteps")

                                Log.i(TAG, "$key: $value")
                                Log.i(TAG, "details: $details")

                            }

                            team_steps = iniTeamSteps


                            Log.w(TAG, "teammates" + teammates.toString())
                            val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getHeartPoints))
                            teammates = ArrayList(teammatesSort)
                            var indexM = 1
                            for(teammate in teammates) {
                                teammate.rank = indexM.toString()
                                indexM++
                            }
                            Log.w(TAG, "teammates" + teammates.toString())

                            if (activity !=null){
                                teammatesAdapter = TeammatesAdapter(
                                    activity,
                                    R.layout.decoviews,
                                    teammates
                                )
                                gridViewDecoViews!!.adapter = teammatesAdapter
                            }


                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                            // ...
                        }
                    }
                    myRef.addValueEventListener(postListener)

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // called when tab unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // called when a tab is reselected
            }
        })



        team_frame.visibility =View.GONE
        gridViewDecoViews!!.visibility = View.GONE
        tabLayoutHome.visibility =View.GONE
        decoView_frame.visibility = View.GONE

        val handler = Handler()
        handler.postDelayed({


            if(team == "none"){
                decoView_frame.visibility = View.VISIBLE
                switchButton.visibility = View.GONE
                team_frame.visibility = View.GONE
                tabLayoutHome.visibility =View.GONE
                gridViewDecoViews!!.visibility = View.GONE

                createBackSeries(60F, my_goal.toFloat(), 10F)
                createDataSeries("", "", 60F, my_goal.toFloat(), 10F)
                createEvents(60F, my_goal.toFloat(), 10F)
            }
            else{
                decoView_frame.visibility = View.GONE
                team_frame.visibility = View.VISIBLE
                tabLayoutHome.visibility =View.VISIBLE
                gridViewDecoViews!!.visibility = View.VISIBLE


                createBackSeriesTeam(team_goal.toFloat())
                createDataSeriesTeam(team_goal.toFloat())
                createEventsTeam(team_goal.toFloat())
            }

        }, 0)


        gridViewDecoViews!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->


            if(decoView_frame.visibility == View.VISIBLE && newSelected == teammates[position].name) {
                decoView_frame.visibility = View.GONE
                team_pie_frame.visibility = View.GONE
                team_frame.visibility = View.VISIBLE
            }
            else {
                team_frame.visibility = View.GONE
                team_pie_frame.visibility = View.GONE
                decoView_frame.visibility = View.VISIBLE
                createBackSeries(60F, teammates[position].goal.toFloat(), 10F)
                createDataSeries(teammates[position].name, "No. "+ teammates[position].rank,60F, teammates[position].goal.toFloat(), 10F)
                createEvents(60F, teammates[position].goal.toFloat(), 10F)
                scrollView.scrollTo(0, scrollView.top)

                newSelected = teammates[position].name
            }

            Log.w(TAG, "selectedItemPosition$id")

        }


        switchButton.setOnClickListener {

            decoView_frame.visibility = View.GONE
            team_frame.visibility = View.VISIBLE
        }

        switchToPie.setOnClickListener {

            team_pie_frame.visibility = View.VISIBLE
            team_frame.visibility = View.GONE
        }

        switchToTeam.setOnClickListener {

            team_pie_frame.visibility = View.GONE
            team_frame.visibility = View.VISIBLE
        }

        imageActivity1.setOnClickListener {
            newGraph = "dur"
            val barGraphsDialogFragment = BarGraphsDialogFragment()
            val args = Bundle()
            args.putString("selectedGraph", newGraph)
            args.putString("selectedUser", newSelected)
            barGraphsDialogFragment.arguments = args
            barGraphsDialogFragment.show(childFragmentManager, "barGraphsDialogFragment")
        }

        imageActivity2.setOnClickListener {
            newGraph = "steps"
            val barGraphsDialogFragment = BarGraphsDialogFragment()
            val args = Bundle()
            args.putString("selectedGraph", newGraph)
            args.putString("selectedUser", newSelected)
            barGraphsDialogFragment.arguments = args
            barGraphsDialogFragment.show(childFragmentManager, "barGraphsDialogFragment")
        }
        imageActivity3.setOnClickListener {
            newGraph = "hps"
            val barGraphsDialogFragment = BarGraphsDialogFragment()
            val args = Bundle()
            args.putString("selectedGraph", newGraph)
            args.putString("selectedUser", newSelected)
            barGraphsDialogFragment.arguments = args
            barGraphsDialogFragment.show(childFragmentManager, "barGraphsDialogFragment")
        }

        imageActivity4.setOnClickListener {
            newGraph = "dis"
            val barGraphsDialogFragment = BarGraphsDialogFragment()
            val args = Bundle()
            args.putString("selectedGraph", newGraph)
            args.putString("selectedUser", newSelected)
            barGraphsDialogFragment.arguments = args
            barGraphsDialogFragment.show(childFragmentManager, "barGraphsDialogFragment")
        }

        imageActivity5.setOnClickListener {
            newGraph = "cals"
            val barGraphsDialogFragment = BarGraphsDialogFragment()
            val args = Bundle()
            args.putString("selectedGraph", newGraph)
            args.putString("selectedUser", newSelected)
            barGraphsDialogFragment.arguments = args
            barGraphsDialogFragment.show(childFragmentManager, "barGraphsDialogFragment")
        }

        imageActivity6.setOnClickListener {
            //Temp Implementation
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder
                .setMessage("Available soon!")
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })
            val alert = dialogBuilder.create()
            alert.show()
        }


        val myRefVersion = FirebaseDatabase.getInstance().reference.child("Version")
        val postListenerVersion = object : ValueEventListener {
            override fun onDataChange(dataSnapshotDR: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshotDR.value != null) {
                    val cVersion = dataSnapshotDR.value as String

                    val versionName = context!!.packageManager.getPackageInfo("com.vt.fitaware", 0).versionName.toString()

                    Log.i(TAG, "cVersion: $cVersion")
                    Log.i(TAG, "versionName: $versionName")

                    if(versionName.toDouble() < cVersion.toDouble()){
                        Snackbar.make(view, "New Update Available !", Snackbar.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onCancelled(databaseErrorDR: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseErrorDR.toException())
                // ...
            }
        }
        myRefVersion.addValueEventListener(postListenerVersion)



        return view
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


//    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//        R.id.share -> {
//            // User chose the "Settings" item, show the app settings UI...
//            true
//        }
//
//        else -> {
//            // If we got here, the user's action was not recognized.
//            // Invoke the superclass to handle it.
//            super.onOptionsItemSelected(item)
//        }
//    }



    private fun createBackSeries(teammateGoal:Float, myGoal: Float, teamGoal: Float) {
        val seriesItem = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, teammateGoal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndex = mDecoView!!.addSeries(seriesItem)

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

    }

    private fun createDataSeries(userName: String, userRank: String, teammateGoal:Float, myGoal: Float, teamGoal: Float) {

        textName.text = userName
        textRank.text = userRank


        val seriesItem = SeriesItem.Builder(Color.parseColor("#77e6f1")) //colorActivity1
            .setRange(-1f, teammateGoal, 0f)
            .setInitialVisibility(false)
            .build()

        seriesItem.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                rankActivity1.text = "Mins"
                textActivity1.text = String.format("%.0f / %.0f", currentPosition, teammateGoal)
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })

        mSeries1Index = mDecoView!!.addSeries(seriesItem)

        val seriesItem2 = SeriesItem.Builder(Color.parseColor("#3ebfab")) //colorActivity2
            .setRange(-1f, myGoal, 0f)
            .setInitialVisibility(false)
            .build()


        seriesItem2.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                rankActivity2.text = "Steps"
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

                if(currentPosition < myGoal) {
                    textRemaining.text = String.format("%.0f steps to goal", remainingKm)
                }
                else {
                    textRemaining.text = "Goal Complete!"

                }

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
                rankActivity3.text = "HeartPoints"
                textActivity3.text = String.format("%.0f / %.0f", currentPosition, teamGoal)
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })

        mSeries3Index = mDecoView3!!.addSeries(seriesItem3)


    }


    private fun createEvents(teammateGoal:Float, myGoal: Float, teamGoal: Float) {

        textActivity4.text = "0"
        textActivity5.text = "0"
        textActivity6.text = "0"

        mDecoView!!.executeReset()

        mDecoView!!.addEvent(
            DecoEvent.Builder(teammateGoal)
                .setIndex(mBackIndex)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )
        mDecoView2!!.addEvent(
            DecoEvent.Builder(myGoal)
                .setIndex(mBackIndex2)
                .setDuration(1000)
                .setDelay(100)
                .build()
        )
        mDecoView3!!.addEvent(
            DecoEvent.Builder(teamGoal)
                .setIndex(mBackIndex3)
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

    }

    private fun refreshEvents(userName: String, userRank: String, teammateSteps: Float, mySteps: Float, teamSteps: Float) {

        textName.text = userName
        textRank.text = userRank

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

    }


    private fun createBackSeriesTeam(teamGoal: Float) {
        val seriesItemTeam = SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
            .setRange(-1f, teamGoal, 0f)
            .setInitialVisibility(true)
            .build()

        mBackIndexTeam = mDecoViewTeam!!.addSeries(seriesItemTeam)


    }

    private fun createDataSeriesTeam(teamGoal: Float) {

        rankOfTeam.text = "NO. "
        teamID.text = team

        val seriesItemTeam = SeriesItem.Builder(Color.parseColor("#ff6347")) //colorActivity1
            .setRange(-1f, teamGoal, 0f)
            .setInitialVisibility(false)
            .build()


        seriesItemTeam.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(percentComplete: Float, currentPosition: Float) {
                stepsOfGoal.text = String.format("%.0f / %.0f", currentPosition, teamGoal)
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {

            }
        })

        mSeriesIndexTeam = mDecoViewTeam!!.addSeries(seriesItemTeam)

    }


    private fun createEventsTeam(teamGoal: Float) {
        mDecoViewTeam!!.executeReset()

        mDecoViewTeam!!.addEvent(
            DecoEvent.Builder(teamGoal)
                .setIndex(mBackIndex)
                .setDuration(0)
                .setDelay(0)
                .build()
        )

        mDecoViewTeam!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeriesIndexTeam)
                .setDuration(0)
                .setDelay(0)
                .build()
        )
    }

    private fun refreshEventsTeam(teamSteps: Float, teamR: String) {

        rankOfTeam.text = "NO.$teamR"
        teamID.text = team

        mDecoViewTeam!!.addEvent(
            DecoEvent.Builder(teamSteps)
                .setIndex(mSeriesIndexTeam)
                .setDuration(500)
                .setDelay(0)
                .build()
        )
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

    private fun resetPost(id: String, currentSteps: String, duration:String, heartPoints:String, distance:String, calories:String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/User/$id/currentSteps"] = currentSteps
        childUpdates["/User/$id/duration"] = duration
        childUpdates["/User/$id/heartPoints"] = heartPoints
        childUpdates["/User/$id/distance"] = distance
        childUpdates["/User/$id/calories"] = calories


        Log.w(TAG, "resetPost childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)
    }

    private fun initDaily(id: String, date: String, Cals: String, Goal: String, HPs: String, Minis: String, Ms: String, Steps: String, Token: String) {

        val childUpdates = java.util.HashMap<String, Any>()
        childUpdates["/DailyRecord/$id/$date/Cals"] = Cals
        childUpdates["/DailyRecord/$id/$date/Goal"] = Goal
        childUpdates["/DailyRecord/$id/$date/HPs"] = HPs
        childUpdates["/DailyRecord/$id/$date/Minis"] = Minis
        childUpdates["/DailyRecord/$id/$date/Ms"] = Ms
        childUpdates["/DailyRecord/$id/$date/Steps"] = Steps
        childUpdates["/DailyRecord/$id/$date/Token"] = Token

        Log.w(TAG, "initDaily childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)

    }
    private fun showSnackBarMessage(message: String) {

        if (view != null) {

            Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent!!.data
                Log.i(TAG, "selectedImage: $selectedImage")

                var bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedImage)

                bitmap = getCroppedBitmap(bitmap!!)

                val iconRef = mStorageRef!!.child("team_icon/$team/icon.jpg")

                val uploadImage = convertBitmapToByteArray(bitmap!!)
                iconRef.putBytes(uploadImage)

                imageView_teamIcon!!.setImageBitmap(bitmap)

                showSnackBarMessage("Team icon updated !")
            }
        }
    }

    fun convertBitmapToByteArray(bMap: Bitmap): ByteArray {
        var baos: ByteArrayOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            bMap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            return baos!!.toByteArray()
        } finally {
            if (baos != null) {
                try {
                    baos!!.close()
                } catch (e: IOException) {
                }

            }
        }
    }
}
