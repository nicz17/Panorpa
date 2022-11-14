package model;

import java.util.Comparator;
import java.util.Date;

import common.data.HasMapCoordinates;

public class HerbierPic extends DataObject 
	implements HasPhoto, HasMapCoordinates, Comparable<HerbierPic> {
	
	/**
	 * Compares two photos by their shot-at timestamp, oldest first.
	 */
	public static Comparator<HerbierPic> comparatorByShotAt = new Comparator<HerbierPic>() {
		@Override
		public int compare(HerbierPic pic1, HerbierPic pic2) {
			return (pic1.getShotAt().before(pic2.getShotAt()) ? -1 : 1);
		}
	};

	private int idx;
	private String fileName;
	private Date shotAt;
	private Location location;
	private String remarks;
	private int idxTaxon;
	private Taxon taxon;
	private Date updatedAt;
	private int rating;
	
	private Double dLat;
	private Double dLon;
	
	public HerbierPic(int idx, String fileName) {
		this.fileName = fileName;
		this.idx = idx;
		this.idxTaxon = -1;
		this.rating = 3;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getGenus() {
		return getTaxonName(TaxonRank.GENUS, false);
	}

	public String getSpecie() {
		return getTaxonName(TaxonRank.SPECIES, false);
	}

	public String getFamily() {
		return getTaxonName(TaxonRank.FAMILY, false);
	}
	
	public String getClassification() {
		String result = "";
		
		Taxon parent = (taxon == null ? null : taxon.getParent());
		while (parent != null) {
			if (!result.isEmpty()) {
				result += " - ";
			}
			result += parent.getName();
			parent = parent.getParent();
		}
		
		return result;
	}

	public String getFrenchName() {
		String name = "(inconnu)";
		if (taxon != null) {
			name = taxon.getNameFr();
		}
		return name;
		//return getTaxonName(TaxonRank.SPECIES, true);
	}

	public Date getShotAt() {
		return shotAt;
	}

	public void setShotAt(Date shotAt) {
		this.shotAt = shotAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getRemarks() {
		return remarks == null ? "" : remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	@Override
	public String toString() {
		return "HerbierPic [" + fileName + "]";
	}

	public int getIdxTaxon() {
		return idxTaxon;
	}

	public void setIdxTaxon(int idxTaxon) {
		this.idxTaxon = idxTaxon;
	}

	public Taxon getTaxon() {
		return taxon;
	}

	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

	@Override
	public Double getLatitude() {
		return dLat;
	}

	@Override
	public Double getLongitude() {
		return dLon;
	}

	public void setLatitude(Double dLat) {
		this.dLat = dLat;
	}

	public void setLongitude(Double dLon) {
		this.dLon = dLon;
	}

	@Override
	public int getMapZoom() {
		return 0;
	}

	@Override
	public Double getDistance(HasMapCoordinates objTo) {
		return null;
	}


	/**
	 * Gets the latin taxon name.
	 */
	@Override
	public String getName() {
		String name = "(inconnu)";
		if (taxon != null) {
			name = taxon.getName();
		}
		return name;
	}

	@Override
	public int getIdx() {
		return idx;
	}

	@Override
	public String[] getDataRow() {
		String locationName = location == null ? "" : location.getName();
		return new String[] {String.valueOf(getIdx()), getName(), 
				getFrenchName(), getFamily(), locationName, 
				String.valueOf(rating)};
	}
	
	private String getTaxonName(TaxonRank rank, boolean isFrenchName) {
		String result = null;
		
		Taxon taxon = this.taxon;
		while (taxon != null) {
			if (taxon.getRank().equals(rank)) {
				result = isFrenchName ? taxon.getNameFr() : taxon.getName();
				break;
			} else {
				taxon = taxon.getParent();
			}
		}
		
		if (result == null) result = "";
		return result;
	}

	@Override
	public int compareTo(HerbierPic pic) {
		if (pic == null) return -1;
		return getFileName().compareTo(pic.getFileName());
	}
	

}
