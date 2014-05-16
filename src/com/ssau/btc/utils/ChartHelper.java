package com.ssau.btc.utils;

import com.ssau.btc.model.IndexSnapshot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.Hour;
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

    public static TimeSeriesCollection createTimeDataSet(Collection<IndexSnapshot> indexSnapshots, String seriesName) {
        return new TimeSeriesCollection(createTimeSeries(indexSnapshots, seriesName));
    }

    public static TimeSeries createTimeSeries(Collection<IndexSnapshot> indexSnapshots, String seriesName) {
        TimeSeries timeSeries = new TimeSeries(seriesName);

        for (IndexSnapshot indexSnapshot : indexSnapshots) {
            timeSeries.add(new TimeSeriesDataItem(new Hour(indexSnapshot.date), indexSnapshot.value));
        }
        return timeSeries;
    }

    public static JFreeChart createTimeChart(XYDataset dataset, String title, String xAxis, String yAxis) {
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,      // chart title
                xAxis,                      // x axis label
                yAxis,                      // y axis label
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

        plot.getDomainAxis().setAutoRange(true);
        plot.getRangeAxis().setAutoRange(true);

        XYSplineRenderer xySplineRenderer = new XYSplineRenderer();
        plot.setRenderer(xySplineRenderer);

        return chart;
    }

    public static JFreeChart createDoublesChart(XYDataset dataset, String title, String xAxis, String yAxis) {
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,      // chart title
                xAxis,                      // x axis label
                yAxis,                      // y axis label
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

    private ChartHelper() {
    }
}
