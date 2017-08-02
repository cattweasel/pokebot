package net.cattweasel.pokebot.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class RFC4180LineParser {

	public static final char QUOTE_CHAR = '"';
	private char _delimiter;
	private boolean _tolerateColumnSizeMismatch;
	private int _numberOfColumns;
	private boolean _ignoreQuotes;
	private boolean _trimValues;
	private boolean _filterEmpty;

	private static final Logger log = Logger.getLogger(RFC4180LineParser.class);

	private RFC4180LineParser() {
		this._tolerateColumnSizeMismatch = false;
		this._numberOfColumns = -1;
		this._ignoreQuotes = false;
		this._trimValues = false;
		this._filterEmpty = false;
	}

	public RFC4180LineParser(char delimiter) {
		this();
		this._delimiter = delimiter;
	}

	public RFC4180LineParser(String delimiter) {
		this();
		setDelimiter(delimiter);
	}

	public RFC4180LineParser(char delimiter, int numCols) {
		this(delimiter);
		this._numberOfColumns = numCols;
	}

	public void tolerateMissingColumns(boolean tolerate) {
		this._tolerateColumnSizeMismatch = tolerate;
	}

	public boolean tolerateMissingColumns() {
		return this._tolerateColumnSizeMismatch;
	}

	public void setNumberOfColumns(int cols) {
		this._numberOfColumns = cols;
	}

	public int getNumberOfColumns() {
		return this._numberOfColumns;
	}

	public void setIgnoreQuotes(boolean ignore) {
		this._ignoreQuotes = ignore;
	}

	public boolean ignoreQuotes() {
		return this._ignoreQuotes;
	}

	public void setTrimValues(boolean trim) {
		this._trimValues = trim;
	}

	public boolean trimValues() {
		return this._trimValues;
	}

	public void setDelimiter(String delimiter) {
		if (delimiter == null) {
			throw new UnsupportedOperationException(
					"Delimiter cannot be null, you must specify the delimiter either as a single character or a unicode String value. For example  \\u0009 to represent a tab.");
		}
		if (delimiter.length() > 1) {
			if (delimiter.startsWith("\\u")) {
				this._delimiter = ((char) Integer.parseInt(delimiter.substring(2)));
				log.debug("Delimiter '" + delimiter
						+ "' was more then one character and started with \\u treating it as a unicode character.");
			} else {
				throw new UnsupportedOperationException("'" + delimiter
						+ "' is invalid you must specify the delimiter either as a single character or a unicode String value. For example  \\u0009 to represent a tab.");
			}
		}
		if (delimiter.length() == 1) {
			this._delimiter = delimiter.charAt(0);
		}
	}

	public void setDelimiter(char c) {
		this._delimiter = c;
	}

	public char getDelimiter() {
		return this._delimiter;
	}

	public boolean filterEmpty() {
		return this._filterEmpty;
	}

	public void setFilterEmpty(boolean filter) {
		this._filterEmpty = filter;
	}

	public ArrayList<String> parseLine(String line) throws GeneralException {
		log.debug("Line to Parse[" + line + "]");
		ArrayList<String> tokens = null;
		if (this._numberOfColumns > 0) {
			tokens = new ArrayList<String>(this._numberOfColumns);
		} else {
			tokens = new ArrayList<String>();
		}
		if (line != null) {
			boolean inQuotes = false;
			StringBuffer token = new StringBuffer();
			char[] chars = line.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				if (!this._ignoreQuotes && ch == '"') {
					if (inQuotes) {
						boolean nestedQuotes = false;
						int j = i + 1;
						if (j < chars.length) {
							char nextChar = chars[j];
							if (nextChar == '"') {
								token.append(ch);
								i++;
								nestedQuotes = true;
							}
						}
						if (!nestedQuotes)
							inQuotes = false;
					} else {
						inQuotes = true;
					}
				} else if (ch == this._delimiter) {
					if (inQuotes) {
						token.append(ch);
					} else {
						String tokenValue = token.toString();
						if (tokenValue.length() == 0) {
							tokenValue = null;
						}
						if (this._trimValues && tokenValue != null) {
							tokenValue = tokenValue.trim();
						}
						if (Util.getString(tokenValue) != null || !this._filterEmpty) {
							tokens.add(tokenValue);
						}
						token = new StringBuffer();
					}
				} else {
					token.append(ch);
				}
				if (i == chars.length - 1) {
					String tokenValue = token.toString();
					if (tokenValue.length() == 0) {
						tokenValue = null;
					}
					if (this._trimValues && tokenValue != null) {
						tokenValue = tokenValue.trim();
					}
					if (Util.getString(tokenValue) != null || !this._filterEmpty) {
						tokens.add(tokenValue);
					}
				}
			}
			if (inQuotes) {
				throw new GeneralException("\nLine [" + line + "]\n" + "\nProblem: Line has mis-matched quotes.\n");
			}
			if (!tolerateMissingColumns()) {
				if (this._numberOfColumns > 0 && tokens.size() != this._numberOfColumns) {
					throw new GeneralException(
							"\nLine [" + line + "]\n" + "\nProblem: Line has invalid number of columns. Expected ["
									+ this._numberOfColumns + "] but found [" + tokens.size() + "]");
				}
			} else if (this._numberOfColumns > 0 && tokens.size() != this._numberOfColumns) {
				int currentSize = tokens.size();
				for (int i = currentSize; i < this._numberOfColumns; i++)
					tokens.add("");
			}
		}
		return tokens.size() > 0 ? tokens : null;
	}

	public static List<String> parseLine(String delimiter, String src, boolean filterEmpty) {
		RFC4180LineParser parser = new RFC4180LineParser(delimiter);
		parser.setTrimValues(true);
		parser.setFilterEmpty(filterEmpty);
		List<String> tokens = new ArrayList<String>();
		try {
			tokens = parser.parseLine(src);
		} catch (Exception e) {
			log.error("RFC4180LineParser.parseLine: " + e.toString());
		}
		return tokens;
	}
}
