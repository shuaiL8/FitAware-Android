package com.example.fitaware.Team


import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import com.example.fitaware.R



class AllTeamsFragment : Fragment() {

    private var teams = ArrayList<Team>(1)
    private lateinit var teamsAdapter: TeamAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(
            R.layout.fragment_all_teams, container,
            false)
        setHasOptionsMenu(false)


        val gridViewAllTeams = view.findViewById<GridView>(R.id.gridViewAllTeams)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.teamwork)
        teams.add(Team(bitmap, "Avengers", "Tony", "1st"))

        val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.together)
        teams.add(Team(bitmap2, "Guardians of the Galaxy", "Peter", "2nd"))

        val bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.group)
        teams.add(Team(bitmap3, "Titan", "Thanos", "3rd"))


        teamsAdapter = TeamAdapter(
            activity,
            R.layout.teams,
            teams
        )
        gridViewAllTeams.adapter = teamsAdapter

        gridViewAllTeams.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

        }


        return view
    }


}
