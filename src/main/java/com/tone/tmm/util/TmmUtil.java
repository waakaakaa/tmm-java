package com.tone.tmm.util;

import org.apache.commons.math3.complex.Complex;

import java.util.Arrays;

public class TmmUtil {
    // Python linspace函数的Java版本
    public static double[] linspace(double start, double stop, int num) {
        double[] array = new double[num];
        double temp = start;
        double stepLength = (stop - start) / num;
        for (int i = 0; i < num; i++) {
            array[i] = temp;
            temp = temp + stepLength;
        }
        return array;
    }

    public static double[] makeArray(double center, double step, int num) {
        double[] array = new double[num];
        double start = center - step * (double)num / 2;
        for (int i = 0; i < array.length; i++) {
            array[i] = start;
            start += step;
        }
        return array;
    }

    // 二维复数矩阵乘法。apache库的矩阵乘法似乎只支持实数矩阵，什么鬼？（下同）
    public static Complex[][] dot(Complex[][] mat1, Complex[][] mat2) {
        int m = mat1.length;
        int n = mat2.length;
        Complex[][] mat = new Complex[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                mat[i][j] = Complex.ZERO;
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < mat1[0].length; k++) {
                    mat[i][j] = mat1[i][k].multiply(mat2[k][j]).add(mat[i][j]);
                }
            }
        }
        return mat;
    }

    // 二维复数矩阵×复数一维矩阵的乘法
    public static Complex[] dot(Complex[][] mat1, Complex[] mat2) {
        int m = mat1.length;
        Complex[] mat = new Complex[m];
        for (int i = 0; i < m; i++) {
            mat[i] = Complex.ZERO;
        }
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < mat1[0].length; k++) {
                mat[i] = mat1[i][k].multiply(mat2[k]).add(mat[i]);
            }
        }
        return mat;
    }

    // 复数×复数二维矩阵的乘法
    public static Complex[][] dot(Complex a, Complex[][] mat1) {
        int m = mat1.length;
        int n = mat1[0].length;
        Complex[][] mat2 = new Complex[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                mat2[i][j] = a.multiply(mat1[i][j]);
            }
        }
        return mat2;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(makeArray(1310,0.001,11)));
    }
}
