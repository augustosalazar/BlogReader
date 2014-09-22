package com.uninorte.blogreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.uninorte.blogreader.R.id;

public class MainListActivity extends ListActivity {

	protected String[] mBlogPost;
	public static final int NUMBER_OF_POST = 20;
	public static final String TAG = MainListActivity.class.getSimpleName();
	protected JSONObject mBlogData;
	protected ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		

		// this is how we fetch the strings_lists from the string.xml
		// Resources resources = getResources();
		// mBlogPost = resources.getStringArray(R.array.android_names);
		// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, mBlogPost);
		// setListAdapter(adapter);

		// this is how we fetch strings from the string.xml
		// String label = getString(R.string.saveLabel);

		if (isNetworkAvailable()) {
			mProgressBar.setVisibility(View.VISIBLE);
			GetBlogTask getBlogTask = new GetBlogTask();
			getBlogTask.execute();
		}

	}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		boolean isNetworkAvaible = false;
		if (networkInfo != null && networkInfo.isConnected()) {
			isNetworkAvaible = true;
			Toast.makeText(this, "Network is available ", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(this, "Network not available ", Toast.LENGTH_LONG)
					.show();
		}
		return isNetworkAvaible;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
		if (mBlogData == null){
			updateDisplayForError();
		} else {
			try {
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
				ArrayList<HashMap<String, String>> blogPosts = new ArrayList<HashMap<String,String>>();
				for (int i = 0;i< jsonPosts.length();i++){
					JSONObject post = jsonPosts.getJSONObject(i);
					String title = post.getString("title");
					title  = Html.fromHtml(title).toString();
					String author = post.getString("author");
					author  = Html.fromHtml(author).toString();
					
					HashMap<String, String > blogPost = new HashMap<String, String>();
					blogPost.put("title", title);
					blogPost.put("author", author);
					blogPosts.add(blogPost);
					
					
				}
				String[] keys = {"title","author"};
				int ids[] = {android.R.id.text1,android.R.id.text2};
				SimpleAdapter adapter = new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, 
						keys, ids);
				setListAdapter(adapter);
			} catch (JSONException e) {
				Log.e(TAG,"Exception caught!",e);
			}
		}
	}

	private void updateDisplayForError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.erro_titile);
		builder.setMessage(R.string.error_msg);
		builder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
		
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText(getString(R.string.no_items));
	}

	public class GetBlogTask extends AsyncTask<Object, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Object... params) {
			int responseCode = -1;
			JSONObject jsonResponse = null;
			try {
				URL blogFeedUsr = new URL(
						"http://blog.teamtreehouse.com/api/get_recent_summary/?count="
								+ NUMBER_OF_POST);
				HttpURLConnection connection = (HttpURLConnection) blogFeedUsr
						.openConnection();
				connection.connect();

				responseCode = connection.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream inputStram = connection.getInputStream();
					Reader reader = new InputStreamReader(inputStram);
					char[] charArray = new char[connection.getContentLength()];
					reader.read(charArray);
					String responseData = new String(charArray);
					// Log.v(TAG,responseData);
					jsonResponse = new JSONObject(responseData);
					// String response = jsonResponse.getString("status");
					// Log.v(TAG,response);
					// JSONArray jsonPosts = jsonResponse.getJSONArray("posts");
					// for (int i= 0;i<jsonPosts.length();i++){
					// JSONObject jsonPost = jsonPosts.getJSONObject(i);
					// //Log.v(TAG,"Post "+ i +
					// " title ="+jsonPost.getString("title"));
					// }
				} else {
					Log.i(TAG,
							"Response code unsuccesfull "
									+ String.valueOf(responseCode));
				}
			} catch (MalformedURLException e) {
				Log.e(TAG, "Exception", e);
			} catch (IOException e) {
				Log.e(TAG, "Exception", e);
			} catch (Exception e) {
				Log.e(TAG, "Exception", e);
			}
			return jsonResponse;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			mBlogData = result;
			handleBlogResponse();
		}

	}
}
