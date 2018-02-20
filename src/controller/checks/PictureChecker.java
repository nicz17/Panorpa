package controller.checks;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import model.DataProblem;
import model.HerbierPic;

import common.base.Logger;

import controller.Controller;
import controller.PictureCache;

public class PictureChecker implements DataChecker {

	private static final Logger log = new Logger("PictureChecker", true);
	
	/** Largest image size (height or width) before displaying a warning */
	public static final int IMAGE_LARGE_SIZE_WARN = 2000;

	public PictureChecker() {
	}

	@Override
	public Vector<DataProblem> check() {
		Vector<DataProblem> vecProblems = new Vector<>();
		
		Collection<HerbierPic> pictures = PictureCache.getInstance().getAll();
		
		log.info("Quality check of " + pictures.size() + " pictures");
		
		for (HerbierPic picture : pictures) {
			if (picture.getLocation() == null) {
				DataProblem problem = new DataProblem(picture.getIdx(), ProblemKind.PIC_NO_LOCATION, 
						"La photo " + picture.getFileName() + " n'a pas de lieu");
				vecProblems.add(problem);
			}

			File file = new File(Controller.picturesPath + picture.getFileName());
			if (file.exists()) {
				int size = getImageSize(file);
				if (size > IMAGE_LARGE_SIZE_WARN) {
					DataProblem problem = new DataProblem(picture.getIdx(), ProblemKind.PIC_TOO_LARGE, 
							"La photo " + picture.getFileName() + " est trop grande (" + size + "px)");
					vecProblems.add(problem);
				}
			} else {
				DataProblem problem = new DataProblem(picture.getIdx(), ProblemKind.PIC_MISSING, 
						"La photo " + picture.getFileName() + " est manquante!");
				vecProblems.add(problem);
			}
		}
		
		log.info("Quality check of " + pictures.size() + " pictures: " +
				vecProblems.size() + " problems");
		
		return vecProblems;
	}
	
	/**
	 * Get image size (largest of width or height)
	 * @param file  the jpeg file to parse
	 */
	public static int getImageSize(File file) {
		int size = -1;
		int width  = -1;
		int height = -1;
		try {
			ImageInputStream in = ImageIO.createImageInputStream(file);
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					width  = reader.getWidth(0);
					height = reader.getHeight(0);
				} finally {
					reader.dispose();
				}
			}

			log.debug("Image size : " + width + "x" + height + " - " + file.getName());
			size = Math.max(width, height);

		} catch (Exception e) {
			log.error("Failed to read image size", e);
		}
		return size;
	}

}
