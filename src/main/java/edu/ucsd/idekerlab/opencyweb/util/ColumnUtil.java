package edu.ucsd.idekerlab.opencyweb.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author churas
 */
public class ColumnUtil {
	
	public String getTruncatedColumnName(CyColumn column, int truncatedColLength){
		String rawColName = column.getName();
		if (rawColName == null){
			return null;
		}
		if (rawColName.length() <= truncatedColLength){
			return rawColName;
		}
		return rawColName.substring(0, truncatedColLength - 3) + "...";
	}
	
	public String getColumnData(CyNetwork network, CyColumn column, CyNode selectedNode){
		if (column.getType() == String.class){
			String rawValue = network.getRow(selectedNode).get(column.getName(),
					                                           String.class);
			if (rawValue == null || rawValue.trim().length() == 0){
				return null;
			}
			return rawValue;
		}
		if (column.getType() == List.class){
			List rawList = network.getRow(selectedNode).get(column.getName(),
					                                        List.class);
			if (rawList == null){
				return null;
			}
			StringBuilder sb = new StringBuilder();
			for (Object listItem : rawList){
				if (listItem.getClass() != String.class){
					return null;
				}
				sb.append((String)listItem);
				sb.append(" ");
			}
			String res = sb.toString();
			if (res.trim().length() == 0){
				return null;
			}
			return res;
		}
		return null;
	}
	
	/**
	 * Creates term list by getting data from node column named 
	 * TODO FIX
	 * and replacing the default delimiter with {@literal %20} so it can be put in
	 * a web link
	 * @param networkView
	 * @return 
	 */
	public String getQueryString(final String data, final String replaceWhiteSpaceWithDelimiter) throws UnsupportedEncodingException {
		
		if (data == null || data.trim().isEmpty()){
			return null;
		}
		
		String theQuery = data;
		if (replaceWhiteSpaceWithDelimiter != null){
			theQuery = replaceWhiteSpaceWith(data, replaceWhiteSpaceWithDelimiter);
		}
		
		return URLEncoder.encode(theQuery, StandardCharsets.UTF_8.toString());
	}
	
	protected String replaceWhiteSpaceWith(final String data, final String delimiter){
		StringBuilder sb = new StringBuilder();
		boolean emptyBuilder = true;
		for (String dataStrFrag : data.split("\\s+")){
			if (emptyBuilder == true){
				sb.append(dataStrFrag);
				emptyBuilder = false;
				continue;
			}
			sb.append(delimiter);
			sb.append(dataStrFrag);
		}
		return sb.toString();
	}
	
}
