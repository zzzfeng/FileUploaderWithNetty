package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtil {

	public static boolean copyFileUsingFileChannels(String source, String dest) throws IOException {
		File sourceFile = null;
		File destFile = null;
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			sourceFile = new File(source);
			destFile = new File(dest);
			inputChannel = new FileInputStream(sourceFile).getChannel();
			outputChannel = new FileOutputStream(destFile).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			inputChannel.close();
			outputChannel.close();
			sourceFile = null;
			destFile = null;
		}
		deleteFile(source);
		return true;
	}

	public static boolean deleteFile(String filePath) {

		File file = new File(filePath);

		if (file.exists()) {
			return file.delete();
		}
		return false;
	}

	public static void main(String[] args) {
		File srcDir = new File("D:\\busData\\测试\\client\\数据");
		File copyDir = new File("D:\\busData\\测试\\client\\备份");

		for (File f : srcDir.listFiles()) {
			try {
				boolean flag = copyFileUsingFileChannels(f.getAbsolutePath(),
						copyDir.getAbsolutePath() + File.separator + f.getName());
				// if (flag) {
				// System.out.println("copy " + f.getName() + " success !");
				// boolean temp = deleteFile(f.getAbsolutePath());
				// if (temp) {
				// System.out.println("Delete file " + f.getName() + "success");
				// } else {
				// System.out.println("Delete file " + f.getName() + "fail");
				// }
				// }

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
