package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.common.split.SplitPointDataSource;

/**
 * Provides a list of consecutive sheets without manual volume breaks
 * inside.
 * @author Joel Håkansson
 *
 */
class SheetGroup {
	private SplitPointDataSource<Sheet> units;
	private VolumeSplitter splitter;
	private int overheadCount;
	private int sheetCount;

	/**
	 * Creates a new sheet group.
	 */
	SheetGroup() {
		reset();
	}
	
	/**
	 * Resets this sheet group's state.
	 */
	void reset() {
		this.overheadCount = 0;
		this.sheetCount =  0;
	}
	
	/**
	 * Gets the number of sheets outside of the regular text body.
	 * @return returns the number of sheets
	 */
	int getOverheadCount() {
		return overheadCount;
	}
	
	/**
	 * Sets the number of sheets outside of the regular text body.
	 * @param value the number of sheets
	 */
	void setOverheadCount(int value) {
		this.overheadCount = value;
	}
	
	/**
	 * Gets the number of processed sheets belonging to the regular text body.
	 * @return returns the number of sheets
	 */
	int getSheetCount() {
		return sheetCount;
	}
	
	/**
	 * Sets the number of processed sheets belonging to the regular text body.
	 * @param value the number of sheets
	 */
	void setSheetCount(int value) {
		this.sheetCount = value;
	}

	/**
	 * Gets the remaining sheets in this group
	 * @return the remaining sheets
	 */
	SplitPointDataSource<Sheet> getUnits() {
		return units;
	}

	/**
	 * Sets the remaining sheets in this group
	 * @param units a list of remaining sheets
	 */
	void setUnits(SplitPointDataSource<Sheet> units) {
		this.units = units;
	}
	
	/**
	 * Sets the volume splitter used for this sheet group
	 * @param splitter the splitter to use 
	 */
	void setSplitter(VolumeSplitter splitter) {
		this.splitter = splitter;
	}

	/**
	 * Gets the volume splitter used for this sheet group
	 * @return returns the splitter
	 */
	VolumeSplitter getSplitter() {
		return splitter;
	}
	
	/**
	 * Gets the total sheet count, including processed sheets, overhead sheets and remaining sheets.
	 * <b>Note: only use after all volumes have been calculated.</b>
	 * @return returns the total sheet count
	 */
	int countTotalSheets() {
		return getOverheadCount() + getSheetCount() + getUnits().getRemaining().size();
	}
	
	/**
	 * Returns true if this group has any remaining sheets left
	 * @return returns true if this group has remaining sheets, false otherwise
	 */
	boolean hasNext() {
		return !getUnits().isEmpty();
	}

}
