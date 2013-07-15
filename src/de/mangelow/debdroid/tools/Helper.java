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
package de.mangelow.debdroid.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import de.mangelow.debdroid.R;
import de.mangelow.debdroid.data.SearchResult;
import de.mangelow.debdroid.data.Suite;
import de.mangelow.debdroid.data.Version;

public class Helper {

	private final String TAG	= "dD."+getClass().getSimpleName();
	private final boolean D = false;

	private final String DEBIAN_PACKAGESEARCH_URL = "http://packages.debian.org/search?";

	private final String [] SEARCHON  = { "names", "all" };
	public final int SEARCHON_DEFAULT  = 0;

	public final int EXACT_DEFAULT  = 0;

	public final String [] SUITE  = { "stable", "testing", "unstable", "experimental", "oldstable" };
	public final int SUITE_DEFAULT  = 1;

	public final String [] SECTION  = { "main", "contrib", "non-free"};
	public final int SECTION_DEFAULT  = 1;

	public final String ACTION_CHECK_PACKAGES = "ACTION_CHECK_PACKAGES";
	public final int CHECKPACKAGES_DEFAULT  = 0;

	public final int TEXTSIZE_DEFAULT  = 1;
	public final int SEARCHRESULTSVIEW_DEFAULT  = 0;
	public final int IMAGES_DEFAULT  = 0;

	private final String PREF_FILE = "Prefs";

	public static Suite selected_suite;
	public static Version selected_version;

