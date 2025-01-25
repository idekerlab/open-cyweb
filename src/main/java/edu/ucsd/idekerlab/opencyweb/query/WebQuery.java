package edu.ucsd.idekerlab.opencyweb.query;

/**
 * Pojo representing a web query
 * @author churas
 */
public class WebQuery {
	

	private String _name;
	private String _guiVisibleName;
	private String _urlAsString;
	private String _columnName;
	private String _replaceWhiteSpaceWith;

	public WebQuery(final String name,final String guiVisibleName,
			final String urlAsString,
			final String columnName, final String replaceWhiteSpaceWith){
		_name = name;
		_guiVisibleName = guiVisibleName;
		_urlAsString = urlAsString;
		_columnName = columnName;
		_replaceWhiteSpaceWith = replaceWhiteSpaceWith;
		
	}
	
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}
	
	public String getGuiVisibleName() {
		return _guiVisibleName;
	}

	public void setGuiVisibleName(String guiVisibleName) {
		this._guiVisibleName = guiVisibleName;
	}

	public String getUrlAsString() {
		return _urlAsString;
	}

	public void setUrlAsString(String urlAsString) {
		this._urlAsString = urlAsString;
	}

	public String getColumnName() {
		return _columnName;
	}

	public void setColumnName(String columnName) {
		this._columnName = columnName;
	}

	public String getReplaceWhiteSpaceWith() {
		return _replaceWhiteSpaceWith;
	}

	public void setReplaceWhiteSpaceWith(String replaceWhiteSpaceWith) {
		this._replaceWhiteSpaceWith = replaceWhiteSpaceWith;
	}
}
