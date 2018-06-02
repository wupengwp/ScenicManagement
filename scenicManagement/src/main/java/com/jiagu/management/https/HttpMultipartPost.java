package com.jiagu.management.https;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jiagu.management.https.CustomMultipartEntity.ProgressListener;

/** 
* @ClassName: HttpMultipartPost 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:35:04 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/ 
public class HttpMultipartPost extends AsyncTask<String, Integer, String> {

	private Context context;
	private String filePath;
	private String url;
	private ProgressDialog pd;
	private long totalSize;
	private List<NameValuePair> postData;
	private String paramskey;

	private OnUploadExecutedListener listener;

	public HttpMultipartPost(Context context, String url, String filePath) {
		this.context = context;
		this.url = url;
		this.filePath = filePath;
	}

	public HttpMultipartPost(Context context, String url, String filePath,
			List<NameValuePair> postData,String paramskey) {
		this.context = context;
		this.url = url;
		this.filePath = filePath;
		this.postData = postData;
		this.paramskey = paramskey;
	}

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(context);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("Uploading Picture...");
		pd.setCancelable(false);
		pd.show();
	}

	@Override
	protected String doInBackground(String... params) {
		String serverResponse = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();

		HttpPost httpPost = new HttpPost(url);

		try {
			CustomMultipartEntity multipartContent = new CustomMultipartEntity(
					new ProgressListener() {
						@Override
						public void transferred(long num) {
							publishProgress((int) ((num / (float) totalSize) * 100));
						}
					});

			if (postData != null) {
				for (NameValuePair nameValuePair : postData) {
					multipartContent.addPart(
							nameValuePair.getName(),
							new StringBody(nameValuePair.getValue(), Charset
									.forName("utf-8")));
				}
			}
			// We use FileBody to transfer an image
			multipartContent
					.addPart(paramskey, new FileBody(new File(filePath)));
			totalSize = multipartContent.getContentLength();
			// Send it
			httpPost.setEntity(multipartContent);
			HttpResponse response = httpClient.execute(httpPost, httpContext);
			serverResponse = EntityUtils.toString(response.getEntity());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return serverResponse;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		pd.setProgress((int) (progress[0]));
	}

	@Override
	protected void onPostExecute(String result) {
		System.out.println("result: " + result);
		pd.dismiss();
		if (listener != null) {
			listener.OnUploadExecuted(result);
		}
	}

	@Override
	protected void onCancelled() {
		Toast.makeText(context, "Upload Cancel", Toast.LENGTH_SHORT).show();
	}

	public void setOnUploadExecutedListener(OnUploadExecutedListener listener) {
		this.listener = listener;
	}

	public interface OnUploadExecutedListener {
		public void OnUploadExecuted(String result);
	};
}
