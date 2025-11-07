package com.tone.tmm.sample;

import com.tone.tmm.chart.LineChartFactory;
import com.tone.tmm.TmmCore;
import com.tone.tmm.util.TmmUtil;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.complex.Complex;

import java.util.Map;

public class TmmSample {
    public static void samplePaper() {
        double lambdaTarget = 1310;
        Complex[] dList = new Complex[]{
                Complex.INF,
                Complex.valueOf(0.05445 * lambdaTarget / 1.58),
                Complex.valueOf(0.09943 * lambdaTarget / 3.56),
                Complex.valueOf(0.27729 * lambdaTarget / 1.44),
                Complex.INF};
        Complex[] nList = new Complex[]{
                Complex.valueOf(3.39),
                Complex.valueOf(1.58),
                Complex.valueOf(3.56),
                Complex.valueOf(1.44),
                Complex.valueOf(1.00)};
        double[] lambdaList = TmmUtil.linspace(1100, 1600, 100);
        double[] RList = new double[lambdaList.length];
        for (int i = 0; i < lambdaList.length; i++) {
            RList[i] = (double) TmmCore.cohTmm('s', nList, dList, Complex.ZERO, lambdaList[i]).get("R");
        }
        new LineChartFactory().lambdaRChart(lambdaList, RList);
    }

    public static void samplePaperTwoAngled() {
        double lambdaTarget = 1310;
        double angle = 6.3;
        String[] legend = {"0 degree", "6.3 degree"};
        Complex[] dList = new Complex[]{
                Complex.INF,
                Complex.valueOf(0.05445 * lambdaTarget / 1.58),
                Complex.valueOf(0.09943 * lambdaTarget / 3.56),
                Complex.valueOf(0.27729 * lambdaTarget / 1.44),
                Complex.INF};
        Complex[] nList = new Complex[]{
                Complex.valueOf(3.39),
                Complex.valueOf(1.58),
                Complex.valueOf(3.56),
                Complex.valueOf(1.44),
                Complex.valueOf(1.00)};
        double[] lambdaList = TmmUtil.linspace(1100, 1600, 100);
        double[] RList = new double[lambdaList.length];
        double[] RListAngled = new double[lambdaList.length];
        for (int i = 0; i < lambdaList.length; i++) {
            RList[i] = (double) TmmCore.cohTmm('s', nList, dList, Complex.ZERO, lambdaList[i]).get("R");
            RListAngled[i] = (double) TmmCore.cohTmm('s', nList, dList, Complex.valueOf(angle * TmmCore.DEGREE), lambdaList[i]).get("R");
        }
        new LineChartFactory().lambdaRChart(lambdaList, RList, RListAngled, legend);
    }


    /**
     * 这是一个薄的非吸收层，位于厚的吸收层之上，两侧都有空气。绘制反射强度与波数的关系图，在两个不同的入射角下。
     */
    public static void sample1() {
        // 厚度列表以nm为单位
        Complex[] dList = new Complex[]{
                Complex.INF,
                Complex.valueOf(100),
                Complex.valueOf(300),
                Complex.INF};
        // 折射率列表
        Complex[] nList = new Complex[]{
                Complex.valueOf(1),
                Complex.valueOf(2.2),
                Complex.valueOf(3.3, 0.3),
                Complex.valueOf(1)};
        // 要绘制的波数列表，单位为 nm^-1
        double[] ks = TmmUtil.linspace(0.0001, 0.01, 400);
        // 初始化要绘制的 y 值列表
        double[] Rnorm = new double[ks.length];
        double[] R45 = new double[ks.length];
        for (int i = 0; i < ks.length; i++) {
            // 对于正入射，s 和 p 是相同的。我随意决定使用 's'。
            Rnorm[i] = (double) TmmCore.cohTmm('s', nList, dList, Complex.ZERO, 1 / ks[i]).get("R");
            R45[i] = TmmCore.unpolarizedRT(nList, dList, Complex.valueOf(45 * TmmCore.DEGREE), 1 / ks[i]).get("R");
        }
        // 以 cm⁻¹ 而不是 nm⁻¹ 表示的 ks
        double[] kcm = new double[ks.length];
        for (int i = 0; i < kcm.length; i++) {
            kcm[i] = ks[i] * 1e7;
        }
        String[] legend = {"0 degree", "45 degree"};
        new LineChartFactory().kRChart(ks, Rnorm, R45, legend);
    }

