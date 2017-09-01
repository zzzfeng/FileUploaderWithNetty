package handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import model.FileUploadFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUploadServerHandler extends ChannelInboundHandlerAdapter {

	private volatile int byteRead;
	private volatile int start = 0;
	private String upload_dir = null;
	private long fileSize = -1;
	private RandomAccessFile randomAccessFile = null;
	private File file = null;

	public FileUploadServerHandler(String upload_dir) {
		// TODO Auto-generated method stub
		this.upload_dir = upload_dir;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FileUploadFile) {
			FileUploadFile ef = (FileUploadFile) msg;
			byte[] bytes = ef.getBytes();
			byteRead = ef.getEndPos();

			String md5 = ef.getFile_md5();// 文件名

			if (start == 0) {

				String path = upload_dir + File.separator + md5;
				// System.out.println("File path : " + path);
				file = new File(path);
				fileSize = ef.getFileSize();

				if (!file.exists()) {
					randomAccessFile = new RandomAccessFile(file, "rw");
				} else {
					// System.out.println("The file" + file.getName() + " is
					// exist");
					throw new Exception("The file" + file.getName() + " is exist");
				}
			}
			randomAccessFile.seek(start);
			randomAccessFile.write(bytes);
			start = start + byteRead;

			if (byteRead > 0) {
				ctx.writeAndFlush(start);
			}

			if (start == fileSize && fileSize != -1) {
				// System.out
				// .println("create file success:" + ef.getFile_md5() + "[" + ctx.channel().remoteAddress() + "]");

				randomAccessFile.close();
				file = null;
				fileSize = -1;
				randomAccessFile = null;
				ctx.close();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		// 当连接断开的时候 关闭未关闭的文件流
		if (randomAccessFile != null) {
			try {
				randomAccessFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ctx.close();
	}
}