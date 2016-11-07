import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

public class LSB {
	BufferedImage evenImage,encodeImage,decodeImage;
	BufferedInputStream encodeData;
	BufferedOutputStream decodeData;
	
	int[] rgb = new int[1024*1024];
	public static void main(String[] args) throws IOException {
		 //TODO Auto-generated method stub
		LSB lsb = new LSB();
		
		lsb.evenImageRead("c.jpg");
		lsb.dataRead("b.txt");
		lsb.encodeDataInImage();
		lsb.encodeImageSave("a.jpg");
		
		lsb.dataSave("d.txt");
		lsb.decodeImageRead("a.jpg");
		lsb.decodeDataInImage();
	}
	//读取图片
	boolean decodeImageRead(String fileName) {
		try {
			File f =  new File(fileName) ;
			decodeImage = ImageIO.read(f);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	boolean evenImageRead(String fileName) {
		try {
			File f =  new File(fileName) ;
			evenImage = ImageIO.read(f);
			ColorModel cm = evenImage.getColorModel();
			encodeImage = new BufferedImage(cm, cm.createCompatibleWritableRaster(
					evenImage.getWidth(), evenImage.getHeight()),cm.isAlphaPremultiplied(),  null);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	//存储隐藏了信息的图片
	boolean encodeImageSave(String fileName) {
		try {
			File f= new File(fileName);
			ImageIO.write(encodeImage, "jpg", f);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	//读取需要隐藏的信息
	boolean dataRead(String fileName) {
		try {
			File f = new File(fileName);
			encodeData = new BufferedInputStream(new FileInputStream(f));
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	boolean dataSave(String fileName) {
		try {
			File f = new File(fileName);
			if (!f.exists()) f.createNewFile();
			decodeData = new BufferedOutputStream(new FileOutputStream(f));
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;	
	}
	//将信息放入图片之中
	boolean encodeDataInImage() {
		try {
			//Graphics2D g = evenImage.createGraphics();
			int sum = encodeData.available();
			int width = evenImage.getWidth();
			int height = evenImage.getHeight();
			System.out.println(sum);
			if (height*width < (sum << 1)) return false;

			String st = "";
			while (encodeData.available() > 0) {
				int k = encodeData.read();
				//读取一个字节，转换为二进制并补足八位
				for (int i = 7;i >= 0; i--)
					if ((k & (1 << i)) == 0) st += "0";
					else break;
				st += Integer.toBinaryString(k);
			}
			st += "0000000000000000";
			while (st.length()%3 != 0) st += "0";
			System.out.println(st + " " + st.length());
			rgb = evenImage.getRGB(0, 0, width, height, rgb,0, height); 
			for (int i = 0;i < height; i++)
				for (int j = 0;j < width; j++) {
					int pos = i*width + j;		
					if (pos*3 < st.length()) {
						rgb[pos] &= (0xffffffff - 0x010101);
						int num = /*((st.charAt(pos*4 + 0) == '1')?(1 << 24):0) +*/ ((st.charAt(pos*3 + 0) == '1')?(1 << 16):0)
								+ ((st.charAt(pos*3 + 1) == '1')?(1 << 8):0) + ((st.charAt(pos*3 + 2) == '1')?1:0);
						//System.out.println(pos + " " + num);
						rgb[pos] |= num;
					}
					//if (pos < 10) System.out.print(rgb[pos] + " ");
					//rgb[pos] = i*i + 1;
				}
			System.out.println();
			encodeImage.setRGB(0, 0, width, height, rgb, 0, height);
		    return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false; 
	}
	
	boolean decodeDataInImage() throws IOException {
		int width = encodeImage.getWidth();
		int height = encodeImage.getHeight();
		rgb = encodeImage.getRGB(0, 0, width, height, rgb, 0, height);
		int owari = 0,cnt = 0,sum = 0;
		String st = "";
		lambel:
		for (int i = 0;i < width; i++) 
			for (int j = 0;j < height; j++) {
				int pos = i*height + j;
				//if (pos < 10) System.out.print(rgb[pos] + " ");
				for (int k = 16;k >= 0; k -= 8) {
					if ((rgb[pos] & (1 << k)) != 0) {
						st += "1";
						owari = 0;
					} else {
						st += "0";
						if (++owari >= 16) break lambel;
					}
					if (st.length() == 8) {
						int rec = Integer.parseInt(st,2);
						System.out.println(st);
						decodeData.write(rec);
						st = "";
					}
				}
			}
		decodeData.close();
		return true;
	}

	
}
