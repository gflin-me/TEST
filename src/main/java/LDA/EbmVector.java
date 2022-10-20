package LDA;

import java.util.Arrays;
import java.util.Objects;

/**

 * EbmVector 向量

 * @author YueFan

 * @version 1.0.0

 */

/* <定义>
 * 向量类提供一个向量. 该向量的实际值使用一维数组储存在coordinate中
 * "EbmVector"代表一个向量(包括向量坐标、维数和一些方法),而"coordinate"代表向量坐标，两者不同
 * 在本类中, 向量的运算实质是向量坐标的运算, 但在外部调用时, 除了向量构造之外,
 * 都应使用向量和其对应的封装方法进行运算, 而不是读取数组进行运算
 */

/* <XX和selfXX方法>
 * (包括向量复制、加法、减法、数乘、叉乘操作)
 * 这些方法在调用时提供两种方法:.XX()和 .selfXX()
 * .()xx方法将会new一个新的对象返回，而selfXX()则将结果返回给调用该方法的对象
 * .()xx方法更符合算式规律，如a = (b+c)*d 则可写为 a = b.time(c).plus(d),但是会在计算中多次new新的向量对象
 * .()selfXX开销更小, 但是需要一个新的向量承接结果.上例可写为 a = b.copy().selfTime(c).selfPlus(d)
 */

public class EbmVector {

	private double[] coordinate;//向量坐标
	
	private int dimension;//向量维数


	/**
	 * 构造函数
	 * @param coordinate 使用double数组构成的向量值
	 */ 

	public EbmVector(double[] coordinate){
		this.coordinate = coordinate;
		this.dimension = coordinate.length;
	}

	// ######## 静态方法 ########

	/*---------
	 * 静态方法
	 */

	/**
	 * (静态方法)将数组值打印出来
	 * @param inputCoordinate 要打印的数组
	 * @return 数组内所有值
	 */
	static String cooToString(double[] inputCoordinate) {
		return Arrays.toString(inputCoordinate);
	}

	/**
	 * (静态方法)生成一个新的三维向量
	 */
	public static EbmVector v(double x,double y,double z) {
		return new EbmVector(new double[] {x,y,z});
	}

	/**
	 * (静态方法)生成一个新的多维向量
	 */
	public static EbmVector mv(double... x) {
			return new EbmVector(x);
	}

	/**
	 * (静态方法)生成一个(0,0,0)的三维向量
	 */
	public static EbmVector zeros() {
		return new EbmVector(new double[] {0,0,0});
	}

	/**
	 * (静态方法)计算两个向量的距离
	 */
	public static double distance(EbmVector v1,EbmVector v2){
		if(v1.dimension !=v2.dimension){
			throw new IllegalArgumentException("进行距离运算的两个向量维数不一致");
		}
		double ans = 0;
		for(int i = 0; i < v1.dimension; i++){
			ans += Math.pow((v1.getCoo()[i]-v2.getCoo()[i]),2);
		}
		return Math.sqrt(ans);

	}

	/**
	 * (私有方法)检查向量维数, 若不相等则抛出异常
	 * @param ebmVector 检查的向量
	 */
	private void checkDimensionEqual(EbmVector ebmVector) {
		if(dimension != ebmVector.dimension) {
			throw new IllegalArgumentException("进行运算的两个向量维数不一致");
		}

	}

	
	// ######## 主要方法 ########
	
	/**
	 * 向量加法, 结果返回给调用的向量
	 * @param inputVector 参与加法的另一个向量
	 * @return this(方便继续进行运算, 也可以不承接返回值)
	 */
	public EbmVector selfPlus(EbmVector inputVector){
		checkDimensionEqual(inputVector);
		for(int i = 0; i <  dimension; i++) {
			coordinate[i] += inputVector.coordinate[i];
		}
		return this;
	}
	
	/**
	 * 向量加法, 返回一个新向量
	 * @param inputVector 参与加法的另一个向量
	 * @return 加法运算结果向量
	 */
	public EbmVector plus(EbmVector inputVector) {
		return this.copy().selfPlus(inputVector);
	}

	/**
	 * 向量减法, 结果返回给调用的向量
	 * @param inputVector 参与减法的另一个向量
	 * @return this(方便继续进行运算, 也可以不承接返回值)
	 */
	public EbmVector selfMinus(EbmVector inputVector){
		checkDimensionEqual(inputVector);
		for(int i = 0; i <  dimension; i++) {
			coordinate[i] -= inputVector.coordinate[i];
		}
		return this;
	}
	
