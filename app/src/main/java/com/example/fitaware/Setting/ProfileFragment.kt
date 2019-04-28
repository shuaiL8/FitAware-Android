package com.example.fitaware.Setting


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitaware.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap
import com.example.fitaware.utils.Validation.validateFields
import android.support.design.widget.TextInputLayout
import androidx.navigation.Navigation
import com.example.fitaware.Communicator
import android.arch.lifecycle.Observer
import android.widget.*


class ProfileFragment : Fragment() {

    private val TAG = "ProfileFragment"
    private lateinit var database: DatabaseReference
    private var newStepsGoal: String = ""
    private var newPeriodical: String = ""
    private var currentStepsGoal: String = ""
    private var user_id: String = ""
    private var periodical: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_profile, container,
            false
        )
        setHasOptionsMenu(true)

        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = "Profile"

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

            Log.w(TAG, "currentStepsGoal" + allSteps["my_goal"]!!)
            currentStepsGoal = allSteps["my_goal"]!!.toString()
            user_id = allSteps["user_id"]!!.toString()
            periodical = allSteps["periodical"]!!.toString()

        }

        model.message.observe(activity!!, `object`)

        database = FirebaseDatabase.getInstance().reference
        val arrayPeriodical = resources.getStringArray(R.array.Daily_Weekly_Monthly)
        val ti_stepsGoal = view.findViewById<TextInputLayout>(R.id.ti_stepsGoal)
        val et_stepsGoal = view.findViewById<EditText>(R.id.et_stepsGoal)
        val btn_update = view.findViewById<Button>(R.id.btn_update)

        val mEtSpinner = view.findViewById<Spinner>(R.id.mEtSpinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            activity,
            R.array.Daily_Weekly_Monthly,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            mEtSpinner.adapter = adapter
        }

        for(x in 0..2 ){
            if(periodical == arrayPeriodical[x]) {
                mEtSpinner.setSelection(x)
                Log.w(TAG, "arrayPeriodical"+x+arrayPeriodical[x])

            }
        }


        et_stepsGoal.hint = "Current Goal: $currentStepsGoal"


        btn_update.setOnClickListener {
            newStepsGoal = et_stepsGoal.text.toString()
            newPeriodical = mEtSpinner.selectedItem.toString()


            if (!validateFields(newStepsGoal)) {

                ti_stepsGoal.error = "Goal should not be empty !"
                showSnackBarMessage("Enter Valid Details !")

            }
            else {
                writeNewPost(user_id, newPeriodical, newStepsGoal)
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.settingFragment)
                showSnackBarMessage("Goal Update Success !")
            }

        }

        return view
    }



    private fun writeNewPost(id: String, periodical: String, goal: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/User/$id/periodical"] = periodical
        childUpdates["/User/$id/goal"] = goal

        Log.w(TAG, "childUpdates: $childUpdates")

        database.updateChildren(childUpdates)
    }


    private fun showSnackBarMessage(message: String) {

        if (view != null) {

            Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }


}
