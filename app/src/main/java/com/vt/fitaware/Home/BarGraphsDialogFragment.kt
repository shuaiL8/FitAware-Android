package com.vt.fitaware.Home

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vt.fitaware.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BarGraphsDialogFragment : DialogFragment() {

    private val TAG = "HomeFragment"
    private var user_id: String = "none"

    private lateinit var graphName:TextView
    private lateinit var personalStepsGraph: BarChart
    private lateinit var imgCross: ImageView
    private var sharedPreferences: SharedPreferences? = null

    private var selectedGraph: String = "steps"
    private var selectedUser: String = user_id


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_bargraphs_dialog, container,
            false)
        initSharedPreferences()

        user_id = sharedPreferences!!.getString("user_id", "none")

        graphName = view.findViewById(R.id.graphName)
        personalStepsGraph = view.findViewById(R.id.personalStepsGraph)
        imgCross = view.findViewById(R.id.img_cross)

        selectedGraph = arguments!!.getString("selectedGraph")
        selectedUser = arguments!!.getString("selectedUser")

        imgCross.setOnClickListener { dismiss() }


        Log.d(TAG, "selectedGraph: $selectedGraph")

        when (selectedGraph) {
            "dur" -> {
                graphName.text = "Duration"
                setDurGraph(selectedUser)
                graphName.setTextColor(ResourcesCompat.getColor(resources, R.color.colorDis, null))
            }
            "steps" -> {
                graphName.text = "Steps"
                setStepsGraph(selectedUser)
                graphName.setTextColor(ResourcesCompat.getColor(resources, R.color.colorCals, null))
            }
            "hps" -> {
                graphName.text = "HeartPoints"
                setHPsGraph(selectedUser)
                graphName.setTextColor(ResourcesCompat.getColor(resources, R.color.colorTime, null))
            }
            "dis" -> {
                graphName.text = "Distance"
                setDisGraph(selectedUser)
                graphName.setTextColor(ResourcesCompat.getColor(resources, R.color.colorCals, null))
            }
            "cals" -> {
                graphName.text = "Calories"
                setCalsGraph(selectedUser)
                graphName.setTextColor(ResourcesCompat.getColor(resources, R.color.colorCals, null))
            }
            else -> {
                graphName.text = "Steps"
                setStepsGraph(selectedUser)
                graphName.setTextColor(ResourcesCompat.getColor(resources, R.color.colorCals, null))
            }
        }

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




                    if(activity != null) {
                        val barDataSet = BarDataSet(entries, "Minis")

                        val data = BarData(labels, barDataSet)
                        personalStepsGraph.data = data // set the data and list of lables into chart

                        personalStepsGraph.setDescription("Duration VS Date")  // set the description
                        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorDis, null)
                        personalStepsGraph.animateY(100)
                    }



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



                    if(activity != null) {
                        val barDataSet = BarDataSet(entries, "Steps")

                        val data = BarData(labels, barDataSet)
                        personalStepsGraph.data = data // set the data and list of lables into chart

                        personalStepsGraph.setDescription("Steps VS Date")  // set the description
                        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorCals, null)
                        personalStepsGraph.animateY(100)
                    }


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





                    if(activity != null) {
                        val barDataSet = BarDataSet(entries, "HPs")

                        val data = BarData(labels, barDataSet)
                        personalStepsGraph.data = data // set the data and list of lables into chart

                        personalStepsGraph.setDescription("HeartPoints VS Date")  // set the description
                        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorTime, null)
                        personalStepsGraph.animateY(100)
                    }


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




                    if(activity != null) {
                        val barDataSet = BarDataSet(entries, "Ms")

                        val data = BarData(labels, barDataSet)
                        personalStepsGraph.data = data // set the data and list of lables into chart

                        personalStepsGraph.setDescription("Distance VS Date")  // set the description
                        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorCals, null)
                        personalStepsGraph.animateY(100)
                    }


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




                    if(activity != null) {
                        val barDataSet = BarDataSet(entries, "Cals")

                        val data = BarData(labels, barDataSet)
                        personalStepsGraph.data = data // set the data and list of lables into chart

                        personalStepsGraph.setDescription("Calories VS Date")  // set the description
                        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
                        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorCals, null)
                        personalStepsGraph.animateY(100)
                    }

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

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }
}