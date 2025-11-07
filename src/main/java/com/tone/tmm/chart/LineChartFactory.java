package com.tone.tmm.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class LineChartFactory extends JFrame {
    public void kRChart(double[] k, double[] R) {
        JFrame frame = new JFrame("Wave numbers-Reflected Power Chart");
        XYSeries dataset = new XYSeries("First");
        for (int i = 0; i < k.length; i++) {
            dataset.add(k[i], R[i] * 100);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wave numbers-Reflected Power Chart",
                "Wave numbers (1/cm)",
                "Reflected Power (%)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

        processFrame(frame, chart);
    }

    public void kRChart(double[] k, double[] R1, double[] R2, String[] legend) {
        JFrame frame = new JFrame("Wave numbers-Reflected Power Chart");
        XYSeries dataset1 = new XYSeries(legend[0]);
        for (int i = 0; i < k.length; i++) {
            dataset1.add(k[i], R1[i] * 100);
        }
        XYSeries dataset2 = new XYSeries(legend[1]);
        for (int i = 0; i < k.length; i++) {
            dataset2.add(k[i], R2[i] * 100);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset1);
        xyseriescollection.addSeries(dataset2);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wave numbers-Reflected Power Chart",
                "Wave numbers (1/cm)",
                "Reflected Power (%)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        processFrame(frame, chart);
    }

    public void lambdaRChart(double[] lambdaList, double[] RList) {
        JFrame frame = new JFrame("Wavelength-Reflected Power Chart");
        XYSeries dataset = new XYSeries("First");
        for (int i = 0; i < lambdaList.length; i++) {
            dataset.add(lambdaList[i], RList[i] * 100);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wavelength-Reflected Power Chart",
                "Wavelength (nm)",
                "Reflected Power (%)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

        processFrame(frame, chart);
    }

    public void addDepRChart(double[] addDepList, double[] r) {
        JFrame frame = new JFrame("Add dep thickness-Reflected Power Chart");
        XYSeries dataset = new XYSeries("First");
        for (int i = 0; i < addDepList.length; i++) {
            dataset.add(addDepList[i], r[i] * 100);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Add dep thickness-Reflected Power Chart",
                "Add dep thickness (nm)",
                "Reflected Power (%)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

        XYPlot plot =  chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        processFrame(frame, chart);
    }

    public void lambdaRChart(double[] lambdaList, double[] rList1, double[] rList2, String[] legend) {
        JFrame frame = new JFrame("Wavelength-Reflected Power Chart");
        XYSeries dataset1 = new XYSeries(legend[0]);
        for (int i = 0; i < lambdaList.length; i++) {
            dataset1.add(lambdaList[i], rList1[i] * 100);
        }
        XYSeries dataset2 = new XYSeries(legend[1]);
        for (int i = 0; i < lambdaList.length; i++) {
            dataset2.add(lambdaList[i], rList2[i] * 100);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset1);
        xyseriescollection.addSeries(dataset2);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wavelength-Reflected Power Chart",
                "Wavelength (nm)",
                "Reflected Power (%)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        processFrame(frame, chart);
    }

    public void lambdaRChart(double[] lambdaList, double[] rList1, double[] rList2, double[] rList3, String[] legend) {
        JFrame frame = new JFrame("Wavelength-Reflected Power Chart");
        XYSeries dataset1 = new XYSeries(legend[0]);
        for (int i = 0; i < lambdaList.length; i++) {
            dataset1.add(lambdaList[i], rList1[i] * 100);
        }
        XYSeries dataset2 = new XYSeries(legend[1]);
        for (int i = 0; i < lambdaList.length; i++) {
            dataset2.add(lambdaList[i], rList2[i] * 100);
        }
        XYSeries dataset3 = new XYSeries(legend[2]);
        for (int i = 0; i < lambdaList.length; i++) {
            dataset3.add(lambdaList[i], rList3[i] * 100);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset1);
        xyseriescollection.addSeries(dataset2);
        xyseriescollection.addSeries(dataset3);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wavelength-Reflected Power Chart",
                "Wavelength (nm)",
                "Reflected Power (%)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        processFrame(frame, chart);
    }

    public void lambdaTChart(double[] lambdaList, double[] tList) {
        JFrame frame = new JFrame("Wavelength-Transmitted Power Chart");
        XYSeries dataset = new XYSeries("First");
        for (int i = 0; i < lambdaList.length; i++) {
            dataset.add(lambdaList[i], tList[i] * 100);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wavelength-Transmitted Power Chart",
                "Wavelength (nm)",
                "Transmitted Power (%)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

        processFrame(frame, chart);
    }

    public void lambdaNKChart(double[] lambdaList, double[] materialNList, double[] materialKList) {
        JFrame frame = new JFrame("Wavelength-Material nk Chart");
        XYSeries dataset1 = new XYSeries("n");
        for (int i = 0; i < lambdaList.length; i++) {
            dataset1.add(lambdaList[i], materialNList[i]);
        }
        XYSeries dataset2 = new XYSeries("k");
        for (int i = 0; i < lambdaList.length; i++) {
            dataset2.add(lambdaList[i], materialKList[i]);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset1);
        xyseriescollection.addSeries(dataset2);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Wavelength-Material nk Chart",
                "Wavelength (nm)",
                "",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        processFrame(frame, chart);
    }

    public void thetaR(double[] thetaList, double[] rp) {
        JFrame frame = new JFrame("Theta-Reflected Power Chart");
        XYSeries dataset = new XYSeries("First");
        for (int i = 0; i < thetaList.length; i++) {
            dataset.add(thetaList[i], rp[i]);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Theta-Reflected Power Chart",
                "Theta (degree)",
                "Reflected Power",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

        processFrame(frame, chart);
    }

    public void ellipsometricChart(double[] ds, double[] psis, double[] deltas) {
        JFrame frame = new JFrame("Ellipsometric Parameters Chart");
        XYSeries dataset1 = new XYSeries("psi");
        for (int i = 0; i < ds.length; i++) {
            dataset1.add(ds[i], psis[i]);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset1);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Ellipsometric Parameters Chart",
                "Film thickness (nm)",
                "psi (degrees)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRenderer().setSeriesPaint(0, Color.BLACK);// 设置主plot为黑色


        // 第二个纵轴
        NumberAxis axis1 = new NumberAxis("delta (degrees)");
        axis1.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, axis1);// 添加到Y轴上
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);// 设置坐标轴位置
        XYSeries dataset2 = new XYSeries("delta");
        for (int i = 0; i < ds.length; i++) {
            dataset2.add(ds[i], deltas[i]);
        }
        XYSeriesCollection xyseriescollection2 = new XYSeriesCollection();
        xyseriescollection2.addSeries(dataset2);
        plot.setDataset(1, xyseriescollection2);
        plot.mapDatasetToRangeAxis(1, 1);
        StandardXYItemRenderer renderer1 = new StandardXYItemRenderer();
        plot.setRenderer(1, renderer1);
        renderer1.setSeriesPaint(0, Color.red);
        axis1.setLabelPaint(Color.red);
        axis1.setTickLabelPaint(Color.red);


        processFrame(frame, chart);
    }

    public void poynChart(double[] ds, double[] poyn) {
        JFrame frame = new JFrame("Depth-Poynting vector Chart");
        XYSeries dataset = new XYSeries("First");
        for (int i = 0; i < ds.length; i++) {
            dataset.add(ds[i], poyn[i]);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Depth-Poynting vector Chart",
                "Depth (nm)",
                "Poynting vector (a.u.)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

        processFrame(frame, chart);
    }

    public void poynChart(double[] ds, double[] poyn,double[] absor) {
        JFrame frame = new JFrame("Depth-Poynting-Absorption Chart");
        XYSeries dataset = new XYSeries("Poynting");
        for (int i = 0; i < ds.length; i++) {
            dataset.add(ds[i], poyn[i]);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Depth-Poynting-Absorption Chart",
                "Depth (nm)",
                "Poynting vector (a.u.)",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRenderer().setSeriesPaint(0, Color.BLACK);// 设置主plot为黑色


        // 第二个纵轴
        NumberAxis axis1 = new NumberAxis("Absorption");
        axis1.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, axis1);// 添加到Y轴上
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);// 设置坐标轴位置
        XYSeries dataset2 = new XYSeries("delta");
        for (int i = 0; i < ds.length; i++) {
            dataset2.add(ds[i], absor[i]);
        }
        XYSeriesCollection xyseriescollection2 = new XYSeriesCollection();
        xyseriescollection2.addSeries(dataset2);
        plot.setDataset(1, xyseriescollection2);
        plot.mapDatasetToRangeAxis(1, 1);
        StandardXYItemRenderer renderer1 = new StandardXYItemRenderer();
        plot.setRenderer(1, renderer1);
        renderer1.setSeriesPaint(0, Color.red);
        axis1.setLabelPaint(Color.red);
        axis1.setTickLabelPaint(Color.red);

        processFrame(frame, chart);
    }

    public void distanceE(double[] ds, double[] es, double[] ep, double[] thicknesses) {
        JFrame frame = new JFrame("Distance-Normalized Electric Field Intensity Chart");
        double maxValue= 0;
        for(int i=0;i<es.length;i++){
            if(es[i]>maxValue){
                maxValue=es[i];
            }
            if(ep[i]>maxValue){
                maxValue=ep[i];
            }
        }
        XYSeries dataset1 = new XYSeries("Es");
        for (int i = 0; i < ds.length; i++) {
            dataset1.add(ds[i], es[i]);
        }
        XYSeries dataset2 = new XYSeries("Ep");
        for (int i = 0; i < ds.length; i++) {
            dataset2.add(ds[i], ep[i]);
        }
        XYSeries dataset3 = new XYSeries("Layer 0");
        dataset3.add(0,0);
        dataset3.add(0,maxValue);
        XYSeries[] datasets = new XYSeries[thicknesses.length];
        for(int i=0;i<datasets.length;i++){
            datasets[i]=new XYSeries("Layer"+(i+1));
            datasets[i].add(thicknesses[i],0);
            datasets[i].add(thicknesses[i],maxValue);
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(dataset1);
        xyseriescollection.addSeries(dataset2);
        xyseriescollection.addSeries(dataset3);
        for(int i=0;i<datasets.length;i++){
            xyseriescollection.addSeries(datasets[i]);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Distance-Normalized Electric Field Intensity Chart",
                "Distance (nm)",
                "",
                xyseriescollection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        processFrame(frame, chart);
    }

    private void processFrame(JFrame frame, JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1200, 900));
        frame.setContentPane(chartPanel);

        frame.setSize(1200, 900);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
