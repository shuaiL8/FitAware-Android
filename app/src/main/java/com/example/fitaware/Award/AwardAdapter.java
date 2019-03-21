package com.example.fitaware.Award;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fitaware.R;

public class AwardAdapter {

//    private final Context mContext;
//    private final Award[] awards;
//
//    // 1
//    public BooksAdapter(Context context, Award[] books) {
//        this.mContext = context;
//        this.awards = books;
//    }
//
//    // 2
//    @Override
//    public int getCount() {
//        return awards.length;
//    }
//
//    // 3
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    // 4
//    @Override
//    public Object getItem(int position) {
//        return null;
//    }
//
//    // 5
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // 1
//        final Award award = awards[position];
//
//        // 2
//        if (convertView == null) {
//            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//            convertView = layoutInflater.inflate(R.layout.linearlayout_book, null);
//        }
//
//        // 3
//        final ImageView imageView = (ImageView)convertView.findViewById(R.id.imageview_cover_art);
//        final TextView nameTextView = (TextView)convertView.findViewById(R.id.textview_book_name);
//        final TextView authorTextView = (TextView)convertView.findViewById(R.id.textview_book_author);
//        final ImageView imageViewFavorite = (ImageView)convertView.findViewById(R.id.imageview_favorite);
//
//        // 4
//        imageView.setImageResource(award.getImageResource());
//        nameTextView.setText(mContext.getString(award.getName()));
//        authorTextView.setText(mContext.getString(award.getAuthor()));
//
//        return convertView;
//    }
}
