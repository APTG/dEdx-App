package dk.au.aptg.dEdx;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DedxIdxNameAdapter extends ArrayAdapter<DedxIdxName> {
	public DedxIdxNameAdapter(Context context, List<DedxIdxName> list) {
		super(context, android.R.layout.simple_list_item_1, list);
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = View.inflate(getContext(), android.R.layout.simple_list_item_1, null);
		}
		TextView txt = (TextView)convertView.findViewById(android.R.id.text1);
		txt.setText(getItem(position).getName());
		
		return convertView; 
    }
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = View.inflate(getContext(), android.R.layout.simple_list_item_1, null);
		}
		TextView txt = (TextView)convertView.findViewById(android.R.id.text1);
		txt.setText(getItem(position).getName());
		
		return convertView; 
	}

	public int getSelectedIdx(int selected) {
		return getItem(selected).getIdx();
	}
}
