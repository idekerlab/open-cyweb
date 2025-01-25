package edu.ucsd.idekerlab.opencyweb.util;

import edu.ucsd.idekerlab.opencyweb.exceptions.WebbyMcSearchException;
import org.cytoscape.model.CyTable;


/**
 * Contains utility methods to interact with {@link org.cytoscape.model.CyNetwork}
 * objects
 * @author churas
 */
public class CyNetworkUtil {

	public static final String WEBBY_MCSEARCH_NAMESPACE = "WebbyMcSearch";
	/**
	 * Creates column in table passed in if it does <b>NOT</b> already exist
	 * @param <T> type of column
	 * @param table where column will be added to
	 * @param colName name of column
	 * @param type column type
	 * @param isImmutable denotes if values in new column can be changed
	 * @param defaultValue the default values for cells in new column
	 * @throws WebbyMcSearchException if table or column name is {@code null}
	 */
	public <T> void createTableColumn(CyTable table,
			String colName, Class<? extends T> type, boolean isImmutable,
			T defaultValue) throws WebbyMcSearchException {
		if (table == null){
			throw new WebbyMcSearchException("table is null");
		}
		if (colName == null){
			throw new WebbyMcSearchException("column name is null");
		}
		if (type == null){
			throw new WebbyMcSearchException("type is null");
		}
		if (table.getColumn(colName) == null) {
			table.createColumn(WEBBY_MCSEARCH_NAMESPACE, colName, type, isImmutable, defaultValue);
		}
	}
}
