package controller.export;

import model.Category;
import model.Taxon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.base.Logger;
import common.io.HtmlComposite;
import controller.TaxonCache;

/**
 * Creates HTML pages containing Highcharts.
 * 
 * @author nicz
 *
 */
public class HighchartsExporter extends BaseExporter {
	
	private static final Logger log = new Logger("HighchartsExporter", true);

	public HighchartsExporter() {
	}
	
	public void export() {
		createTestChart();
	}
	
	private void createTestChart() {
		HtmlPage page = new HtmlPage("Nature - Graphiques");
		HtmlComposite main = page.getMainDiv();
		HtmlComposite head = page.getHead();
		
		// add Highcharts JS files
		head.addScript("https://code.jquery.com/jquery-3.1.1.min.js");
		head.addScript("https://code.highcharts.com/highcharts.js");
		head.addScript("https://code.highcharts.com/modules/exporting.js");
		head.addScript("https://code.highcharts.com/modules/networkgraph.js");
		
		main.addTitle(1, "Graphiques");

		HtmlComposite table = main.addFillTable(2, "600px");
		
		// Pie chart
		HtmlComposite divPieChart = table.addTableData().addDiv("chart-pie");
		divPieChart.setCssClass("highchart");
		JSONObject json = generatePieChart();
		String script = "  Highcharts.chart('chart-pie', " + json.toJSONString() + ");";
		main.addDocumentReady(script);
		
		// Network chart
		HtmlComposite divNetworkChart = table.addTableData().addDiv("chart-network");
		divNetworkChart.setCssClass("highchart");
		json = generateNetworkChart();
		script = "  Highcharts.chart('chart-network', " + json.toJSONString() + ");";
		main.addDocumentReady(script);
		
		page.saveAs(htmlPath + "charts.html");
	}

	@SuppressWarnings("unchecked")
	private JSONObject generateNetworkChart() {
		JSONObject json = new JSONObject();
		
		JSONObject jsonChart = new JSONObject();
		jsonChart.put("type", "networkgraph");
		json.put("chart", jsonChart);
		
		JSONObject jsonExporting = new JSONObject();
		jsonExporting.put("enabled", false);
		json.put("exporting", jsonExporting);
		
		JSONObject jsonTitle = new JSONObject();
		json.put("title", jsonTitle);
		
		JSONObject jsonOptions = new JSONObject();
		JSONObject jsonOptionsNetwork = new JSONObject();
		JSONArray jsonKeys = new JSONArray();
		jsonKeys.add("from");
		jsonKeys.add("to");
		jsonOptionsNetwork.put("keys", jsonKeys);
		jsonOptions.put("networkgraph", jsonOptionsNetwork);
		json.put("plotOptions", jsonOptions);
		
		JSONArray  jsonSeries = new JSONArray();
		JSONObject jsonSerie = new JSONObject();
		json.put("series", jsonSeries);
		jsonSeries.add(jsonSerie);
		JSONArray  jsonData = new JSONArray();
		jsonSerie.put("name", "Taxons");
		jsonSerie.put("data", jsonData);
		
		JSONObject jsonLabels = new JSONObject();
		jsonLabels.put("enabled", true);
		jsonSerie.put("dataLabels", jsonLabels);
		
		Taxon taxTop = TaxonCache.getInstance().getTaxon("Odonata");
		if (taxTop != null) {
			jsonTitle.put("text", "Groupe des " + taxTop.getNameFr());
			addTaxon(taxTop, jsonData);
		}
		
		return json;
	}
	
	@SuppressWarnings("unchecked")
	private void addTaxon(Taxon taxon, JSONArray json) {
		for (Taxon taxChild : taxon.getChildren()) {
			JSONArray value = new JSONArray();
			value.add(taxon.getName());
			value.add(taxChild.getName());
			json.add(value);
			
			addTaxon(taxChild, json);
		}
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject generatePieChart() {
		JSONObject json = new JSONObject();
		
		JSONObject jsonChart = new JSONObject();
		jsonChart.put("type", "pie");
		json.put("chart", jsonChart);
		
		JSONObject jsonExporting = new JSONObject();
		jsonExporting.put("enabled", false);
		json.put("exporting", jsonExporting);
		
		JSONObject jsonTitle = new JSONObject();
		jsonTitle.put("text", "Nombre de photos par catégorie");
		json.put("title", jsonTitle);
		
		JSONObject jsonOptions = new JSONObject();
		JSONObject jsonOptionsPie = new JSONObject();
		jsonOptionsPie.put("showInLegend", false);
		jsonOptions.put("pie", jsonOptionsPie);
		json.put("plotOptions", jsonOptions);
		
		JSONArray  jsonSeries = new JSONArray();
		JSONObject jsonSerie = new JSONObject();
		json.put("series", jsonSeries);
		jsonSeries.add(jsonSerie);
		JSONArray  jsonData = new JSONArray();
		jsonSerie.put("name", "Photos");
		jsonSerie.put("data", jsonData);
		
		//for (Taxon taxon : TaxonCache.getInstance().getTopLevel()) {
		for (Category category : Category.values()) {
			Taxon taxon = TaxonCache.getInstance().getTaxon(category.getName());
			if (taxon != null) {
				JSONObject jsonValue = new JSONObject();
				jsonValue.put("name", taxon.getNameFr());
				String color = category.getColor();
				if (color != null) {
					jsonValue.put("color", color);
				}
				jsonValue.put("y", taxon.getPicsCascade().size());
				jsonData.add(jsonValue);
			} else {
				log.error("Failed to find taxon named '" + category.getName() + "' in cache.");
			}
		}
		
		return json;
	}
	
//	private String getTaxonColor(Taxon taxon) {
//		String color = null;
//		
//		switch(taxon.getName()) {
//		case "Plantae":
//			color = "#00ee00";
//			break;
//		case "Animalia":
//			color = "#ee0000";
//			break;
//		case "Fungi":
//			color = "#cdba96";
//			break;
//		}
//		
//		return color;
//	}

}
