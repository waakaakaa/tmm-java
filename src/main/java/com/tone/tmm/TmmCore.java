package com.tone.tmm;

import com.tone.tmm.util.TmmUtil;
import org.apache.commons.math3.complex.Complex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 本工具是由python tmm的库重写而成，原始库的信息，可从 https://github.com/sbyrnes321/tmm/blob/master/manual.pdf 获取。
 * 物理背景、约定、定义和推导请参见 https://arxiv.org/abs/1603.02720。
 * 最重要的两个函数是：
 * coh_tmm(...) —— 用于相干情况的传输矩阵法计算（即薄膜）
 * inc_tmm(...) —— 用于非相干情况的传输矩阵法计算（即厚度为几十至几百倍波长的薄膜，或厚度不太均匀的薄膜）。
 */
public class TmmCore {
    public static final double DEGREE = Math.PI / 180;
    public static final float EPSILON = 1e-8f;
    private static final Logger logger = LogManager.getLogger();

    private static Complex[][] make2By2Array(Complex a, Complex b, Complex c, Complex d) {
        Complex[][] myArray = new Complex[2][2];
        myArray[0][0] = a;
        myArray[0][1] = b;
        myArray[1][0] = c;
        myArray[1][1] = d;
        return myArray;
    }

    /**
     * 如果一个波在折射率为 n 的介质中以角度 θ 偏离法线传播，计算它是否为前向传播波（即从堆叠的前端到后端传播的波，就像入射或出射波一样，但不同于反射波）。
     * 对于实数 n 和 θ，判断标准很简单：-π/2 < θ < π/2，但对于复数 n 和 θ，情况则更复杂。
     * 请参见 https://arxiv.org/abs/1603.02720 附录 D。
     * 如果 θ 是前向角，那么 (π - θ) 就是后向角，反之亦然。
     */
    private static boolean isForwardAngle(Complex n, Complex theta) {
        assert n.getReal() * n.getImaginary() > 0 : "对于有增益的材料，光束是入射还是出射是模棱两可的。参见 https://arxiv.org/abs/1603.02720 附录 C.";
        Complex ncostheta = n.multiply(theta.cos());
        boolean answer;
        if (Math.abs(ncostheta.getImaginary()) > 100 * EPSILON) {
            answer = (ncostheta.getImaginary() > 0);
        } else {
            answer = (ncostheta.getReal() > 0);
        }
        String errorStr = "目前不清楚哪束光是入射光，哪束光是出射光。可能是折射率有问题？";
        if (answer) {
            assert ncostheta.getImaginary() > -100 * EPSILON : errorStr;
            assert ncostheta.getReal() > -100 * EPSILON : errorStr;
            assert n.multiply(theta.conjugate().cos()).getReal() > -100 * EPSILON : errorStr;
        } else {
            assert ncostheta.getImaginary() < 100 * EPSILON : errorStr;
            assert ncostheta.getReal() < 100 * EPSILON : errorStr;
            assert n.multiply(theta.conjugate().cos()).getReal() < 100 * EPSILON : errorStr;
        }
        return answer;
    }

    /**
     * 返回在折射率为 n_2 的第 2 层中的角度 θ，假设它在折射率为 n_1 的层中有角度 θ_1。
     * 使用斯涅尔定律。
     * 注意，“角度”可能是复数！！
     */
    private static Complex snell(Complex n1, Complex n2, Complex th1) {
        Complex th2Guess = n1.multiply(th1.sin()).divide(n2).asin();
        if (isForwardAngle(n2, th2Guess)) {
            return th2Guess;
        } else {
            return Complex.valueOf(Math.PI).subtract(th2Guess);
        }
    }

    /**
     * 基于第0层的角度 th_0，使用斯涅尔定律返回每一层的角度 theta 列表。
     *
     * @param nList 每层的折射率
     * @param th0   注意，“角度”可能是复数!!
     */
    private static Complex[] listSnell(Complex[] nList, Complex th0) {
        int size = nList.length;
        Complex angles[] = new Complex[size];
        for (int i = 0; i < size; i++) {
            angles[i] = nList[0].multiply(th0.sin()).divide(nList[i]).asin();
        }
        if (!isForwardAngle(nList[0], angles[0])) {
            angles[0] = Complex.valueOf(Math.PI).subtract(angles[0]);
        }
        if (!isForwardAngle(nList[size - 1], angles[size - 1])) {
            angles[size - 1] = Complex.valueOf(Math.PI).subtract(angles[size - 1]);
        }
        return angles;
    }

