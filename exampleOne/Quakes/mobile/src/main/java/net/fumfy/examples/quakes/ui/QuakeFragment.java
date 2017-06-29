package net.fumfy.examples.quakes.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.fumfy.examples.quakes.QuakeApplication;
import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.activity.PrefsActivity;
import net.fumfy.examples.quakes.adapter.QuakeRecyclerViewAdapter;
import net.fumfy.examples.quakes.events.NewQuakes;
import net.fumfy.examples.quakes.events.QuakesRefreshed;
import net.fumfy.examples.quakes.events.QuakesRefreshedError;
import net.fumfy.examples.quakes.service.QuakeService;
import net.fumfy.examples.quakes.sugar.Quake;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class QuakeFragment extends Fragment implements
	SharedPreferences.OnSharedPreferenceChangeListener {

	private OnListFragmentInteractionListener mListener = null;
	private List<Quake> mQuakes;
	private Double magnitude;
	private QuakeRecyclerViewAdapter mAdapter;
	private RecyclerView mRecyclerView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	
	private final String TAG=QuakeApplication.TAG;

	public QuakeFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpPrefs();

		initQuakeList(this.magnitude);
	}

	private void setUpPrefs() {

		SharedPreferences sharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(getContext());
		magnitude = (double) Integer.parseInt(sharedPreferences
			.getString(PrefsActivity.PREF_MIN_MAG, "3"));
	}

	private void initQuakeList(Double magnitude) {
		String select = "magnitude >= ?";
		String[] args  = { magnitude.toString() };
		String orderBy = "date DESC";
		mQuakes = Quake.find(Quake.class,select, args, null, orderBy, null);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_quake_list, container, false);
		mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
		mSwipeRefreshLayout = (SwipeRefreshLayout) view.
			findViewById(R.id.swipeRefreshLayout);
		mSwipeRefreshLayout.setOnRefreshListener(() -> mListener.refreshQuakes());

		// Set the adapter
		if (mRecyclerView != null) {
			Context context = view.getContext();
			// mRecyclerView = (RecyclerView) view;
			mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
			//mRecyclerView.setEmptyView(view.findViewById(android.R.id.empty));
			mAdapter = new QuakeRecyclerViewAdapter(getContext(), mListener, mQuakes);
			//mRecyclerView.setHasFixedSize(true);
			mRecyclerView.setAdapter(mAdapter);
		}
		return view;
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnListFragmentInteractionListener) {
			mListener = (OnListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
				+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNewQuakes(NewQuakes event) {

		if (event.list != null) {
			Observable.fromIterable(event.list)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeOn(Schedulers.trampoline())
				.doOnComplete(() -> mRecyclerView.smoothScrollToPosition(0))
				.doOnError(throwable -> Log.e(TAG, "QuakeFragment:onNewQuakes:doOnError:accept: " + throwable.getMessage()))
				.subscribe(quake -> {
					if ((quake != null) && (quake.magnitude != null)) {
						if (quake.magnitude >= magnitude) mAdapter.addQuake(quake);
					}
				});
		} else if (event.size > QuakeService.NEW_QUAKES_LIST_LIMIT) {
			initQuakeList(magnitude);
			mAdapter.swap((ArrayList<Quake>)mQuakes);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
		PreferenceManager
			.getDefaultSharedPreferences(getContext())
			.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (getActivity() != null) {
			if (key.equals(PrefsActivity.PREF_MIN_MAG)) {
				magnitude = (double) Integer.parseInt(sharedPreferences
					.getString(PrefsActivity.PREF_MIN_MAG, "3"));
				initQuakeList(magnitude);
				mAdapter.swap(new ArrayList<>(mQuakes));
			}
		}
	}

	public void clearQuakes() {
		mQuakes.clear();
		mAdapter.swap(new ArrayList<>(mQuakes));
	}

	public void onQuakesRefreshed(QuakesRefreshed quakesRefreshed) {
		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(false);
		}
		int count = quakesRefreshed.new_quakes;
		String text;
		if (count > 0) {
			if (count == 1) {
				text = getString(R.string.ui_new_quakes_singular);
			} else {
				text = String.format(getString(R.string.ui_new_quakes_plural), count);
			}
		} else {
			text = getString(R.string.ui_no_new_quakes);
		}
		Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
	}

	public void onQuakesRefreshedError(QuakesRefreshedError quakesRefreshedError) {
		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(false);
		}
		Toast.makeText(getContext(), quakesRefreshedError.error_message, Toast.LENGTH_LONG).show();
	}

	public interface OnListFragmentInteractionListener {
		void onListFragmentInteraction(Quake item, View view);
		void refreshQuakes();
	}
}
