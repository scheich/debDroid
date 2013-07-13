/***
*
* (The MIT License)
* 
* Copyright (c) 2009-2012 Fedor Vlasov <thest2@gmail.com>
* 
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* 'Software'), to deal in the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to
* the following conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
*/
package com.fedorvlasov.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.mangelow.debdroid.R;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class ImageLoader {

	MemoryCache memoryCache=new MemoryCache();
	FileCache fileCache;
	private Map<TextView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<TextView, String>());
	ExecutorService executorService;
	Handler handler=new Handler();//handler to display images in UI thread

	private Context context;

	public ImageLoader(Context context){

		this.context = context;

		fileCache=new FileCache(context);
		executorService=Executors.newFixedThreadPool(5);
	}

	final int stub_id=R.drawable.stub;
	public void DisplayImage(String url, TextView textView)
	{
		imageViews.put(textView, url);
		Bitmap bitmap=memoryCache.get(url);
		if(bitmap!=null) {
			
			@SuppressWarnings("deprecation")
			Drawable drawable = new BitmapDrawable(getResizedBitmap(bitmap, 50, 50)); 
			textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
		}
		else
		{
			queuePhoto(url, textView);
			textView.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(stub_id), null, null, null);
		}
	}

	private void queuePhoto(String url, TextView textView)
	{
		PhotoToLoad p=new PhotoToLoad(url, textView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) 
	{
		File f=fileCache.getFile(url);

		//from SD cache
		Bitmap b = decodeFile(f);
		if(b!=null)
			return b;

		//from web
		try {
			Bitmap bitmap=null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is=conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Throwable ex){
			ex.printStackTrace();
			if(ex instanceof OutOfMemoryError)
				memoryCache.clear();
			return null;
		}
	}

	//decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f){
		try {
			//decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1=new FileInputStream(f);
			BitmapFactory.decodeStream(stream1,null,o);
			stream1.close();

			//Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE=70;
			int width_tmp=o.outWidth, height_tmp=o.outHeight;
			int scale=1;
			while(true){
				if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
					break;
				width_tmp/=2;
				height_tmp/=2;
				scale*=2;
			}

			//decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize=scale;
			FileInputStream stream2=new FileInputStream(f);
			Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	//Task for the queue
	private class PhotoToLoad
	{
		public String url;
		public TextView textView;
		public PhotoToLoad(String u, TextView i){
			url=u; 
			textView=i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;
		PhotosLoader(PhotoToLoad photoToLoad){
			this.photoToLoad=photoToLoad;
		}

		@Override
		public void run() {
			try{
				if(imageViewReused(photoToLoad))
					return;
				Bitmap bmp=getBitmap(photoToLoad.url);
				memoryCache.put(photoToLoad.url, bmp);
				if(imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			}catch(Throwable th){
				th.printStackTrace();
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad){
		String tag=imageViews.get(photoToLoad.textView);
		if(tag==null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	//Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable
	{
		Bitmap bitmap;
		PhotoToLoad photoToLoad;
		public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
		public void run()
		{
			if(imageViewReused(photoToLoad))
				return;

			if(bitmap!=null) {
				
				Drawable drawable = new BitmapDrawable(getResizedBitmap(bitmap, 50, 50)); 

				photoToLoad.textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			}
			else
				photoToLoad.textView.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(stub_id), null, null, null);

		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();

		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;

		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation

		Matrix matrix = new Matrix();

		// resize the bit map

		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

		return resizedBitmap;

	}

}
