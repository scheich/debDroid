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
package de.mangelow.debdroid.data;

public class Suite {

	String packagename;

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}
	public String getPackagename() {
		return packagename;
	}
	
	String suite;

	public void setSuite(String suite) {
		this.suite = suite;
	}
	public String getSuite() {
		return suite;
	}
	
	String alias;

	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getAlias() {
		return alias;
	}

	String category;

	public void setCategory(String category) {
		this.category = category;
	}
	public String getCategory() {
		return category;
	}

	String description;

	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}

	Version versions [];

	public void setVersions(Version versions []) {
		this.versions = versions;
	}
	public Version [] getVersions() {
		return versions;
	}
}
