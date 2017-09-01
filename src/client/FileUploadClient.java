package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import model.FileUploadFile;
import util.IniReader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import handler.FileUploadClientHandler;

public class FileUploadClient {

	private static String sourceDir;
	private static String copyDir;;
	private static String serverIp;
	private static String serverPort;
	private static JList<File> jList;
	private static File[] fileList;
	private static JButton uploadBtn;

	public void connect(int port, String host, final FileUploadFile fileUploadFile) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {

			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<Channel>() {

						@Override
						protected void initChannel(Channel ch) throws Exception {
							ch.pipeline().addLast(new ObjectEncoder());
							ch.pipeline()
									.addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
							ch.pipeline().addLast(new FileUploadClientHandler(fileUploadFile, copyDir));
						}
					});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();

		} catch (Exception e) {
			throw new Exception("fail to connect Server");
		} finally {
			group.shutdownGracefully();
		}
	}

	private static void placeComponents(JPanel panel) {
		panel.setLayout(null);
		JScrollPane jsPane = new JScrollPane();
		jsPane.setBounds(30, 50, 400, 550);
		jsPane.setAutoscrolls(true);
		jList = new JList();
		jsPane.setViewportView(jList);
		panel.add(jsPane);
		uploadBtn = new JButton("上传");
		uploadBtn.setBounds(480, 80, 80, 30);
		openTimer(uploadBtn);
		panel.add(uploadBtn);
		setListener();
	}

	private static void showPanel() {
		try {
			JFrame frame = new JFrame("文件上传客户端");
			String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			UIManager.setLookAndFeel(windows);
			frame.setSize(600, 700);
			frame.setDefaultCloseOperation(3);
			JPanel panel = new JPanel();
			frame.add(panel);
			frame.setResizable(false);
			frame.setLocationRelativeTo(null);
			placeComponents(panel);
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setListener() {
		uploadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((fileList == null) || (fileList.length == 0)) {
					JOptionPane.showMessageDialog(null, "文件列表为空");
				} else {
					uploadBtn.setText("上传中");
					uploadBtn.setEnabled(false);

					new Thread(new Runnable() {

						@Override
						public void run() {
							createClient();
						}

					}).start();
				}
			}
		});
	}

	private static void createClient() {
		// TODO Auto-generated method stub
		try {

			File file = new File(sourceDir);
			File[] listFiles = file.listFiles();

			for (File temp : listFiles) {
				FileUploadFile uploadFile = new FileUploadFile();
				String fileMd5 = temp.getName();// 文件名
				uploadFile.setFile(temp);
				uploadFile.setFile_md5(fileMd5);
				uploadFile.setStarPos(0);// 文件开始位置
				new FileUploadClient().connect(Integer.parseInt(serverPort), serverIp, uploadFile);
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "连接服务器失败");
			resetPanel();
		}
	}

	private static void openTimer(JButton uploadBtn) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			private boolean isShow = true;

			public void run() {
				File f = new File(sourceDir);
				if ((f == null) || (!f.exists())) {
					if (this.isShow) {
						JOptionPane.showMessageDialog(null, "找不到数据源文件夹");
						this.isShow = false;
					}
				} else {
					this.isShow = true;
					fileList = f.listFiles();
					if ((fileList.length == 0) || (fileList == null)) {
						resetPanel();
					}
					changeData();
				}
			}
		}, 500L, 1000L);
	}

	private static void changeData() {
		jList.removeAll();
		jList.setListData(fileList);
	}

	private static void resetPanel() {
		uploadBtn.setEnabled(true);
		uploadBtn.setText("上传");
	}

	private static void initData() {
		sourceDir = IniReader.getInstance().getProperty("sourceDir");
		serverPort = IniReader.getInstance().getProperty("port");
		serverIp = IniReader.getInstance().getProperty("serverIp");
		copyDir = IniReader.getInstance().getProperty("copyDir");
	}

	public static void main(String[] args) {
		initData();
		showPanel();
		// createClient();
	}
}