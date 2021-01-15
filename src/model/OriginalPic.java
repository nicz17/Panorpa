package model;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import controller.FileManager;

/**
 * This object represents an original JPEG file
 * and is used for preselection.
 * 
 * @author nicz
 *
 */
public class OriginalPic extends DataObject implements HasPhoto, Comparable<OriginalPic> {
	
	public  static final Pattern patOrigFileName = Pattern.compile("^[_A-z]+([0-9]+)\\.JPG");
	private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	private int idx;
	private File origFile;
	private Date tShotAt;

	public OriginalPic(int idx, File origFile) {
		this.idx = idx;
		this.origFile = origFile;
		this.tShotAt = FileManager.getInstance().getShotAt(origFile);
	}
	
	
	/**
	 * @return the original file object.
	 */
	public File getOrigFile() {
		return origFile;
	}

	/**
	 * @return the thumbnail file object.
	 */
	public File getThumbFile() {
		String sThumbName = origFile.getAbsolutePath().replace("orig/", "thumbs/");
		return new File(sThumbName);
	}

	public Date getShotAt() {
		return tShotAt;
	}

	@Override
	public String getFileName() {
		return origFile.getAbsolutePath().replace("orig/", "thumbs/");
		//return origFile.getAbsolutePath();
	}

	/**
	 * Return the file name without path.
	 */
	@Override
	public String getName() {
		return origFile.getName();
	}

	@Override
	public int getIdx() {
		return idx;
	}

	@Override
	public String[] getDataRow() {
		return new String[] {String.valueOf(idx), getName(), dateFormat.format(tShotAt)};
	}
	
	@Override
	public String toString() {
		return "OriginalPic " + getName();
	}

	@Override
	public int compareTo(OriginalPic pic) {
		return getName().compareTo(pic.getName());
	}

}
