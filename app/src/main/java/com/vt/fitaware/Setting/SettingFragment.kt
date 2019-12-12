package com.vt.fitaware.Setting


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.*
import androidx.navigation.Navigation
import com.vt.fitaware.R
import android.content.Intent
import android.util.Log
import com.vt.fitaware.MainActivity
import com.google.firebase.database.*
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.app.FragmentManager
import android.support.v4.widget.SwipeRefreshLayout
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.vt.fitaware.MyNotificationService
import java.io.ByteArrayOutputStream
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 *
 */
class SettingFragment : Fragment() {

    private val TAG = "SettingFragment"

    private var user_id: String = "none"

    private var sharedPreferences: SharedPreferences? = null

    internal lateinit var icon: ImageView

    private var mStorageRef: StorageReference? = null

    private var settings = ArrayList<Settings>(1)
    private lateinit var settingAdapter: SettingAdaptor

    private var myNotificationServiceStatus: String = "startMyNotificationService"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_setting, container,
            false)
        setHasOptionsMenu(true)
        initSharedPreferences()

        myNotificationServiceStatus = sharedPreferences!!.getString("MyNotificationServiceStatus", "startMyNotificationService")

        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = ""

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.settingFragment)
        }

        val userListView = view.findViewById<ListView>(R.id.userListView)
        icon = view.findViewById<ImageView>(R.id.icon)



        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE

        val tv_name = view.findViewById<TextView>(R.id.tv_name)

        val tv_email = view.findViewById<TextView>(R.id.tv_email)

        val tv_captain = view.findViewById<TextView>(R.id.tv_captain)

        val tv_group = view.findViewById<TextView>(R.id.tv_group)

        settings.clear()

        settings.add(Settings("Profile", false, false))

        Log.i(TAG, "myNotificationServiceStatus: $myNotificationServiceStatus")

        if(myNotificationServiceStatus == "startMyNotificationService") {
            settings.add(Settings("Aways on Notification", true, true))
        }
        else {
            settings.add(Settings("Aways on Notification", true, false))
        }

        settings.add(Settings("Change Password", false, false))
        settings.add(Settings("Log out", false, false))
        settings.add(Settings("Contact Us", false, false))

        if (activity !=null){
            settingAdapter = SettingAdaptor(
                activity,
                R.layout.settings,
                settings
            )
            userListView.adapter = settingAdapter
        }

//        val user = resources.getStringArray(R.array.UserStatus)
//        val adapter = ArrayAdapter<String>(
//            activity!!,
//            android.R.layout.simple_list_item_1, user
//        )
//
//        userListView.adapter = adapter


        userListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            if (position == 0) {
//                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.profileFragment)
                val profileFragment = ProfileFragment()
                profileFragment.show(fragmentManager, "ProfileFragment")
            }
            if (position == 2) {
                showChangepasswordDialog()
            }
            if (position == 3) {

                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(context)

                // set message of alert dialog
                dialogBuilder.setMessage("Do you want to logout ?")
                    // if the dialog is cancelable
                    // positive button text and action
                    .setPositiveButton("Logout", DialogInterface.OnClickListener {
                            dialog, id ->
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                        val editor = sharedPreferences!!.edit()
                        editor.remove("loginStatus")
                        editor.remove("user_id")

                        editor.remove("my_goal")
                        editor.remove("rank")
                        editor.remove("currentSteps")
                        editor.remove("duration")
                        editor.remove("heartPoints")
                        editor.remove("distance")
                        editor.remove("calories")

                        editor.commit()
                        goToLogin()
                        stopMyNotificationService()
                    })
                    // negative button text and action
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
//                alert.setTitle("AlertDialogExample")
                // show alert dialog
                alert.show()

            }
            if (position == 4) {
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder.setMessage("Email: shuail8@vt.edu")
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                alert.show()

            }
        }

        user_id = sharedPreferences!!.getString("user_id", "none")


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
                tv_name.text = my["id"].toString()
                tv_email.text = my["email"].toString()

                if(my["team"].toString() != "none") {
                    if(my["captain"].toString() == user_id) {
                        tv_captain.text = "Captain of " + my["team"].toString()
                    }
                    else {
                        tv_captain.text = "Team captain: " + my["captain"].toString()
                        tv_group.text = "In team: " + my["team"].toString()
                    }
                }
                else {
                    tv_captain.text = "Currently not in any team"

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

        mStorageRef = FirebaseStorage.getInstance().reference


        val iconRef = mStorageRef!!.child("user_icon/$user_id/icon.jpg")

        iconRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadIconUrl = task.result

                Picasso.get().load(downloadIconUrl).into(icon)

            } else {
                var bitmap = BitmapFactory.decodeResource(resources, R.drawable.shuail8)
                bitmap = getCroppedBitmap(bitmap)
                icon.setImageBitmap(bitmap)
            }
        }

        icon.setOnClickListener{

            // build alert dialog
            val dialogBuilder = AlertDialog.Builder(context)

            dialogBuilder.setMessage("Do you want to change your icon ?")
                .setPositiveButton("Choose from Gallery", DialogInterface.OnClickListener {
                        dialog, id ->
                    val pickPhoto = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(pickPhoto, 0)


                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })

            val alert = dialogBuilder.create()
            alert.show()

        }

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




    private fun goToLogin() {

        activity!!.finish()

        val intent = Intent(
            activity!!.baseContext,
            MainActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        activity!!.startActivity(intent)

    }


    private fun stopMyNotificationService(){

        val notificationService = MyNotificationService::class.java
        val intent = Intent(context, notificationService)

        intent.putExtra("user_id", "none")
        intent.putExtra("team", "none")

        context!!.stopService(intent)
        Log.i(TAG, "Stop MyNotificationService.")

    }


    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
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

                val iconRef = mStorageRef!!.child("user_icon/$user_id/icon.jpg")

                var bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedImage)

                bitmap = getCroppedBitmap(bitmap)

                val uploadImage = convertBitmapToByteArray(bitmap)

                iconRef.putBytes(uploadImage)

                icon.setImageBitmap(bitmap)
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