package com.tone.tmm.adddep;

import com.tone.tmm.TmmCore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.complex.Complex;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoatingSimple {
    private double refLambdaInNm; // in nm
    private double incidentAngle; // in degree
    private double incidentN;
    private LayerSimple[] layers;
    private double exitN;

    public double calReflectedPower(char polarization) {
        double R = 0;
        Complex[] nList = new Complex[layers.length + 2];
        Complex[] dList = new Complex[layers.length + 2];

        nList[0] = Complex.valueOf(incidentN);
        dList[0] = Complex.INF;
        for (int j = 0; j < layers.length; j++) {
            nList[j + 1] = Complex.valueOf(layers[j].getN());
            dList[j + 1] = Complex.valueOf(layers[j].getThickness());
        }
        nList[nList.length - 1] = Complex.valueOf(exitN);
        dList[dList.length - 1] = Complex.INF;
        if (polarization == 'p' | polarization == 's') {
            R = (double) TmmCore.cohTmm(
                    polarization,
                    nList,
                    dList,
                    Complex.valueOf(incidentAngle * TmmCore.DEGREE),
                    refLambdaInNm)
                    .get("R");
        } else if (polarization == 'a') {
            R = TmmCore.unpolarizedRT(
                    nList,
                    dList,
                    Complex.valueOf(incidentAngle * TmmCore.DEGREE),
                    refLambdaInNm)
                    .get("R");
        }
        return R;
    }
}
