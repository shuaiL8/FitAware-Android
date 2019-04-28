package com.example.fitaware.Setting


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.*
import android.widget.*
import androidx.navigation.Navigation
import com.example.fitaware.R
import android.content.Intent
import android.util.Log
import com.example.fitaware.Communicator
import com.example.fitaware.MainActivity
import com.google.firebase.database.*
import android.arch.lifecycle.Observer


/**
 * A simple [Fragment] subclass.
 *
 */
class SettingFragment : Fragment() {

    private val TAG = "SettingFragment"

    private var user_id: String = ""


    var loginStatus = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_setting, container,
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

        userListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            if (position == 0) {
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.profileFragment)
            }
            if (position == 1) {
            }
            if (position == 2) {
                showChangepasswordDialog()
            }
            if (position == 3) {
                loginStatus = 0
                sendData()
            }
            if (position == 4) {

            }
        }

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

        val myRef = FirebaseDatabase.getInstance().reference.child("User/$user_id")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                Log.i(TAG, "my: $my")

                for((key, value) in my){
                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "email: ${my["email"]}")
                }
                tv_name.text = my["id"] as CharSequence?
                tv_email.text = my["email"] as CharSequence?
                tv_group.text = my["team"] as CharSequence?
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

    private fun showChangepasswordDialog() {

        val changepasswordFragment = ChangepasswordFragment()

        val bundle = Bundle()
//        bundle.putString(Constants.EMAIL, mEmail)
//        bundle.putString(Constants.TOKEN, mToken)
        changepasswordFragment.setArguments(bundle)

        changepasswordFragment.show(fragmentManager, ChangepasswordFragment.TAG)
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

    private fun sendData() {
        //INTENT OBJ
        val intent = Intent(activity!!.baseContext, MainActivity::class.java)

        //PACK DATA
        intent.putExtra("Login_Status", loginStatus)

        //START ACTIVITY
        activity!!.startActivity(intent)

    }

}
