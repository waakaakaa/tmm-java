package com.tone.tmm.optimizer;

import com.tone.tmm.Coating;
import com.tone.tmm.Layer;
import com.tone.tmm.Material;
import com.tone.tmm.chart.HeatMapFactory;
import com.tone.tmm.chart.LineChartFactory;
import com.tone.tmm.util.TmmUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Optimizer {
    private static final double UPPER_R_LIMIT = 0.0005;
    private static final double MINIMUM_LAYER_THICKNESS = 2;
    private static double[] lambdaListForSquareSum = TmmUtil.linspace(1210, 1410, 200);
    private static Material[] materialsToUse = {
            Material.TY_Si_new,
            Material.TY_SiO2_new,
            Material.TY_Ta2O5_new,
            Material.TY_Si3N4_new,
            Material.TY_Al2O3
    };
    private Coating coating;

    public Optimizer(Coating coating) {
        this.coating = coating;
    }

    public static void example1() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1000, 1600, 600));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 20),
                new Layer(Material.TY_Si, 100),
                new Layer(Material.TY_Ta2O5, 100),
                new Layer(Material.TY_SiO2, 220)});

        double[] thicknessTaO = TmmUtil.linspace(50, 150, 50);
        double[] thicknessSiO2 = TmmUtil.linspace(50, 150, 50);
        double[][] R = new double[50][50];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                coating.setLayers(new Layer[]{
                        new Layer(Material.TY_Si3N4, 20),
                        new Layer(Material.TY_Si, 100),
                        new Layer(Material.TY_Ta2O5, thicknessTaO[i]),
                        new Layer(Material.TY_SiO2, thicknessSiO2[j])});
                R[i][j] = coating.calReflectedPower('p', coating.getRefLambdaInNm());
            }
        }
        new HeatMapFactory().xyRMap(
                "TaO", thicknessTaO[0], thicknessTaO[49],
                "SiO2", thicknessSiO2[0], thicknessSiO2[49],
                R);
    }

    public static void example2() {
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1000, 1600, 600));
        coating.setIncidentAngle(0);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si_new, 158.64),
                new Layer(Material.TY_Ta2O5_new, 94.56),
                new Layer(Material.TY_SiO2_new, 146.31)});
        double[] R1 = coating.calReflectedPower('p');

        double[] var1 = TmmUtil.linspace(157, 167, 20);
        double[] var2 = TmmUtil.linspace(107, 117, 20);
        double[] var3 = TmmUtil.linspace(116, 126, 20);
        double[] lambdaList = TmmUtil.linspace(1210, 1410, 200);
        double[][][] R = new double[var1.length][var2.length][var3.length];
        double[][][] squareSum = new double[var1.length][var2.length][var3.length];
        double[][][] bandWidth = new double[var1.length][var2.length][var3.length];

        for (int i = 0; i < var1.length; i++) {
            for (int j = 0; j < var2.length; j++) {
                for (int k = 0; k < var3.length; k++) {
                    coating.setLayers(new Layer[]{
                            new Layer(Material.TY_Si_new, var1[i]),
                            new Layer(Material.TY_Ta2O5_new, var2[j]),
                            new Layer(Material.TY_SiO2_new, var3[k])});
                    R[i][j][k] = coating.calReflectedPower('p', coating.getRefLambdaInNm());
                    bandWidth[i][j][k] = coating.calBandwidth('p', UPPER_R_LIMIT);
                    squareSum[i][j][k] = 0;
                    for (double v : lambdaList) {
                        double Rtemp = coating.calReflectedPower('p', v);
                        squareSum[i][j][k] += Rtemp * Rtemp;
                    }
                }
            }
        }
        int minI = 0;
        int minJ = 0;
        int minK = 0;
        double score = 0;
        for (int i = 0; i < var1.length; i++) {
            for (int j = 0; j < var2.length; j++) {
                for (int k = 0; k < var3.length; k++) {
                    double temp = 1 / R[i][j][k] * 0.0 + 1 / squareSum[i][j][k] * 1 + bandWidth[i][j][k] * 0.0;
                    if (temp > score) {
                        score = temp;
                        minI = i;
                        minJ = j;
                        minK = k;
                    }
                }
            }
        }
        System.out.println("i=" + minI + " j=" + minJ + " k=" + minK);
        System.out.println("Thickness=" + var1[minI] + " " + var2[minJ] + " " + var3[minK]);
        System.out.println("R=万分之" + (R[minI][minJ][minK] * 10000) + " squareSum=" + squareSum[minI][minJ][minK] + " bandWidth=" + bandWidth[minI][minJ][minK]);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si_new, var1[minI]),
                new Layer(Material.TY_Ta2O5_new, var2[minJ]),
                new Layer(Material.TY_SiO2_new, var3[minK])});
        double[] R2 = coating.calReflectedPower('p');
        new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), R1, R2, new String[]{"Before", "After"});
        System.out.println("bandWidth=" + coating.calBandwidth('p', UPPER_R_LIMIT));
    }

    private double optimiza1LayerAR(char polarization, Material material, double[] var1) {
        double[] squareSum = new double[var1.length];
        int minI = 0;
        double bestThickness = 0;
        double score = 0;
        for (int i = 0; i < var1.length; i++) {
            coating.setLayers(new Layer[]{new Layer(material, var1[i])});
            squareSum[i] = 0;
            for (double v : lambdaListForSquareSum) {
                double Rtemp = coating.calReflectedPower(polarization, v);
                squareSum[i] += Rtemp * Rtemp;
            }
            double temp = 1 / squareSum[i];
            if (temp > score) {
                score = temp;
                minI = i;
            }
        }
        bestThickness = var1[minI];
        double step = var1[1] - var1[0];
        if (step > 0.01) {
            double stepNew = step / 2;
            double[] varNew = TmmUtil.makeArray(var1[minI], stepNew, var1.length);
            bestThickness = optimiza1LayerAR(polarization, material, varNew);
        }
        return bestThickness;
    }

    private double[] optimiza2LayerAR(char polarization, Material[] materials, double[] var1, double[] var2) {
        double[][] squareSum = new double[var1.length][var2.length];
        int minI = 0;
        int minJ = 0;
        double[] bestThickness = new double[2];
        double score = 0;
        for (int i = 0; i < var1.length; i++) {
            for (int j = 0; j < var2.length; j++) {
                coating.setLayers(new Layer[]{
                        new Layer(materials[0], var1[i]),
                        new Layer(materials[1], var2[j]),
                });
                squareSum[i][j] = 0;
                for (double v : lambdaListForSquareSum) {
                    double Rtemp = coating.calReflectedPower(polarization, v);
                    squareSum[i][j] += Rtemp * Rtemp;
                }
                double temp = 1 / squareSum[i][j];
                if (temp > score) {
                    score = temp;
                    minI = i;
                    minJ = j;
                }
            }
        }
        bestThickness[0] = var1[minI];
        bestThickness[1] = var2[minJ];
        double step1 = var1[1] - var1[0];
        double step2 = var2[1] - var2[0];
        if (step1 > 0.01 || step2 > 0.01) {
            double stepNew1 = step1 / 2;
            double stepNew2 = step2 / 2;
            double[] varNew1 = TmmUtil.makeArray(var1[minI], stepNew1, var1.length);
            double[] varNew2 = TmmUtil.makeArray(var2[minJ], stepNew2, var2.length);
            bestThickness = optimiza2LayerAR(polarization, materials, varNew1, varNew2);
        }
        return bestThickness;
    }

    private double[] optimiza3LayerAR(char polarization, Material[] materials, double[] var1, double[] var2, double[] var3) {
        double[][][] squareSum = new double[var1.length][var2.length][var3.length];
        int minI = 0;
        int minJ = 0;
        int minK = 0;
        double[] bestThickness = new double[3];
        double score = 0;
        for (int i = 0; i < var1.length; i++) {
            for (int j = 0; j < var2.length; j++) {
                for (int k = 0; k < var3.length; k++) {
                    coating.setLayers(new Layer[]{
                            new Layer(materials[0], var1[i]),
                            new Layer(materials[1], var2[j]),
                            new Layer(materials[2], var3[k])
                    });
                    squareSum[i][j][k] = 0;
                    for (double v : lambdaListForSquareSum) {
                        double Rtemp = coating.calReflectedPower(polarization, v);
                        squareSum[i][j][k] += Rtemp * Rtemp;
                    }
                    double temp = 1 / squareSum[i][j][k];
                    if (temp > score) {
                        score = temp;
                        minI = i;
                        minJ = j;
                        minK = k;
                    }
                }
            }
        }
        bestThickness[0] = var1[minI];
        bestThickness[1] = var2[minJ];
        bestThickness[2] = var3[minK];
        double step1 = var1[1] - var1[0];
        double step2 = var2[1] - var2[0];
        double step3 = var3[1] - var3[0];
        System.out.println("\t"+new Date()
                + "\t" + Arrays.toString(materials)
                + "\tbestThk:\t" + Arrays.toString(bestThickness));
        if (step1 > 0.01 || step2 > 0.01 || step3 > 0.01) {
            double stepNew1 = step1 / 2;
            double stepNew2 = step2 / 2;
            double stepNew3 = step3 / 2;
            double[] varNew1 = TmmUtil.makeArray(var1[minI], stepNew1, var1.length);
            double[] varNew2 = TmmUtil.makeArray(var2[minJ], stepNew2, var2.length);
            double[] varNew3 = TmmUtil.makeArray(var3[minK], stepNew3, var3.length);
            bestThickness = optimiza3LayerAR(polarization, materials, varNew1, varNew2, varNew3);
        }
        return bestThickness;
    }

    private double[] optimiza4LayerAR(char polarization, Material[] materials, double[] var1, double[] var2, double[] var3, double[] var4) {
        double[][][][] squareSum = new double[var1.length][var2.length][var3.length][var4.length];
        int minI = 0;
        int minJ = 0;
        int minK = 0;
        int minQ = 0;
        double[] bestThickness = new double[4];
        double score = 0;
        for (int i = 0; i < var1.length; i++) {
            for (int j = 0; j < var2.length; j++) {
                for (int k = 0; k < var3.length; k++) {
                    for (int q = 0; q < var4.length; q++) {
                        coating.setLayers(new Layer[]{
                                new Layer(materials[0], var1[i]),
                                new Layer(materials[1], var2[j]),
                                new Layer(materials[2], var3[k]),
                                new Layer(materials[3], var4[q])
                        });
                        squareSum[i][j][k][q] = 0;
                        for (double v : lambdaListForSquareSum) {
                            double Rtemp = coating.calReflectedPower(polarization, v);
                            squareSum[i][j][k][q] += Rtemp * Rtemp;
                        }
                        double temp = 1 / squareSum[i][j][k][q];
                        if (temp > score) {
                            score = temp;
                            minI = i;
                            minJ = j;
                            minK = k;
                            minQ = q;
                        }
                    }
                }
            }
        }
        bestThickness[0] = var1[minI];
        bestThickness[1] = var2[minJ];
        bestThickness[2] = var3[minK];
        bestThickness[3] = var4[minQ];
        System.out.println("------"+new Date()
                + "\t" + Arrays.toString(materials)
                + "\tbestThk:\t" + Arrays.toString(bestThickness));
        for (double bt : bestThickness) {
            if (bt <= MINIMUM_LAYER_THICKNESS) {
                return null;
            }
        }
        double step1 = var1[1] - var1[0];
        double step2 = var2[1] - var2[0];
        double step3 = var3[1] - var3[0];
        double step4 = var4[1] - var4[0];
        if (step1 > 0.1 || step2 > 0.1 || step3 > 0.1 || step4 > 0.1) {
            double stepNew1 = step1 * 0.5;
            double stepNew2 = step2 * 0.5;
            double stepNew3 = step3 * 0.5;
            double stepNew4 = step4 * 0.5;
            double[] varNew1 = TmmUtil.makeArray(var1[minI], stepNew1, var1.length);
            double[] varNew2 = TmmUtil.makeArray(var2[minJ], stepNew2, var2.length);
            double[] varNew3 = TmmUtil.makeArray(var3[minK], stepNew3, var3.length);
            double[] varNew4 = TmmUtil.makeArray(var4[minQ], stepNew4, var4.length);
            bestThickness = optimiza4LayerAR(polarization, materials, varNew1, varNew2, varNew3, varNew4);
        }
        return bestThickness;
    }

    public static void iterate1LayerAR() {
        char pol = 'p';
        for (Material material : materialsToUse) {
            Coating coating = new Coating();
            coating.setRefLambdaInNm(1310);
            coating.setLambdaListInNm(TmmUtil.linspace(1000, 1600, 600));
            coating.setIncidentAngle(0);
            coating.setIncidentMaterial(Material.InP23191);
            coating.setExitMaterial(Material.Air);
            coating.setLayers(new Layer[]{
                    new Layer(material, 146.31)
            });
            double[] R1 = coating.calReflectedPower(pol);

            double[] var1 = TmmUtil.linspace(10, 500, 100);

            Optimizer optimizer = new Optimizer(coating);
            double bestThickness = optimizer.optimiza1LayerAR('p', material, var1);
            coating.setLayers(new Layer[]{
                    new Layer(material, bestThickness)
            });
            System.out.println("material:\t" + material
                    + "\tbestThickness:\t" + bestThickness
                    + "\tR:\t" + coating.calReflectedPower(pol, coating.getRefLambdaInNm())
                    + "\tbandWidth:\t" + coating.calBandwidth(pol, UPPER_R_LIMIT));
            double[] R2 = coating.calReflectedPower(pol);
            new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), R1, R2, new String[]{"Before", "After"});
        }
    }

    public static void iterate2LayerAR() {
        char pol = 'p';
        Material[][] materialMatrix = listAll2Layers(materialsToUse);
        for (Material[] materials : materialMatrix) {
            Coating coating = new Coating();
            coating.setRefLambdaInNm(1310);
            coating.setLambdaListInNm(TmmUtil.linspace(1000, 1600, 600));
            coating.setIncidentAngle(0);
            coating.setIncidentMaterial(Material.InP23191);
            coating.setExitMaterial(Material.Air);
            coating.setLayers(new Layer[]{
                    new Layer(materials[0], 200),
                    new Layer(materials[1], 200),
            });
            double[] R1 = coating.calReflectedPower(pol);

            double[] var1 = TmmUtil.linspace(10, 500, 40);
            double[] var2 = TmmUtil.linspace(10, 500, 40);

            Optimizer optimizer = new Optimizer(coating);
            double[] bestThickness = optimizer.optimiza2LayerAR(pol, materials, var1, var2);
            coating.setLayers(new Layer[]{
                    new Layer(materials[0], bestThickness[0]),
                    new Layer(materials[1], bestThickness[1])
            });
            System.out.println("material:\t" + Arrays.toString(materials)
                    + "\tbestThickness:\t" + Arrays.toString(bestThickness)
                    + "\tR:\t" + coating.calReflectedPower(pol, coating.getRefLambdaInNm())
                    + "\tbandWidth:\t" + coating.calBandwidth(pol, UPPER_R_LIMIT));
            double[] R2 = coating.calReflectedPower(pol);
            new LineChartFactory().lambdaRChart(coating.getLambdaListInNm(), R1, R2, new String[]{"Before", "After"});
        }
    }

    public static void iterate3LayerAR() {
        char pol = 'p';
        Material[][] materialMatrix = listAll3Layers(materialsToUse);
        for (Material[] materials : materialMatrix) {
            Coating coating = new Coating();
            coating.setRefLambdaInNm(1310);
            coating.setLambdaListInNm(TmmUtil.linspace(1000, 1600, 600));
            coating.setIncidentAngle(0);
            coating.setIncidentMaterial(Material.InP23191);
            coating.setExitMaterial(Material.Air);
            coating.setLayers(new Layer[]{
                    new Layer(materials[0], 200),
                    new Layer(materials[1], 200),
                    new Layer(materials[2], 200),
            });

            double[] var1 = TmmUtil.linspace(10, 400, 20);
            double[] var2 = TmmUtil.linspace(10, 400, 20);
            double[] var3 = TmmUtil.linspace(10, 400, 20);

            Optimizer optimizer = new Optimizer(coating);
            double[] bestThickness = optimizer.optimiza3LayerAR(pol, materials, var1, var2, var3);
            coating.setLayers(new Layer[]{
                    new Layer(materials[0], bestThickness[0]),
                    new Layer(materials[1], bestThickness[1]),
                    new Layer(materials[2], bestThickness[2])
            });
            System.out.println("material:\t" + Arrays.toString(materials)
                    + "\tbestThickness:\t" + Arrays.toString(bestThickness)
                    + "\tR:\t" + coating.calReflectedPower(pol, coating.getRefLambdaInNm())
                    + "\tbandWidth:\t" + coating.calBandwidth(pol, UPPER_R_LIMIT));
        }
    }

    public static void iterate4LayerAR() {
        char pol = 'p';
        Material[][] materialMatrix = listAll4Layers(materialsToUse);
        for (Material[] materials : materialMatrix) {
            Coating coating = new Coating();
            coating.setRefLambdaInNm(1310);
            coating.setLambdaListInNm(TmmUtil.linspace(1000, 1600, 600));
            coating.setIncidentAngle(0);
            coating.setIncidentMaterial(Material.InP23191);
            coating.setExitMaterial(Material.Air);
            coating.setLayers(new Layer[]{
                    new Layer(materials[0], 200),
                    new Layer(materials[1], 200),
                    new Layer(materials[2], 200),
                    new Layer(materials[3], 200),
            });

            double[] var1 = TmmUtil.linspace(10, 400, 20);
            double[] var2 = TmmUtil.linspace(10, 400, 20);
            double[] var3 = TmmUtil.linspace(10, 400, 20);
            double[] var4 = TmmUtil.linspace(10, 400, 20);

            Optimizer optimizer = new Optimizer(coating);
            double[] bestThickness = optimizer.optimiza4LayerAR(pol, materials, var1, var2, var3, var4);
            if (bestThickness == null) {
                System.out.println(new Date()
                        + "\t" + Arrays.toString(materials)
                        + "\tbestThk:\tnull");
            } else {
                coating.setLayers(new Layer[]{
                        new Layer(materials[0], bestThickness[0]),
                        new Layer(materials[1], bestThickness[1]),
                        new Layer(materials[2], bestThickness[2]),
                        new Layer(materials[3], bestThickness[3])
                });
                System.out.println(new Date()
                        + "\t" + Arrays.toString(materials)
                        + "\tbestThk:\t" + Arrays.toString(bestThickness)
                        + "\tR:\t" + coating.calReflectedPower(pol, coating.getRefLambdaInNm())
                        + "\tbw:\t" + coating.calBandwidth(pol, UPPER_R_LIMIT));
            }
        }
    }

    private static Material[][] listAll4Layers(Material[] materialsToUse) {
        List<Material[]> list = new ArrayList<>();
        for (Material m1 : materialsToUse) {
            for (Material m2 : materialsToUse) {
                for (Material m3 : materialsToUse) {
                    for (Material m4 : materialsToUse) {
                        if (!m1.equals(m2) && !m2.equals(m3) && !m3.equals(m4)) {
                            list.add(new Material[]{m1, m2, m3, m4});
                        }
                    }
                }
            }
        }
        int size = list.size();
        Material[][] materialMatrix = new Material[size][4];
        for (int i = 0; i < size; i++) {
            materialMatrix[i] = list.get(i);
        }
        return materialMatrix;
    }

    private static Material[][] listAll3Layers(Material[] materialsToUse) {
        List<Material[]> list = new ArrayList<>();
        for (Material m1 : materialsToUse) {
            for (Material m2 : materialsToUse) {
                for (Material m3 : materialsToUse) {
                    if (!m1.equals(m2) && !m2.equals(m3)) {
                        list.add(new Material[]{m1, m2, m3});
                    }
                }
            }
        }
        int size = list.size();
        Material[][] materialMatrix = new Material[size][3];
        for (int i = 0; i < size; i++) {
            materialMatrix[i] = list.get(i);
        }
        return materialMatrix;
    }

    private static Material[][] listAll2Layers(Material[] materialsToUse) {
        List<Material[]> list = new ArrayList<>();
        for (Material m1 : materialsToUse) {
            for (Material m2 : materialsToUse) {
                if (!m1.equals(m2)) {
                    list.add(new Material[]{m1, m2});
                }
            }
        }
        int size = list.size();
        Material[][] materialMatrix = new Material[size][2];
        for (int i = 0; i < size; i++) {
            materialMatrix[i] = list.get(i);
        }
        return materialMatrix;
    }

    public static void main(String[] args) {
        //iterate1LayerAR();
        //iterate2LayerAR();
        //iterate3LayerAR();
        iterate4LayerAR();
    }
}