    /**
     * 反射振幅（来自菲涅耳方程）
     *
     * @param polarization 偏振类型为“s”或“p”
     * @param ni           入射的（复数）折射率
     * @param nf           最终的（复数）折射率
     * @param thi          入射的（复数）传播角（以弧度为单位，其中0=法线）
     * @param thf          最终的（复数）传播角（以弧度为单位，其中0=法线）
     */
    private static Complex interface_r(char polarization, Complex ni, Complex nf, Complex thi, Complex thf) {
        if (polarization == 's') {
            return ni.multiply(thi.cos()).subtract(nf.multiply(thf.cos()))
                    .divide(ni.multiply(thi.cos()).add(nf.multiply(thf.cos())));
        } else if (polarization == 'p') {
            return nf.multiply(thi.cos()).subtract(ni.multiply(thf.cos()))
                    .divide(nf.multiply(thi.cos()).add(ni.multiply(thf.cos())));
        } else {
            throw new RuntimeException("Polarization must be 's' or 'p'");
        }
    }

    /**
     * 传输振幅（由 Fresnel 方程求得）
     *
     * @param polarization 偏振类型为“s”或“p”
     * @param ni           入射的（复）折射率
     * @param nf           出射的（复）折射率
     * @param thi          入射的（复）传播角（以弧度为单位，0 = 法线方向）
     * @param thf          出射的（复）传播角（以弧度为单位，0 = 法线方向）
     */
    private static Complex interface_t(char polarization, Complex ni, Complex nf, Complex thi, Complex thf) {
        if (polarization == 's') {
            return Complex.valueOf(2).multiply(ni).multiply(thi.cos())
                    .divide(ni.multiply(thi.cos()).add(nf.multiply(thf.cos())));
        } else if (polarization == 'p') {
            return Complex.valueOf(2).multiply(ni).multiply(thi.cos())
                    .divide(nf.multiply(thi.cos()).add(ni.multiply(thf.cos())));
        } else {
            throw new RuntimeException("Polarization must be 's' or 'p'");
        }
    }

    /**
     * 从反射振幅 r 开始，计算反射功率 R。
     */
    private static double calR(Complex r) {
        return Math.pow(r.abs(), 2);
    }

    /**
     * 计算透射功率 T，从传输振幅 t 开始。
     * 当 n_i、n_f、th_i 和 th_f 为实数时，公式简化为：T=|t|^2 * (n_f cos(th_f)) / (n_i cos(th_i))。
     * 请参见 https://arxiv.org/abs/1603.02720 了解公式的讨论。
     *
     * @param polarization 偏振类型为“s”或“p”
     * @param t            传输振幅
     * @param ni           入射介质的折射率
     * @param nf           最终介质的折射率
     * @param thi          通过入射介质的（复）传播角（以弧度为单位，0 表示法线方向）
     * @param thf          通过最终介质的（复）传播角（以弧度为单位，0 表示法线方向）
     */
    private static double calT(char polarization, Complex t, Complex ni, Complex nf, Complex thi, Complex thf) {
        if (polarization == 's') {
            return t.pow(2).abs() *
                    (nf.multiply(thf.cos()).getReal() / (ni.multiply(thi.cos()).getReal()));
        } else if (polarization == 'p') {
            return t.pow(2).abs() *
                    (nf.multiply(thf.cos().conjugate()).getReal() / (ni.multiply(thi.cos().conjugate()).getReal()));
        } else {
            throw new RuntimeException("Polarization must be 's' or 'p'");
        }
    }

