package com.tone.tmm;

import com.tone.tmm.adddep.CoatingSimple;
import com.tone.tmm.adddep.LayerSimple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.complex.Complex;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coating {
    private double refLambdaInNm; // in nm
    private double[] lambdaListInNm; // in nm
    private double incidentAngle; // in degree
    private Material incidentMaterial;
    private Layer[] layers;
    private Material exitMaterial;

    private SplineNk[] splineNks;

    public double[] calReflectedPower(char polarization) {
        double[] RList = new double[lambdaListInNm.length];
        Complex[] nList;
        Complex[] dList = processDList();
        for (int i = 0; i < lambdaListInNm.length; i++) {
            nList = processNList(lambdaListInNm[i]);
            if (polarization == 'p' | polarization == 's') {
                RList[i] = (double) TmmCore.cohTmm(
                        polarization,
                        nList,
                        dList,
                        Complex.valueOf(incidentAngle * TmmCore.DEGREE),
                        lambdaListInNm[i])
                        .get("R");
            } else if (polarization == 'a') {
                RList[i] = TmmCore.unpolarizedRT(
                        nList,
                        dList,
                        Complex.valueOf(incidentAngle * TmmCore.DEGREE),
                        lambdaListInNm[i])
                        .get("R");
            }
        }
//        for (int i = 0; i < RList.length; i++) {
//            System.out.println(lambdaListInNm[i] + "\t" + RList[i]);
//        }
        return RList;
    }

    public double calReflectedPower(char polarization, double lambda) {
        double R = 0;
        Complex[] nList = processNList(lambda);
        Complex[] dList = processDList();
        if (polarization == 'p' | polarization == 's') {
            R = (double) TmmCore.cohTmm(
                    polarization,
                    nList,
                    dList,
                    Complex.valueOf(incidentAngle * TmmCore.DEGREE),
                    lambda)
                    .get("R");
        } else if (polarization == 'a') {
            R = TmmCore.unpolarizedRT(
                    nList,
                    dList,
                    Complex.valueOf(incidentAngle * TmmCore.DEGREE),
                    lambda)
                    .get("R");
        }
        return R;
    }

    public Map<String, Object> cohTmm(char polarization, double lambda) {
        Complex[] nList = processNList(lambda);
        Complex[] dList = processDList();
        return TmmCore.cohTmm(
                polarization,
                nList,
                dList,
                Complex.valueOf(incidentAngle * TmmCore.DEGREE),
                lambda);
    }

    public CoatingSimple getCoatingSimple() {
        CoatingSimple cs = new CoatingSimple();
        cs.setRefLambdaInNm(refLambdaInNm);
        cs.setIncidentAngle(incidentAngle);
        cs.setIncidentN(new SplineNk(incidentMaterial).nk(refLambdaInNm).getReal());
        cs.setExitN(new SplineNk(exitMaterial).nk(refLambdaInNm).getReal());
        LayerSimple[] ls = new LayerSimple[layers.length];
        for (int i = 0; i < ls.length; i++) {
            LayerSimple l = new LayerSimple(
                    new SplineNk(layers[i].getMaterial()).nk(refLambdaInNm).getReal(),
                    layers[i].getThickness());
            ls[i] = l;
        }
        cs.setLayers(ls);
        return cs;
    }

    public double calTotalThickness() {
        double totalThickness = 0;
        for (Layer layer : layers) {
            totalThickness += layer.getThickness();
        }
        return totalThickness;
    }

    public Map<String, double[]> getNormalizedE(double[] ds) {
        Map<String, double[]> map = new HashMap<>();
        Complex[] dList = processDList();
        Map<String, Object> cohTmmDataP = cohTmm('p', refLambdaInNm);
        Map<String, Object> cohTmmDataS = cohTmm('s', refLambdaInNm);
        double[] Exs = new double[ds.length];
        double[] Eys = new double[ds.length];
        double[] Ezs = new double[ds.length];
        double[] Es = new double[ds.length];
        for (int i = 0; i < ds.length; i++) {
            Map<String, Object> map2 = TmmCore.findInStructureWithInf(dList, Complex.valueOf(ds[i]));
            Map<String, Object> data = TmmCore.positionResolved((int) map2.get("layer"), (Complex) map2.get("distance"), cohTmmDataS);
            Exs[i] = Math.pow(((Complex) data.get("Ex")).abs(), 2);
            Eys[i] = Math.pow(((Complex) data.get("Ey")).abs(), 2);
            Ezs[i] = Math.pow(((Complex) data.get("Ez")).abs(), 2);
            Es[i] = Exs[i] + Eys[i] + Ezs[i];
        }
        double[] Exp = new double[ds.length];
        double[] Eyp = new double[ds.length];
        double[] Ezp = new double[ds.length];
        double[] Ep = new double[ds.length];
        for (int i = 0; i < ds.length; i++) {
            Map<String, Object> map2 = TmmCore.findInStructureWithInf(dList, Complex.valueOf(ds[i]));
            Map<String, Object> data = TmmCore.positionResolved((int) map2.get("layer"), (Complex) map2.get("distance"), cohTmmDataP);
            Exp[i] = Math.pow(((Complex) data.get("Ex")).abs(), 2);
            Eyp[i] = Math.pow(((Complex) data.get("Ey")).abs(), 2);
            Ezp[i] = Math.pow(((Complex) data.get("Ez")).abs(), 2);
            Ep[i] = Exp[i] + Eyp[i] + Ezp[i];
        }
        map.put("Es", Es);
        map.put("Ep", Ep);
        return map;
    }

    public double[] calThicknesses() {
        double[] thicknesses = new double[layers.length];
        double sum = 0;
        for (int i = 0; i < layers.length; i++) {
            thicknesses[i] = layers[i].getThickness() + sum;
            sum = thicknesses[i];
        }
        return thicknesses;
    }

    private void processSplineNKUtil() {
        if (splineNks == null) {
            splineNks = new SplineNk[layers.length + 2];
            splineNks[0] = new SplineNk(incidentMaterial);
            for (int j = 0; j < layers.length; j++) {
                splineNks[j + 1] = new SplineNk(layers[j].getMaterial());
            }
            splineNks[splineNks.length - 1] = new SplineNk(exitMaterial);
        }
    }

    private Complex[] processDList() {
        Complex[] dList = new Complex[layers.length + 2];
        dList[0] = Complex.INF;
        for (int j = 0; j < layers.length; j++) {
            dList[j + 1] = Complex.valueOf(layers[j].getThickness());
        }
        dList[dList.length - 1] = Complex.INF;
        return dList;
    }

    private Complex[] processNList(double lambda) {
        Complex[] nList = new Complex[layers.length + 2];
        processSplineNKUtil();
        nList[0] = splineNks[0].nk(lambda);
        for (int j = 0; j < layers.length; j++) {
            nList[j + 1] = splineNks[j + 1].nk(lambda);
        }
        nList[nList.length - 1] = splineNks[splineNks.length - 1].nk(lambda);
        return nList;
    }

    public double calBandwidth(char polarization, double upperRLimit) {
        double R0 = calReflectedPower(polarization, this.getRefLambdaInNm());
        if (R0 > upperRLimit) {
            return 0;
        }
        double[] R = calReflectedPower(polarization);
        int lambdaIndex = 0;
        for (int i = 0; i < lambdaListInNm.length - 1; i++) {
            if (lambdaListInNm[i] <= this.getRefLambdaInNm() && lambdaListInNm[i + 1] >= this.getRefLambdaInNm()) {
                lambdaIndex = i;
                break;
            }
        }
        double lambdaMin = lambdaListInNm[lambdaIndex];
        for (int i = lambdaIndex; i > 0; i--) {
            if (R[i] < upperRLimit) {
                lambdaMin = lambdaListInNm[i];
            }else{
                break;
            }
        }
        double lambdaMax = lambdaListInNm[lambdaIndex];
        for (int i = lambdaIndex; i < lambdaListInNm.length - 1; i++) {
            if (R[i] < upperRLimit) {
                lambdaMax = lambdaListInNm[i];
            }else{
                break;
            }
        }
        return lambdaMax - lambdaMin;
    }
}
