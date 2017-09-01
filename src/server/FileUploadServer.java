package server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import handler.FileUploadServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import util.IniReader;

public class FileUploadServer {

	private static String serverPort;
	private static String serverIp;
	private static JTextField portText;
	private static String destDir;

	public void bind(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
					.childHandler(new ChannelInitializer<Channel>() {
						@Override
						protected void initChannel(Channel ch) throws Exception {
							ch.pipeline().addLast(new ObjectEncoder());
							ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE,
									ClassResolvers.weakCachingConcurrentResolver(null)));
							ch.pipeline().addLast(new FileUploadServerHandler(destDir));
						}
					});
			System.out.println("Already bind " + port);
			ChannelFuture f = b.bind(port).sync();
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	private static void showPanel() {
		try {
			JFrame frame = new JFrame("文件上传服务器");
			String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			UIManager.setLookAndFeel(windows);
			frame.setSize(500, 180);
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

	private static void placeComponents(JPanel panel) {
		panel.setLayout(null);

		JLabel ipLabel = new JLabel("服务器IP:");
		ipLabel.setBounds(100, 20, 80, 25);
		panel.add(ipLabel);

		JTextField ipText = new JTextField(20);
		ipText.setBounds(160, 20, 250, 25);
		ipText.setText(serverIp);
		ipText.setEditable(false);
		panel.add(ipText);

		JLabel portLabel = new JLabel("接收端口:");
		portLabel.setBounds(100, 50, 80, 25);
		panel.add(portLabel);

		portText = new JTextField(6);
		portText.setBounds(160, 50, 65, 25);
		portText.setText(serverPort);
		portText.setEditable(false);
		panel.add(portText);

		JButton receiveButton = new JButton("接收");
		receiveButton.setBounds(200, 90, 80, 25);
		panel.add(receiveButton);

		setListener(receiveButton);
	}

	private static void setListener(final JButton receiveButton) {
		receiveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serverPort = portText.getText().toString();
				receiveButton.setText("接收中");
				receiveButton.setEnabled(false);
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						createServer();
					}
				}).start();
			}
		});

		receiveButton.doClick();

	}

	private static void createServer() {
		// TODO Auto-generated method stub

		try {
			new FileUploadServer().bind(Integer.parseInt(serverPort));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void init() {
		serverPort = IniReader.getInstance().getProperty("port");
		destDir = IniReader.getInstance().getProperty("destDir");
		serverIp = IniReader.getInstance().getProperty("serverIp");
	}

	public static void main(String[] args) {

		init();
		showPanel();

	}
}