    /**
     * 计算进入堆栈第一个界面的功率，从反射振幅 r 开始。
     * 通常这等于 1-R，但在 n_i 不是实数的特殊情况下，它可能与 1-R 略有不同。
     * 请参见 https://arxiv.org/abs/1603.02720。
     *
     * @param polarization 偏振类型为“s”或“p”
     * @param r            反射振幅
     * @param ni           入射介质的折射率
     * @param thi          入射介质中的（复数）传播角（以弧度为单位，其中 0 表示垂直入射）
     */
    private static double calPowerEntering(char polarization, Complex r, Complex ni, Complex thi) {
        if (polarization == 's') {
            return ni.multiply(thi.cos()).multiply(Complex.valueOf(1).add(r.conjugate())).multiply(Complex.valueOf(1).subtract(r)).getReal()
                    / ni.multiply(thi.cos()).getReal();
        } else if (polarization == 'p') {
            return ni.multiply(thi.cos().conjugate()).multiply(Complex.valueOf(1).add(r)).multiply(Complex.valueOf(1).subtract(r.conjugate())).getReal()
                    / ni.multiply(thi.cos().conjugate()).getReal();
        } else {
            throw new RuntimeException("Polarization must be 's' or 'p'");
        }
    }

    /**
     * 在界面上反射的光强分数。
     */
    private static double interface_R(char polarization, Complex ni, Complex nf, Complex thi, Complex thf) {
        Complex r = interface_r(polarization, ni, nf, thi, thf);
        return calR(r);
    }

    /**
     * 在界面上透射的光强分数。
     */
    private static double interface_T(char polarization, Complex ni, Complex nf, Complex thi, Complex thf) {
        Complex t = interface_t(polarization, ni, nf, thi, thf);
        return calT(polarization, t, ni, nf, thi, thf);
    }

