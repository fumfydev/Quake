package net.fumfy.examples.quakes.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.adapter.QuakeRecyclerViewAdapter;
import net.fumfy.examples.quakes.sugar.Quake;

import java.util.ArrayList;

public class QuakeSearchActivityActivityFragment extends Fragment {

	private ArrayList<Quake> mQuakes;

	public QuakeSearchActivityActivityFragment() {
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent searchIntent = getActivity().getIntent();
		if (Intent.ACTION_SEARCH.equals(searchIntent.getAction())) {
			String query = searchIntent.getStringExtra(SearchManager.QUERY);
			mQuakes = doSearch(query);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_quake_search_activity, container, false);
		RecyclerView mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);

		// Set the adapter
		if (mRecyclerView != null) {
			Context context = view.getContext();
			mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
			QuakeRecyclerViewAdapter mAdapter = new QuakeRecyclerViewAdapter(getContext(), mQuakes);
			mRecyclerView.setAdapter(mAdapter);
		}

		return view;
	}

	private ArrayList<Quake> doSearch(String query) {
		ArrayList<Quake> quakes;
		// This simple implementation matches on description alone
		quakes = (ArrayList<Quake>)Quake
			.find(Quake.class,
				  "description" + " like \"%" + query + "%\"",
				  null,
				  null,
				  null,
				  null);

		return quakes;
	}

}
