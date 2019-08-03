package com.vt.fitaware.History.userRank

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.navigation.Navigation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vt.fitaware.R


class UserFragment : Fragment() {

    private val TAG = "UserFragment"

    private var sharedPreferences: SharedPreferences? = null
    private var userDetail = ArrayList<UserDetail>(1)
    private lateinit var userAdapter: UserAdapter

    private var user_id: String = "none"
    private var newSelectedDate: String = "none"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_user, container,
            false
        )
        initSharedPreferences()

        user_id = sharedPreferences!!.getString("user_id", "none")
        newSelectedDate = sharedPreferences!!.getString("newSelectedDate", "none")


        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = newSelectedDate


        val userList = view.findViewById<ListView>(R.id.userList)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.userFragment)
        }


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                userDetail.clear()

                Log.i(TAG, "myTeamMember: $my")
                var index = 1
                for((key, value) in my){
                    val details = value as Map<String, Map<String, String>>

                    for((name, data) in details) {
                        val date = data

                        if(newSelectedDate == name) {


                            if(date.containsKey("Likes")) {

                                val likes = date.getValue("Likes") as Map<String, String>
                                Log.i(TAG, "likestest: $likes")

                                var count = 0
                                for((name1, name2) in likes) {
                                    count++
                                }

                                if(likes.containsKey(user_id)){
                                    Log.i(TAG, "likeChecked: $user_id")

                                    userDetail.add(
                                        UserDetail(
                                            name,
                                            key,
                                            index.toString(),
                                            date.getValue("Steps").toInt(),
                                            count.toString(),
                                            "checked",
                                            date.getValue("Token")
                                        )
                                    )
                                }
                                else{
                                    userDetail.add(
                                        UserDetail(
                                            name,
                                            key,
                                            index.toString(),
                                            date.getValue("Steps").toInt(),
                                            count.toString(),
                                            "unchecked",
                                            date.getValue("Token")
                                        )
                                    )
                                }
                            }
                            else{
                                userDetail.add(
                                    UserDetail(
                                        name,
                                        key,
                                        index.toString(),
                                        date.getValue("Steps").toInt(),
                                        "0",
                                        "unchecked",
                                        date.getValue("Token")
                                    )
                                )
                            }

                        }


                    }

                    index++
                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")


                }
                Log.w(TAG, "userDetail" + userDetail.toString())
                val membersSort = userDetail.sortedWith(compareByDescending(UserDetail::getmSteps))
                userDetail = java.util.ArrayList(membersSort)


                var indexM = 1
                for(userD in userDetail) {
                    userD.setmRank(indexM.toString())
                    indexM++
                }

                if (activity !=null){
                    userAdapter = UserAdapter(
                        activity,
                        R.layout.users,
                        userDetail
                    )
                    userList.adapter = userAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

        return view
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

}
