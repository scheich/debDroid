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

import com.fedorvlasov.lazylist.ImageLoader;

import de.mangelow.debdroid.R;
import de.mangelow.debdroid.data.SearchResult;
import de.mangelow.debdroid.data.Suite;
import de.mangelow.debdroid.data.Version;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ListViewAdapter extends BaseExpandableListAdapter {

	private final String URL_SCREENSHOTS = "http://screenshots.debian.net/thumbnail/";
	
	private Context context;

	private SearchResult searchresults [];

	private Helper mHelper = new Helper();

	private int[] textsize_values;
	private int textsize = mHelper.TEXTSIZE_DEFAULT;

	private int searchresultsview;
	private String lastpackagename = "";

	private ImageLoader il;
	private int images;
	
	public ListViewAdapter(Context context, SearchResult searchresults [], int searchresultsview) {
		
		this.context = context;
		this.searchresults = searchresults;	
		this.searchresultsview = searchresultsview;

		textsize_values = context.getResources().getIntArray(R.array.textsize_values);
		textsize = mHelper.loadIntPref(context, "textsize", mHelper.TEXTSIZE_DEFAULT);
		images = mHelper.loadIntPref(context, "images", mHelper.IMAGES_DEFAULT);

		if(images>0)il = new ImageLoader(context);
		
	}
	@Override
	public int getGroupCount() {
		return searchresults.length;
	}

	@Override
	public int getChildrenCount(int i) {

		SearchResult r = (SearchResult) searchresults[i];

		if(searchresultsview==mHelper.SEARCHRESULTSVIEW_DEFAULT)return r.getSuites().length;
		return r.getSuites()[0].getVersions().length;
	}

	@Override
	public Object getGroup(int i) {
		return (SearchResult) searchresults[i];
	}

	@Override
	public Object getChild(int i, int i1) {
		return (SearchResult) searchresults[i];
	}

	@Override
	public long getGroupId(int i) {
		return i;
	}

	@Override
	public long getChildId(int i, int i1) {
		return i1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int i, boolean expanded, View parent, ViewGroup viewGroup) {

		SearchResult sr = (SearchResult) getGroup(i);

		String text = "<b>";
		if(images>0)text += "&nbsp;&nbsp;";
		text = sr.getPackagename() + "</b>";
		
		TextView tv = new TextView(context);
		tv.setPadding(20, 10, 0, 10);
		tv.setCompoundDrawablePadding(4);
		tv.setTextSize(18 + textsize_values[textsize]);
		tv.setTextColor(Color.BLACK);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		
		if(searchresultsview!=mHelper.SEARCHRESULTSVIEW_DEFAULT) {
			text += "<br>";

			Suite s = sr.getSuites()[0];

			if(i>0&&lastpackagename.equals(s.getPackagename())) {
				text = "";
				if(images>0)tv.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.empty), null, null, null);
			}
			else {
				lastpackagename = s.getPackagename();
				if(images>0)il.DisplayImage(URL_SCREENSHOTS + sr.getPackagename(), tv);
			}

			text += "&nbsp;&nbsp;<font color=\"blue\">" + s.getAlias() 
					+ " (" + s.getSuite() + ")</font>";
					//+ "<br>&nbsp;&nbsp;(" +  context.getResources().getQuantityString(R.plurals.version, s.getVersions().length, s.getVersions().length)  +  ")";
		}

		tv.setText(Html.fromHtml(text));
		if(searchresultsview==mHelper.SEARCHRESULTSVIEW_DEFAULT&&images>0)il.DisplayImage(URL_SCREENSHOTS + sr.getPackagename(), tv);
		
		return tv;
	}

	@Override
	public View getChildView(int i, int i1, boolean islastchild, View view, ViewGroup viewGroup) {
		
		SearchResult sr = (SearchResult) getChild(i, i1);
		
		Suite s = (Suite) sr.getSuites()[0];
		if(searchresultsview==mHelper.SEARCHRESULTSVIEW_DEFAULT)s = (Suite) sr.getSuites()[i1];

		Version vs [] = s.getVersions();

		String text = "";

		if(searchresultsview==mHelper.SEARCHRESULTSVIEW_DEFAULT) {
			text = "<b><font color=\"blue\">" + s.getAlias() 
					+ " (" + s.getSuite() + ")</font> (" 
					+ s.getCategory() + ")</b><br>" 
					+ s.getDescription();

			for (int j = 0; j < vs.length; j++) {
				Version v = vs[j];

				String [] archs = v.getArchs();
				if(archs!=null) {
					int length_archs = archs.length;

					text += "<br><br><b>" + v.getVersion() + "</b>" + /*" (" +  context.getResources().getQuantityString(R.plurals.arch, length_archs, length_archs)  +  ")" */ "<br>";

					for (int k = 0; k < length_archs; k++) {
						text += archs[k] + " ";
					}
				}
			}
		}
		else {

			Version v = vs[i1];

			String [] archs = v.getArchs();
			if(archs!=null) {
				int length_archs = archs.length;

				text = "<b>" + v.getVersion() + "</b>" + /*" (" +  context.getResources().getQuantityString(R.plurals.arch, length_archs, length_archs)  +  ")" */ "<br>";

				for (int k = 0; k < length_archs; k++) {
					text += archs[k] + " ";
				}
			}

		}
		
		TextView tv = new TextView(context);
		tv.setPadding(30, 6, 0, 6);
		tv.setCompoundDrawablePadding(2);
		if(searchresultsview!=mHelper.SEARCHRESULTSVIEW_DEFAULT) {
			tv.setPadding(40, 8, 0, 8);
			if(images>0)tv.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.empty), null, null, null);
		}
		tv.setTextSize(14 + textsize_values[textsize]);
		tv.setTextColor(Color.DKGRAY);
		tv.setText(Html.fromHtml(text));
		
		return tv;
	}

	@Override
	public boolean isChildSelectable(int i, int i1) {
		return true;
	}
}
