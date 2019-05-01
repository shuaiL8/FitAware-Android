package com.example.fitaware.Team


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.example.fitaware.Communicator
import com.example.fitaware.R
import java.util.ArrayList
import android.arch.lifecycle.Observer
import com.google.firebase.database.*
import java.util.HashMap


class MyTeamFragment : Fragment() {

    private var members = ArrayList<Member>(1)
    private lateinit var memberAdapter: MemberAdapter
    private val TAG = "MyTeamFragment"
    private var user_id: String = ""

    private lateinit var database: DatabaseReference

    private val mRank = arrayOfNulls<String>(1)
    private val mName = arrayOfNulls<String>(1)
    private val mSteps = arrayOfNulls<String>(1)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(
            R.layout.fragment_my_team, container,
            false)
        setHasOptionsMenu(false)

        val memberList = view.findViewById<ListView>(R.id.memberList)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        database = FirebaseDatabase.getInstance().reference

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

            user_id = allSteps["user_id"]!!.toString()
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

                    if(key == user_id) {
                        members.add(Member(index.toString(), key, details.getValue("currentSteps").toInt(), details["goal"], "#3ebfab"))
                    }
                    else{
                        members.add(Member(index.toString(), key, details.getValue("currentSteps").toInt(), details["goal"], "#000000"))

                    }

                    index++
                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")

                }
                Log.w(TAG, "members" + members.toString())
                val membersSort = members.sortedWith(compareByDescending(Member::getmSteps))
                members = ArrayList(membersSort)
                var indexM = 1
                for(member in members) {
                    member.setmRank(indexM.toString())
                    indexM++
                }
                Log.w(TAG, "members" + members.toString())

                memberAdapter = MemberAdapter(
                    activity,
                    R.layout.group_member_detail,
                    members
                )
                memberList.adapter = memberAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)






//        members.add(Member("1", "Tony", "1500", "#77e6f1", "3000"))
//        members.add(Member("2", "Thor", "1300", "#000000", "3000"))
//        members.add(Member("3", "Me", "1200", "#3ebfab", "3000"))
//        members.add(Member("4", "Steve", "1000", "#000000", "3000"))
//        members.add(Member("5", "Nat", "800", "#000000", "3000"))
//
//
//
//        memberAdapter = MemberAdapter(
//            activity,
//            R.layout.group_member_detail,
//            members
//        )
//        memberList.adapter = memberAdapter

        memberList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->


        }


        return view
    }


}
