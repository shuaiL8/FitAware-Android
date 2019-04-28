package com.example.fitaware.Home

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.navigation.Navigation
import com.example.fitaware.R
import com.example.fitaware.Team.Member
import com.example.fitaware.Team.MemberAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import android.app.Activity







class TeamMemberListFragment : DialogFragment() {

    private val TAG = "TeamMemberListFragment"
    private var members = ArrayList<Member>(1)
    private lateinit var memberAdapter: MemberBriefAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.teammember_list, container,
            false
        )
        setHasOptionsMenu(true)

        val teamMemberList = view.findViewById<ListView>(R.id.teamMemberList)

        val myRef = FirebaseDatabase.getInstance().reference.child("User")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                Log.i(TAG, "my: $my")
                members.add(Member("", "Display None", "", "", "#000000"))

                var index = 1
                for((key, value) in my){
                    val details = value as Map<String, String>

                    members.add(Member(index.toString(), key, details["currentSteps"], details["goal"], "#000000"))
                    memberAdapter = MemberBriefAdapter(
                        activity,
                        R.layout.member_brief,
                        members
                    )
                    teamMemberList.adapter = memberAdapter

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



        teamMemberList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->


            val itemId = teamMemberList.getItemIdAtPosition(position).toInt()
            val selectedName = members[itemId].getmName()


            OnSelectedCompleted(selectedName)
            Log.i(TAG, "selectedName: $selectedName")

            dismiss()


        }

        return view
    }


    fun OnSelectedCompleted(selectedName: String) {
        this.mListener!!.onComplete(selectedName)
    }

    interface OnCompleteListener {
        fun onComplete(selectedName: String)
    }

    private var mListener: OnCompleteListener? = null

    // make sure the Activity implemented it
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            this.mListener = activity as OnCompleteListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement OnCompleteListener")
        }

    }

}