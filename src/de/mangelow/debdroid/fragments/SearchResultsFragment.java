/***
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.mangelow.debdroid.fragments;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import de.mangelow.debdroid.DetailsActivity;
import de.mangelow.debdroid.OptionsActivity;
import de.mangelow.debdroid.R;
import de.mangelow.debdroid.data.SearchResult;
import de.mangelow.debdroid.data.Suite;
import de.mangelow.debdroid.tools.Helper;
import de.mangelow.debdroid.tools.ListViewAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class SearchResultsFragment extends SherlockFragment {

	private final String TAG	= "dD."+getClass().getSimpleName();
	private final boolean D = false;

	private Context context;

	private ActionBar ab;

	private ListViewAdapter adapter;

	private ExpandableListView elv;
	private ProgressBar pb;
	private TextView tv;

	private SearchView sv;

	private SearchResult searchresults [];

	private Helper mHelper = new Helper();

	private String packagename;

	//

	public static SearchResultsFragment newInstance(String packagename) {

		SearchResultsFragment fragment = new SearchResultsFragment();		
		fragment.setData(packagename);
		return fragment;
	}
	public void setData(String packagename) {
		this.packagename = packagename;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_searchresults, container, false);

		context = getSherlockActivity().getApplicationContext();

		ab = getSherlockActivity().getSupportActionBar();
		ab.setHomeButtonEnabled(true);
		setHasOptionsMenu(true);

		elv = (ExpandableListView) v.findViewById(android.R.id.list);
		elv.setGroupIndicator(null);
		elv.setEmptyView(v.findViewById(android.R.id.empty));
		elv.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,int i, int i1, long id) {
				if(D)Log.d(TAG, "onChildClick - " + i + ":" + i1);				

				SearchResult searchresult = searchresults[i];

				Suite ss [] = searchresult.getSuites(); 
				Helper.selected_suite = ss[i1];

				if(mHelper.isTabletAndLandscape(context)) {
					getSherlockActivity().invalidateOptionsMenu();
					getSherlockActivity().getSupportFragmentManager().beginTransaction().replace(R.id.right, new DetailsFragment()).commit();
				}
				else {
					Intent intent = new Intent(getSherlockActivity(), DetailsActivity.class);
					startActivity(intent);
				}

				return false;
			}
		});
		elv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					final int i = ExpandableListView.getPackedPositionGroup(id);
					final int i1 = ExpandableListView.getPackedPositionChild(id);
					if(D)Log.d(TAG, "onChildLongClick - " + i + ":" + i1);

					final SearchResult searchresult = searchresults[i];

					final Suite ss [] = searchresult.getSuites();
					final Suite s = (Suite) ss[i1];

					String entries [] = getResources().getStringArray(R.array.context_menu);

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setItems(entries, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							switch (which) {
							case 0:
								Helper.selected_suite = s;

								if(mHelper.isTabletAndLandscape(context)) {	
									getSherlockActivity().invalidateOptionsMenu();
									getSherlockActivity().getSupportFragmentManager().beginTransaction().replace(R.id.right, new DetailsFragment()).commit();
								}
								else {
									Intent intent = new Intent(getSherlockActivity(), DetailsActivity.class);
									startActivity(intent);
								}

								break;
							case 1:
								Helper.selected_suite = s;
								String subject = Helper.selected_suite.getPackagename() + " " + context.getString(R.string.from) + " " + Helper.selected_suite.getAlias() + " (" + Helper.selected_suite.getSuite() + ")";

								Intent i = new Intent(android.content.Intent.ACTION_SEND);
								i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								i.setType("text/plain");
								i.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
								i.putExtra(android.content.Intent.EXTRA_TEXT, mHelper.getShareBody(context));

								startActivity(Intent.createChooser(i, context.getString(R.string.share) + " " + subject));
								break;
							}
						}
					});
					AlertDialog alert = builder.create();
					alert.show();				

				}

				return true;
			}
		});

		pb = (ProgressBar) v.findViewById(R.id.pb);
		tv = (TextView) v.findViewById(R.id.tv);

		if(packagename!=null&&packagename.length()>0) {
			new ListViewTask(packagename, mHelper.loadIntPref(context, "searchresultsview", mHelper.SEARCHRESULTSVIEW_DEFAULT)).execute();					
		}

		return v;
	}
	private class ListViewTask extends AsyncTask<Void, String, Boolean> {

		private String keywords;
		private int searchresultsview;

		public ListViewTask(String keywords, int searchresultsview) {
			this.keywords = keywords;
			this.searchresultsview = searchresultsview;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			elv.setVisibility(View.GONE);
			pb.setVisibility(View.VISIBLE);
			tv.setVisibility(View.VISIBLE);

			ab.setTitle(getResources().getString(R.string.app_name));
			ab.setSubtitle(null);

			tv.setText(getResources().getString(R.string.loading));
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			String html = mHelper.getSearchResults(context, keywords);
			if(html==null)return false;

			searchresults = mHelper.parseSearchResults(html, searchresultsview);

			return true;

		}

		@Override
		protected void onPostExecute(Boolean result) {

			pb.setVisibility(View.GONE);

			if(!result||searchresults==null) {
				tv.setText(getResources().getString(R.string.error));
				ab.setTitle(getResources().getString(R.string.app_name));
			}
			else if(searchresults.length==0) {

				tv.setText(getResources().getString(R.string.noresults));

			}
			else {
				tv.setVisibility(View.GONE);				
				elv.setVisibility(View.VISIBLE);

				adapter = new ListViewAdapter(context, searchresults, searchresultsview);
				elv.setAdapter(adapter);
				adapter.notifyDataSetChanged();

			}

			ab.setTitle("\"" + keywords + "\"");
			ab.setSubtitle(getResources().getQuantityString(R.plurals.match, searchresults.length, searchresults.length));

		}
	}	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		sv = new SearchView(getSherlockActivity().getSupportActionBar().getThemedContext());
		sv.setQueryHint(getResources().getString(R.string.queryhint));
		sv.setIconified(true);
		sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {return false;}
			@Override
			public boolean onQueryTextSubmit(String keywords) {
				if (keywords.length() > 1) {
					sv.onActionViewCollapsed(); 

					new ListViewTask(keywords, mHelper.loadIntPref(context, "searchresultsview", mHelper.SEARCHRESULTSVIEW_DEFAULT)).execute();					

					return true;
				}
				return false;
			}
		});

		Menu m_menu = menu;
		menu.clear();

		m_menu.add(Menu.NONE, Menu.FIRST + 1, Menu.FIRST + 1, null).setIcon(R.drawable.ic_action_search).setActionView(sv).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		m_menu.add(Menu.NONE, Menu.FIRST + 2, Menu.FIRST + 2, getString(R.string.settings)).setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		if(Helper.selected_suite!=null)m_menu.add(Menu.NONE, Menu.FIRST + 3, Menu.FIRST + 3, getString(R.string.share)).setIcon(R.drawable.ic_action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			showAbout();
			break;
		case Menu.FIRST + 2:
			Intent intent = new Intent(getSherlockActivity(), OptionsActivity.class);
		startActivity(intent);
		break;
		case Menu.FIRST + 3:
			String subject = Helper.selected_suite.getPackagename() + " " + context.getString(R.string.from) + " " + Helper.selected_suite.getAlias() + " (" + Helper.selected_suite.getSuite() + ")";

		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setType("text/plain");
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		i.putExtra(android.content.Intent.EXTRA_TEXT, mHelper.getShareBody(context));

		startActivity(Intent.createChooser(i, context.getString(R.string.share) + " " + subject));
		break;
		}

		return super.onOptionsItemSelected(item);
	}
	public void myOnKeyDown(KeyEvent event) {

		char firstchar = (char)event.getUnicodeChar();
		if(Character.isLetter(firstchar)||Character.isDigit(firstchar)) {
			sv.setQuery(sv.getQuery() + String.valueOf(firstchar), false);
			sv.setIconified(false);
		}
	}
	private void showAbout() {

		View layout = LayoutInflater.from(getSherlockActivity()).inflate(R.layout.about,null);

		TextView tv_version = (TextView) layout.findViewById(R.id.tv_version);
		try {
			tv_version.setText(getSherlockActivity().getPackageManager().getPackageInfo(getSherlockActivity().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(layout);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});

		AlertDialog alert = builder.create();
		alert.show();	
	}	
}
