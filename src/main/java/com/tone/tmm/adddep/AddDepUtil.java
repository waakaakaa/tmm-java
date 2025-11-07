package com.tone.tmm.adddep;

import com.tone.tmm.*;
import com.tone.tmm.chart.LineChartFactory;
import com.tone.tmm.util.TmmUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class AddDepUtil {
    private Coating designCoating;
    private double[] actualLayerThickness;
    private double[] actualLayerN;

    public Map<String, Double> checkAddDep(boolean plot) {
        Map<String, Double> map = new HashMap<>();
        CoatingSimple cs = designCoating.getCoatingSimple();
        LayerSimple[] ls = cs.getLayers();
        for (int i = 0; i < ls.length; i++) {
            LayerSimple l = new LayerSimple(actualLayerN[i], actualLayerThickness[i]);
            ls[i] = l;
        }
        double topLayerThickness = actualLayerThickness[actualLayerThickness.length - 1];
        int listSize = (int) (topLayerThickness * 0.1);
        double[] addDepList = new double[listSize];
        double[] R = new double[listSize];
        for (int i = 0; i < listSize; i++) {
            addDepList[i] = i;
            ls[ls.length - 1].setThickness(topLayerThickness + i);
            R[i] = cs.calReflectedPower('s');
        }
        if (plot) {
            new LineChartFactory().addDepRChart(addDepList, R);
        }
        double minR = 1.0;
        int minRIndex = 0;
        for (int i = 0; i < listSize; i++) {
            if (R[i] < minR) {
                minR = R[i];
                minRIndex = i;
            }
        }
        map.put("calculatedR", R[0]);
        map.put("minR", minR);
        map.put("addDepThickness", addDepList[minRIndex]);
        return map;
    }

    public static void main(String[] args) {
        // 新建补镀的工具类，建立初始膜系
        AddDepUtil sc = new AddDepUtil();
        Coating coating = new Coating();
        coating.setRefLambdaInNm(1310);
        coating.setLambdaListInNm(TmmUtil.linspace(1100, 1500, 1000));
        coating.setIncidentAngle(6.3);
        coating.setIncidentMaterial(Material.InP23191);
        coating.setExitMaterial(Material.Air);
        coating.setLayers(new Layer[]{
                new Layer(Material.TY_Si3N4, 56.20),
                new Layer(Material.TY_Si, 23.3),
                new Layer(Material.TY_SiO2, 246.30)});
        sc.setDesignCoating(coating);
        // 设置工具类的计算参数
        sc.setActualLayerThickness(new double[]{50, 20, 220});
        sc.setActualLayerN(new double[]{1.975, 3.9, 1.47});
        Map<String, Double> map = sc.checkAddDep(true);
        System.out.println(map);
    }
}
