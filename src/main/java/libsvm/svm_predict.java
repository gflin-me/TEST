package libsvm;

import libsvm.*;
import libsvm.unit.*;
import org.opencv.core.Mat;

import java.io.*;
import java.util.*;

public class svm_predict {
	private static svm_print_interface svm_print_null = new svm_print_interface()
	{
		public void print(String s) {}
	};

	private static svm_print_interface svm_print_stdout = new svm_print_interface()
	{
		public void print(String s)
		{
			System.out.print(s);
		}
	};

	private static svm_print_interface svm_print_string = svm_print_stdout;

	static void info(String s)
	{
		svm_print_string.print(s);
	}

	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}
	public static List<Double> labelTarget = new ArrayList<>();
	public static List<Double> getLabelTarget(){
		return labelTarget;
	}
	/**
	 * 预测函数
	 * @param input 输入文件
	 * @param output 输出文件
	 * @param model 训练好的SVM模型
	 * @param predict_probability 是否输出后验概率标志位
	 * @throws IOException
	 */
	public static List<Double> predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException {
		int correct = 0;
		int total = 0;
		double error = 0;
		double sump = 0, sumt = 0, sumpp = 0, sumtt = 0, sumpt = 0;
		List<Double> resultLabel = new ArrayList<>();

		int svm_type= svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
				svm_predict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			} else {
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
				output.writeBytes("labels");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(" "+labels[j]);
				output.writeBytes("\n");
			}
		}
		while(true){
			//读取一行样本数据
			String line = input.readLine();
			if(line == null) break;
			//通过分隔符取出每个词
			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			//取出标记的类别标签
			double target_label = atof(st.nextToken());
			labelTarget.add(target_label);
			//计算单个样本的特征数量，因为样本的格式中每个特征前带一个序号，所以要除以2
			int m = st.countTokens()/2;
			//将特征及其标号存入特征数组
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double predict_label;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC)) {
				predict_label = svm.svm_predict_probability(model,x,prob_estimates);
				output.writeBytes(predict_label+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			} else {
				predict_label = svm.svm_predict(model,x);
				output.writeBytes(predict_label+"\n");
			}

			resultLabel.add(predict_label);
			if(predict_label == target_label)
				++correct;
			error += (predict_label-target_label)*(predict_label-target_label);
			sump += predict_label;
			sumt += target_label;
			sumpp += predict_label*predict_label;
			sumtt += target_label*target_label;
			sumpt += predict_label*target_label;
			++total;
		}
		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			svm_predict.info("Mean squared error = "+error/total+" (regression)\n");
			svm_predict.info("Squared correlation coefficient = "+
				 ((total*sumpt-sump*sumt)*(total*sumpt-sump*sumt))/
				 ((total*sumpp-sump*sump)*(total*sumtt-sumt*sumt))+
				 " (regression)\n");
		} else {
			svm_predict.info("Accuracy = " + (double) correct / total * 100 +
					"% (" + correct + "/" + total + ") (classification)\n");
		}
		return resultLabel;
	}

	/**
	 * 单样本特征向量的SVM识别
	 * @param feaMat 特征向量
	 * @param model 训练完毕的svm模型
	 * @param predict_probability 是否输出分类概率，是为1，否取0
	 * @return svm_result识别结果类，包含分类结果predict_label和分类概率数组prob_estimates（predict_probability为0时为null）
	 */
	public static svm_result predict(Mat feaMat , svm_model model, int predict_probability)   {

		int svm_type= svm.svm_get_svm_type(model);//获取SVM类型
		int nr_class=svm.svm_get_nr_class(model); //获取分类种类数量
		double[] prob_estimates=null;

		//若需要获取分类概率
		if(predict_probability == 1) {
			if(svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
				svm_predict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			} else {
				int[] labels=new int[nr_class];//初始化标签数组
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];//分类概率数组

			}
		}

		//特征填入容器
		int numFea = feaMat.cols();
		svm_node[] fea = new svm_node[numFea];
		for(int j=0 ; j<numFea ; j++ ) {
			fea[j] = new svm_node();
			fea[j].index = j+1;
			fea[j].value = feaMat.get(0,j)[0];
		}
		//识别
		double predict_label;
		if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC)){
			predict_label = svm.svm_predict_probability(model,fea,prob_estimates);
		} else {
			predict_label = svm.svm_predict(model,fea);
		}
		//结果输出
		return new svm_result(predict_label,prob_estimates);
	}

	/**
	 * 单样本特征向量的SVM识别
	 * @param features 特征向量
	 * @param model 训练完毕的svm模型
	 * @param predict_probability 是否输出分类概率，是为1，否取0
	 * @return svm_result识别结果类，包含分类结果predict_label和分类概率数组prob_estimates（predict_probability为0时为null）
	 */
	public static svm_result predict(double[] features , svm_model model, int predict_probability)   {

		int svm_type= svm.svm_get_svm_type(model);//获取SVM类型
		int nr_class=svm.svm_get_nr_class(model); //获取分类种类数量
		double[] prob_estimates=null; //分类概率容器

		//若需要获取分类概率
		if(predict_probability == 1) {
			if(svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
				svm_predict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			} else {
				//初始化标签数组
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				//创建分类概率数组
				prob_estimates = new double[nr_class];

			}
		}

		//特征填入容器
		int numFea = features.length;
		svm_node[] fea = new svm_node[numFea];
		for(int j=0 ; j<numFea ; j++ ) {
			fea[j] = new svm_node();
			fea[j].index = j+1;
			fea[j].value = features[j];
		}
		//识别&计算分类概率
		double predict_label;
		if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC)){
			predict_label = svm.svm_predict_probability(model,fea,prob_estimates);
		} else {
			predict_label = svm.svm_predict(model,fea);
		}
		//结果输出
		return new svm_result(predict_label,prob_estimates);
	}

	private static void exit_with_help()
	{
		System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
		+"options:\n"
		+"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
		+"-q : quiet mode (no outputs)\n");
		System.exit(1);
	}

	public static void main(String argv[]) throws IOException
	{
		int i, predict_probability=0;
        	svm_print_string = svm_print_stdout;

		// parse options
		for(i=0;i<argv.length;i++)
		{
			if(argv[i].charAt(0) != '-') break;
			++i;
			switch(argv[i-1].charAt(1))
			{
				case 'b':
					predict_probability = atoi(argv[i]);
					break;
				case 'q':
					svm_print_string = svm_print_null;
					i--;
					break;
				default:
					System.err.print("Unknown option: " + argv[i-1] + "\n");
					exit_with_help();
			}
		}
		if(i>=argv.length-2)
			exit_with_help();
		try
		{
			BufferedReader input = new BufferedReader(new FileReader(argv[i]));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
			svm_model model = svm.svm_load_model(argv[i+1]);
			if (model == null)
			{
				System.err.print("can't open model file "+argv[i+1]+"\n");
				System.exit(1);
			}
			if(predict_probability == 1)
			{
				if(svm.svm_check_probability_model(model)==0)
				{
					System.err.print("Model does not support probabiliy estimates\n");
					System.exit(1);
				}
			}
			else
			{
				if(svm.svm_check_probability_model(model)!=0)
				{
					svm_predict.info("Model supports probability estimates, but disabled in prediction.\n");
				}
			}
			predict(input,output,model,predict_probability);
			input.close();
			output.close();
		}
		catch(FileNotFoundException e)
		{
			exit_with_help();
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			exit_with_help();
		}
	}

	public static void predict(ArrayList<double[]> data, svm_model svmModel, int i) {
	}
}
