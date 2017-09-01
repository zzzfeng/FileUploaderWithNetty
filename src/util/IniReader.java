package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class IniReader {
	public static volatile IniReader reader = null;
	String configpath;
	private Properties properties;
	InputStream fis;
	// OutputStream fos;
	BufferedReader bf;

	public static IniReader getInstance() {
		if (reader == null) {
			synchronized (IniReader.class) {
				reader = new IniReader();
			}
		}
		return reader;
	}

	private IniReader() {
		try {
			configpath = "./config.ini";
			properties = new Properties();

			File config = new File(configpath);
			fis = new FileInputStream(config);
			bf = new BufferedReader(new InputStreamReader(fis, "GBK"));
			properties.load(bf);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				bf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public String getProperty(String key) {
		String object = this.properties.getProperty(key);
		return object;
	}

	// public void setProperty(String key, String value) {
	// try {
	// this.fos = new FileOutputStream(this.configpath);
	// this.properties.setProperty(key, value);
	// this.properties.store(this.fos, null);
	// this.fos.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public static void main(String[] args) {
		IniReader ini = new IniReader();
		System.out.println(ini.getProperty("sourceDir"));
		System.out.println(ini.getProperty("copyDir"));
		System.out.println(ini.getProperty("destDir"));
	}
}
