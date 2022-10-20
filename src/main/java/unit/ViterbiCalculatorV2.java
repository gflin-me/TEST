package unit;

import be.ac.ulg.montefiore.run.jahmm.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
/**
 * @className: ViterbiCalculatorV2
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/01/20 21:35
 * @version: 1.0
 **/
/**
 * This class can be used to compute the most probable state sequence matching
 * a given observation sequence (given an HMM).
 */
public class ViterbiCalculatorV2 {
    /* jahmm package - v0.6.1 */






    /*
     * The psy and delta values, as described in Rabiner and Juand classical
     * papers.
     */
    private double[][] delta;
    private int[][] psy;
    private int[] stateSequence;
    private double lnProbability;


    /**
     * Computes the most likely state sequence matching an observation sequence given an HMM.
     * 计算与给定 HMM 的观测序列匹配的最可能的状态序列。
     * @param oseq An observations sequence.观测序列
     * @param hmm A Hidden Markov Model; HMM模型
     * @param OPDFS Observation Probability Distribution Function Sequence 观测概率分布矩阵序列（每一帧对应的观测概率矩阵组成的序列）
     */
    public <O extends Observation> ViterbiCalculatorV2(List<? extends O> oseq, Hmm<O> hmm , List<List<OpdfDiscrete>> OPDFS ) {

        if (oseq.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty sequence");
        }

        //创建 δ 、 Ψ  和 状态 序列
        delta = new double[oseq.size()][hmm.nbStates()];
        psy = new int[oseq.size()][hmm.nbStates()];
        stateSequence = new int[oseq.size()];
        List< OpdfDiscrete > aaaa = OPDFS.get(0);
        OpdfDiscrete aaaaaaa = aaaa.get(0);
        for (int i = 0; i < hmm.nbStates(); i++) {
            //该帧的观测概率矩阵
//            ArrayList<Opdf<O>> opdfs_frame = new ArrayList<Opdf<O>>((Collection<? extends Opdf<O>>) OPDFS.get(0));
            List<OpdfDiscrete> opdfs_frame = new ArrayList<OpdfDiscrete>(OPDFS.get(0));
//            delta[0][i] = -Math.log(hmm.getPi(i)) - Math.log(opdfs_frame.get(0).probability(oseq.get(0)));
            delta[0][i] = -Math.log(hmm.getPi(i)) - Math.log(opdfs_frame.get(0).probability((ObservationDiscrete) oseq.get(0)));
            psy[0][i] = 0;
        }

        //观测序列迭代器
        Iterator<? extends O> oseqIterator = oseq.iterator();
        if (oseqIterator.hasNext()) {
            oseqIterator.next();
        }


        int t = 1;  //迭代次数，最大值为观测序列长度
        while (oseqIterator.hasNext()) {
            O observation = oseqIterator.next();

            for (int i = 0; i < hmm.nbStates(); i++)
                computeStep(hmm, observation, t, i , OPDFS);

            t++;
        }

        lnProbability = Double.MAX_VALUE;
        for (int i = 0; i < hmm.nbStates(); i++) {
            double thisProbability = delta[oseq.size()-1][i];

            if (lnProbability > thisProbability) {
                lnProbability = thisProbability;
                stateSequence[oseq.size() - 1] = i;
            }
        }
        lnProbability = -lnProbability;

        for (int t2 = oseq.size() - 2; t2 >= 0; t2--)
            stateSequence[t2] = psy[t2+1][stateSequence[t2+1]];
    }



    /**
     * 计算 【δ[t][j]】（delta[t][j]）：当前时刻t，状态值为j的概率最大值.
     *     【Ψ[t][j]】（psy[t][j]）：时刻t状态为j的所有单个路径中概率最大的路径的第t-1时刻的状态值.
     * @param hmm HMM模型
     * @param o 当前帧观测值
     * @param t 迭代次数/时刻
     * @param j 本帧状态值
     * @param OPDFS 观测概率分布矩阵序列
     * @param <O>
     */
    private <O extends Observation> void computeStep(Hmm<O> hmm, O o, int t, int j ,List<List<OpdfDiscrete>> OPDFS){
        //最小delta值迭代
        double minDelta = Double.MAX_VALUE;
        int min_psy = 0;

        for (int i = 0; i < hmm.nbStates(); i++) {
            double Aij = hmm.getAij(i, j);
            double thisDelta = delta[t-1][i] - Math.log(Aij);

            if (minDelta > thisDelta) {
                minDelta = thisDelta;
                min_psy = i;
            }
        }
//        ArrayList<Opdf<O>> opdfs_frame = new ArrayList<Opdf<O>>((Collection<? extends Opdf<O>>) opds.get(t));
//        delta[t][j] = minDelta - Math.log(opdfs_frame.get(j).probability(o));

        List<OpdfDiscrete> opdf = new ArrayList<OpdfDiscrete>(OPDFS.get(t));//当前帧对应的观测概率分布函数
        delta[t][j] = minDelta - Math.log(opdf.get(j).probability((ObservationDiscrete) o));
        psy[t][j] = min_psy;
    }


    /**
     * Returns the neperian logarithm of the probability of the given
     * observation sequence on the most likely state sequence of the given
     * HMM.
     * 返回给定 HMM 最可能的状态序列上给定观测序列的概率的涅伯利亚neperian对数
     * @return <code>ln(P[O,S|H])</code> where <code>O</code> is the given
     *         observation sequence, <code>H</code> the given HMM and
     *         <code>S</code> the most likely state sequence of this observation
     *         sequence given this HMM.
     */
    public double lnProbability()
    {
        return lnProbability;
    }


    /**
     * Returns a (clone of) the array containing the computed most likely
     * state sequence.
     * 返回包含最可能状态序列的数组的(克隆)。
     * @return The state sequence; the i-th value of the array is the index of the i-th state of the state sequence.
     *         状态序列;数组的第i个值是状态序列的第i个状态的索引。
     */
    public int[] stateSequence()
    {
        return stateSequence.clone();
    }

}
