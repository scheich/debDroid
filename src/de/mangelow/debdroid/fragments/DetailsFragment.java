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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import de.mangelow.debdroid.R;
import de.mangelow.debdroid.tools.Helper;

public class DetailsFragment extends SherlockFragment {

	private final String TAG	= "dD."+getClass().getSimpleName();
	private final boolean D = false;
	
	private final String DEBIAN_PACKAGES_URL = "http://packages.debian.org/";

	private View v;
	private WebView wv;
	private ProgressBar pb;
	private TextView tv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.fragment_details, container, false);

		String url = DEBIAN_PACKAGES_URL;
		
		try {
			url = DEBIAN_PACKAGES_URL + Helper.selected_suite.getSuite() + "/" + Helper.selected_suite.getPackagename();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(D)Log.d(TAG, "url - " + url);				

		wv = (WebView) v.findViewById(R.id.wv);
		pb = (ProgressBar) v.findViewById(R.id.pb);
		tv = (TextView) v.findViewById(R.id.tv);

		wv.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				pb.setVisibility(View.GONE);
				tv.setVisibility(View.GONE);
				wv.setVisibility(View.VISIBLE);
			}
		});
		wv.getSettings().setSupportZoom(true);  
		wv.getSettings().setBuiltInZoomControls(true);
		wv.loadUrl(url);

		return v;

	}

}