    /**
     * 这是通过单层薄膜的透射强度随波长变化的情况，该薄膜具有某种复杂的波长依赖折射率。
     * 这些数据是我编造的，但在现实中可以从文献中发布的图表或表格中读取。
     * 薄膜两侧都是空气，光垂直入射。
     */
    public static void sample2() {
        double[] materialWavelength = {200.0, 300.0, 400.0, 500.0, 750.0};
        double[] mateiralN = {2.1, 2.4, 2.3, 2.2, 2.2};
        double[] materialK = {0.1, 0.3, 0.4, 0.4, 0.5};

        SplineInterpolator interpolatorN = new SplineInterpolator();
        PolynomialSplineFunction functionN = interpolatorN.interpolate(materialWavelength, mateiralN);

        SplineInterpolator interpolatorK = new SplineInterpolator();
        PolynomialSplineFunction functionK = interpolatorK.interpolate(materialWavelength, materialK);

        Complex[] dList = {Complex.INF, Complex.valueOf(300), Complex.INF};// 单位nm
        double[] lambdaList = TmmUtil.linspace(200, 750, 400);// 单位nm
        double[] materialNList = new double[lambdaList.length];
        double[] materialKList = new double[lambdaList.length];
        double[] TList = new double[lambdaList.length];

        for (int i = 0; i < lambdaList.length; i++) {
            double n = functionN.value(lambdaList[i]);
            double k = functionK.value(lambdaList[i]);
            materialNList[i] = n;
            materialKList[i] = k;
            Complex[] nList = {Complex.valueOf(1), Complex.valueOf(n, k), Complex.valueOf(1)};
            TList[i] = (double) TmmCore.cohTmm('s', nList, dList, Complex.ZERO, lambdaList[i]).get("T");
        }

        new LineChartFactory().lambdaNKChart(lambdaList, materialNList, materialKList);
        new LineChartFactory().lambdaTChart(lambdaList, TList);
    }

    /**
     * 这是在椭偏测量中计算psi和Delta参数的结果。
     * 这再现了Tompkins于2005年出版的《椭偏术手册》中图1.14的内容。
     */
    public static void sample3() {
        Complex[] nList = {
                Complex.valueOf(1),
                Complex.valueOf(1.46),
                Complex.valueOf(3.87, 0.02)};
        double[] ds = TmmUtil.linspace(0, 1000, 1000); // 单位：nm
        double[] psis = new double[ds.length];
        double[] deltas = new double[ds.length];
        for (int i = 0; i < ds.length; i++) {
            Complex[] dList = {Complex.INF, Complex.valueOf(ds[i]), Complex.INF};
            Map<String, Double> map = TmmCore.ellips(nList, dList, Complex.valueOf(70 * TmmCore.DEGREE), 633);
            psis[i] = map.get("psi") / TmmCore.DEGREE;
            deltas[i] = map.get("delta") / TmmCore.DEGREE;
        }
        new LineChartFactory().ellipsometricChart(ds, psis, deltas);
    }

    /**
     * 这里是一个示例，我们绘制了吸收和波印廷矢量随深度变化的关系。
     */
    public static void sample4() {
        Complex[] dList = {
                Complex.INF,
                Complex.valueOf(100),
                Complex.valueOf(300),
                Complex.INF};// 单位nm
        Complex[] nList = {
                Complex.valueOf(1),
                Complex.valueOf(2.2, 0.2),
                Complex.valueOf(3.3, 0.3),
                Complex.valueOf(1)};
        Complex th0 = Complex.valueOf(Math.PI / 4);
        double lamVac = 400;
        char polarization = 'p';
        Map<String, Object> cohTmmData = TmmCore.cohTmm(polarization, nList, dList, th0, lamVac);

        double[] ds = TmmUtil.linspace(-50, 400, 1000);// 结构中的位置
        double[] poyn = new double[ds.length];
        double[] absor = new double[ds.length];
        for (int i = 0; i < ds.length; i++) {
            Map<String, Object> map = TmmCore.findInStructureWithInf(dList, Complex.valueOf(ds[i]));
            int layer = (int) map.get("layer");
            Complex distance = (Complex) map.get("distance");
            Map<String, Object> data = TmmCore.positionResolved(layer, distance, cohTmmData);
            poyn[i] = (double) data.get("poyn");
            absor[i] = (double) data.get("absor") * 200;
        }
        new LineChartFactory().poynChart(ds, poyn, absor);
    }

    /**
     * 一个带有表面等离子体共振 (SPR) 谷的反射示例图。
     * 可与 http://doi.org/10.2320/matertrans.M2010003（“基于克雷希曼棱镜配置的表面等离子体共振的光谱和角度响应”）图 6a 进行比较
     */
    public static void sample6() {
        Complex[] dList = new Complex[]{
                Complex.INF,
                Complex.valueOf(5),
                Complex.valueOf(30),
                Complex.INF};
        Complex[] nList = new Complex[]{
                Complex.valueOf(1.517),
                Complex.valueOf(3.719, 4.362),
                Complex.valueOf(0.130, 3.162),
                Complex.valueOf(1.00)};
        double lamVac = 633;
        double[] thetaList = TmmUtil.linspace(
                30 * TmmCore.DEGREE,
                60 * TmmCore.DEGREE,
                300);
        double[] Rp = new double[thetaList.length];
        for (int i = 0; i < Rp.length; i++) {
            Rp[i] = (double) TmmCore.cohTmm('p', nList, dList, Complex.valueOf(thetaList[i]), lamVac).get("R");
        }
        new LineChartFactory().thetaR(thetaList, Rp);
    }



    public static void main(String[] args) {
        sample1();
        sample2();
        sample3();
        sample4();
        sample6();
    }
}