    /**
     * 主要的“相干传递矩阵方法”计算。给定堆叠的参数，计算你希望了解的光在其中传播的所有信息。（如果性能是问题，你可以删除一些计算而不影响其他部分。）
     * 这个函数，像包里的其他所有功能一样，隐式地要求你选择一个长度单位。你可以使用任何单位，但需要保持一致。例如，如果你以纳米为单位输入波长，那么你也必须以纳米为单位输入层的厚度。这样，任何角波数输出将以每纳米弧度为单位，而任何吸收输出将以（每纳米深度的入射光功率的分数）为单位，依此类推。
     * 然后该函数将以下内容作为字典输出（详情请参见 https://arxiv.org/abs/1603.02720）
     * r--反射振幅
     * t--透射振幅
     * R--反射波功率（占入射波的比例）
     * T--透射波功率（占入射波的比例）
     * power_entering--进入第一层的功率，通常（但不总是）等于 1-R（见 https://arxiv.org/abs/1603.02720 ）
     * vw_list--第 n 个元素为 [v_n,w_n]，分别表示在第 n 层与第 (n-1) 层界面刚接触处的前向和后向传播振幅
     * kz_list--每层中前向传播波的复数角波数的法向分量
     * th_list--每层的（复数）传播角（以弧度为单位）
     * pol, n_list, d_list, th_0, lam_vac--与输入相同
     *
     * @param polarization 光的偏振，“s” 或 “p”
     * @param nList        折射率的列表，按照光线通过它们的顺序排列。列表的第0个元素应是光进入的半无限介质，最后一个元素应是光退出的半无限介质（如果有退出的话）。
     * @param dList        层厚列表（从前到后）。应与 n_list 的元素一一对应。首尾元素应为“inf”。
     * @param th0          入射角：0 表示垂直入射，π/2 表示掠射入射。记住，对于耗散性的入射介质（n_list[0] 不是实数），th_0 应该是复数，以使得 n0 sin(th_0) 为实数（强度随横向位置变化保持恒定）。
     * @param lamVac       光的真空波长
     */
    public static Map<String, Object> cohTmm(char polarization, Complex[] nList, Complex[] dList, Complex th0, double lamVac) {
        //输入值测试
        if (nList.length != dList.length) {
            throw new RuntimeException("Problem with n_list or d_list!");
        }
        assert dList[0].isInfinite() & dList[dList.length - 1].isInfinite() : "d_list must start and end with inf!";
        assert Math.abs(nList[0].multiply(th0.sin()).getImaginary()) < 100 * EPSILON : "Error in n0 or th0!";
        assert isForwardAngle(nList[0], th0) : "Error in n0 or th0!";
        int numLayers = nList.length;

        // thList 是一个列表，对于每一层，记录光在该层中传播的角度。通过斯涅尔定律计算。请注意，这些“角度”可能是复数！
        Complex[] thList = listSnell(nList, th0);

        // kz 是前进波的（复）角波矢的 z 分量。虚部为正表示衰减。
        Complex[] kzList = new Complex[numLayers];
        for (int i = 0; i < numLayers; i++) {
            kzList[i] = Complex.valueOf(2 * Math.PI).multiply(nList[i]).multiply(thList[i].cos()).divide(lamVac);
        }

        // delta 是通过特定层传播时累积的总相位
        Complex[] delta = new Complex[numLayers];
        for (int i = 0; i < numLayers; i++) {
            delta[i] = kzList[i].multiply(dList[i]);
        }

        // 对于非常不透明的层，重置 delta 以避免除以 0 及类似错误。判据 imag(delta) > 35 对应单次透射 < 1e-30——小到精确数值无关紧要。
        for (int i = 1; i < numLayers - 1; i++) {
            if (delta[i].getImaginary() > 35) {
                delta[i] = Complex.valueOf(delta[i].getReal(), 35);
                logger.warn("警告：几乎完全不透明的图层会被修改为稍微透光的状态，允许大约 1/10^30 的光子穿过。这是为了数值稳定性。此警告将不再显示。");
            }
        }

        // t_list[i,j] 和 r_list[i,j] 分别是从 i 到 j 的透射幅度和反射幅度。仅在 j=i+1 时需要计算。（二维数组有些大材小用，但有助于避免混淆。）
        Complex[][] tList = new Complex[numLayers][numLayers];
        Complex[][] rList = new Complex[numLayers][numLayers];
        for (int i = 0; i < numLayers - 1; i++) {
            tList[i][i + 1] = interface_t(polarization, nList[i], nList[i + 1], thList[i], thList[i + 1]);
            rList[i][i + 1] = interface_r(polarization, nList[i], nList[i + 1], thList[i], thList[i + 1]);
        }

        // 在第 (n-1) 层与第 n 层材料的界面处，设 v_n 为沿第 n 层向前（远离边界）传播的波的振幅，w_n 为沿第 n 层向后（朝向边界）传播的波的振幅。
        // 则有 (v_n, w_n) = M_n (v_{n 1}, w_{n 1})。
        // M_n 是 M_list[n]。
        // M_0 和 M_{num_layers-1} 未定义。
        // 我的 M 与 Sernelius 的略有不同，但 Mtilde 相同。
        Complex[][][] mList = new Complex[numLayers][2][2];
        for (int i = 1; i < numLayers - 1; i++) {
            Complex[][] tmp = new Complex[2][2];
            Complex[][] mat1 = make2By2Array(
                    Complex.valueOf(0, -1).multiply(delta[i]).exp(),
                    Complex.ZERO,
                    Complex.ZERO,
                    Complex.valueOf(0, 1).multiply(delta[i]).exp());
            Complex[][] mat2 = make2By2Array(
                    Complex.ONE,
                    rList[i][i + 1],
                    rList[i][i + 1],
                    Complex.ONE);
            tmp = TmmUtil.dot(mat1, mat2);
            Complex a = Complex.ONE.divide(tList[i][i + 1]);
            mList[i] = TmmUtil.dot(a, tmp);
        }
        Complex[][] mtilde = make2By2Array(
                Complex.ONE,
                Complex.ZERO,
                Complex.ZERO,
                Complex.ONE);
        for (int i = 1; i < numLayers - 1; i++) {
            mtilde = TmmUtil.dot(mtilde, mList[i]);
        }
        mtilde = TmmUtil.dot(make2By2Array(
                Complex.ONE.divide(tList[0][1]),
                rList[0][1].divide(tList[0][1]),
                rList[0][1].divide(tList[0][1]),
                Complex.ONE.divide(tList[0][1])),
                mtilde);

        // 净复数传输与反射振幅
        Complex r = mtilde[1][0].divide(mtilde[0][0]);
        Complex t = Complex.ONE.divide(mtilde[0][0]);

        // vw_list[n] = [v_n, w_n]。v_0 和 w_0 未定义，因为第 0 个介质没有左侧界面。
        Complex[][] vwList = new Complex[numLayers][2];
        Complex[] vw = new Complex[]{t, Complex.ZERO};
        vwList[vwList.length - 1] = vw;
        for (int i = numLayers - 2; i > 0; i--) {
            vw = TmmUtil.dot(mList[i], vw);
            vwList[i] = vw;
        }

        // 净传输和反射功率，占入射光功率的比例。
        double R = calR(r);
        double T = calT(polarization, t, nList[0], nList[nList.length - 1], th0, thList[thList.length - 1]);
        double powerEntering = calPowerEntering(polarization, r, nList[0], th0);

        Map<String, Object> map = new HashMap<>();
        map.put("r", r);
        map.put("t", t);
        map.put("R", R);
        map.put("T", T);
        map.put("PowerEntering", powerEntering);
        map.put("VwList", vwList);
        map.put("KzList", kzList);
        map.put("ThList", thList);
        map.put("Polarization", polarization);
        map.put("NList", nList);
        map.put("DList", dList);
        map.put("Th0", th0);
        map.put("LamVac", lamVac);
        return map;
    }

