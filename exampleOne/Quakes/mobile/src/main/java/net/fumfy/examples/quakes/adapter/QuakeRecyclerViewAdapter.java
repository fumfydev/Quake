package net.fumfy.examples.quakes.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.fumfy.examples.quakes.QuakeApplication;
import net.fumfy.examples.quakes.ui.QuakeFragment.OnListFragmentInteractionListener;
import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.sugar.Quake;
import net.fumfy.examples.quakes.utility.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class QuakeRecyclerViewAdapter extends RecyclerView.Adapter<QuakeRecyclerViewAdapter.ViewHolder> {

	private List<Quake> mValues;
	private final OnListFragmentInteractionListener mListener;
	private final Context context;
	private final String TAG=QuakeApplication.TAG;

	public QuakeRecyclerViewAdapter(Context context, OnListFragmentInteractionListener listener, List<Quake> quakes) {
		mListener = listener;
		mValues = quakes;
		notifyDataSetChanged();
		this.context = context;
	}


	public QuakeRecyclerViewAdapter(Context context, List<Quake> quakes) {
		mListener = null;
		mValues = quakes;
		notifyDataSetChanged();
		this.context = context;
	}

	public void addQuake(Quake quake) {
		if (mValues == null) {
			mValues = new ArrayList<>();
		}

		int index = findInsertIndex(quake);
		mValues.add(index, quake);
		notifyItemInserted(index);

	}

	private int findInsertIndex(Quake quake) {
		long date = quake.date;
		for (int i=0 ; i < mValues.size() ; i++) {
			if (date >= mValues.get(i).date) return i;
		}
		return mValues.size();
	}

	public void swap(ArrayList<Quake> quakes) {
		mValues.clear();
		try {
			mValues.addAll(quakes);
			Collections.sort(mValues, (o1, o2) -> {
				if (o1.date > o2.date) return -1;
				if (o1.date < o2.date) return 1;
				return 0;
			});
		} catch (Exception e) {
			Log.e(TAG, "In QuakeRecyclerViewAdapter swap: ", e);
		}
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.fragment_quake, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.mItem = mValues.get(position);
		holder.mMagnitude.setText(
			String.format(Locale.getDefault(),
				context.getString(R.string.qcv_mag_format),
				mValues.get(position).magnitude));
		holder.mDescription.setText(mValues.get(position).description);
		holder.mDate.setText(formatDateString(mValues.get(position).date));
		GradientDrawable color_bar=null;
		try {

			color_bar = (GradientDrawable)
				holder.mView.findViewById(R.id.color_bar_frame_layout).getBackground();
		} catch (Exception e) {
			Log.e(TAG, "onBindViewHolder: ", e);
		}
		Drawable icon;

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			icon = context.getResources().getDrawable(R.mipmap.ic_quake, context.getTheme());
		} else {
			icon = ContextCompat.getDrawable(context, R.mipmap.ic_quake);
		}
		int color = utils.quakeColor(holder.mItem.magnitude);
		if (color_bar != null) {
			color_bar.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}
		icon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		holder.mQuakeView.setImageDrawable(icon);

		if (mListener != null ) {
			holder.mView.setOnClickListener(View -> mListener.onListFragmentInteraction(holder.mItem, View));
		}
		holder.mView.setFocusable(true);
		holder.mView.setClickable(true);
	}

	@SuppressLint("SimpleDateFormat")
	private String formatDateString(long inDate) {
		String date;
		SimpleDateFormat fmt;
		Calendar today = GregorianCalendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis());
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		long startOfDay = today.getTimeInMillis();
		if (inDate > startOfDay) {
			// This is today
			fmt = new SimpleDateFormat("HH:mm");
		} else {
			// yesterday or older
			fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		}
		Date date1 = new Date(inDate);
		date = fmt.format(date1);
		return date;
	}

	@Override
	public int getItemCount() {
		return mValues == null ? 0 : mValues.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		final View mView;
		final TextView mDescription;
		final TextView mDate;
		final TextView mMagnitude;
		private final ImageView mQuakeView;
		Quake mItem;

		ViewHolder(View view) {
			super(view);
			mView = view;
			mDescription = (TextView) view.findViewById(R.id.widget_description);
			mMagnitude = (TextView) view.findViewById(R.id.widget_magnitude);
			mDate = (TextView) view.findViewById(R.id.widget_date);
			mQuakeView = (ImageView) view.findViewById(R.id.quakeView);
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mDescription.getText() + "'";
		}
	}
}
