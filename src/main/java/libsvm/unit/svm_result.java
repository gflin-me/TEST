package libsvm.unit;

/**
 * @className: svm_result
 * @description: 单个样本识别结果容器
 * @author: Lin Guifeng
 * @date: 2022/02/12 22:58
 * @version: 1.0
 **/
public class svm_result {
    public int predict_label;
    public double[] prob_estimates;

    public svm_result(double predict_label, double[] prob_estimates) {
        this.predict_label = (int)predict_label;
        this.prob_estimates = prob_estimates;
    }

    public int getPredict_label(){
        return this.predict_label;
    }

    public double[] getProb_estimates(){
        return this.prob_estimates;
    }


}