    /**
     * 计算非偏振光的反射和透射功率。
     */
    public static Map<String, Double> unpolarizedRT(Complex[] nList, Complex[] dList, Complex th0, double lamVac) {
        Map<String, Object> sData = cohTmm('s', nList, dList, th0, lamVac);
        Map<String, Object> pData = cohTmm('p', nList, dList, th0, lamVac);
        double R = ((double) sData.get("R") + (double) pData.get("R")) / 2;
        double T = ((double) sData.get("T") + (double) pData.get("T")) / 2;
        Map<String, Double> map = new HashMap<>();
        map.put("R", R);
        map.put("T", T);
        return map;
    }

    /**
     * 计算椭偏参数，以弧度为单位。
     * 警告：约定可能有所不同。你可能需要减去 π/2 或其他值。
     */
    public static Map<String, Double> ellips(Complex[] nList, Complex[] dList, Complex th0, double lamVac) {
        Map<String, Object> sData = cohTmm('s', nList, dList, th0, lamVac);
        Map<String, Object> pData = cohTmm('p', nList, dList, th0, lamVac);
        Complex rs = (Complex) sData.get("r");
        Complex rp = (Complex) pData.get("r");
        Map<String, Double> map = new HashMap<>();
        map.put("psi", Math.atan(rp.divide(rs).abs()));
        map.put("delta", rp.divide(rs).multiply(-1).getArgument());
        return map;
    }

    /**
     * 从 coh_tmm() 的输出开始，计算坡印廷向量、吸收能量密度以及特定位置的电场。
     * 位置由 (layer, distance) 定义，其定义方式与 find_in_structure_with_inf(...) 中相同。
     * <p>
     * 返回一个包含以下内容的字典：
     * poyn - 庞廷向量在界面法向的分量
     * absor - 该点的吸收能量密度
     * Ex、Ey 和 Ez - 电场幅值，其中 z 为垂直界面的方向，光线在 x,z 平面内。
     * <p>
     * 电场的单位设定为入射 |E|=1；
     * 公式请参见 https://arxiv.org/pdf/1603.02720.pdf。
     */
    public static Map<String, Object> positionResolved(int layer, Complex distance, Map<String, Object> cohTmmData) {
        Map<String, Object> map = new HashMap<>();
        Complex v;
        Complex w;
        if (layer > 0) {
            Complex[][] vwList = (Complex[][]) cohTmmData.get("VwList");
            Complex[] vw = vwList[layer];
            v = vw[0];
            w = vw[1];
        } else {
            v = Complex.valueOf(1);
            w = (Complex) cohTmmData.get("r");
        }
        Complex kz = ((Complex[]) cohTmmData.get("KzList"))[layer];
        Complex th = ((Complex[]) cohTmmData.get("ThList"))[layer];
        Complex n = ((Complex[]) cohTmmData.get("NList"))[layer];
        Complex n0 = ((Complex[]) cohTmmData.get("NList"))[0];
        Complex th0 = (Complex) cohTmmData.get("Th0");
        char polarization = (char) cohTmmData.get("Polarization");

        assert (layer >= 1 && distance.getReal() >= 0
                && distance.getReal() <= ((Complex[]) cohTmmData.get("DList"))[layer].getReal())
                ||
                (layer == 0
                        && distance.getReal() <= 0);

        // 向前传播波的振幅是 Ef，向后传播波的振幅是 Eb
        Complex Ef = Complex.valueOf(0, 1).multiply(kz).multiply(distance).exp().multiply(v);
        Complex Eb = Complex.valueOf(0, -1).multiply(kz).multiply(distance).exp().multiply(w);

        // 坡印廷矢量
        double poyn = 0;
        if (polarization == 's') {
            poyn = n.multiply(th.cos()).multiply((Ef.add(Eb)).conjugate()).multiply(Ef.subtract(Eb)).getReal()
                    / (n0.multiply(th0.cos()).getReal());
        } else if (polarization == 'p') {
            poyn = n.multiply(th.cos().conjugate()).multiply(Ef.add(Eb)).multiply(Ef.subtract(Eb).conjugate()).getReal()
                    / (n0.multiply(th0.cos().conjugate()).getReal());
        }


        // 吸收能量密度
        double absor = 0;
        if (polarization == 's') {
            absor = n.multiply(th.cos()).multiply(kz).multiply(Math.pow(Ef.add(Eb).abs(), 2)).getImaginary()
                    / (n0.multiply(th0.cos()).getReal());
        } else if (polarization == 'p') {
            absor = n.multiply(th.cos().conjugate())
                    .multiply(kz.multiply(Math.pow(Ef.subtract(Eb).abs(), 2)).subtract(kz.conjugate().multiply(Math.pow(Ef.add(Eb).abs(), 2))))
                    .getImaginary() / (n0.multiply(th0.cos().conjugate()).getReal());
        }

        // 电场
        Complex Ex = null;
        Complex Ey = null;
        Complex Ez = null;
        if (polarization == 's') {
            Ex = Complex.ZERO;
            Ey = Ef.add(Eb);
            Ez = Complex.ZERO;
        } else if (polarization == 'p') {
            Ex = Ef.subtract(Eb).multiply(th.cos());
            Ey = Complex.ZERO;
            Ez = Complex.ZERO.subtract(Ef).subtract(Eb).multiply(th.sin());
        }

        map.put("poyn", poyn);
        map.put("absor", absor);
        map.put("Ex", Ex);
        map.put("Ey", Ey);
        map.put("Ez", Ez);
        return map;
    }

