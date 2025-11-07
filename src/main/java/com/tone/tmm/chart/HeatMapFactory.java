package com.tone.tmm.chart;

import com.tone.heatmap.Gradient;
import com.tone.heatmap.HeatMap;

import javax.swing.*;
import java.awt.*;

public class HeatMapFactory {
    public void xyRMap(String x, double xmin,double xmax,String y,double ymin,double ymax,double[][] data) {
        JFrame frame = new JFrame("Heat Map Frame");
        HeatMap panel = new HeatMap(data, true, Gradient.GRADIENT_RAINBOW);

        panel.setDrawLegend(true);
        panel.setTitle("Reflectivity (a.u.)");
        panel.setDrawTitle(true);
        panel.setXAxisTitle(x);
        panel.setDrawXAxisTitle(true);
        panel.setYAxisTitle(y);
        panel.setDrawYAxisTitle(true);
        panel.setCoordinateBounds(xmin, xmax, ymin, ymax);
        panel.setDrawXTicks(true);
        panel.setDrawYTicks(true);
        panel.setColorForeground(Color.black);
        panel.setColorBackground(Color.white);

        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 900);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
