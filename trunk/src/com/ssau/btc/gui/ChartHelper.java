package com.ssau.btc.gui;

import com.ssau.btc.model.IndexSnapshot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Collection;

/**
 * Author: Sergey42
 * Date: 29.03.14 18:24
 */
public class ChartHelper {

    public static XYDataset createXYDataSet(double[] data) {
        XYSeries xySeries = new XYSeries("Data");

        for (int i = 0; i < data.length; i++) {
            xySeries.add(i, data[i]);
        }
        return new XYSeriesCollection(xySeries);
    }

    public static TimeSeriesCollection createTimeDataSet(Collection<IndexSnapshot> indexSnapshots) {
        TimeSeries timeSeries = new TimeSeries("Index");

        for (IndexSnapshot indexSnapshot : indexSnapshots) {
            timeSeries.add(new TimeSeriesDataItem(new Day(indexSnapshot.date), indexSnapshot.value));
        }

        return new TimeSeriesCollection(timeSeries);
    }

    public static JFreeChart createTimeChart(XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "BTC index",      // chart title
                "Time",                      // x axis label
                "Price",                      // y axis label
                dataset,                  // data
                false,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        XYSplineRenderer xySplineRenderer = new XYSplineRenderer();
        plot.setRenderer(xySplineRenderer);

        return chart;
    }

    public static JFreeChart createDoublesChart(XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "BTC index",      // chart title
                "Time",                      // x axis label
                "Price",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                false,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        XYSplineRenderer xySplineRenderer = new XYSplineRenderer();
        plot.setRenderer(xySplineRenderer);

        return chart;
    }
}