    /**
     * d_list 是各层厚度的列表，所有厚度都是有限的。
     * distance 是从整个多层结构的前端（即从第 0 层的起点）计算的距离。
     * 函数返回 [layer, z]，其中：
     * layer 表示你当前所在的层数。
     * z 是进入该层的距离。
     * 对于较大的距离，layer = len(d_list)，即使此时 d_list[layer] 不存在。
     * 对于负距离，返回 [-1, distance]。
     */
    public static Map<String, Object> findInStructure(Complex[] dList, Complex distance) {
        Map<String, Object> map = new HashMap<>();
        for (Complex d : dList) {
            if (d.isInfinite()) {
                throw new RuntimeException("此函数需要有限的参数");
            }
        }
        if (distance.getReal() < 0) {
            map.put("layer", -1);
            map.put("distance", distance);
            return map;
        }
        int layer = 0;
        Complex distance2 = distance;
        while (layer < dList.length && distance2.getReal() >= dList[layer].getReal()) {
            distance2 = distance2.subtract(dList[layer]);
            layer = layer + 1;
        }
        map.put("layer", layer);
        map.put("distance", distance2);
        return map;
    }

    /**
     * d_list 是各层厚度的列表 [inf, blah, blah, ..., blah, inf]
     * distance 是从整个多层结构的前端（即第 1 层的起点）计算的距离。
     * 函数返回 [layer, z]，其中：
     * layer 表示当前所在的层号，
     * z 是在该层内的距离。
     * 对于 distance < 0，返回 [0, distance]。
     * 因此，第一个界面可以描述为 [0,0] 或 [1,0]。
     */
    public static Map<String, Object> findInStructureWithInf(Complex[] dList, Complex distance) {
        Map<String, Object> map = new HashMap<>();
        if (distance.getReal() < 0) {
            map.put("layer", 0);
            map.put("distance", distance);
            return map;
        }
        Complex[] dListShort = new Complex[dList.length - 2];
        for (int i = 0; i < dListShort.length; i++) {
            dListShort[i] = dList[i + 1];
        }
        map = findInStructure(dListShort, distance);
        int layer = (int) map.get("layer");
        map.put("layer", layer + 1);
        return map;
    }

}
