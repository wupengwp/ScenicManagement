package com.jiagu.management.models;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** 
* @ClassName: Attract 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:35:19 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/ 
public class Attract implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int SPOTMARKING = 1;//点
	public static final int REGIONMARKING = 2;//区域
	public static final int PATHMARKING = 3;//路

	public String id = "";
	public String name;
	public String date;
	public int type;
	public double x;
	public double y;
	public ArrayList<Location> locations;

	public boolean isChoose = false;
	public boolean isBind = false;

	public Attract() {
		locations = new ArrayList<Attract.Location>();
	}

	public Attract(JSONObject attract) throws JSONException {
		this.id = attract.optString("attracID");
		this.name = attract.optString("attractName");
		this.date = attract.optString("attractDate");
		this.type = Integer.parseInt(attract.optString("attractType"));
		this.x = Double.parseDouble(attract.optString("attractX"));
		this.y = Double.parseDouble(attract.optString("attractY"));
		
		if ((x==-99999) && (y==-99999)){
			this.isBind = false;
		}
		else{
			this.isBind = true;
		}
		
		JSONArray array = attract.optJSONArray("attractLocation");
		if (array.length() == 0) {
			return;
		} else {
			locations = new ArrayList<Attract.Location>();
			for (int i = 0; i < array.length(); i++) {
				JSONObject location = array.optJSONObject(i);
				double lon = Double.parseDouble(location.optString("longitude"));
				double lat = Double.parseDouble(location.optString("latitude"));		    
				int    markerFlg =  location.getInt("isMarker");
				boolean isMarker = markerFlg==0?false:true;
				this.locations.add(new Location(lon, lat, isMarker));
//				this.locations.add(new Location(lon, lat));
			}
		}
	}

	public static class Location implements Serializable {
		private static final long serialVersionUID = 1L;

		public double lon;//定位的经度
		public double lat;//定位的纬度
		public boolean isMarker;//是否需要在地图上marker

		public Location(double lon, double lat, boolean isMarker) {
			this.lon = lon;
			this.lat = lat;
			this.isMarker = isMarker;
		}
		
		public Location(double lon, double lat){
			this.lon = lon;
			this.lat = lat;
			this.isMarker = true;
		}
	}

	public String getJSONLocations() {
		StringBuilder json = new StringBuilder();
		json.append("[");
		if (locations.size() > 0) {
			for (Location l : locations) {
				json.append("{\"longitude\":\"" + l.lon + "\",\"latitude\":\""
						+ l.lat + "\",\"isMarker\":\""+(l.isMarker?1:0)+"\"},");
			}
			json.deleteCharAt(json.length() - 1);
		}
		json.append("]");
		return json.toString();
	}

	public String getInfoLocations() {
		StringBuilder info = new StringBuilder();
		if (locations.size() > 0) {
			for (Location l : locations) {
				if (l.isMarker == true){
					info.append(l.lon + "  " + l.lat + "\n");
				}
			}
			info.deleteCharAt(info.length() - 1);
		}
		return info.toString();
	}
}
