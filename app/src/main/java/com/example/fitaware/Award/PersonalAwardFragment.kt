package com.example.fitaware.Award


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitaware.R
import android.widget.AdapterView




class PersonalAwardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_personal_award, container,
            false)
        setHasOptionsMenu(true)

//        gridView.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
//            val book = books[position]
//            book.toggleFavorite()
//
//            // This tells the GridView to redraw itself
//            // in turn calling your BooksAdapter's getView method again for each cell
//            booksAdapter.notifyDataSetChanged()
//        })

        return view
    }


}
