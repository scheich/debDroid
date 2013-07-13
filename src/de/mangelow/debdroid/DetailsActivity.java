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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.mangelow.debdroid.fragments.DetailsFragment;
import de.mangelow.debdroid.tools.Helper;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DetailsActivity extends SherlockFragmentActivity {

	private ActionBar ab;

	private Context context;
	
	private Helper mHelper = new Helper();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.context = getApplicationContext();
		
		ab = getSupportActionBar();
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		
		getSupportFragmentManager().beginTransaction().add(android.R.id.content, new DetailsFragment()).commit();
		
	}	
	@Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	
		Menu m_menu = menu;
		menu.clear();

		m_menu.add(Menu.NONE, Menu.FIRST + 1, Menu.FIRST + 1, getString(R.string.share)).setIcon(R.drawable.ic_action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

	    return super.onCreateOptionsMenu(menu);
	  }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case Menu.FIRST + 1:
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
}