	public SearchResult [] parseSearchResults(String html, int searchresultsview) {

		String packages_pattern = "<h3>(.*?)</ul>";
		ArrayList<String> al_temp_searchresults = getContent(packages_pattern,html);
		int length_searchresults = al_temp_searchresults.size();

		SearchResult searchresults [] = new SearchResult[length_searchresults];

		ArrayList<Suite> al_suites = new ArrayList<Suite>();

		for (int i = 0; i < length_searchresults; i++) {

			SearchResult searchresult = new SearchResult();

			String temp_package = al_temp_searchresults.get(i);

			String packagename_pattern = "Package (.*?)</h3>";
			ArrayList<String> al_temp_packagename = getContent(packagename_pattern,temp_package);
			String packagename = "";
			if(al_temp_packagename.size()>0)packagename = al_temp_packagename.get(0).trim();
			searchresult.setPackagename(packagename);

			String suites_pattern = "<li[^>]*>(.*?)</li>";
			ArrayList<String> al_temp_suites = getContent(suites_pattern,temp_package);
			int length_suites = al_temp_suites.size();

			Suite suites [] = new Suite[length_suites];

			for (int j = 0; j < length_suites; j++) {

				Suite s = new Suite();

				s.setPackagename(packagename);

				String temp_suite = al_temp_suites.get(j);

				String suitename_pattern = "<a class[^>]*>(.*?)</a>";
				ArrayList<String> al_temp_suite = getContent(suitename_pattern,temp_suite);
				String suitename = "";
				String aliasname = "";
				if(al_temp_suite.size()>0) {
					String temp_split [] = al_temp_suite.get(0).trim().split(" ");
					if(temp_split.length>0) {
						aliasname = temp_split[0];
						s.setAlias(aliasname);
					}
					if(temp_split.length>1) {
						suitename = temp_split[1].replaceAll("\\(", "").replaceAll("\\)", "");
						s.setSuite(suitename);
					}
				}

				String category_pattern = "</a> \\((.*?)\\):";
				ArrayList<String> al_temp_category = getContent(category_pattern,temp_suite);
				String category = "";
				if(al_temp_category.size()>0)category = al_temp_category.get(0).trim();				
				s.setCategory(category);

				String description_pattern = "\\):(.*?)<br>";
				ArrayList<String> al_temp_description = getContent(description_pattern,temp_suite);
				String description = "";
				if(al_temp_description.size()>0)description = al_temp_description.get(0).trim();				
				s.setDescription(description);

				String temp_versions [] = temp_suite.split("<br>");
				int length_temp_versions = temp_versions.length;

				ArrayList<Version> al_temp_versions = new ArrayList<Version>();

				for (int k = 0; k < length_temp_versions; k++) {

					String temp_version =  temp_versions[k];
					temp_version = temp_version.replaceAll("<a [^>]*>(.*?)\">","");
					temp_version = temp_version.replaceAll("</strong></a>", "");

					if(!temp_version.contains("also provided by")&&!temp_version.contains("<a class=\"resultlink\"")) {
						String versionname = "";
						String temp_archs = "";

						Version version = new Version();

						String temp_versionname_archs [] = temp_version.split(": ");
						if(temp_versionname_archs.length>0) {
							versionname = temp_versionname_archs[0].trim();
							version.setVersion(versionname);
							if(temp_versionname_archs.length>1) {
								temp_archs = temp_versionname_archs[1];		
								String archs [] = temp_archs.split(" ");
								version.setArchs(archs);					
							}
						}

						if(temp_archs.trim().length()>0)al_temp_versions.add(version);
					}
				}

				int length_temp_al_versions = al_temp_versions.size();				

				Version versions [] = new Version[length_temp_al_versions];
				for (int k = 0; k < length_temp_al_versions; k++) {
					versions[k] = al_temp_versions.get(k);
				}

				s.setVersions(versions);
				suites[j] = s;
				if(searchresultsview!=SEARCHRESULTSVIEW_DEFAULT)al_suites.add(s);
			}

			searchresult.setSuites(suites);
			searchresults[i] = searchresult;
		}

		if(searchresultsview!=SEARCHRESULTSVIEW_DEFAULT) {
			int length_al_suites = al_suites.size();

			searchresults  = new SearchResult[length_al_suites];

			for (int k = 0; k < length_al_suites; k++) {

				SearchResult sr = new SearchResult();
				Suite s = al_suites.get(k);
				sr.setPackagename(s.getPackagename());

				sr.setSuites(new Suite [] { s });

				searchresults[k] = sr;
			}

		}
		return searchresults;

	}
	public String getSearchResults(Context context, String keywords) {

		String url = DEBIAN_PACKAGESEARCH_URL;

		int searchon = loadIntPref(context, "searchon", SEARCHON_DEFAULT);
		url += "searchon=" + SEARCHON[searchon];

		url += "&suite=";
		int length = SUITE.length;
		for (int i = 0; i < length; i++) {
			int value = loadIntPref(context, "suite_" + SUITE[i], SUITE_DEFAULT);
			if(value==1)url +=SUITE[i];
			if(i<length-1)url +=",";
		}

		url += "&section=";
		length = SECTION.length;
		for (int i = 0; i < length; i++) {
			int value = loadIntPref(context, "section_" + SECTION[i], SECTION_DEFAULT);
			if(value==1)url +=SECTION[i];
			if(i<length-1)url +=",";
		}

		int exact = loadIntPref(context, "exact", SEARCHON_DEFAULT);
		url += "&exact=" + exact;


		url += "&keywords=" + keywords;

		if(D)Log.d(TAG, "url - " + url);				

		return downloadHTML(url);

	}	
	private String downloadHTML(String url) {

		try {
			URL website = new URL(url);
			URLConnection connection = website.openConnection();
			connection.setRequestProperty("Accept-Language", Locale.getDefault().toString().replace("_", "-"));
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String inputLine;

			while ((inputLine = in.readLine()) != null)response.append(inputLine);

			in.close();

			return response.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	private ArrayList<String> getContent(String pattern, String html) {

		ArrayList<String> temp = new ArrayList<String>();

		Pattern patt = Pattern.compile(pattern, Pattern.DOTALL);
		Matcher m = patt.matcher(html);
		while (m.find()) {
			String mgroup = m.group(1);
			if(mgroup.length()>0)temp.add(mgroup);
		}
		return temp;

	}

	//

	public boolean isTabletAndLandscape(Context context) {
		boolean isTablet = (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				>= Configuration.SCREENLAYOUT_SIZE_LARGE;
				int orientation = context.getResources().getConfiguration().orientation;
				if(isTablet&&orientation==Configuration.ORIENTATION_LANDSCAPE)return true;

				return false;
	}
	public String getShareBody(Context context) {

		if(selected_suite!=null) {
			Version versions[] = selected_suite.getVersions();
			int length_versions = versions.length;

			String body = selected_suite.getPackagename() + " " + context.getString(R.string.from) + " " + selected_suite.getAlias() + " (" + selected_suite.getSuite() + ")" + " \n\n";
			if(selected_version!=null) {				
				String [] archs = selected_version.getArchs();
				if(archs!=null) {
					int length_archs = archs.length;

					body += selected_version.getVersion() + " (" + context.getResources().getQuantityString(R.plurals.arch, length_archs, length_archs) + ")\n";

					for (int k = 0; k < length_archs; k++) {
						body += archs[k] + " ";
					}
				}
			}
			else {
				
				body += context.getResources().getQuantityString(R.plurals.version, selected_suite.getVersions().length, selected_suite.getVersions().length) + ":\n\n";

				for (int i = 0; i < length_versions; i++) {
					Version v = versions[i];

					String [] archs = v.getArchs();
					if(archs!=null) {
						int length_archs = archs.length;

						body += v.getVersion() + " (" + context.getResources().getQuantityString(R.plurals.arch, length_archs, length_archs) + ")\n";

						for (int k = 0; k < length_archs; k++) {
							body += archs[k] + " ";
						}
					}

					if(i<length_versions-1)body += "\n\n";

				}
			}
			
			selected_suite = null;
			selected_version = null;

			return body;

		}
		return null;
	}
	//

	@SuppressWarnings("deprecation")
	public long setRTCAlarm(Context context, int checkpackages) {

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		//
		//
		//		c.set(Calendar.HOUR_OF_DAY, 0);
		//		c.set(Calendar.MINUTE, 0);
		//		c.set(Calendar.SECOND, 0);

		switch (checkpackages) {

		case 1: 
			c.add(Calendar.DAY_OF_YEAR,1);
			break;
		case 2: 	
			int weekday = c.get(Calendar.DAY_OF_WEEK);  
			int days = 7;
			if (weekday != Calendar.MONDAY)	{
				days = (Calendar.SATURDAY - weekday + 2) % 7;
			}
			c.add(Calendar.DAY_OF_YEAR, days);

			break;
		case 3: 
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.MONTH, 1);
			break;
		case 4: // Test
			c.add(Calendar.SECOND, 15);
			break;
		}


		Intent active = new Intent(context, Receiver.class);
		active.setAction(ACTION_CHECK_PACKAGES);

		PendingIntent sender = PendingIntent.getBroadcast(context, 1234, active, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		if(checkpackages==0) {
			am.cancel(sender);
			if(D)Log.d(TAG, "Alarm cancelled");

			return 0;
		}
		else {
			am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
			if(D)Log.d(TAG, "Alarm set to " + new Date(c.getTimeInMillis()).toLocaleString());

			return c.getTimeInMillis();
		}
	}

	//

	public void saveIntPref(Context context, String name, int value) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
		prefs.putInt(name, value);
		prefs.commit();
	}
	public int loadIntPref(Context context, String name, int defaultvalue) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
		int pref = prefs.getInt(name, defaultvalue);
		return pref;
	}

	public void saveStringPref(Context context, String name, String value) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
		prefs.putString(name, value);
		prefs.commit();
	}
	public String loadStringPref(Context context, String name, String defaultvalue) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
		String pref = prefs.getString(name, defaultvalue);
		return pref;
	}
}
