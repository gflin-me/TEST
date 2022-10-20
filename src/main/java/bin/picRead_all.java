package bin;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import unit.StereoBMUtil;
import unit.pcProcess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.opencv.imgcodecs.Imgcodecs.imread;
/*
【批量】读取指定文件夹内的双目图像，并执行双目匹配和输出点云数据为txt文件
操作说明：指定L27的savePath变量"xxx\\picture\\SA":双目图像文件夹
        输出点云txt数据将保存在"xxx\\pointCloud\\SA"：点云数据文件夹
        2021/08/31更新：直接运行,遍历picture文件夹下所有图片
 */
public class picRead_all {


    static {
    // 动态链接opencv
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final StereoBMUtil stereoBMUtil = new StereoBMUtil( 0 , 2  ,0 ,0);
    public static void main(String[] args) throws IOException {
        String[] terrainName = {"LG","OB","SA","SD"};
        for (int indexTerrain = 0 ; indexTerrain<terrainName.length;indexTerrain++) {
            //读取图片，计算深度图，点云图
            String terrain = terrainName[indexTerrain];
            String defaultPath = "C:\\Users\\vincent\\Desktop\\stereoCamera\\PCread\\picwithIMU\\picture\\default";
            String savePath = defaultPath.replace("default", terrain);
            File file = new File(savePath);
            File[] fs = file.listFiles();

            assert fs != null;
            for (File f : fs) {
                //如果不是该路径下的目录，即是一个文件
                if (!f.isDirectory()) {
                    System.out.println(f);

                    String picPath = f.getAbsolutePath();             //获取文件绝对路径
                    Mat frameBit = imread(picPath);                    //读取图像
                    Mat result = stereoBMUtil.SGBMCompute(frameBit);//双目匹配算法

                    //获取点云数据
                    Mat mat3D = stereoBMUtil.get3Dmat();
                    double[][] coordinate = pcProcess.coordinateArray(mat3D);
                    double[] x = coordinate[0];
                    double[] y = coordinate[1];
                    double[] z = coordinate[2];
                    //点云数据保存为txt
                    String txtPath = picPath.replace(".png", ".txt");
                    String txtPath1 = txtPath.replace("picture", "pointCloud");
                    FileWriter fw = new FileWriter(txtPath1);
                    for (int i = 0; i < x.length; i++) {
                        fw.write(x[i] + "\t" + y[i] + "\t" + z[i] + "\t\n");//读取一个数字,就写入文件一次
                    }
                    fw.close();//输出流用完就关闭

//                imwrite(savePath+depthPath+readName+"_depth.jpg",result);

                }

            }
        }
    }


}


