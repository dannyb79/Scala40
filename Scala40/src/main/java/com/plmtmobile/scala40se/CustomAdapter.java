package com.plmtmobile.scala40se;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by daniele on 03/11/13.
 */
public class CustomAdapter extends ArrayAdapter<RankListItem> {
    public CustomAdapter(Context context, int textViewResourceId,
                         ArrayList<RankListItem> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.rowcustom, null);
        TextView pos        = (TextView)convertView.findViewById(R.id.tv_Position );
        TextView username   = (TextView)convertView.findViewById(R.id.tv_Name );
        TextView score      = (TextView)convertView.findViewById(R.id.tv_Score );
        TextView wontotal   = (TextView)convertView.findViewById(R.id.tv_WonTotal );
        TextView medals     = (TextView)convertView.findViewById(R.id.tv_Medals );
        RankListItem c = getItem(position);
        pos.setText( String.format( "%d", position + 1 ) );
        username.setText( c.username );
        score.setText( String.format( "%d", c.score ) );
        wontotal.setText(String.format("%d/%d", c.wongames, c.totalgames));
        if( c.medals > 0 ) {
            medals.setText( String.format( "%d", c.medals ) );
        } else {
            medals.setText( "" );
        }
        return convertView;
    }

}
