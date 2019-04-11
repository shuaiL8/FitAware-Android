package com.example.fitaware.Team


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.example.fitaware.R
import java.util.ArrayList


class MyTeamFragment : Fragment() {

    private var members = ArrayList<Member>(1)
    private lateinit var memberAdapter: MemberAdapter

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

        members.add(Member("1", "Tony", "1500", "#77e6f1"))
        members.add(Member("2", "Thor", "1300", "#000000"))
        members.add(Member("3", "Me", "1200", "#3ebfab"))
        members.add(Member("4", "Steve", "1000", "#000000"))
        members.add(Member("5", "Nat", "800", "#000000"))



        memberAdapter = MemberAdapter(
            activity,
            R.layout.group_member_detail,
            members
        )
        memberList.adapter = memberAdapter

        memberList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->


        }


        return view
    }


}