	/**
	 * 向量减法, 返回一个新向量
	 * @param inputVector 参与减法的另一个向量
	 * @return 加法运算结果向量
	 */
	public EbmVector minus(EbmVector inputVector) {
		return this.copy().selfMinus(inputVector);
	}

	/**
	 * 向量数乘, 结果返回给调用的向量
	 * @param para 数乘值
	 * @return this(方便继续进行运算, 也可以不承接返回值)
	 */
	public EbmVector selfTimes(double para){
		for(int i = 0; i < dimension; i++) {
			coordinate[i] *=  para;	
		}
		return this;
	}

	/**
	 * 向量数乘, 返回一个新向量
	 * @param param 数乘值
	 * @return 数乘运算结果向量
	 */
	public EbmVector times(double param) {
		return this.copy().selfTimes(param);
	}

	/**
	 * (重载)向量点乘
	 * @param inputVector 参与点乘的另一个向量
	 * @return 点乘运算结果值
	 */
	public double times(EbmVector inputVector) {
		double answer = 0;
		for(int i = 0; i < 3; i++) {
			answer = answer + coordinate[i] * inputVector.coordinate[i];
		}
		return answer;
	}

	/**
	 * 向量叉乘, 调用向量在叉乘前, 结果返回给调用的向量
	 * 若向量超过三维，则返回前三维的叉乘结果，调用向量其他维数值保留到结果
	 * @param inputVector 参与叉乘的另一个向量
	 * @return this(方便继续进行运算, 也可以不承接返回值)
	 */
	public EbmVector selfCross(EbmVector inputVector) {
		checkDimensionEqual(inputVector);
		double[] inCoordinate = inputVector.coordinate;
		double[] answerCoordinate = {0,0,0};
		answerCoordinate[0] = coordinate[1] * inCoordinate[2] - coordinate[2] * inCoordinate[1];
		answerCoordinate[1] = coordinate[2] * inCoordinate[0] - coordinate[0] * inCoordinate[2];
		answerCoordinate[2] = coordinate[0] * inCoordinate[1] - coordinate[1] * inCoordinate[0];
		System.arraycopy(answerCoordinate,0,coordinate,0,dimension);
		return this;
	}
	
	/**
	 * 向量叉乘, 调用向量在叉乘前, 返回一个新向量
	 * 若向量超过三维，则返回前三维的叉乘结果，调用向量其他维数值保留到结果
	 * @param inputVector 参与叉乘的另一个向量
	 * @return 叉乘运算结果向量
	 */
	public EbmVector cross(EbmVector inputVector) {
		return this.copy().selfCross(inputVector);
	}
	
	/**
	 * 向量复制, 返回一个与调用向量相同的新向量
	 * @return 复制的新向量
	 */
	public EbmVector copy() {
		EbmVector answerVector = new EbmVector(new double[dimension]);
		answerVector.selfCopy(this);
		return answerVector;
	}
	
	/**
	 * 向量复制, 返回给调用向量
	 * @param ebmVector 预备复制的向量
	 */
	public void selfCopy(EbmVector ebmVector) {
		if(dimension != ebmVector.dimension) {
			throw new IllegalArgumentException("进行复制的两个向量维数不一致");
		}
		System.arraycopy(ebmVector.coordinate,0,this.coordinate,0,dimension);
	}


	// ######## getter和setter方法 ########

	public double[] getCoo(){
		return coordinate;
	}

	public void setCoo(double[] coordinate){
		this.coordinate = coordinate;
		this.dimension = coordinate.length;

	}

	public int getDimension() {
		return dimension;
	}

	public double getX(){
		return coordinate[0];
	}

	public double getY(){
		return coordinate[1];
	}

	public double getZ(){
		return coordinate[2];
	}

	public void setX(double x){
		this.coordinate[0] = x;
	}

	public void setY(double y){
		this.coordinate[1] = y;
	}

	public void setZ(double z){
		this.coordinate[2] = z;
	}

	// ######## 其他方法 ########

	@Override
	public String toString() {
		return Arrays.toString(coordinate);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EbmVector ebmVector = (EbmVector) o;
		return dimension == ebmVector.dimension &&
				Arrays.equals(coordinate, ebmVector.coordinate);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(dimension);
		result = 31 * result + Arrays.hashCode(coordinate);
		return result;
	}
}
