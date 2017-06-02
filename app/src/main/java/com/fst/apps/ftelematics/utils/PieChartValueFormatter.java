package com.fst.apps.ftelematics.utils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;


public class PieChartValueFormatter implements ValueFormatter {
	private DecimalFormat mFormat;

    public PieChartValueFormatter() {
        mFormat = new DecimalFormat("###,###,##0"); // use one decimal
    }


	@Override
	public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
		return mFormat.format(value);
	}
}
