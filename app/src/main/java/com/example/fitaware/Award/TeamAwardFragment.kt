package com.example.fitaware.Award


import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import com.example.fitaware.R
import kotlinx.android.synthetic.main.fragment_team_award.*
import java.util.ArrayList


class TeamAwardFragment : Fragment() {

    private var awards = ArrayList<Award>(1)
    private lateinit var awardsAdapter: AwardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_team_award, container,
            false)
        setHasOptionsMenu(true)


        val gridViewTeamAwards = view.findViewById<GridView>(R.id.gridViewTeamAwards)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_trophy)
        awards.add(Award(bitmap, "Best of Year", "12/31/2018"))


        awardsAdapter = AwardAdapter(
            activity,
            R.layout.awards,
            awards
        )
        gridViewTeamAwards.adapter = awardsAdapter

        gridViewTeamAwards.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

        }

        return view
    }


}
