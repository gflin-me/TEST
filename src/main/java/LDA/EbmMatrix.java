package LDA;

import java.util.Arrays;
import java.util.Objects;

/**

 * EbmMatrix 矩阵

 * @author YueFan

 * @version 1.0.0

 */

/* <定义>
 * 矩阵类提供一个矩阵. 该矩阵的实际值使用二维数组储存在elements中
 * "EbmMatrix"代表一个矩阵(包括矩阵元素值、行列数和一些方法),而"elements"代表矩阵元素值，两者不同
 * 在本类中, 矩阵的运算实质是矩阵元素值的运算, 但在外部调用时, 除了矩阵构造之外,
 * 都应使用矩阵和其对应的封装方法进行运算, 而不是读取数组进行运算
 */

/* <XX和selfXX方法>
 * (包括矩阵复制操作)
 * 这个方法在调用时提供两种方法:.XX()和 .selfXX()
 * .()xx方法将会new一个新的对象返回，而selfXX()则将结果返回给调用该方法的对象
 */

public class EbmMatrix {
	
	private double[][] elements; //矩阵元素值
	
	private int rows; //行数
	
	private int columns; //列数
	
	
	/**
	 * 构造函数
	 * @param elements 使用二维double数组建立矩阵
	 */ 
	public EbmMatrix(double[][] elements){
		this.ebmMatrixBuild(elements);
	}

	// ######## 静态方法 ########
	
	/*---------
	 * 静态方法(公共)
	 */
	/**
	 * (静态方法)将矩阵值打印出来
	 * @param inputMatrix 要打印的矩阵值
	 * @return 矩阵值字符串
	 */
	public static String matrixToString(EbmMatrix inputMatrix) {
		double[][] inputElements = inputMatrix.elements;
		StringBuilder ans = new StringBuilder();
		ans.append("----------\n");
		for(int i=0; i < inputMatrix.rows; i++) {
			ans.append(Arrays.toString(inputElements[i]));
			ans.append("\n");
		}
		return ans.toString();
	}


	
	// ######## 主要方法 ########
	/**
	 * (私有)通过传入的二维数组构造矩阵
	 * @param inputElements 构造矩阵的二维数组
	 */
	private void ebmMatrixBuild(double[][] inputElements) {
		int length0 = inputElements[0].length;
		for(int i = 1; i < inputElements.length; i++) {
			if(inputElements[i].length != length0) {
				throw new IllegalArgumentException("传入构成矩阵的数组行列有缺漏");
			}
		}
		elements = inputElements;
		rows = elements.length;
		columns = length0;
	}
	
	/**
	 * 矩阵乘法, 调用矩阵在乘法前, 返回一个新矩阵
	 * @param inputMatrix 参与矩阵乘法的另一个矩阵
	 * @return 乘法运算结果矩阵
	 */
	public EbmMatrix times(EbmMatrix inputMatrix ) {
		int ansRows = rows;
		int ansColumns = inputMatrix.columns;
		int colNumbers = columns;
		if ( this.columns != inputMatrix.rows) {
			throw new IllegalArgumentException("矩阵A的列不等于矩阵B的行！");
		}
		EbmMatrix answerMatrix = new EbmMatrix(new double[ansRows][ansColumns]);
		double[][] inputElements  = inputMatrix.elements;
		double[][] answerElements = answerMatrix.elements;
		for (int i = 0; i < ansRows; i++) {
			for (int j = 0; j < ansColumns; j++) {
				for (int k = 0; k < colNumbers; k++) {
					answerElements[i][j] += elements[i][k] * inputElements[k][j];
				}
			}
		}
		return answerMatrix;
	}
	
	/**
	 * (重载)调用矩阵乘以向量, 调用矩阵在乘法前, 返回一个新向量
	 * @param inputVector 参与乘法的向量
	 * @return 乘法运算结果向量
	 */
	public EbmVector times(EbmVector inputVector){
		if(columns != inputVector.getDimension()) {
			throw new IllegalArgumentException("参与矩阵与向量乘法的尺寸不匹配");
		}
		double[] inputCoordinate = inputVector.getCoo();
		EbmVector answerVector = new EbmVector(new double[rows]);
		double[] answerCoordinate = answerVector.getCoo();
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < inputVector.getDimension();j++) {
				answerCoordinate[i] += elements[i][j] * inputCoordinate[j];
			}
		}
		return answerVector;
	}

	/**
	 * 矩阵复制, 返回一个与输入矩阵相同的新矩阵
	 * @param inputMatrix 被复制的矩阵
	 * @return 复制的新矩阵
	 */
	public static EbmMatrix copy(EbmMatrix inputMatrix) {
		EbmMatrix answerMatrix = 
				new EbmMatrix(new double[inputMatrix.rows][inputMatrix.columns]);
		answerMatrix.selfCopy(inputMatrix);
		return answerMatrix;
	}
	
	/**
	 * (重载)矩阵复制, 返回一个与调用矩阵相同的新矩阵
	 * @return 调用矩阵复制的新矩阵
	 */
	public EbmMatrix copy() {
		EbmMatrix answerMatrix = 
				new EbmMatrix(new double[rows][columns]);
		answerMatrix.selfCopy(this);
		return answerMatrix;
	}
	
	/**
	 * 矩阵复制，结果返回给调用矩阵
	 * @param inputMatrix 被复制的矩阵
	 */
	public void selfCopy(EbmMatrix inputMatrix) {
		double[][] inputElements  = inputMatrix.elements;
		if(inputMatrix.rows != rows || inputMatrix.columns != columns) {
			throw new IllegalArgumentException("需要复制的两个矩阵大小不相等");
		}
		for(int i = 0; i < inputMatrix.rows; i++) {
			System.arraycopy(inputElements[i],0,elements[i],0,columns);
		}
	}
	
	// ######## getter和setter方法 ########
	public double[][] getElements() {
		return elements;
	}


	public void setElements(double[][] elements) {
		this.ebmMatrixBuild(elements);
	}


	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}
	// ######## 其他方法 ########
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EbmMatrix ebmMatrix = (EbmMatrix) o;
		return rows == ebmMatrix.rows &&
				columns == ebmMatrix.columns &&
				Arrays.equals(elements, ebmMatrix.elements);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(rows, columns);
		result = 31 * result + Arrays.hashCode(elements);
		return result;
	}

	@Override
	public String toString() {
		return matrixToString(this);
	}
}
