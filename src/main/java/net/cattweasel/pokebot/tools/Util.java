package net.cattweasel.pokebot.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Util {

	private static final Logger log = Logger.getLogger(Util.class);
	
	@SuppressWarnings("unchecked")
	public static List<Object> asList(Object o) {
		List<Object> list = new ArrayList<Object>();
		if (o instanceof Collection) {
			list.addAll((Collection<Object>) o);
		} else {
			list.add(o);
		}
		return list;
	}

	private static String assemblePath(String root, String leaf) {
		StringBuffer b = new StringBuffer();
		b.append(root);
		if (leaf != null) {
			b.append("/");
			b.append(leaf);
		}
		String path = b.toString();
		if (path.indexOf("\\") >= 0) {
			path = path.replace("\\", "/");
		}
		return path;
	}

	public static float atof(String a) {
		float f = 0.0F;
		if (a != null) {
			try {
				f = Float.parseFloat(a);
			} catch (NumberFormatException e) {
				log.error(e);
			}
		}
		return f;
	}

	public static int atoi(String a) {
		int i = 0;
		if (a != null && a.length() > 0) {
			try {
				int dotIndex = a.indexOf(46);
				if (dotIndex > 0) {
					a = a.substring(0, dotIndex);
				}
				i = Integer.parseInt(a);
			} catch (NumberFormatException e) {
				log.error(e);
			}
		}
		return i;
	}

	public static long atol(String a) {
		long i = 0L;
		if (a != null) {
			try {
				i = Long.parseLong(a);
			} catch (NumberFormatException e) {
				log.error(e);
			}
		}
		return i;
	}

	public static String bytesToString(byte[] bytes) throws GeneralException {
		String string = null;
		if (bytes != null)
			try {
				string = new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				throw new GeneralException(ex);
			}
		return string;
	}

	public static String capitalize(String str) {
		char first = str.charAt(0);
		if (Character.isLowerCase(first)) {
			str = new StringBuilder().append(Character.toUpperCase(first)).append(str.substring(1)).toString();
		}
		return str;
	}

	public static int countChars(String sourceString, char lookFor) {
		int count = 0;
		if (sourceString != null)
			for (int i = 0; i < sourceString.length(); i++) {
				char c = sourceString.charAt(i);
				if (c == lookFor)
					count++;
			}
		return count;
	}

	public static Object createObjectByClassName(String name) throws GeneralException {
		Object instance = null;
		Class<?> c = null;
		try {
			c = Class.forName(name);
		} catch (Exception e) {
			StringBuffer sb = new StringBuffer();
			sb.append("Couldn't load class: ");
			sb.append(name);
			throw new GeneralException(sb.toString());
		}
		try {
			instance = c.newInstance();
		} catch (Exception e) {
			throw new GeneralException(
					new StringBuilder().append("Failed to create object: ").append(e.toString()).toString());
		}
		if (instance == null) {
			throw new GeneralException("Class instance is null.");
		}
		return instance;
	}

	public static List<String> csvToList(String src) {
		return csvToList(src, false);
	}

	public static List<String> csvToList(String src, boolean filterEmpty) {
		List<String> list = new ArrayList<String>();
		if (src != null) {
			list = RFC4180LineParser.parseLine(",", src, filterEmpty);
		}
		return list;
	}

	public static String dateToString(Date src) {
		return dateToString(src, null);
	}

	public static String dateToString(Date src, String format) {
		return dateToString(src, format, TimeZone.getDefault());
	}

	public static String dateToString(Date src, String format, TimeZone tz) {
		DateFormat f = null;
		if (format == null) {
			format = "M/d/y H:m:s a z";
		}
		f = new SimpleDateFormat(format);
		f.setTimeZone(tz);
		return f.format(src);
	}

	public static String decodeJavaIdentifier(String s) throws Util.ParseException {
		String decoded = null;
		if (null != s) {
			StringBuilder sb = new StringBuilder(s);
			for (int i = 0; i < sb.length(); i++) {
				char c = sb.charAt(i);
				if ('_' == c && i != sb.length() - 1) {
					char next = sb.charAt(i + 1);
					if ('_' == next) {
						decodeJavaIdentifierChar(sb, i);
					}
				}
			}
			decoded = sb.toString();
		}
		return decoded;
	}

	private static void decodeJavaIdentifierChar(StringBuilder sb, int startIdx) throws Util.ParseException {
		StringBuilder buffer = new StringBuilder();
		boolean potentialEnd = false;
		for (int i = startIdx; i < sb.length(); i++) {
			char current = sb.charAt(i);
			if (i == startIdx || i == startIdx + 1) {
				if ('_' != current) {
					throw new ParseException();
				}
			} else {
				if ('_' == current) {
					if (potentialEnd) {
						buffer.deleteCharAt(buffer.length() - 1);
						int intVal = atoi(buffer.toString());
						sb.replace(startIdx, startIdx + buffer.length() + 4,
								new StringBuilder().append(intVal).toString());
						return;
					}
					potentialEnd = true;
				} else {
					potentialEnd = false;
				}
				buffer.append(current);
			}
		}
		throw new ParseException();
	}

	public static void dumpProperties(PrintWriter out) {
		Properties props = System.getProperties();
		if (props != null) {
			props.list(out);
		}
		out.flush();
	}

	public static String encodeJavaIdentifier(String s) {
		String encoded = null;
		if (null != s) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (0 == i) {
					if (Character.isJavaIdentifierStart(c)) {
						sb.append(c);
					} else {
						sb.append(encodeJavaIdentifierChar(c));
					}
				} else if (Character.isJavaIdentifierPart(c) || '.' == c || ' ' == c) {
					sb.append(c);
				} else {
					sb.append(encodeJavaIdentifierChar(c));
				}
			}
			encoded = sb.toString();
		}
		return encoded;
	}

	private static String encodeJavaIdentifierChar(char c) {
		return new StringBuilder().append("__").append(c).append("__").toString();
	}

	public static String escapeHTMLNewlines(String src) {
		return escapeNewlines(src, "&#xA;", "&#xD;");
	}

	public static String escapeNewlines(String src, String nRepl, String rRepl) {
		String escaped = null;
		if (src != null) {
			StringBuffer b = new StringBuffer();
			int max = src.length();
			for (int i = 0; i < max; i++) {
				char ch = src.charAt(i);
				if (ch == '\n') {
					b.append(nRepl);
				} else if (ch == '\r') {
					b.append(rRepl);
				} else {
					b.append(ch);
				}
			}
			escaped = b.toString();
		}
		return escaped;
	}

	public static String findFile(String name) {
		return findFile("user.dir", name);
	}

	public static String findFile(String property, String name) {
		return findFile(property, name, false);
	}

	public static String findFile(String property, String name, boolean searchClasspath) {
		String path = null;
		if (name.charAt(0) == '/' || name.indexOf(":") != -1) {
			path = name;
		} else {
			File f = new File(name);
			if (f.isFile()) {
				path = name;
			}
			if (path == null && property != null) {
				String root = System.getProperty(property);
				if (root != null) {
					String testpath = assemblePath(root, name);
					f = new File(testpath);
					if (f.isFile()) {
						path = testpath;
					}
				}
			}
			if (path == null) {
				String home = null;
				try {
					home = getApplicationHome();
				} catch (GeneralException ex) {
					log.error(ex);
				}
				if (home != null) {
					String testPath = assemblePath(home, name);
					f = new File(testPath);
					if (f.isFile()) {
						path = testPath;
					} else {
						testPath = assemblePath(home,
								new StringBuilder().append("/WEB-INF/config/").append(name).toString());
						f = new File(testPath);
						if (f.isFile()) {
							path = testPath;
						}
					}
				}
			}
			if (path == null && searchClasspath == true) {
				path = getResourcePath(name);
			}
			if (path == null)
				path = name;
		}
		return path;
	}

	public static String findOutputFile(String name) {
		String path = null;
		if (name.charAt(0) == '/' || name.indexOf(":") != -1) {
			path = name;
		} else {
			String root = System.getProperty("user.dir");
			if (root != null) {
				path = assemblePath(root, name);
			} else {
				path = name;
			}
		}
		return path;
	}

	public static String getApplicationHome() throws GeneralException {
		String root = System.getProperty("pokebot.home");
		if (root == null) {
			String propertyFilename = "log4j.properties";
			String path = getResourcePath(propertyFilename);
			if (path == null) {
				String msg = "Could not derive PokeBot home directory.";
				throw new GeneralException(msg);
			}
			int i = path.indexOf("WEB-INF");
			if (i > 0) {
				root = path.substring(0, i - 1);
			}
		}
		return root;
	}

	public static Date getBeginningOfDay(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.set(11, 0);
		cal.set(12, 0);
		cal.set(13, 0);
		cal.set(14, 0);
		return cal.getTime();
	}

	public static boolean getBoolean(Map<?, ?> map, String name) {
		boolean value = false;
		if (map != null) {
			Object o = map.get(name);
			if (o != null)
				value = otob(o);
		}
		return value;
	}

	public static String getClasspathDirectory(int idx) {
		String dir = null;
		Properties props = System.getProperties();
		if (props != null) {
			String cp = props.getProperty("java.class.path");
			if (cp != null) {
				StringTokenizer toks = new StringTokenizer(cp, ";");
				for (int i = 0; toks.hasMoreElements(); i++) {
					String s = (String) toks.nextElement();
					if (i == idx) {
						StringBuffer b = new StringBuffer();
						for (int j = 0; j < s.length(); j++) {
							char c = s.charAt(j);
							if (c == '\\') {
								c = '/';
							}
							if (c != '/' || j < s.length() - 1) {
								b.append(c);
							}
						}
						dir = b.toString();
						break;
					}
				}
			}
		}
		return dir;
	}

	public static Date getEndOfDay(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.set(11, 23);
		cal.set(12, 59);
		cal.set(13, 59);
		cal.set(14, 999);
		return cal.getTime();
	}

	public static String getHostname() {
		String name = null;
		try {
			name = System.getProperty("pokebot.hostname");
			if (name == null || name.length() == 0) {
				InetAddress addr = InetAddress.getLocalHost();
				name = addr.getHostName();
			}
		} catch (UnknownHostException e) {
			log.error(e);
		}
		if (name == null) {
			name = "????";
		}
		return name;
	}

	public static int getInt(Map<?, ?> map, String name) {
		int value = 0;
		if (map != null) {
			Object o = map.get(name);
			if (o != null) {
				value = otoi(o);
			}
		}
		return value;
	}

	public static String getJsonSafeKey(String key) {
		return key != null ? key.replace('.', '-') : null;
	}

	public static String getResourcePath(String name) {
		String path = null;
		if (name.indexOf("\\") >= 0) {
			name = name.replace("\\", "/");
		}
		URL res = null;
		try {
			ClassLoader l = Util.class.getClassLoader();
			if (l != null) {
				res = l.getResource(name);
			}
			if (res == null) {
				res = ClassLoader.getSystemResource(name);
			}
		} catch (Exception e) {
			log.error(e);
		}
		if (res != null) {
			path = res.getFile();
			String fileEncoding = System.getProperty("file.encoding");
			try {
				path = URLDecoder.decode(path, fileEncoding);
			} catch (UnsupportedEncodingException ex) {
				System.err.println(new StringBuilder().append("Encoding ").append(fileEncoding)
						.append(" unsupported.  Using UTF-8").toString());
				try {
					path = URLDecoder.decode(path, "UTF-8");
				} catch (UnsupportedEncodingException ex2) {
					System.err.println("UTF-8 encoding not supported. No decoding will be performed.");
				}
			}
			if (path.startsWith("/FILE")) {
				StringBuffer b = new StringBuffer();
				int psn = 5;
				boolean prevSlash = false;
				if (Character.isDigit(path.charAt(psn))) {
					int sep = path.indexOf(47, 6);
					if (sep != -1) {
						String nstr = path.substring(5, sep);
						int n = atoi(nstr);
						String root = getClasspathDirectory(n);
						b.append(root);
						b.append("/");
						psn = sep + 1;
						prevSlash = true;
					}
				}
				for (int i = psn; i < path.length(); i++) {
					char c = path.charAt(i);
					if (c == '/') {
						if (!prevSlash) {
							b.append("/");
							prevSlash = true;
						}
					} else if (c != '+') {
						if (c == '\\') {
							b.append("/");
							prevSlash = true;
						} else {
							b.append(c);
							prevSlash = false;
						}
					}
				}
				path = b.toString();
			} else if (path.charAt(0) == '/' && path.indexOf(":") != -1) {
				path = path.substring(1);
			}
		}
		return path;
	}

	public static String getString(String string) {
		if (string != null) {
			string = string.trim();
			string = "".equals(string) ? null : string;
		}
		return string == null ? null : string;
	}

	public static String getString(Map<String, Object> map, String name) {
		String value = null;
		if (map != null) {
			Object o = map.get(name);
			if (o != null) {
				value = o.toString();
			}
		}
		return value;
	}

	public static Date incrementDateByMinutes(Date date, int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(12, minutes);
		return cal.getTime();
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isEmpty(String csv) {
		return isNullOrEmpty(csv) || isEmpty(csvToList(csv, true));
	}

	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isNotNullOrEmpty(String str) {
		return !isNullOrEmpty(str);
	}

	public static List<Map<String, Object>> iteratorToMaps(Iterator<Object[]> iterator, String[] keys) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (keys.length == 1) {
			List<String> keyList = csvToList(keys[0]);
			keys = (String[]) keyList.toArray(new String[keyList.size()]);
		}
		if (null != iterator)
			while (iterator.hasNext()) {
				Object[] current = (Object[]) iterator.next();
				if (current.length != keys.length) {
					throw new RuntimeException("Current element does not"
							+ " have expected columns: " + Arrays.toString(current)
							+ " - " + Arrays.toString(keys));
				}
				Map<String, Object> map = new HashMap<String, Object>();
				result.add(map);
				for (int i = 0; i < current.length; i++) {
					map.put(keys[i], current[i]);
				}
			}
		return result;
	}

	public static String itoa(int i) {
		StringBuilder sb = new StringBuilder();
		sb.append(i);
		return sb.toString();
	}

	public static String join(Collection<?> c, String delimiter) {
		StringBuffer buf = new StringBuffer();
		Iterator<?> iter = c.iterator();
		while (iter.hasNext()) {
			buf.append(iter.next());
			if (iter.hasNext()) {
				buf.append(delimiter);
			}
		}
		return buf.toString();
	}

	public static String listToCsv(List<?> list) {
		return listToCsv(list, false);
	}

	public static String listToCsv(List<?> list, boolean filterEmpty) {
		return listToQuotedCsv(list, Character.valueOf('"'), filterEmpty, true);
	}

	public static String listToQuotedCsv(List<?> list, Character quoteChar, boolean filterEmpty) {
		return listToQuotedCsv(list, quoteChar, filterEmpty, false);
	}

	public static String listToQuotedCsv(List<?> list, Character quoteChar, boolean filterEmpty,
			boolean conditionallyQuote) {
		String csv = null;
		if (list != null) {
			StringBuffer b = new StringBuffer();
			int added = 0;
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				String s = o != null ? o.toString() : null;
				if (!filterEmpty || s != null && s.length() > 0) {
					if (added > 0) {
						b.append(",");
						b.append(" ");
					}
					b.append(s);
					added++;
				}
			}
			csv = b.toString();
			if (csv != null && csv.length() == 0) {
				csv = null;
			}
		}
		return csv;
	}

	public static String ltoa(long i) {
		StringBuilder sb = new StringBuilder();
		sb.append(i);
		return sb.toString();
	}

	public static String memoryFormat(long size) {
		String units = "bytes";
		float adjustedSize = (float) size;
		if (size > 1073741824L) {
			adjustedSize = (float) (adjustedSize / 1.073741824E9D);
			units = "GB";
		} else if (size > 1048576L) {
			adjustedSize = (float) (adjustedSize / 1048576.0D);
			units = "MB";
		} else if (size > 1024L) {
			adjustedSize = (float) (adjustedSize / 1024.0D);
			units = "KB";
		}
		StringBuffer value = new StringBuffer();
		value.append(new DecimalFormat("###.000").format(adjustedSize)).append(" ").append(units);
		return value.toString();
	}

	public static boolean nullSafeEq(Object o1, Object o2) {
		return nullSafeEq(o1, o2, false);
	}

	public static boolean nullSafeEq(Object o1, Object o2, boolean nullsEq) {
		return nullSafeEq(o1, o2, nullsEq, false);
	}

	public static boolean nullSafeEq(Object o1, Object o2, boolean nullsEq, boolean emptyStringToNull) {
		if (emptyStringToNull) {
			if (o1 instanceof String) {
				o1 = getString((String) o1);
			}
			if (o2 instanceof String) {
				o2 = getString((String) o2);
			}
		}
		return o1 != null && o1.equals(o2);
	}

	public static int nullSafeHashCode(Object o) {
		return null != o ? o.hashCode() : -1;
	}

	public static boolean otob(Object o) {
		boolean val = false;
		if (o != null) {
			if (o instanceof Boolean) {
				val = ((Boolean) o).booleanValue();
			} else {
				val = o.toString().equalsIgnoreCase("true") || o.toString().equals("1");
			}
		}
		return val;
	}

	public static int otoi(Object o) {
		int val = 0;
		if (o != null) {
			if (o instanceof Number) {
				val = ((Number) o).intValue();
			} else {
				val = atoi(o.toString());
			}
		}
		return val;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String otos(Object o) {
		String s = null;
		if (null != o) {
			if (o instanceof List) {
				s = listToCsv((List) o);
			} else if (o instanceof Collection) {
				s = listToCsv(new ArrayList((Collection) o));
			} else {
				s = o.toString();
			}
		}
		return s;
	}

	public static byte[] readBinaryFile(String name) throws GeneralException {
		byte[] bytes;
		try {
			String path = findFile(name);
			FileInputStream fis = new FileInputStream(path);
			try {
				int size = fis.available();
				bytes = new byte[size];
				fis.read(bytes);
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		} catch (IOException e) {
			throw new GeneralException(e);
		}
		return bytes;
	}

	public static String readFile(String name) throws GeneralException {
		byte[] bytes = readBinaryFile(name);
		return bytesToString(bytes);
	}

	public static String readWebsite(String url) throws GeneralException {
		String content = null;
		URLConnection conn = null;
		try {
			conn = new URL(url).openConnection();
			content = IOUtils.toString(conn.getInputStream(), "UTF-8");
		} catch (MalformedURLException ex) {
			throw new GeneralException(ex);
		} catch (IOException ex) {
			throw new GeneralException(ex);
		}
		return content;
	}
	
	public static String separateNumber(Number n) {
		return separateNumber(n, 3);
	}
	
	public static String separateNumber(Number n, int len) {
		return separateNumber(n, len, ".");
	}
	
	public static String separateNumber(Number n, int len, String sep) {
		String result = "";
		String tmp = n.toString();
		if (!tmp.contains(".")) {
			result = tmp;
		}
		String output = "";
		if (result.length() >= 3) {
			while (result.length() >= 3) {
				if (!output.equals("")) {
					output = "." + output;
				}
				output = result.substring((result.length() - 3), result.length()) + output;
				result = result.substring(0, (result.length() - 3));
			}
		} else {
			output = result;
		}
		if (!result.equals("") && output.length() >= 3) {
			output = result + "." + output;
		}
		return output;
	}

	public static String setToCsv(Set<?> set) {
		return listToCsv(Arrays.asList(set.toArray()));
	}

	public static int size(Collection<?> c) {
		return null != c ? c.size() : 0;
	}

	public static String stackToString(Throwable th) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			th.printStackTrace(pw);
			return sw.toString();
		} catch (Exception e) {
			log.error(e);
		}
		return "Bad stackToString";
	}

	public static Date stringToDate(String src) throws java.text.ParseException {
		Date d = null;
		if (src.equals("now")) {
			d = new Date();
		} else {
			boolean isTime = false;
			boolean isTimezone = false;
			boolean afterTime = false;
			boolean isAmPm = false;
			for (int i = 0; i < src.length(); i++) {
				char c = src.charAt(i);
				if (c == ':') {
					isTime = true;
				} else if (Character.isSpaceChar(c) && isTime) {
					afterTime = true;
				} else if (afterTime) {
					isTimezone = true;
				}
				if (Character.isSpaceChar(c) && isTimezone) {
					isAmPm = true;
				}
			}
			SimpleDateFormat f = null;
			if (isAmPm)
				f = new SimpleDateFormat("M/d/y H:m:s a z");
			else if (isTimezone)
				f = new SimpleDateFormat("M/d/y H:m:s z");
			else if (isTime)
				f = new SimpleDateFormat("M/d/y H:m:s");
			else
				f = new SimpleDateFormat("M/d/y");
			d = f.parse(src);
		}
		return d;
	}

	public static List<String> stringToList(String value) {
		List<String> list = new ArrayList<String>();
		if (null != getString(value)) {
			String tempVal = value;
			if (tempVal.length() > 1 && tempVal.startsWith("[") && tempVal.endsWith("]")) {
				tempVal = tempVal.substring(1, tempVal.length() - 1);
			}
			String[] parts = tempVal.split(",");
			if (parts.length > 0) {
				for (int i = 0; i < parts.length; i++) {
					String val = parts[i].trim();
					if (val.length() > 0)
						list.add(val);
				}
			}
		}
		return list;
	}

	public static Date stringToTime(String src) throws java.text.ParseException {
		Date d = null;
		if (src.equals("now")) {
			d = new Date();
		} else {
			boolean isTime = false;
			boolean isTimezone = false;
			boolean afterTime = false;
			boolean isAmPm = false;
			for (int i = 0; i < src.length(); i++) {
				char c = src.charAt(i);
				if (c == ':') {
					isTime = true;
				} else if (Character.isSpaceChar(c) && isTime) {
					afterTime = true;
				} else if (afterTime) {
					isTimezone = true;
				}
				if (Character.isSpaceChar(c) && isTimezone) {
					isAmPm = true;
				}
			}
			SimpleDateFormat f = null;
			if (isAmPm)
				f = new SimpleDateFormat("H:m:s a z");
			else if (isTimezone)
				f = new SimpleDateFormat("H:m:s z");
			else {
				f = new SimpleDateFormat("H:m:s");
			}
			d = f.parse(src);
		}
		return d;
	}

	public static String trimWhitespace(String src) {
		String dest = null;
		int end = 0;
		if (src != null) {
			for (end = src.length() - 1; end >= 0; end--) {
				char c = src.charAt(end);
				if (!Character.isSpaceChar(c) && c != '\n' && c != '\r') {
					break;
				}
			}
			if (end >= 0) {
				dest = src.substring(0, end + 1);
			}
		}
		return dest;
	}

	public static String uuid() {
		String id = UUID.randomUUID().toString();
		id = id.replaceAll("-", "");
		return id;
	}

	public static void writeFile(String name, String contents) throws GeneralException {
		if (name != null && contents != null) {
			try {
				byte[] bytes = contents.getBytes("UTF-8");
				writeFile(name, bytes);
			} catch (UnsupportedEncodingException ex) {
				throw new GeneralException(ex);
			}
		}
	}

	public static void writeFile(String name, byte[] contents) throws GeneralException {
		try {
			String path = findOutputFile(name);
			FileOutputStream fos = new FileOutputStream(path);
			try {
				fos.write(contents, 0, contents.length);
			} finally {
				try {
					fos.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		} catch (IOException e) {
			throw new GeneralException(e);
		}
	}

	public static class ParseException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ParseException() {
			super();
		}
	}

	public static List<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					assert !file.getName().contains(".");
					classes.addAll(findClasses(file, packageName + "." + file.getName()));
				} else if (file.getName().endsWith(".class")) {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(
							0, file.getName().length() - 6)));
				}
			}
		}
		return classes;
	}
}
