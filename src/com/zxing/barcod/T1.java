package com.zxing.barcod;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;

public class T1 {
    private String destFolder = System.getProperty("user.dir") + File.separator + "imageStructure";

    private List<String> listImg=null;
    		
	public void setDestFolder(String destFolder) {
		this.destFolder = destFolder;
	}
	
	

	public List getListImg() {
		return listImg;
	}



	public String getBarcode(String path) throws Exception {
		InputStream barCodeInputStream = new FileInputStream(path);
		BufferedImage barCodeBufferedImage = ImageIO.read(barCodeInputStream);

		LuminanceSource source = new BufferedImageLuminanceSource(barCodeBufferedImage);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Reader reader = new MultiFormatReader();
		Result result = reader.decode(bitmap);
		return result.getText();

	}

	public String getBarcodes(String path) throws NotFoundException, ChecksumException, FormatException, IOException {
		return getMultipleBarcode(path);
	}

	private String getMultipleBarcode(String img)
			throws NotFoundException, ChecksumException, FormatException, IOException {
		InputStream barCodeInputStream = new FileInputStream(img);
		BufferedImage barCodeBufferedImage = ImageIO.read(barCodeInputStream);
		LuminanceSource source = new BufferedImageLuminanceSource(barCodeBufferedImage);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		com.google.zxing.Reader reader = new MultiFormatReader();

		MultipleBarcodeReader bcReader = new GenericMultipleBarcodeReader(reader);
		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		StringBuilder sb = new StringBuilder();
		for (Result result : bcReader.decodeMultiple(bitmap, hints)) {
			if (result.getBarcodeFormat() != BarcodeFormat.QR_CODE)
				sb.append(result.getText()).append(",");
		}
		return sb.toString();
	}

	public void scanByBatch(String folderPath) {
		int flag = 5;
		File folder = new File(folderPath);
		Arrays.sort(folder.listFiles(), new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.compare(f1.lastModified(), f2.lastModified());
			}
		});
		try {
			if (folder.isDirectory()) {
				String barcodePath = "";
				for (File file : folder.listFiles()) {
					try {
						if (flag == 5) {
							String[] barcodes = getMultipleBarcode(file.getAbsolutePath()).split(",");
							if (barcodes.length == 1) {
								barcodePath = createDirectory(barcodes[0]);
								copyFiles(file, barcodePath);
								flag--;
							} else {
//								System.out.println("Skipped: Multiple Barcode Found in " + barcodes);
								copyFiles(file, barcodePath);
							}
						} else {
							copyFiles(file, barcodePath);
							if (flag != 1) {
								flag--;
							} else {
								flag = 5;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<String> scanAllFiles(String folderPath) throws IOException {
		File folder = new File(folderPath);
		String previousBarcode=null;
		Arrays.sort(folder.listFiles(), new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.compare(f1.lastModified(), f2.lastModified());
			}
		});
		try {
			if (folder.isDirectory()) {
				listImg=new ArrayList<String>();
				String barcodePath = "";
				for (File file : folder.listFiles()) {
					try {
							String[] barcodes = getMultipleBarcode(file.getAbsolutePath()).split(",");
							if (barcodes.length == 1) {
								previousBarcode=barcodes[0];
								barcodePath = createDirectory(previousBarcode);
								copyFiles(file, barcodePath);
							}
					} catch (NotFoundException e) {
						copyFiles(file, barcodePath);
					}catch(Exception e) {
						e.printStackTrace();
					}
					listImg.add(previousBarcode+" , "+file.getName());
				}
			}else {
				throw new NotDirectoryException("Plase select directory not File");
			}
		} catch (NotDirectoryException e) {
			throw new NotDirectoryException("Plase select directory not File");
		}
		return listImg;

	}

	private String createDirectory(String barcodes) {
//		System.out.println("Creating Directory "+barcodes);
		File f = new File(destFolder + File.separator + barcodes);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f.getAbsolutePath();
	}

	private void copyFiles(File file, String dest) throws IOException {
//		System.out.println("Copying file :"+file.getName()+" at "+dest);
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(file);
			os = new FileOutputStream(new File(dest + File.separator + file.getName()));
			byte[] buffer = new byte[4096];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}
}
