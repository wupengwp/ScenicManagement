package com.jiagu.management.models;

import android.content.SharedPreferences;

import com.jiagu.management.application.ScenicApplication;

/** 
* @ClassName: User 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:35:24 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/ 
public class User {
	/**
	 * SharedPreferences Key Constants
	 **/
	public static final String USERINFO_KEY = "user_config";

	public static final String PHONENUMBER_KEY = "phonenumber";
	public static final String PHOTOURL_KEY = "photourl";
	public static final String TOURGRADE_KEY = "tourGrade";
	public static final String IDCARD_KEY = "idcard";
	public static final String LASTLOGININFO_KEY = "lastlogininfo";
	public static final String PASS_KEY = "pass";
	public static final String ID_KEY = "id";
	public static final String UNIT_KEY = "unit";
	public static final String MOBILEPHONE_KEY = "mobilephone";
	public static final String ADRESS_KEY = "adress";
	public static final String USERNAME_KEY = "username";
	public static final String AREA_KEY = "area";
	public static final String EMAIL_KEY = "email";
	public static final String TOURICON_KEY = "tourIcon";
	public static final String SCENICID_KEY = "scenicid";
	public static final String SCENICNAME_KEY = "scenicName";
	public static final String SCENICUSERID_KEY = "scenicuserid";
	public static final String USER_KEY = "user";

	/** 电话号码 **/
	public static String phonenumber;

	/** 图片地址（作用不详） **/
	public static String photourl;

	/** 用户级别 **/
	public static String tourGrade;

	/** 身份证 **/
	public static String idcard;

	/** 最近登录信息 **/
	public static String lastlogininfo;

	/** 密码 **/
	public static String pass;

	/** 用户ID **/
	public static String id;

	/** 公司 **/
	public static String unit;

	/** 手机号 **/
	public static String mobilephone;

	/** 地址 **/
	public static String adress;

	/** 用户名 显示 **/
	public static String username;

	/** 区域 **/
	public static String area;

	/** 邮箱 **/
	public static String email;

	/** 头像 **/
	public static String tourIcon;

	/** 景区ID **/
	public static String scenicid;

	/** 景区名称 **/
	public static String scenicName;

	/** 景区用户ID **/
	public static String scenicuserid;

	/** 用户名 登录 **/
	public static String user;

	public static void save() {
		SharedPreferences.Editor editor = ScenicApplication
				.getUserPreferences().edit();
		editor.putString(PHONENUMBER_KEY, phonenumber);
		editor.putString(PHOTOURL_KEY, photourl);
		editor.putString(TOURGRADE_KEY, tourGrade);
		editor.putString(IDCARD_KEY, idcard);
		editor.putString(LASTLOGININFO_KEY, lastlogininfo);
		editor.putString(PASS_KEY, pass);
		editor.putString(ID_KEY, id);
		editor.putString(UNIT_KEY, unit);
		editor.putString(MOBILEPHONE_KEY, mobilephone);
		editor.putString(ADRESS_KEY, adress);
		editor.putString(USERNAME_KEY, username);
		editor.putString(AREA_KEY, area);
		editor.putString(EMAIL_KEY, email);
		editor.putString(TOURICON_KEY, tourIcon);
		editor.putString(SCENICID_KEY, scenicid);
		editor.putString(SCENICNAME_KEY, scenicName);
		editor.putString(SCENICUSERID_KEY, scenicuserid);
		editor.putString(USER_KEY, user);
		editor.commit();
	}

	public static String getPhonenumber() {
		if (phonenumber == null || "".equals(phonenumber)) {
			phonenumber = ScenicApplication.getUserPreferences().getString(
					PHONENUMBER_KEY, "");
		}
		return phonenumber;
	}

	public static String getPhotourl() {
		if (photourl == null || "".equals(photourl)) {
			photourl = ScenicApplication.getUserPreferences().getString(
					PHOTOURL_KEY, "");
		}
		return photourl;
	}

	public static String getTourGrade() {
		if (tourGrade == null || "".equals(tourGrade)) {
			tourGrade = ScenicApplication.getUserPreferences().getString(
					TOURGRADE_KEY, "");
		}
		return tourGrade;
	}

	public static String getIdcard() {
		if (idcard == null || "".equals(idcard)) {
			idcard = ScenicApplication.getUserPreferences().getString(
					IDCARD_KEY, "");
		}
		return idcard;
	}

	public static String getLastlogininfo() {
		if (lastlogininfo == null || "".equals(lastlogininfo)) {
			lastlogininfo = ScenicApplication.getUserPreferences().getString(
					LASTLOGININFO_KEY, "");
		}
		return lastlogininfo;
	}

	public static String getPass() {
		if (pass == null || "".equals(pass)) {
			pass = ScenicApplication.getUserPreferences().getString(PASS_KEY,
					"");
		}
		return pass;
	}

	public static String getId() {
		if (id == null || "".equals(id)) {
			id = ScenicApplication.getUserPreferences().getString(ID_KEY, "");
		}
		return id;
	}

	public static String getUnit() {
		if (unit == null || "".equals(unit)) {
			unit = ScenicApplication.getUserPreferences().getString(UNIT_KEY,
					"");
		}
		return unit;
	}

	public static String getMobilephone() {
		if (mobilephone == null || "".equals(mobilephone)) {
			mobilephone = ScenicApplication.getUserPreferences().getString(
					MOBILEPHONE_KEY, "");
		}
		return mobilephone;
	}

	public static String getAdress() {
		if (adress == null || "".equals(adress)) {
			adress = ScenicApplication.getUserPreferences().getString(
					ADRESS_KEY, "");
		}
		return adress;
	}

	public static String getUsername() {
		if (username == null || "".equals(username)) {
			username = ScenicApplication.getUserPreferences().getString(
					USERNAME_KEY, "");
		}
		return username;
	}

	public static String getArea() {
		if (area == null || "".equals(area)) {
			area = ScenicApplication.getUserPreferences().getString(AREA_KEY,
					"");
		}
		return area;
	}

	public static String getEmail() {
		if (email == null || "".equals(email)) {
			email = ScenicApplication.getUserPreferences().getString(EMAIL_KEY,
					"");
		}
		return email;
	}

	public static String getTourIcon() {
		if (tourIcon == null || "".equals(tourIcon)) {
			tourIcon = ScenicApplication.getUserPreferences().getString(
					TOURICON_KEY, "");
		}
		return tourIcon;
	}

	public static String getScenicid() {
		if (scenicid == null || "".equals(scenicid)) {
			scenicid = ScenicApplication.getUserPreferences().getString(
					SCENICID_KEY, "");
		}
		return scenicid;
	}

	public static String getScenicName() {
		if (scenicName == null || "".equals(scenicName)) {
			scenicName = ScenicApplication.getUserPreferences().getString(
					SCENICNAME_KEY, "");
		}
		return scenicName;
	}

	public static String getScenicuserid() {
		if (scenicuserid == null || "".equals(scenicuserid)) {
			scenicuserid = ScenicApplication.getUserPreferences().getString(
					SCENICUSERID_KEY, "");
		}
		return scenicuserid;
	}

	public static String getUser() {
		if (user == null || "".equals(user)) {
			user = ScenicApplication.getUserPreferences().getString(USER_KEY,
					"");
		}
		return user;
	}

	public static void setUsername(String username) {
		ScenicApplication.getUserPreferences().edit()
				.putString(USERNAME_KEY, username);
		User.username = username;
	}

	public static void setMobilephone(String mobilephone) {
		ScenicApplication.getUserPreferences().edit()
				.putString(MOBILEPHONE_KEY, mobilephone);
		User.mobilephone = mobilephone;
	}

	public static void setTourIcon(String tourIcon) {
		ScenicApplication.getUserPreferences().edit()
				.putString(TOURICON_KEY, tourIcon);
		User.tourIcon = tourIcon;
	}
}
