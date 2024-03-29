package controller.export;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;
import common.html.HtmlTag;
import common.html.HtmlTagFactory;
import common.html.ListHtmlTag;
import common.html.TableHtmlTag;

import controller.DataAccess;
import controller.DatabaseTools.eOrdering;
import controller.PictureCache;
import controller.TaxonCache;

/**
 * Subclass of Exporter to export pictures.
 * 
 * @author nicz
 *
 */
public class PictureExporter extends BaseExporter {
	
	protected static final int nLatestPics = 20;

	public PictureExporter() {
		
	}
	
	public void export() {
		createAlphaNamesPage("Noms latins",        "Noms latins",  "noms-latins.html", eOrdering.BY_NAME);
		createAlphaNamesPage("Noms vernaculaires", "Vernaculaire", "noms-verna.html",  eOrdering.BY_NAME_FR);
		
		createLatestPicsPage();
		createBestPicsPage();
	}
	
	
	private void createAlphaNamesPage(String title, String menuTitle, String filename, eOrdering order) {
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - " + title, htmlPath + filename, "");
		page.addTitle(1, title);

		// menu
		page.addMenuItem(2, menuTitle, menuTitle);
		TableHtmlTag menuTable = new TableHtmlTag(4);
		menuTable.addAttribute("width", "120px");
		page.getMenu().addTag(menuTable);
		
		// taxon list
		ListHtmlTag ul = null;
		String currLetter = null;
		
		Comparator<Taxon> comparator = null;
		switch(order) {
		case BY_NAME:
			comparator = new Comparator<Taxon>() {
				@Override
				public int compare(Taxon tax1, Taxon tax2) {
					return tax1.getName().compareTo(tax2.getName());
				}
			};
			break;
		case BY_NAME_FR:
			// Use a French collator to ignore accents in comparison
			final Collator collator = Collator.getInstance(Locale.FRENCH);
			comparator = new Comparator<Taxon>() {
				@Override
				public int compare(Taxon tax1, Taxon tax2) {
					return collator.compare(tax1.getNameFr(), tax2.getNameFr());
				}
			};
			break;
		default:
			break;
		}

		List<Taxon> listTaxa = new ArrayList<>(TaxonCache.getInstance().getAll());
		Collections.sort(listTaxa, comparator);
		
		for (Taxon taxon : listTaxa) {
			
			// include only species and genus without species
			if (taxon.getRank() != TaxonRank.SPECIES && taxon.getRank() != TaxonRank.GENUS) {
				continue;
			}
			
			if (taxon.getRank() == TaxonRank.GENUS && taxon.hasSubTaxa()) {
				continue;
			}
			
			// TODO add Families without subtaxa
			
			String name = null;
			switch(order) {
			case BY_NAME_FR:
				name = taxon.getNameFr();
				break;
			default:
				name = taxon.getName();
				break;
			}
			
			// TODO log an error and continue if name is empty or null
			
			String letter = name.substring(0, 1);
			if (!letter.equals(currLetter)) {
				HtmlTag tAnchor = HtmlTagFactory.anchor(letter);
				tAnchor.addTag(HtmlTagFactory.title(2, letter));
				page.add(tAnchor);
				ul = page.addList();
				menuTable.addCell(HtmlTagFactory.link("#" + letter, letter, "Aller à la lettre " + letter));
				currLetter = letter;
			}
			
			String picFile = getTaxonHtmlFileName(taxon);
			Vector<HtmlTag> tags = new Vector<>();
			tags.add(HtmlTagFactory.link("pages/" + picFile, name, getTooltiptext(taxon)));
			tags.add(HtmlTagFactory.grayFont(" - " + taxon.getAncestor(TaxonRank.FAMILY).getName()));
			ul.addItem(tags);
		}
		
		page.save();
	}
	
	/**
	 * Creates a HTML page with the latest pictures.
	 */
	private void createLatestPicsPage() {
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - Dernières photos", htmlPath + "latest.html", "");
		page.addTitle(1, "Dernières photos");
		
		Vector<HerbierPic> pics = new Vector<>(PictureCache.getInstance().getAll());
		Collections.sort(pics, new Comparator<HerbierPic>() {
			@Override
			public int compare(HerbierPic pic1, HerbierPic pic2) {
				Date date1 = pic1.getShotAt();
				Date date2 = pic2.getShotAt();
				if (date1.equals(date2)) {
					return pic1.getFileName().compareTo(pic2.getFileName());
				}
				return date2.compareTo(date1);
			}
		});
		
		TableHtmlTag table = page.addTable(nColumns, "100%");
		table.setClass("table-thumbs");
		
		Iterator<HerbierPic> it = pics.iterator();
		int nPics = 0;
		while (it.hasNext() && nPics < nLatestPics) {
			HerbierPic pic = it.next();
			nPics++;
			exportPicture(pic, table, true);
		}
		
		page.save();
	}
	
	/**
	 * Creates a HTML page with the best pictures.
	 */
	private void createBestPicsPage() {
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - Florilège", htmlPath + "bestof.html", "");
		page.addTitle(1, "Florilège");
		
		Vector<HerbierPic> pics = 
				DataAccess.getInstance().getHerbierPics("WHERE picRating >= 5", null, null);
		Collections.sort(pics, new Comparator<HerbierPic>() {
			@Override
			public int compare(HerbierPic pic1, HerbierPic pic2) {
				Date date1 = pic1.getShotAt();
				Date date2 = pic2.getShotAt();
				if (date1.equals(date2)) {
					return pic1.getFileName().compareTo(pic2.getFileName());
				}
				return date2.compareTo(date1);
			}
		});

		int nPics = pics.size();
		page.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));

		TableHtmlTag table = page.addTable(nColumns);
		table.addAttribute("width", "100%");
		table.setClass("table-thumbs");
		
		Iterator<HerbierPic> it = pics.iterator();
		while (it.hasNext()) {
			HerbierPic pic = it.next();
			// reload from cache to have taxa
			pic = PictureCache.getInstance().getPicture(pic.getIdx());
			exportPicture(pic, table, true);
		}
		
		page.save();
	}
	

}
