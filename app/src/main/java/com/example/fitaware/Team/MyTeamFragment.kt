package com.example.fitaware.Team


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.example.fitaware.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList


class MyTeamFragment : Fragment() {

    private var members = ArrayList<Member>(1)
    private lateinit var memberAdapter: MemberAdapter
    private val TAG = "MyTeamFragment"

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

        val myRef = FirebaseDatabase.getInstance().reference.child("User")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                Log.i(TAG, "my: $my")
                var index = 1
                for((key, value) in my){
                    val details = value as Map<String, String>

                    members.add(Member(index.toString(), key, details["currentSteps"], details["goal"], "#000000"))
                    memberAdapter = MemberAdapter(
                        activity,
                        R.layout.group_member_detail,
                        members
                    )
                    memberList.adapter = memberAdapter

                    index++
                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")

                }
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
