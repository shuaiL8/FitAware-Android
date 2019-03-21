package com.example.fitaware


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.*


/**
 * A simple [Fragment] subclass.
 *
 */
class MeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_me, container,
            false)
        setHasOptionsMenu(true)

        val userListView = view.findViewById<ListView>(R.id.userListView)
        val icon = view.findViewById<ImageView>(R.id.icon)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE

        val tv_name = view.findViewById<TextView>(R.id.tv_name)
        tv_name.text = "4Fit"

        val tv_email = view.findViewById<TextView>(R.id.tv_email)
        tv_email.text = "4fit@vt.edu"

        val tv_group = view.findViewById<TextView>(R.id.tv_group)
        tv_group.text = "Group CS5714"

        val user = resources.getStringArray(R.array.UserStatus)
        val adapter = ArrayAdapter<String>(
            activity!!,
            android.R.layout.simple_list_item_1, user
        )

        userListView.adapter = adapter

        icon.setBackgroundResource(R.drawable.ic_person_black_24dp)

        userListView.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, position, id ->
            if (position == 0) {
//                logout()
            }
            if (position == 1) {
//                showChangepasswordDialog()
            }
            if (position == 2) {

            }
            if (position == 3) {

            }
        })

        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tool_bar2, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.add -> {
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

}
