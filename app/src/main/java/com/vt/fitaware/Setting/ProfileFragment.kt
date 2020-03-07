package com.vt.fitaware.Setting


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vt.fitaware.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap
import com.vt.fitaware.utils.Validation.validateFields
import android.support.design.widget.TextInputLayout
import androidx.navigation.Navigation
import com.vt.fitaware.Communicator
import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.app.DialogFragment
import android.widget.*


class ProfileFragment : DialogFragment () {

    private val TAG = "ProfileFragment"
    private lateinit var database: DatabaseReference
    private var newStepsGoal: String = ""
    private var newPeriodical: String = ""
    private var currentStepsGoal: String = ""
    private var user_id: String = ""
    private var periodical: String = ""
    private var periodicalIndex = 0
    private var sharedPreferences: SharedPreferences? = null
    private var ti_stepsGoal : TextInputLayout? = null

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
        initSharedPreferences()

//        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
//        toolbarTiltle.text = "Profile"


        user_id = sharedPreferences!!.getString("user_id", "none")
        currentStepsGoal = sharedPreferences!!.getString("my_goal", "0")
        periodical = sharedPreferences!!.getString("periodical", "none")
        periodicalIndex = sharedPreferences!!.getInt("periodicalIndex", 0)


        database = FirebaseDatabase.getInstance().reference
        val arrayPeriodical = resources.getStringArray(R.array.Daily_Weekly_Monthly)
        ti_stepsGoal = view.findViewById<TextInputLayout>(R.id.ti_stepsGoal)
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


        mEtSpinner.setSelection(periodicalIndex)
        Log.w(TAG, "arrayPeriodical"+periodicalIndex+arrayPeriodical[periodicalIndex])

        et_stepsGoal.hint = "Current Goal: $currentStepsGoal"


        btn_update.setOnClickListener {

            setError()

            newStepsGoal = et_stepsGoal.text.toString()
            newPeriodical = mEtSpinner.selectedItem.toString()


            var err = 0

            if (!validateFields(newStepsGoal)) {

                ti_stepsGoal!!.error = "Goal should not be empty !"
                err++

            }
            if(err == 0) {
                val editor = sharedPreferences?.edit()
                editor!!.putString("my_goal", newStepsGoal)
                editor!!.putString("periodical", newPeriodical)

                for(x in 0..3 ){
                    if(newPeriodical == arrayPeriodical[x]) {

                        editor!!.putInt("periodicalIndex", x)

                    }
                }

                editor.commit()

                writeNewPost(user_id, newPeriodical, newStepsGoal)
                dismiss()
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.settingFragment)
                showSnackBarMessage("Goal Update Success !")

            }
            else {
                showSnackBarMessage("Enter Valid Details !")
            }

        }

        return view
    }

    private fun setError() {

        ti_stepsGoal!!.error = null
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
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
