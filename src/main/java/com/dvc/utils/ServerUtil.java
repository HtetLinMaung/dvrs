package com.dvc.utils;

import java.security.SecureRandom;
import java.sql.Clob;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class ServerUtil {

	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%&";
	static SecureRandom rnd = new SecureRandom();

	public static String datetoString(String aDate) {
		String l_date = "";

		String[] l_arr = aDate.split("/");
		l_date = l_arr[2] + l_arr[1] + l_arr[0];

		return l_date;
	}

	public static String getStartZero(int aZeroCount, String aValue) {
		while (aValue.length() < aZeroCount) {
			aValue = "0" + aValue;
		}
		return aValue;
	}

	public static String ddMMyyyFormat(String aDate) {
		String l_Date = "";
		if ((!aDate.equals("")) && (aDate != null)) {
			l_Date = aDate.substring(6) + "/" + aDate.substring(4, 6) + "/" + aDate.substring(0, 4);
		}
		return l_Date;
	}

	public static String yyyyMMddFormat(String aDate) {
		String l_Date = "";
		if ((!aDate.equals("")) && (aDate != null)) {
			l_Date = aDate.substring(0, 4) + aDate.substring(4, 6) + aDate.substring(6);
		}
		return l_Date;
	}

	public static String datetimeToString(String p_date) {
		String ret = "";
		if (p_date != null) {
			p_date = p_date.replace("-", "");
			try {
				ret = p_date.substring(6, 8) + "/" + p_date.substring(4, 6) + "/" + p_date.substring(0, 4);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static String FormatyyyyMMdd(Date aDate) {
		String l_ret = "";
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		l_ret = df.format(aDate);
		return l_ret;
	}

	public static String stringtoDateInDateFormat(String aDate) {
		String l_Date = "";
		if (!aDate.equals("")) {
			l_Date = aDate.substring(6) + "/" + aDate.substring(4, 6) + "/" + aDate.substring(0, 4);
		}
		return l_Date;
	}

	public static String FormatyyyyMMdd2(Date aDate) {
		String l_ret = "";
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		l_ret = df.format(aDate);
		return l_ret;
	}

	public static String formatDecimal(double p) {
		DecimalFormat myFormatter = new DecimalFormat("###,###0.00");
		String ret = "";
		ret = myFormatter.format(p);
		return ret;
	}

	public static String getTime() {
		String l_date = "";
		Date date = new Date();
		String strDateFormat = "HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		l_date = sdf.format(date);
		return l_date;
	}

	public static boolean checkSqlInjection(String searchText) {
		boolean check = true;
		double count = 0;
		if (!searchText.equalsIgnoreCase("")) {
			String checkString[] = searchText.split(" ");
			for (int i = 0; i < checkString.length; i++) {
				if (checkString[i].equalsIgnoreCase("from") || checkString[i].equalsIgnoreCase("delete")
						|| checkString[i].equalsIgnoreCase("drop") || checkString[i].equalsIgnoreCase("select")
						|| checkString[i].equalsIgnoreCase("update") || checkString[i].equalsIgnoreCase("where")
						|| checkString[i].equalsIgnoreCase("insert") || checkString[i].equalsIgnoreCase("TRUNCATE")
						|| checkString[i].equalsIgnoreCase("table")) {
					return false;
				}
			}
			if (searchText.contains("=")) {
				return false;
			} else {
				if (searchText.contains("--")) {
					count++;
				}
				if (searchText.contains("'")) {
					return false;
				}
				if (searchText.contains(";")) {
					count++;
				}
				if (searchText.contains(")")) {
					count = count + 0.5;
				}
				if (searchText.contains("(")) {
					count = count + 0.5;
				}
				if ((searchText.contains("'") || (searchText.contains(")") && !searchText.contains("(")))
						&& searchText.contains("--")) {
					count++;
				}
				if (searchText.contains("--") && !searchText.contains(")") && !searchText.contains("(")) {
					count = count + 3;
				}

			}
			if (count > 2) {
				check = false;
			}
		}

		return check;
	}

	public static long getLong(Object o) {
		try {
			return o instanceof String ? Long.parseLong((String) o)
					: o instanceof Integer ? (int) o
							: o instanceof Long ? (long) o
									: o instanceof Double ? (new Double((double) o)).longValue() : 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static int getInteger(Object o) {
		try {
			return o instanceof String ? Integer.parseInt((String) o)
					: o instanceof Integer ? (int) o
							: o instanceof Long ? ((Long) o).intValue()
									: o instanceof Double ? (int) Math.round((double) o) : 0;
		} catch (Exception e) {
			return 0;
		}
	}

	public static String getString(Object o) {
		try {
			if (o == null)
				return null;
			if (o instanceof Clob) {
				Clob clob = (Clob) o;
				return clob.getSubString(1, (int) clob.length());
			} else {
				return String.valueOf(o);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isBlank(String value) {
		return (value == null || value.equals("") || value.equals("null") || value.trim().equals(""));

	}

	public static String timeToString() {
		String l_date = "";
		java.util.Date l_Date = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
		l_date = df.format(l_Date);
		return l_date;
	}

	@SuppressWarnings("unchecked")
	public static boolean isAuth(String token, String userId, String appId)
			throws JsonMappingException, JsonProcessingException {
		String body = TokenUtil.getTokenData(token, "a");
		if (body != null) {
			Map<String, Object> resultMap = new ObjectMapper().readValue(body, Map.class);
			if (getString(resultMap.get("userid")).equals(userId) && getString(resultMap.get("appid")).equals(appId)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static boolean isBTokenAuth(String token) {
		String body = TokenUtil.getTokenData(token, "b");
		if (body != null) {
			// Map<String, Object> resultMap = new Gson().fromJson(body, Map.class);
			// if (getString(resultMap.get("userid")).equals(userId) &&
			// getString(resultMap.get("appid")).equals(appId)
			// && getString(resultMap.get("domain")).equals(domain)
			// && getString(resultMap.get("cid")).equals(cId)) {
			// return true;
			// }
			return true;
		}
		return false;
	}

	public static String generateSession() {
		return randomString(32);

	}

	static String randomString(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public static boolean isBTokenAuthAdmin(String token) {
		String body = TokenUtil.getTokenData(token, "b");
		if (body != null) {
			Map<String, Object> resultMap = new Gson().fromJson(body, Map.class);
			if (getString(resultMap.get("userlevel")).equals("200")) {
				return true;
			}
			return false;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static boolean isBTokenAuthPartner(String token) {
		String body = TokenUtil.getTokenData(token, "b");
		if (body != null) {
			Map<String, Object> resultMap = new Gson().fromJson(body, Map.class);
			if (getString(resultMap.get("userlevel")).equals("500")) {
				return true;
			}
			return false;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static boolean isBTokenAuthNotPartner(String token) {
		String body = TokenUtil.getTokenData(token, "b");
		if (body != null) {
			Map<String, Object> resultMap = new Gson().fromJson(body, Map.class);
			if (!getString(resultMap.get("userlevel")).equals("500")) {
				return true;
			}
			return false;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static String getPartnerSkFromBToken(String token) {
		String body = TokenUtil.getTokenData(token, "b");
		if (body != null) {
			Map<String, Object> resultMap = new Gson().fromJson(body, Map.class);
			String partnerSyskey = getString(resultMap.get("partnersyskey"));
			return partnerSyskey.equals("") ? "0" : partnerSyskey;
		}
		return "0";
	}

}
