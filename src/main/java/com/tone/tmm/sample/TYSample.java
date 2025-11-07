package com.tone.tmm.sample;

import com.tone.tmm.*;
import com.tone.tmm.chart.LineChartFactory;
import com.tone.tmm.util.TmmUtil;
import org.apache.commons.math3.complex.Complex;

import java.util.Map;

public class TYSample {
    public static void AR01_1310_SiNx_Si_SiO2_0deg_old() {
        double lambdaTarget = 1310;
        Complex[] dList = new Complex[]{
                Complex.INF,
                Complex.valueOf(56.20),
                Complex.valueOf(23.30),
                Complex.valueOf(246.30),
                Complex.INF};
        Complex[] nList = new Complex[]{
                Complex.valueOf(3.23191),
                Complex.valueOf(2.00),
                Complex.valueOf(4.00),
                Complex.valueOf(1.46),
                Complex.valueOf(1.00)};
        double[] lambdaList = TmmUtil.linspace(1100, 1600, 100);
        double[] RList = new double[lambdaList.length];
        for (int i = 0; i < lambdaList.length; i++) {
            RList[i] = (double) TmmCore.cohTmm('s', nList, dList, Complex.ZERO, lambdaList[i]).get("R");
        }
        new LineChartFactory().lambdaRChart(lambdaList, RList);
    }

    public static void AR01_1310_SiNx_Si_SiO2_0deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1100, 1500, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 56.20),
                new Layer(Material.TY_Si, 23.3),
                new Layer(Material.TY_SiO2, 246.30)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void AR02_1310_SiNx_Si_SiO2_6d3deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1100, 1500, 1000));
        coating.setIncidentAngle(6.3);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 66.31),
                new Layer(Material.TY_Si, 24.59),
                new Layer(Material.TY_SiO2, 271.45)});
        new LineChartFactory().lambdaRChart(
                coating.getLambdaListInNm(),
                coating.calReflectedPower('s'),
                coating.calReflectedPower('p'),
                coating.calReflectedPower('a'),
                new String[]{"s", "p", "average"});
    }

    public static void AR03_1310_Ta2O5_Si_SiO2_0deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1100, 1500, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Ta2O5, 62.07),
                new Layer(Material.TY_Si, 20.36),
                new Layer(Material.TY_SiO2, 245.23)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void AR04_1290_SiNx_Si_SiO2_0deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1290);
        coating.setLambdaListInNm(TmmUtil.linspace(1100, 1500, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 55.34),
                new Layer(Material.TY_Si, 22.86),
                new Layer(Material.TY_SiO2, 242.54)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void AR05_1290_SiNx_Si_SiO2_0deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1563);
        coating.setLambdaListInNm(TmmUtil.linspace(1300, 1686, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP18515);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 68.03),
                new Layer(Material.TY_Si, 28.46),
                new Layer(Material.TY_SiO2, 294.52)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void HR01_1310_SiNx_SiO2_Si_SiO2_Si_0deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1000, 1500, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 20),
                new Layer(Material.TY_SiO2, 82.79),
                new Layer(Material.TY_Si, 94.43),
                new Layer(Material.TY_SiO2, 150),
                new Layer(Material.TY_Si, 95.29)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void HR02_1310_SiNx_SiO2_Si_SiO2_Ta2O5_Si_0deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1000, 1500, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 20),
                new Layer(Material.TY_SiO2, 82.85),
                new Layer(Material.TY_Si, 85.86),
                new Layer(Material.TY_SiO2, 150),
                new Layer(Material.TY_Ta2O5, 70),
                new Layer(Material.TY_Si, 71.73)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void HR03_1310_SiNx_SiO2_Si_SiO2_Si_0deg() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1563);
        coating.setLambdaListInNm(TmmUtil.linspace(1100, 1680, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP18515);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 20),
                new Layer(Material.TY_SiO2, 108.88),
                new Layer(Material.TY_Si, 104.03),
                new Layer(Material.TY_SiO2, 248.52),
                new Layer(Material.TY_Si, 104.22)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void testTa2O5(){
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1200, 1500, 300));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4_new, 56.2),
                new Layer(Material.TY_Si_new, 23.3),
                new Layer(Material.TY_Ta2O5_new, 320.86),
                new Layer(Material.TY_SiO2_new, 246.3)});
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), coating.calReflectedPower('s'));
    }

    public static void HR01_ElectricFieldIntensity() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1000, 1500, 1000));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 20),
                new Layer(Material.TY_SiO2, 82.79),
                new Layer(Material.TY_Si, 94.43),
                new Layer(Material.TY_SiO2, 150),
                new Layer(Material.TY_Si, 95.29)});

        double[] ds = TmmUtil.linspace(-200, coating.calTotalThickness()+100, 100);// 结构中的位置
        Map<String,double[]> map = coating.getNormalizedE(ds);
        double[] Es = map.get("Es");
        double[] Ep = map.get("Ep");
        double[] thicknesses = coating.calThicknesses();
        new LineChartFactory().distanceE(ds, Es, Ep,thicknesses);
    }

    public static void main(String[] args) {
        AR01_1310_SiNx_Si_SiO2_0deg();
        //AR02_1310_SiNx_Si_SiO2_6d3deg();
        //AR03_1310_Ta2O5_Si_SiO2_0deg();
        //AR04_1290_SiNx_Si_SiO2_0deg();
        //AR05_1290_SiNx_Si_SiO2_0deg();
        HR01_1310_SiNx_SiO2_Si_SiO2_Si_0deg();
        //HR02_1310_SiNx_SiO2_Si_SiO2_Ta2O5_Si_0deg();
        //HR03_1310_SiNx_SiO2_Si_SiO2_Si_0deg();
        HR01_ElectricFieldIntensity();
    }
}
