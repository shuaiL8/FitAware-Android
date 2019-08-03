package com.vt.fitaware.Team


import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.Navigation
import com.vt.fitaware.Communicator
import com.vt.fitaware.R
import com.vt.fitaware.utils.Validation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.provider.MediaStore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.IOException


class CreateNewTeamFragment : Fragment() {

    private val TAG = "CreateNewTeamFragment"
    private lateinit var database: DatabaseReference
    private var stepsGoal: String = "0"
    private var periodical: String = "daily"
    private var captain: String = "none"

    private var user_id: String = "none"
    private var teamName: String = "none"
    private var oldTeamName: String = "none"

    private var teamGoal: String = "0"
    private var sharedPreferences: SharedPreferences? = null

    private var ti_name : TextInputLayout? = null
    private var ti_stepsGoal : TextInputLayout? = null

    private var teamIcon :ImageView? = null
    private var tv_setTeamIcon : TextView? = null

    private var mStorageRef: StorageReference? = null

    private var bitmap :Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(
            R.layout.fragment_create_new_team, container,
            false)
        setHasOptionsMenu(false)
        initSharedPreferences()



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
            oldTeamName = allSteps["team"]!!.toString()
            teamGoal = allSteps["team_goal"]!!.toString()
            captain = allSteps["captain"]!!.toString()
            periodical = allSteps["periodical"]!!.toString()

        }
        model.message.observe(activity!!, `object`)

        database = FirebaseDatabase.getInstance().reference
        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)

        val arrayPeriodical = resources.getStringArray(R.array.Daily_Weekly_Monthly)
        val et_name = view.findViewById<EditText>(R.id.et_name)
        ti_name = view.findViewById<TextInputLayout>(R.id.ti_name)
        val et_stepsGoal = view.findViewById<EditText>(R.id.et_stepsGoal)
        ti_stepsGoal = view.findViewById<TextInputLayout>(R.id.ti_stepsGoal)
        val btn_create = view.findViewById<Button>(R.id.btn_create)

        if(captain == user_id) {
            toolbarTiltle.text = "Update my team Info"
            btn_create.text = "update"
            et_name.setText(oldTeamName)

        }
        else {
            toolbarTiltle.text = "Create a new team"
            btn_create.text = "create"
            et_name.hint = "Current Team: $oldTeamName"

        }

        et_stepsGoal.hint = "Current Team Goal: $teamGoal"

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

        for(x in 0..3 ){
            if(periodical == arrayPeriodical[x]) {
                mEtSpinner.setSelection(x)
                Log.w(TAG, "arrayPeriodical"+x+arrayPeriodical[x])

            }
        }

        mStorageRef = FirebaseStorage.getInstance().reference



        teamIcon = view.findViewById<ImageView>(R.id.teamIcon)
        tv_setTeamIcon = view.findViewById<TextView>(R.id.tv_setTeamIcon)


        val iconRef = mStorageRef!!.child("team_icon/$oldTeamName/icon.jpg")

        iconRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadIconUrl = task.result

                Picasso.get().load(downloadIconUrl).into(teamIcon)

            } else {
                var bMap = BitmapFactory.decodeResource(resources, R.drawable.teamwork)
                bMap = getCroppedBitmap(bMap)
                teamIcon!!.setImageBitmap(bMap)
            }
        }

        tv_setTeamIcon!!.setOnClickListener {

            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(pickPhoto, 0)

        }

        btn_create.setOnClickListener {

            setError()

            teamName = et_name.text.toString()
            stepsGoal = et_stepsGoal.text.toString()
            periodical = mEtSpinner.selectedItem.toString()



            if(captain == user_id) {
                var err = 0

                if (!Validation.validateFields(teamName)) {
                    err++
                    ti_name!!.error = "Team name should not be empty !"
                    showSnackBarMessage("Enter Valid Details !")
                }
                if (!Validation.validateFields(stepsGoal)) {
                    err++
                    ti_stepsGoal!!.error = "Goal should not be empty !"
                    showSnackBarMessage("Enter Valid Details !")
                }
                if (Validation.validateFields(teamName) && oldTeamName != teamName) {
                    err++
                    ti_name!!.error = "Team names not match !"
                    showSnackBarMessage("Enter Valid Details !")
                }
                if (err == 0) {
                    val editor = sharedPreferences?.edit()
                    editor!!.putString("team", teamName)
                    editor!!.putString("team_goal", stepsGoal)

                    editor.commit()

                    updateNewPost(user_id, periodical, teamName, stepsGoal)
                    if(bitmap != null) {
                        val iconRef = mStorageRef!!.child("team_icon/$teamName/icon.jpg")

                        val uploadImage = convertBitmapToByteArray(bitmap!!)
                        iconRef.putBytes(uploadImage)

                    }

                    Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)
                    showSnackBarMessage("Team Info updated !")


                }
            }
            else {
                var err = 0

                if (!Validation.validateFields(teamName)) {
                    err++
                    ti_name!!.error = "Team name should not be empty !"
                    showSnackBarMessage("Enter Valid Details !")
                }
                if (!Validation.validateFields(stepsGoal)) {
                    err++
                    ti_stepsGoal!!.error = "Goal should not be empty !"
                    showSnackBarMessage("Enter Valid Details !")
                }
                if (oldTeamName == teamName) {
                    err++
                    showSnackBarMessage("Change team Info only available for team captain !")

                }
                if (err == 0) {
                    val editor = sharedPreferences?.edit()
                    editor!!.putString("team", teamName)
                    editor!!.putString("team_goal", stepsGoal)

                    editor.commit()

                    writeNewPost(user_id, periodical, teamName, stepsGoal)
                    if(bitmap != null) {
                        val iconRef = mStorageRef!!.child("team_icon/$teamName/icon.jpg")

                        val uploadImage = convertBitmapToByteArray(bitmap!!)
                        iconRef.putBytes(uploadImage)

                    }

                    Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)
                    showSnackBarMessage("Team Created !")


                }

            }

        }
        return view
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

    private fun updateNewPost(id: String, teamPeriodical: String, teamN: String, teamGoal: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/Teams/$teamN/periodical"] = teamPeriodical
        childUpdates["/Teams/$teamN/captain"] = id
        childUpdates["/Teams/$teamN/teamGoal"] = teamGoal
        childUpdates["/Teams/$teamN/teamSteps"] = "0"

        childUpdates["/User/$id/captain"] = id
        childUpdates["/User/$id/team"] = teamN
        childUpdates["/User/$id/teamGoal"] = teamGoal
        childUpdates["/User/$id/periodical"] = teamPeriodical

        Log.w(TAG, "childUpdates: $childUpdates")

        database.updateChildren(childUpdates)

    }

    private fun writeNewPost(id: String, teamPeriodical: String, teamN: String, teamGoal: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/Teams/$teamN/periodical"] = teamPeriodical
        childUpdates["/Teams/$teamN/captain"] = id
        childUpdates["/Teams/$teamN/teamGoal"] = teamGoal
        childUpdates["/Teams/$teamN/teamSteps"] = "0"

        childUpdates["/User/$id/captain"] = id
        childUpdates["/User/$id/team"] = teamN
        childUpdates["/User/$id/teamGoal"] = teamGoal
        childUpdates["/User/$id/periodical"] = teamPeriodical

        Log.w(TAG, "childUpdates: $childUpdates")


        if(oldTeamName != "none") {
            database.child("/Teams/$oldTeamName/teamMembers/$id").removeValue()
        }

        database.updateChildren(childUpdates)

        database.child("/Teams/$teamN/teamMembers/$id").setValue(id)
    }

    private fun setError() {

        ti_name!!.error = null
        ti_stepsGoal!!.error = null
    }

    private fun showSnackBarMessage(message: String) {

        if (view != null) {

            Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        var output  = Bitmap.createBitmap(bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888);
        var  canvas: Canvas = Canvas(output)

        var color:Int = 0xff424242.toInt()
        var paint: Paint = Paint()
        var rect: Rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true;
        canvas.drawARGB(0, 0, 0, 0);
        paint.color = color
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(), paint);
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent!!.data
                Log.i(TAG, "selectedImage: $selectedImage")

                bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedImage)

                bitmap = getCroppedBitmap(bitmap!!)


                teamIcon!!.setImageBitmap(bitmap)
            }
        }
    }

    fun convertBitmapToByteArray(bMap: Bitmap): ByteArray {
        var baos: ByteArrayOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            bMap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            return baos!!.toByteArray()
        } finally {
            if (baos != null) {
                try {
                    baos!!.close()
                } catch (e: IOException) {
                }

            }
        }
    }


}
