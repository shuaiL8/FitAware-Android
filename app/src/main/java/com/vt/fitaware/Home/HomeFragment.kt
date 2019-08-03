package com.vt.fitaware.Home


import android.annotation.SuppressLint
import android.app.Activity
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
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.*
import androidx.navigation.Navigation
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.vt.fitaware.MainActivity
import com.vt.fitaware.MyBackgroundService
import com.vt.fitaware.Team.Team
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

    private var my_rank: String = " "

    private var my_goal: Long = 0
    private var teammate_goal: Long = 0
    private var team_goal: Long = 0

    private lateinit var graphName:TextView
    private lateinit var personalStepsGraph: BarChart

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
    internal lateinit var rankActivity4: TextView
    internal lateinit var rankActivity5: TextView

    private var gridViewDecoViews: ExpandableHeightGridView? = null

    private var teammates = ArrayList<Teammates>(1)
    private var teammatesAdapter: TeammatesAdapter? = null

    private var sharedPreferences: SharedPreferences? = null

    private var mStorageRef: StorageReference? = null

    private var calendar_frameVisibility: String = "none"

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
        my_goal = sharedPreferences!!.getString("my_goal", "0").toLong()
        my_rank = sharedPreferences!!.getString("my_rank", " ")
        periodical = sharedPreferences!!.getString("periodical", "none")
        my_steps = sharedPreferences!!.getString("currentSteps", "0").toLong()
        my_duration = sharedPreferences!!.getString("duration", "0").toLong()
        my_heartPoints = sharedPreferences!!.getString("heartPoints", "0").toLong()
        my_distance = sharedPreferences!!.getString("distance", "0").toLong()
        my_calories = sharedPreferences!!.getString("calories", "0").toLong()
        token = sharedPreferences!!.getString("token", "none")
        calendar_frameVisibility = sharedPreferences!!.getString("calendar_frameVisibility", "none")


        graphName = view.findViewById(R.id.graphName)
        personalStepsGraph = view.findViewById(R.id.personalStepsGraph)

        personalStepsGraph.visibility = View.GONE

        val textDate = view.findViewById<TextView>(R.id.textDate)

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM dd")
        val currentDate = simpleDateFormat.format(calendar.time)
        textDate.text = currentDate
        val calendar_frame = view.findViewById<FrameLayout>(R.id.calendar_frame)
        val decoView_frame = view.findViewById<FrameLayout>(R.id.decoView_frame)
        val team_frame = view.findViewById<FrameLayout>(R.id.team_frame)

        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)

        val dropDownButton = view.findViewById<ImageButton>(R.id.dropDownButton)


        mDecoViewTeam = view.findViewById<DecoView>(R.id.dynamicArcViewsTeam)
        val imageView_teamIcon = view.findViewById<ImageView>(R.id.imageView_teamIcon)
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

        textActivity1 = view.findViewById(R.id.textActivity1)
        textActivity2 = view.findViewById(R.id.textActivity2)
        textActivity3 = view.findViewById(R.id.textActivity3)
        textActivity4 = view.findViewById(R.id.textActivity4)
        textActivity5 = view.findViewById(R.id.textActivity5)

        rankActivity1 = view.findViewById(R.id.rankActivity1)
        rankActivity2 = view.findViewById(R.id.rankActivity2)
        rankActivity3 = view.findViewById(R.id.rankActivity3)
        rankActivity4 = view.findViewById(R.id.rankActivity4)
        rankActivity5 = view.findViewById(R.id.rankActivity5)

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
            my_rank = allSteps["my_rank"]!!.toString()
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
                    }

                }
            }
            else {
                refreshEvents("","" , my_duration.toFloat(), my_steps.toFloat(), my_heartPoints.toFloat())
                textActivity4.text = my_distance.toString()
                textActivity5.text = my_calories.toString()
            }


        }
        model.message.observe(activity!!, `object`)


        val calendarNY = Calendar.getInstance()
        val mdformatNY = SimpleDateFormat("yyyy-MM-dd")
        mdformatNY.timeZone = TimeZone.getTimeZone("America/New_York")
        val strDate = mdformatNY.format(calendarNY.time)

        initDaily(
            user_id,
            strDate,
            my_calories.toString(),
            my_goal.toString(),
            my_heartPoints.toString(),
            my_duration.toString(),
            my_distance.toString(),
            my_rank,
            my_steps.toString(),
            token)

        val intent = Intent()
        intent.action = "com.vt.MyBackgroundServiceReceiver"
        intent.putExtra("my_rank", my_rank)
        intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        activity!!.sendBroadcast(intent)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.homeFragment)

            initDaily(
                user_id,
                strDate,
                my_calories.toString(),
                my_goal.toString(),
                my_heartPoints.toString(),
                my_duration.toString(),
                my_distance.toString(),
                my_rank,
                my_steps.toString(),
                token)

            val intent = Intent()
            intent.action = "com.vt.MyBackgroundServiceReceiver"
            intent.putExtra("my_rank", my_rank)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            activity!!.sendBroadcast(intent)
        }


        mStorageRef = FirebaseStorage.getInstance().reference

        val iconRef = mStorageRef!!.child("team_icon/$team/icon.jpg")

        iconRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadIconUrl = task.result

                Picasso.get().load(downloadIconUrl).into(imageView_teamIcon)

            } else {
                var bitmap = BitmapFactory.decodeResource(resources, R.drawable.teamwork)
                bitmap = getCroppedBitmap(bitmap)
                imageView_teamIcon!!.setImageBitmap(bitmap)
            }
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
                                    mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
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

        val handler = Handler()
        handler.postDelayed({


            if(team == "none"){
                decoView_frame.visibility = View.VISIBLE
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



        decoView_frame.visibility = View.GONE
        personalStepsGraph.visibility = View.GONE
        graphName.visibility = View.GONE
        gridViewDecoViews!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->


            if(decoView_frame.visibility == View.VISIBLE && newSelected == teammates[position].name) {
                decoView_frame.visibility = View.GONE
            }
            else {
                decoView_frame.visibility = View.VISIBLE
                createBackSeries(60F, teammates[position].goal.toFloat(), 10F)
                createDataSeries(teammates[position].name, "No. "+ teammates[position].rank,60F, teammates[position].goal.toFloat(), 10F)
                createEvents(60F, teammates[position].goal.toFloat(), 10F)
                scrollView.scrollTo(0, scrollView.bottom)

                newSelected = teammates[position].name

                if(personalStepsGraph.visibility == View.VISIBLE) {
                    graphName.text = "Steps Graph"
                    setStepsGraph(newSelected)
                    graphName.setTextColor(Color.parseColor("#3ebfab"))
                }

            }

            Log.w(TAG, "selectedItemPosition$id")

        }

        imageActivity1.setOnClickListener {
            if(personalStepsGraph.visibility == View.GONE) {
                personalStepsGraph.visibility = View.VISIBLE
                graphName.visibility = View.VISIBLE
                graphName.text = "Duration Graph"
                setDurGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#77e6f1"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text != "Duration Graph"){
                graphName.text = "Duration Graph"
                setDurGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#77e6f1"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text == "Duration Graph"){
                personalStepsGraph.visibility = View.GONE
                graphName.visibility = View.GONE
            }
        }

        imageActivity2.setOnClickListener {
            if(personalStepsGraph.visibility == View.GONE) {
                personalStepsGraph.visibility = View.VISIBLE
                graphName.visibility = View.VISIBLE
                graphName.text = "Steps Graph"
                setStepsGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#3ebfab"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text != "Steps Graph"){
                graphName.text = "Steps Graph"
                setStepsGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#3ebfab"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text == "Steps Graph"){
                personalStepsGraph.visibility = View.GONE
                graphName.visibility = View.GONE
            }
        }
        imageActivity3.setOnClickListener {
            if(personalStepsGraph.visibility == View.GONE) {
                personalStepsGraph.visibility = View.VISIBLE
                graphName.visibility = View.VISIBLE
                graphName.text = "HeartPoints Graph"
                setHPsGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#ff6347"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text != "HeartPoints Graph"){
                graphName.text = "HeartPoints Graph"
                setHPsGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#ff6347"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text == "HeartPoints Graph"){
                personalStepsGraph.visibility = View.GONE
                graphName.visibility = View.GONE
            }
        }

        imageActivity4.setOnClickListener {
            if(personalStepsGraph.visibility == View.GONE) {
                personalStepsGraph.visibility = View.VISIBLE
                graphName.visibility = View.VISIBLE
                graphName.text = "Distance Graph"
                setDisGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#3ebfab"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text != "Distance Graph"){
                graphName.text = "Distance Graph"
                setDisGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#3ebfab"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text == "Distance Graph"){
                personalStepsGraph.visibility = View.GONE
                graphName.visibility = View.GONE
            }
        }

        imageActivity5.setOnClickListener {
            if(personalStepsGraph.visibility == View.GONE) {
                personalStepsGraph.visibility = View.VISIBLE
                graphName.visibility = View.VISIBLE
                graphName.text = "Calories Graph"
                setCalsGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#3ebfab"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text != "Calories Graph"){
                graphName.text = "Calories Graph"
                setCalsGraph(newSelected)
                graphName.setTextColor(Color.parseColor("#3ebfab"))

            }
            else if(personalStepsGraph.visibility == View.VISIBLE && graphName.text == "Calories Graph"){
                personalStepsGraph.visibility = View.GONE
                graphName.visibility = View.GONE
            }
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


    private fun setDurGraph(id: String) {

        var graphID = id

        if(graphID == "none") {
            graphID = user_id
        }

        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val strDate = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        val strDate1 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val strDate2 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        val strDate3 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val strDate4 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        val strDate5 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val strDate6 = mdformat.format(calendar.time)


        var index = 0

        var entriesDetail = ArrayList<BarEntryDetail>(1)

        val entries = ArrayList<BarEntry>(1)

        var labels = ArrayList<String>(1)


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$graphID")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>


                    entriesDetail.clear()

                    labels.clear()

                    for((key, value) in my){
                        val details = value as Map<String, String>

                        if(strDate == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))

                        }
                        if(strDate1 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate2 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate3 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate4 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate5 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate6 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    if(!my.containsKey(strDate)) {
                        entriesDetail.add(BarEntryDetail(strDate, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))

                    }

                    if(!my.containsKey(strDate1)) {
                        entriesDetail.add(BarEntryDetail(strDate1, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate2)) {
                        entriesDetail.add(BarEntryDetail(strDate2, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate3)) {
                        entriesDetail.add(BarEntryDetail(strDate3, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate4)) {
                        entriesDetail.add(BarEntryDetail(strDate4, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate5)) {
                        entriesDetail.add(BarEntryDetail(strDate5, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate6)) {
                        entriesDetail.add(BarEntryDetail(strDate6, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    val entriesDetailSort = entriesDetail.sortedWith(compareBy(BarEntryDetail::getmDate))
                    entriesDetail = ArrayList(entriesDetailSort)

                    entries.clear()
                    for (value in entriesDetail) {
                        entries.add(BarEntry(value.getmDuration().toFloat(), index))
                        index++
                    }


                    labels = sortSunToMon(labels)



                    val barDataSet = BarDataSet(entries, "Minis")

                    val data = BarData(labels, barDataSet)
                    personalStepsGraph.data = data // set the data and list of lables into chart

                    personalStepsGraph.setDescription("Duration VS Date")  // set the description
                    //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                    barDataSet.color = resources.getColor(R.color.colorDis)

                    personalStepsGraph.animateY(5000)


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

    private fun setStepsGraph(id: String) {

        var graphID = id

        if(graphID == "none") {
            graphID = user_id
        }

        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val strDate = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        val strDate1 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val strDate2 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        val strDate3 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val strDate4 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        val strDate5 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val strDate6 = mdformat.format(calendar.time)

        var index = 0

        var entriesDetail = ArrayList<BarEntryDetail>(1)

        val entries = ArrayList<BarEntry>(1)

        var labels = ArrayList<String>(1)


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$graphID")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>


                    entriesDetail.clear()

                    labels.clear()

                    for((key, value) in my){
                        val details = value as Map<String, String>


                        if(strDate == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))

                        }
                        if(strDate1 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate2 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate3 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate4 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate5 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate6 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    if(!my.containsKey(strDate)) {
                        entriesDetail.add(BarEntryDetail(strDate, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))

                    }

                    if(!my.containsKey(strDate1)) {
                        entriesDetail.add(BarEntryDetail(strDate1, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate2)) {
                        entriesDetail.add(BarEntryDetail(strDate2, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate3)) {
                        entriesDetail.add(BarEntryDetail(strDate3, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate4)) {
                        entriesDetail.add(BarEntryDetail(strDate4, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate5)) {
                        entriesDetail.add(BarEntryDetail(strDate5, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate6)) {
                        entriesDetail.add(BarEntryDetail(strDate6, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    val entriesDetailSort = entriesDetail.sortedWith(compareBy(BarEntryDetail::getmDate))
                    entriesDetail = ArrayList(entriesDetailSort)

                    entries.clear()
                    for (value in entriesDetail) {
                        entries.add(BarEntry(value.getmSteps().toFloat(), index))
                        index++
                    }

                    labels = sortSunToMon(labels)


                    val barDataSet = BarDataSet(entries, "Steps")

                    val data = BarData(labels, barDataSet)
                    personalStepsGraph.data = data // set the data and list of lables into chart

                    personalStepsGraph.setDescription("Steps VS Date")  // set the description
                    //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                    barDataSet.color = resources.getColor(R.color.colorCals)

                    personalStepsGraph.animateY(5000)


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


    private fun setHPsGraph(id: String) {

        var graphID = id

        if(graphID == "none") {
            graphID = user_id
        }

        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val strDate = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        val strDate1 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val strDate2 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        val strDate3 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val strDate4 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        val strDate5 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val strDate6 = mdformat.format(calendar.time)


        var index = 0

        var entriesDetail = ArrayList<BarEntryDetail>(1)

        val entries = ArrayList<BarEntry>(1)

        var labels = ArrayList<String>(1)


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$graphID")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>


                    entriesDetail.clear()

                    labels.clear()

                    for((key, value) in my){
                        val details = value as Map<String, String>


                        if(strDate == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))

                        }
                        if(strDate1 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate2 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate3 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate4 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate5 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate6 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    if(!my.containsKey(strDate)) {
                        entriesDetail.add(BarEntryDetail(strDate, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))

                    }

                    if(!my.containsKey(strDate1)) {
                        entriesDetail.add(BarEntryDetail(strDate1, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate2)) {
                        entriesDetail.add(BarEntryDetail(strDate2, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate3)) {
                        entriesDetail.add(BarEntryDetail(strDate3, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate4)) {
                        entriesDetail.add(BarEntryDetail(strDate4, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate5)) {
                        entriesDetail.add(BarEntryDetail(strDate5, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate6)) {
                        entriesDetail.add(BarEntryDetail(strDate6, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    val entriesDetailSort = entriesDetail.sortedWith(compareBy(BarEntryDetail::getmDate))
                    entriesDetail = ArrayList(entriesDetailSort)

                    entries.clear()
                    for (value in entriesDetail) {
                        entries.add(BarEntry(value.getmHeartPoints().toFloat(), index))
                        index++
                    }


                    labels = sortSunToMon(labels)




                    val barDataSet = BarDataSet(entries, "HPs")

                    val data = BarData(labels, barDataSet)
                    personalStepsGraph.data = data // set the data and list of lables into chart

                    personalStepsGraph.setDescription("HeartPoints VS Date")  // set the description
                    //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                    barDataSet.color = resources.getColor(R.color.colorHeart)

                    personalStepsGraph.animateY(5000)


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


    private fun setDisGraph(id: String) {

        var graphID = id

        if(graphID == "none") {
            graphID = user_id
        }

        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val strDate = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        val strDate1 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val strDate2 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        val strDate3 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val strDate4 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        val strDate5 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val strDate6 = mdformat.format(calendar.time)


        var index = 0

        var entriesDetail = ArrayList<BarEntryDetail>(1)

        val entries = ArrayList<BarEntry>(1)

        var labels = ArrayList<String>(1)


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$graphID")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>


                    entriesDetail.clear()

                    labels.clear()

                    for((key, value) in my){
                        val details = value as Map<String, String>


                        if(strDate == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))

                        }
                        if(strDate1 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate2 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate3 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate4 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate5 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate6 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    if(!my.containsKey(strDate)) {
                        entriesDetail.add(BarEntryDetail(strDate, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))

                    }

                    if(!my.containsKey(strDate1)) {
                        entriesDetail.add(BarEntryDetail(strDate1, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate2)) {
                        entriesDetail.add(BarEntryDetail(strDate2, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate3)) {
                        entriesDetail.add(BarEntryDetail(strDate3, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate4)) {
                        entriesDetail.add(BarEntryDetail(strDate4, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate5)) {
                        entriesDetail.add(BarEntryDetail(strDate5, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate6)) {
                        entriesDetail.add(BarEntryDetail(strDate6, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    val entriesDetailSort = entriesDetail.sortedWith(compareBy(BarEntryDetail::getmDate))
                    entriesDetail = ArrayList(entriesDetailSort)

                    entries.clear()
                    for (value in entriesDetail) {
                        entries.add(BarEntry(value.getmDistance().toFloat(), index))
                        index++
                    }


                    labels = sortSunToMon(labels)



                    val barDataSet = BarDataSet(entries, "Ms")

                    val data = BarData(labels, barDataSet)
                    personalStepsGraph.data = data // set the data and list of lables into chart

                    personalStepsGraph.setDescription("Distance VS Date")  // set the description
                    //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                    barDataSet.color = resources.getColor(R.color.colorCals)

                    personalStepsGraph.animateY(5000)


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


    private fun setCalsGraph(id: String) {

        var graphID = id

        if(graphID == "none") {
            graphID = user_id
        }

        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val strDate = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        val strDate1 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val strDate2 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        val strDate3 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val strDate4 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        val strDate5 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val strDate6 = mdformat.format(calendar.time)


        var index = 0

        var entriesDetail = ArrayList<BarEntryDetail>(1)

        val entries = ArrayList<BarEntry>(1)

        var labels = ArrayList<String>(1)


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$graphID")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>


                    entriesDetail.clear()

                    labels.clear()

                    for((key, value) in my){
                        val details = value as Map<String, String>


                        if(strDate == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))

                        }
                        if(strDate1 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate2 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate3 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate4 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate5 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }
                        if(strDate6 == key) {
                            entriesDetail.add(BarEntryDetail(key, details["Steps"], details["Minis"], details["HPs"], details["Ms"], details["Cals"]))

                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                            labels.add(SimpleDateFormat("E").format(calendar.time))
                        }


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    if(!my.containsKey(strDate)) {
                        entriesDetail.add(BarEntryDetail(strDate, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))

                    }

                    if(!my.containsKey(strDate1)) {
                        entriesDetail.add(BarEntryDetail(strDate1, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate2)) {
                        entriesDetail.add(BarEntryDetail(strDate2, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate3)) {
                        entriesDetail.add(BarEntryDetail(strDate3, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate4)) {
                        entriesDetail.add(BarEntryDetail(strDate4, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate5)) {
                        entriesDetail.add(BarEntryDetail(strDate5, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    if(!my.containsKey(strDate6)) {
                        entriesDetail.add(BarEntryDetail(strDate6, "0", "0", "0", "0", "0"))
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                        labels.add(SimpleDateFormat("E").format(calendar.time))
                    }

                    val entriesDetailSort = entriesDetail.sortedWith(compareBy(BarEntryDetail::getmDate))
                    entriesDetail = ArrayList(entriesDetailSort)

                    entries.clear()
                    for (value in entriesDetail) {
                        entries.add(BarEntry(value.getmCalories().toFloat(), index))
                        index++
                    }


                    labels = sortSunToMon(labels)



                    val barDataSet = BarDataSet(entries, "Cals")

                    val data = BarData(labels, barDataSet)
                    personalStepsGraph.data = data // set the data and list of lables into chart

                    personalStepsGraph.setDescription("Calories VS Date")  // set the description
                    //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                    barDataSet.color = resources.getColor(R.color.colorCals)

                    personalStepsGraph.animateY(5000)

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

    private fun sortSunToMon(lables: ArrayList<String>): ArrayList<String> {

        var newLables = ArrayList<String>(1)


        if(lables.contains("Sun")) {
            newLables.add("Sun")
        }
        if(lables.contains("Mon")) {
            newLables.add("Mon")
        }
        if(lables.contains("Tue")) {
            newLables.add("Tue")
        }
        if(lables.contains("Wed")) {
            newLables.add("Wed")
        }
        if(lables.contains("Thu")) {
            newLables.add("Thu")
        }
        if(lables.contains("Fri")) {
            newLables.add("Fri")
        }
        if(lables.contains("Sat")) {
            newLables.add("Sat")
        }

        return newLables
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
                .setDuration(1000)
                .setDelay(100)
                .build()
        )

        mDecoViewTeam!!.addEvent(
            DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeriesIndexTeam)
                .setDuration(1000)
                .setDelay(300)
                .build()
        )
    }

    private fun refreshEventsTeam(teamSteps: Float, teamR: String) {

        rankOfTeam.text = "NO.$teamR"
        teamID.text = team

        mDecoViewTeam!!.addEvent(
            DecoEvent.Builder(teamSteps)
                .setIndex(mSeriesIndexTeam)
                .setDuration(1000)
                .setDelay(100)
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

    private fun initDaily(id: String, date: String, Cals: String, Goal: String, HPs: String, Minis: String, Ms: String, Rank: String, Steps: String, Token: String) {

        val childUpdates = java.util.HashMap<String, Any>()
        childUpdates["/DailyRecord/$id/$date/Cals"] = Cals
        childUpdates["/DailyRecord/$id/$date/Goal"] = Goal
        childUpdates["/DailyRecord/$id/$date/HPs"] = HPs
        childUpdates["/DailyRecord/$id/$date/Minis"] = Minis
        childUpdates["/DailyRecord/$id/$date/Ms"] = Ms
        childUpdates["/DailyRecord/$id/$date/Rank"] = Rank
        childUpdates["/DailyRecord/$id/$date/Steps"] = Steps
        childUpdates["/DailyRecord/$id/$date/Token"] = Token

        Log.w(TAG, "initDaily childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)

    }
}
