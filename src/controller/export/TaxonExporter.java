package controller.export;

import java.util.Set;
import java.util.Vector;

import common.base.Logger;
import common.html.HtmlTag;
import common.html.HtmlTagFactory;
import common.html.TableHtmlTag;
import common.html.ParHtmlTag;
import model.HerbierPic;
import model.Location;
import model.Taxon;
import model.TaxonRank;

/**
 * Export species as HTML pages.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>04.02.2023: nicz - Creation</li>
 * </ul>
 */
public class TaxonExporter extends BaseExporter {

	private static final Logger log = new Logger("TaxonExporter", true);
	protected TaxonUrlProvider taxonUrlProvider;

	public TaxonExporter() {
		taxonUrlProvider = new TaxonUrlProvider();
	}
	
	public void exportTaxon(Taxon taxon, Taxon prevTaxon, Taxon nextTaxon) {
		Set<HerbierPic> pics = taxon.getPics();
		if (pics.isEmpty()) {
			return;
		}
		
		TaxonRank rank = taxon.getRank();
		String name = taxon.getName();
		String filename = htmlPath + "pages/" + getTaxonHtmlFileName(taxon);
		//log.info("Creating html page for " + name + " with " + pics.size() + " pics as " + filename);
		
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - " + name, filename, "../");
		page.addMenuItem(3, "#", name);

		// Title and subtitle
		String title = name;
		if (!name.equals(taxon.getNameFr())) {
			title += taxonNameSeparator + taxon.getNameFr();
		}
		page.add(HtmlTagFactory.title(1, title));
		if (rank != TaxonRank.SPECIES && rank != TaxonRank.GENUS) {
			page.add(HtmlTagFactory.title(2, "<font color='gray'>Genre indéterminé</font>"));
		}
		
		// Photos table
		TableHtmlTag tablePhotos = new TableHtmlTag(2);
		tablePhotos.addAttribute("width", "1040px");
		tablePhotos.setClass("table-medium");
		
		for (HerbierPic hpic : pics) {
			// image link
			HtmlTag linkImage = new HtmlTag("a");
			linkImage.addAttribute("href", hpic.getFileName());
			linkImage.addTag(HtmlTagFactory.image("../medium/" + hpic.getFileName(), name));

			// image details
			Location location = hpic.getLocation();
			String sCaption = "";
			if (location != null) {
				sCaption += page.getLink("../lieu" + location.getIdx() + ".html", location.getName(), false);
				int altitude = location.getAltitude();
				if (altitude >= 1000) {
					sCaption += " (" + altitude + "m)";
				}
				sCaption += ", " + location.getRegion();
				if (!"Suisse".equals(location.getState())) {
					sCaption += ", " + location.getState();
				}
				sCaption += ", ";
			}
			sCaption += dateFormat.format(hpic.getShotAt());
			
			Vector<HtmlTag> vecTags = new Vector<>();
			vecTags.add(HtmlTagFactory.anchor(hpic.getFileName().replace(".jpg", "")));
			vecTags.add(linkImage);
			vecTags.add(new ParHtmlTag(sCaption));
			
			String remarks = hpic.getRemarks();
			if (remarks != null && !remarks.isEmpty()) {
				remarks = replaceRemarkLinks(remarks);
				vecTags.add(new ParHtmlTag(remarks));
			}
			tablePhotos.addCell(vecTags);
		}
		if (pics.size() % 2 != 0) {
			// Add empty cell for table alignment
			tablePhotos.addCell("");
		}
		page.add(tablePhotos);
		
		// External links
		ParHtmlTag par = new ParHtmlTag("Pages ");
		taxonUrlProvider.addLinks(par, taxon);
		page.add(par);
		
		HtmlTag boxClassif = HtmlTagFactory.blueBox("Classification");
		TableHtmlTag tableClassif = new TableHtmlTag(3);
		tableClassif.addAttribute("width", "500px");
		boxClassif.addTag(tableClassif);
		page.add(boxClassif);

		Vector<Taxon> vecTaxa = new Vector<>();
		vecTaxa.add(taxon);
		Taxon parent = taxon.getParent();
		while (parent != null) {
			vecTaxa.add(0, parent);
			parent = parent.getParent();
		}
		
		for (Taxon tax : vecTaxa) {
			addClassificationTableRank(tableClassif, tax);
			addClassificationTableData(tableClassif, tax);
			tableClassif.addCell(tax.getNameFr());
		}

		page.save();
	}
	
	private void addClassificationTableRank(TableHtmlTag table, Taxon taxon) {
		TaxonRank rank = taxon.getRank();
		HtmlTag tIcon = HtmlTagFactory.image("rank" + rank.getOrder() + ".svg", rank.getGuiName());
		String sIcon = tIcon.toHtml(0, true);
		table.addCell(sIcon + " " + rank.getGuiName());
	}

	/**
	 * Add a cell to the specified table with a link to the specified taxon.
	 * @param table  the classification table
	 * @param taxon  the taxon to add to table
	 */
	private void addClassificationTableData(TableHtmlTag table, Taxon taxon) {
		switch(taxon.getRank()) {
		case KINGDOM:
			table.addCell(HtmlTagFactory.link("../classification.html#" + taxon.getName(), 
				taxon.getName(), "Aller aux " + taxon.getNameFr()));
			break;
		case PHYLUM:
			table.addCell(HtmlTagFactory.link("../" + taxon.getName() + ".html", 
				taxon.getName(), "Aller au phylum des " + taxon.getNameFr()));
			break;
		case CLASS:
			Taxon phylum = taxon.getParent();
			table.addCell(HtmlTagFactory.link("../" + phylum.getName() + ".html#" + taxon.getName(), 
				taxon.getName(), "Aller à la classe des " + taxon.getNameFr()));
			break;
		case ORDER:
			table.addCell(HtmlTagFactory.link("../" + taxon.getName() + ".html", 
				taxon.getName(), "Aller à l'ordre des " + taxon.getNameFr()));
			break;
		case FAMILY:
			Taxon order = taxon.getParent();
			table.addCell(HtmlTagFactory.link("../" + order.getName() + ".html#" + taxon.getName(), 
				taxon.getName(), "Aller à la famille des " + taxon.getNameFr()));
			break;
		case GENUS:
		case SPECIES:
			table.addCell("<i>" + taxon.getName() + "</i>");
			break;
		default:
			table.addCell(taxon.getName());
			break;
		}
	}
}
