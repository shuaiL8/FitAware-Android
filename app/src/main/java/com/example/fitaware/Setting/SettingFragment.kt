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
import java.util.HashMap
import android.R.id.edit
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.Toast


/**
 * A simple [Fragment] subclass.
 *
 */
class SettingFragment : Fragment() {

    private val TAG = "SettingFragment"

    private var user_id: String = ""

    private lateinit var database: DatabaseReference
    private var sharedPreferences: SharedPreferences? = null


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

        database = FirebaseDatabase.getInstance().reference

        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = ""

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.settingFragment)
        }

        val userListView = view.findViewById<ListView>(R.id.userListView)
        val icon = view.findViewById<ImageView>(R.id.icon)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE

        val tv_name = view.findViewById<TextView>(R.id.tv_name)

        val tv_email = view.findViewById<TextView>(R.id.tv_email)

        val tv_group = view.findViewById<TextView>(R.id.tv_group)

        val user = resources.getStringArray(R.array.UserStatus)
        val adapter = ArrayAdapter<String>(
            activity!!,
            android.R.layout.simple_list_item_1, user
        )

        userListView.adapter = adapter

        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.shuail8)
        bitmap = getCroppedBitmap(bitmap)

        icon.setImageBitmap(bitmap)

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

                        editor.commit()
                        sendData()
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

            }
        }

        user_id = sharedPreferences!!.getString("user_id", "")


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

                if(my["captain"].toString() == my["id"].toString()) {
                    tv_group.text = "Captain of " + my["team"].toString()
                }
                else {
                    tv_group.text = "In team: " + my["team"].toString()
                }
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




    private fun sendData() {
        //INTENT OBJ
        val intent = Intent(activity!!.baseContext, MainActivity::class.java)

        //PACK DATA
//        intent.putExtra("Login_Status", loginStatus)

        //START ACTIVITY
        activity!!.startActivity(intent)
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
}
