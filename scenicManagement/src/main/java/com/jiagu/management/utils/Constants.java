package com.jiagu.management.utils;

/** 
* @ClassName: Constants 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:35:38 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/ 
public interface Constants {
	public static final int GETCODING = 1;
	public static final int GETCODED = 0;

	public static final String FILE_NAME = "file_name";
	public static final String FILE_TYPE = "file_type";
	public static final String FILE_ICON = "file_icon";

	public static final String IAMGE_PATH = "iamge_path";

	public static final String OS = "1";
	public static final String APK = "1";

	public static final int WAITING_TIME = 90;

	public static final String MARKER_LOUCS_KEY = "MARKER_LOUCS_KEY";

	public static final String SIZE_KEY = "SIZE_KEY";
	public static final String WIDTHKEY = "widthPixels";
	public static final String HEIGHTKEY = "heightPixels";
	

	/**
	 * 密码最小长度
	 */
	public static final int PASSWORD_LEN_MIN = 6;

	/**
	 * result 0：表示成功
	 */
	public static final String SUCCESS = "0";
	/**
	 * result 1：空值
	 */
	public static final String NULL = "1";

	/**
	 * result 2:错误
	 */
	public static final String ERROR = "2";

	/**
	 * result 3:更新异常
	 */
	public static final String UPDATEXCEPTION = "3";

	/**
	 * result 4:查询异常
	 */
	public static final String SELECTEXCEPTION = "4";

	/**
	 * result 5:数据重复添加
	 */
	public static final String ISREPEATED = "5";
	
	/**
	 * result 11:景区没有景点记录
	 */
	public static final String NOATTRACT = "11";
}
