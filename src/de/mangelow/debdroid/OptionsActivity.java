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
package de.mangelow.debdroid;

import de.mangelow.debdroid.tools.Helper;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class OptionsActivity extends PreferenceActivity {

	private Context context;
	private Resources res;

	private Helper mHelper = new Helper();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		context = getApplicationContext();
		res = getResources();

		setPreferenceScreen(createPreferences());

		setTitle(res.getString(R.string.settings));

	}	
	@SuppressWarnings("deprecation")
	private PreferenceScreen createPreferences() {

		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(context);	

		final String [] searchon_entries = res.getStringArray(R.array.searchon_entries);
		String [] searchon_values = new String [searchon_entries.length];
		for (int i = 0; i < searchon_values.length; i++) {
			searchon_values[i] = String.valueOf(i);
		}

		int searchon = mHelper.loadIntPref(context, "searchon", mHelper.SEARCHON_DEFAULT);

		ListPreference lp_searchon = new ListPreference(this);
		lp_searchon.setTitle(res.getString(R.string.searchon));
		lp_searchon.setEntries(searchon_entries);
		lp_searchon.setEntryValues(searchon_values);
		lp_searchon.setDialogTitle(res.getString(R.string.pleasechoose));
		lp_searchon.setSummary(searchon_entries[searchon]);
		lp_searchon.setValue(String.valueOf(searchon));
		lp_searchon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object nv) {
				final String summary = nv.toString();
				ListPreference lp = (ListPreference) p;	
				int newvalue = lp.findIndexOfValue(summary);
				lp.setSummary(searchon_entries[newvalue]);

				mHelper.saveIntPref(context, "searchon", newvalue);

				return true;
			}
		});		
		root.addPreference(lp_searchon);

		//

		int exact = mHelper.loadIntPref(context, "exact", mHelper.SEARCHON_DEFAULT);
		boolean exact_checked = false;
		if(exact==1)exact_checked = true;

		CheckBoxPreference cbp_exact = new CheckBoxPreference(context);
		cbp_exact.setTitle(getString(R.string.exact));
		cbp_exact.setChecked(exact_checked);
		cbp_exact.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object v) {

				boolean newvalue = Boolean.parseBoolean(v.toString());
				CheckBoxPreference preference = (CheckBoxPreference)p;
				preference.setChecked(newvalue);

				int nv = 0;
				if(newvalue==true)nv = 1;
				mHelper.saveIntPref(context, "exact", nv);

				return true;
			}
		});
		root.addPreference(cbp_exact);

		//

		PreferenceCategory pc_suite = new PreferenceCategory(context);
		pc_suite.setTitle(getString(R.string.suite));
		root.addPreference(pc_suite);

		String [] entries = res.getStringArray(R.array.suite_entries);
		final int length_suite = entries.length;


		for (int i = 0; i < length_suite; i++) {

			int suite = mHelper.loadIntPref(context, "suite_" + mHelper.SUITE[i], mHelper.SUITE_DEFAULT);
			boolean suite_checked = false;
			if(suite==1)suite_checked = true;

			final int position = i;
			CheckBoxPreference cbp = new CheckBoxPreference(context);
			cbp.setTitle(entries[i]);
			cbp.setChecked(suite_checked);
			cbp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object v) {

					boolean newvalue = Boolean.parseBoolean(v.toString());
					
					if(!newvalue) {
						int check = 0;
						for (int j = 0; j < length_suite; j++) {
							if(j!=position) {
								check =  mHelper.loadIntPref(context, "suite_" + mHelper.SUITE[j], mHelper.SUITE_DEFAULT);
								if(check==1)break;
							}
						}
						if(check==0) {
							Toast.makeText(context, getString(R.string.selectatleastone) + " " + getString(R.string.suite), Toast.LENGTH_LONG).show();
							return false;
						}
					}					
					else {
						CheckBoxPreference preference = (CheckBoxPreference)p;
						preference.setChecked(newvalue);
					}


					int nv = 0;
					if(newvalue==true)nv = 1;
					mHelper.saveIntPref(context, "suite_" + mHelper.SUITE[position], nv);

					return true;
				}
			});
			pc_suite.addPreference(cbp);
		}

		//

		PreferenceCategory pc_section = new PreferenceCategory(context);
		pc_section.setTitle(getString(R.string.section));
		root.addPreference(pc_section);

		entries = res.getStringArray(R.array.section_entries);
		final int length_section = entries.length;

		for (int i = 0; i < length_section; i++) {

			int suite = mHelper.loadIntPref(context, "section_" + mHelper.SECTION[i], mHelper.SECTION_DEFAULT);
			boolean section_checkend = false;
			if(suite==1)section_checkend = true;

			final int position = i;
			CheckBoxPreference cbp = new CheckBoxPreference(context);
			cbp.setTitle(entries[i]);
			cbp.setChecked(section_checkend);
			cbp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object v) {

					boolean newvalue = Boolean.parseBoolean(v.toString());
					
					if(!newvalue) {
						int check = 0;
						for (int j = 0; j < length_section; j++) {
							if(j!=position) {
								check =  mHelper.loadIntPref(context, "section_" + mHelper.SECTION[j], mHelper.SECTION_DEFAULT);
								if(check==1)break;
							}
						}
						if(check==0) {
							Toast.makeText(context, getString(R.string.selectatleastone) + " " + getString(R.string.section), Toast.LENGTH_LONG).show();
							return false;
						}
					}					
					else {
						CheckBoxPreference preference = (CheckBoxPreference)p;
						preference.setChecked(newvalue);
					}

					int nv = 0;
					if(newvalue==true)nv = 1;
					mHelper.saveIntPref(context, "section_" + mHelper.SECTION[position], nv);

					return true;
				}
			});
			pc_section.addPreference(cbp);
		}

		//
		
		PreferenceCategory pc_other = new PreferenceCategory(context);
		pc_other.setTitle(getString(R.string.other));
		root.addPreference(pc_other);
		
		final String [] checkpackages_entries = res.getStringArray(R.array.checkpackages_entries);
		String [] checkpackages_values = new String [checkpackages_entries.length];
		for (int i = 0; i < checkpackages_entries.length; i++) {
			checkpackages_values[i] = String.valueOf(i);
		}
		
		int checkpackages = mHelper.loadIntPref(context, "checkpackages", mHelper.CHECKPACKAGES_DEFAULT);

		ListPreference lp_checkpackages = new ListPreference(this);
		lp_checkpackages.setTitle(res.getString(R.string.checkpackages));
		lp_checkpackages.setEntries(checkpackages_entries);
		lp_checkpackages.setEntryValues(checkpackages_values);
		lp_checkpackages.setDialogTitle(res.getString(R.string.pleasechoose));
		lp_checkpackages.setSummary(checkpackages_entries[checkpackages]);
		lp_checkpackages.setValue(String.valueOf(checkpackages));
		lp_checkpackages.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object nv) {
				final String summary = nv.toString();
				ListPreference lp = (ListPreference) p;	
				int newvalue = lp.findIndexOfValue(summary);
				lp.setSummary(checkpackages_entries[newvalue]);

				mHelper.saveIntPref(context, "checkpackages", newvalue);

				return true;
			}
		});		
		//pc_other.addPreference(lp_checkpackages);
		
		//
		
		final String [] textsize_entries = res.getStringArray(R.array.textsize_entries);
		String [] textsize_values = new String [textsize_entries.length];
		for (int i = 0; i < textsize_entries.length; i++) {
			textsize_values[i] = String.valueOf(i);
		}
		
		int textsize = mHelper.loadIntPref(context, "textsize", mHelper.TEXTSIZE_DEFAULT);

		ListPreference lp_textsize = new ListPreference(this);
		lp_textsize.setTitle(res.getString(R.string.textsize));
		lp_textsize.setEntries(textsize_entries);
		lp_textsize.setEntryValues(textsize_values);
		lp_textsize.setDialogTitle(res.getString(R.string.pleasechoose));
		lp_textsize.setSummary(textsize_entries[textsize]);
		lp_textsize.setValue(String.valueOf(textsize));
		lp_textsize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object nv) {
				final String summary = nv.toString();
				ListPreference lp = (ListPreference) p;	
				int newvalue = lp.findIndexOfValue(summary);
				lp.setSummary(textsize_entries[newvalue]);

				mHelper.saveIntPref(context, "textsize", newvalue);
				
				Toast.makeText(context, getResources().getString(R.string.changesnextsearch), Toast.LENGTH_LONG).show();

				return true;
			}
		});		
		pc_other.addPreference(lp_textsize);
		
		//
		
		final String [] view_entries = res.getStringArray(R.array.view_entries);
		String [] view_values = new String [view_entries.length];
		for (int i = 0; i < view_entries.length; i++) {
			view_values[i] = String.valueOf(i);
		}
		
		int searchresultsview = mHelper.loadIntPref(context, "searchresultsview", mHelper.SEARCHRESULTSVIEW_DEFAULT);

		ListPreference lp_searchresultsview = new ListPreference(this);
		lp_searchresultsview.setTitle(res.getString(R.string.searchresultsview));
		lp_searchresultsview.setEntries(view_entries);
		lp_searchresultsview.setEntryValues(view_values);
		lp_searchresultsview.setDialogTitle(res.getString(R.string.pleasechoose));
		lp_searchresultsview.setSummary(view_entries[searchresultsview]);
		lp_searchresultsview.setValue(String.valueOf(searchresultsview));
		lp_searchresultsview.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object nv) {
				final String summary = nv.toString();
				ListPreference lp = (ListPreference) p;	
				int newvalue = lp.findIndexOfValue(summary);
				lp.setSummary(view_entries[newvalue]);

				mHelper.saveIntPref(context, "searchresultsview", newvalue);
				
				Toast.makeText(context, getResources().getString(R.string.changesnextsearch), Toast.LENGTH_LONG).show();

				return true;
			}
		});		
		pc_other.addPreference(lp_searchresultsview);
		
		//
		
		int images = mHelper.loadIntPref(context, "images", mHelper.IMAGES_DEFAULT);
		boolean images_checked = false;
		if(images==1)images_checked = true;
		
		CheckBoxPreference cbp_images = new CheckBoxPreference(context);
		cbp_images.setTitle(getString(R.string.images));
		cbp_images.setChecked(images_checked);
		cbp_images.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object v) {

				boolean newvalue = Boolean.parseBoolean(v.toString());
				CheckBoxPreference preference = (CheckBoxPreference)p;
				preference.setChecked(newvalue);

				int nv = 0;
				if(newvalue==true)nv = 1;
				mHelper.saveIntPref(context, "images", nv);

				Toast.makeText(context, getResources().getString(R.string.changesnextsearch), Toast.LENGTH_LONG).show();

				return true;
			}
		});
		root.addPreference(cbp_images);
		
		//
		//
		
		return root;
	}
}
