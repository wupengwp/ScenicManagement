package com.jiagu.management.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.adapters.PhotoAdapter;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpMultipartPost;
import com.jiagu.management.https.HttpMultipartPost.OnUploadExecutedListener;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.CustomDialog;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.utils.ImageTools;

/**
 * @ClassName: PictureActivity
 * @Description: 图片选择/上传
 * @author zz
 * @date 2015年1月15日 下午12:31:56
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class PictureActivity extends SuperActivity implements OnClickListener,
		OnItemClickListener, OnUploadExecutedListener {
	private final static int SCAN_OK = 1;

	private GridView mGridView;
	private PhotoAdapter mPhotoAdapter;

	private ArrayList<String> paths = new ArrayList<String>();
	private PopupWindow mMenu;
	private View mPicView;
	private ImageView mPicIv;
	private Bitmap currentBitmap;

	private String mImagePath;
	private int windowWidth = -1;
	private int windowHeight = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pic);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		windowWidth = dm.widthPixels;
		windowHeight = dm.heightPixels;
		showProgress();
		findView();
		initOkPopup();
		getImages();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.choose_picture);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);

		mPicView = findViewById(R.id.dialog_picture_view);
		mPicIv = (ImageView) findViewById(R.id.current_choose_pic);
		mGridView = (GridView) findViewById(R.id.photo_wall);
		mGridView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn:
			onBackPressed();
			break;
		case R.id.popup_ok:
			final CustomDialog.GeneralDialog dialog = new CustomDialog.GeneralDialog(
					this);
			dialog.setMessage("重复上传景区图会导致之前绑定的数据丢失");
			dialog.hideMiddleButton();
			dialog.setOnLeftButtonClickListener(getString(R.string.dialog_ok),
					null, new OnClickListener() {
						@Override
						public void onClick(View v) {
							List<NameValuePair> postData = new ArrayList<NameValuePair>();
							postData.add(new BasicNameValuePair("scenicID",
									User.getScenicid()));
							postData.add(new BasicNameValuePair("widthPixels",
									windowWidth + ""));
							postData.add(new BasicNameValuePair("heightPixels",
									windowHeight + ""));
							
							//压缩景区地图
							String uploadfile = mImagePath;
							File image = new File(mImagePath);
							String filepath = Environment.getExternalStorageDirectory()+"/compress.jpg";
							if (image.length() > 512 * 1024){//图片文件超过1M就进行压缩
								try{
									try{
										File compressfile = new File(filepath);
										compressfile.deleteOnExit();
									}
									catch (Exception e){
										FileTools.writeLog("scenic.txt", e.toString());
									}
									
									int compress = 70;
									if (image.length()>1024 * 1024 && image.length()<2*1024 * 1024){
										compress = 50;
									}
									else if (image.length()>2*1024 * 1024 && image.length()<3*1024 * 1024){
										compress = 35;
									}
									else if(image.length()>3*1024 * 1024&& image.length()<5*1024 * 1024){
										compress = 15;
									}
									else if(image.length()>5*1024 * 1024){
										compress = 5;
									}
									
									//FileTools.writeLog("scenic.txt", "compress:"+compress);
									Bitmap chooseBitmap = BitmapFactory.decodeFile(mImagePath);
									FileOutputStream fos = new java.io.FileOutputStream(filepath);
									chooseBitmap.compress(Bitmap.CompressFormat.JPEG,compress, fos);
									fos.close();
									
									try{
										File compressfile = new File(filepath);
										if (compressfile.length()>0){
											uploadfile = filepath;
										}
									}
									catch (Exception e){
										FileTools.writeLog("scenic.txt", e.toString());
									}
								}
								catch (Exception e){
									FileTools.writeLog("scenic.txt", e.toString());
								}
							}

							HttpMultipartPost post = new HttpMultipartPost(
									PictureActivity.this,
									ScenicApplication.SEVER_PATH + "upload.htm",
									uploadfile, postData, "scenicMap");
							post.setOnUploadExecutedListener(PictureActivity.this);
							post.execute();
							dialog.dismiss();
						}
					});
			dialog.setOnRightButtonClickListener(
					getString(R.string.dialog_cancel), null,
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
			dialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String path = (String) mPhotoAdapter.getItem(position);
		mImagePath = path.substring(path.indexOf("file://") + 7);
		FileTools.writeLog("scenic.txt", "image path:"+mImagePath);
		currentBitmap = BitmapFactory.decodeFile(mImagePath);
		if (!mMenu.isShowing()) {
			mPicIv.setImageBitmap(currentBitmap);
			mPicView.setVisibility(View.VISIBLE);
			mMenu.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0,
					0);
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				// 关闭进度条
				hideProgress();
				mPhotoAdapter = new PhotoAdapter(PictureActivity.this, paths);
				mGridView.setAdapter(mPhotoAdapter);
				break;
			}
		}
	};

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}

		// 显示进度条
		showProgress();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = PictureActivity.this
						.getContentResolver();

				// 只查询jpeg和png的图片
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED);

				while (mCursor.moveToNext()) {
					// 获取图片的路径
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					Log.e("img_path============>", path);
					paths.add("file://" + path);

				}
				mCursor.close();

				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(SCAN_OK);

			}
		}).start();
	}

	private void initOkPopup() {
		View view = LayoutInflater.from(this).inflate(R.layout.popup_ok, null);
		mMenu = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mMenu.setAnimationStyle(R.style.pop_anim_style);
		mMenu.setFocusable(true);
		mMenu.setTouchable(true);
		mMenu.setBackgroundDrawable(new BitmapDrawable());
		mMenu.setOutsideTouchable(true);
		view.setFocusableInTouchMode(true);
		view.findViewById(R.id.popup_ok).setOnClickListener(this);

		mMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mPicView.setVisibility(View.GONE);

				if (currentBitmap != null && !currentBitmap.isRecycled()) {
					currentBitmap.recycle();
					currentBitmap = null;
				}
			}
		});
	}

	@Override
	public void OnUploadExecuted(String result) {
		try {
			JSONObject root = new JSONObject(result);
			if (Constants.SUCCESS.equals(root.optString("result"))) {
				String filePath = ScenicApplication.RootPath + "map/"
						+ User.getScenicid() + ".jpg";
				File file = new File(filePath);
				if (file.exists()) {
					file.delete();
				}
				
				String filepath = Environment.getExternalStorageDirectory()+"/compress.jpg";				
				try{
					File compressfile = new File(filepath);
					
					if (compressfile.length() > 0){
						copyFile(filepath, ScenicApplication.RootPath + "map/"+User.getScenicid() + ".jpg");
					}
					else{
						ImageTools.savePhotoToSDCard2(currentBitmap, ScenicApplication.RootPath + "map/", User.getScenicid() + ".jpg");
					}
				}
				catch (Exception e){
					FileTools.writeLog("scenic.txt", e.toString());
				}
						
				
				//ImageTools.savePhotoToSDCard2(currentBitmap, ScenicApplication.RootPath + "map/", User.getScenicid() + ".jpg");
				//ImageIO.read(file2);

				Intent data = new Intent();
				//data.putExtra(Constants.IAMGE_PATH, mImagePath);
				data.putExtra(Constants.IAMGE_PATH, ScenicApplication.RootPath + "map/"+User.getScenicid() + ".jpg");
				setResult(ChoosePicActivity.ACTIVITY_RESULTCODE, data);
				onBackPressed();
			} else {
				showToast(R.string.upload_map_failure_prompt);
			}
		} catch (JSONException e) {
			System.out.println(e);
			showToast(R.string.upload_map_failure_prompt);
		}
	}
	
	/**  
     * 复制单个文件  
     * @param oldPath String 原文件路径 如：c:/fqf.txt  
     * @param newPath String 复制后路径 如：f:/fqf.txt  
     * @return boolean  
     */   
   public void copyFile(String oldPath, String newPath) {   
       try {   
           int bytesum = 0;   
           int byteread = 0;   
           File oldfile = new File(oldPath);   
           if (oldfile.exists()) { //文件不存在时   
               InputStream inStream = new FileInputStream(oldPath); //读入原文件   
               FileOutputStream fs = new FileOutputStream(newPath);   
               byte[] buffer = new byte[1444];   
               int length;   
               while ( (byteread = inStream.read(buffer)) != -1) {   
                   bytesum += byteread; //字节数 文件大小   
                   System.out.println(bytesum);   
                   fs.write(buffer, 0, byteread);   
               }   
               inStream.close();   
           }   
       }   
       catch (Exception e) {   
           System.out.println("复制单个文件操作出错");   
           e.printStackTrace();   
  
       }   
  
   }   
}
