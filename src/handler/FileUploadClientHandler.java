package handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import model.FileUploadFile;
import util.FileUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUploadClientHandler extends ChannelInboundHandlerAdapter {

	private int byteRead;
	private volatile int start = 0;
	private volatile int lastLength = 0;
	public RandomAccessFile randomAccessFile;
	private FileUploadFile fileUploadFile;
	private String copy_Dir;

	public FileUploadClientHandler(FileUploadFile ef, String copy_Dir) {
		if (ef.getFile().exists()) {
			if (!ef.getFile().isFile()) {
				System.out.println("Not a file :" + ef.getFile());
				return;
			}
		}
		this.fileUploadFile = ef;
		this.copy_Dir = copy_Dir;
	}

	public void channelActive(ChannelHandlerContext ctx) {
		try {
			if (start == 0) {
				randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
			}
			randomAccessFile.seek(fileUploadFile.getStarPos());
			lastLength = (int) randomAccessFile.length() / 10;
			byte[] bytes = new byte[lastLength];
			if ((byteRead = randomAccessFile.read(bytes)) != -1) {
				fileUploadFile.setEndPos(byteRead);
				fileUploadFile.setBytes(bytes);
				fileUploadFile.setFileSize(randomAccessFile.length());
				ctx.writeAndFlush(fileUploadFile);
			} else {
				System.out.println("文件已经读完");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Integer) {
			start = (Integer) msg;
			if (start != -1) {

				if (start == 0) {
					randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
				}
				randomAccessFile.seek(start);
				// System.out.println("块儿长度：" + (randomAccessFile.length() /
				// 10));
				// System.out.println("长度：" + (randomAccessFile.length() -
				// start));
				int a = (int) (randomAccessFile.length() - start);
				int b = (int) (randomAccessFile.length() / 10);
				if (a < b) {
					lastLength = a;
				}
				byte[] bytes = new byte[lastLength];
				// System.out.println("-----------------------------" +
				// bytes.length);
				if ((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start) > 0) {
					// System.out.println("byte 长度：" + bytes.length);
					fileUploadFile.setEndPos(byteRead);
					fileUploadFile.setBytes(bytes);
					try {
						ctx.writeAndFlush(fileUploadFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// System.out.println("文件 " + fileUploadFile.getFile_md5() +
					// " 已经读完--------");
					randomAccessFile.close();
					ctx.close();
					FileUtil.copyFileUsingFileChannels(fileUploadFile.getFile().getAbsolutePath(),
							copy_Dir + File.separator + fileUploadFile.getFile_md5());

				}
			}
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}