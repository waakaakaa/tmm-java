package com.tone.tmm;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.complex.Complex;

import java.io.*;

public class SplineNk {
    private PolynomialSplineFunction functionN;
    private PolynomialSplineFunction functionK;

    public SplineNk(Material material) {
        File file = new File("material\\" + material.toString() + ".txt");
        String line;
        int lineNum = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                lineNum++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        double[] lambdaList = new double[lineNum];
        double[] nList = new double[lineNum];
        double[] kList = new double[lineNum];
        int i = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] str = line.split("\t");
                lambdaList[i] = Double.parseDouble(str[0]);
                nList[i] = Double.parseDouble(str[1]);
                kList[i] = Double.parseDouble(str[2]);
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SplineInterpolator interpolatorN = new SplineInterpolator();
        functionN = interpolatorN.interpolate(lambdaList, nList);
        SplineInterpolator interpolatorK = new SplineInterpolator();
        functionK = interpolatorK.interpolate(lambdaList, kList);
    }

    public Complex nk(double lambda) {
        return Complex.valueOf(functionN.value(lambda), functionK.value(lambda));
    }
}
