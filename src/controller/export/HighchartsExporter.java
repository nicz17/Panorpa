package controller.export;

import model.Taxon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.io.HtmlComposite;
import controller.TaxonCache;

/**
 * Creates HTML pages containing Highcharts.
 * 
 * @author nicz
 *
 */
public class HighchartsExporter extends BaseExporter {

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
		
		main.addTitle(1, "Graphiques");

		HtmlComposite divChart = main.addDiv("chart-test");
		divChart.setCssClass("highchart");
		
		JSONObject json = generateChart();
		String script = "  Highcharts.chart('chart-test', " + json.toJSONString() + ");";
		main.addDocumentReady(script);
		
		page.saveAs(htmlPath + "charts.html");
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject generateChart() {
		JSONObject json = new JSONObject();
		
		JSONObject jsonChart = new JSONObject();
		jsonChart.put("type", "pie");
		json.put("chart", jsonChart);
		
		JSONObject jsonExporting = new JSONObject();
		jsonExporting.put("enabled", false);
		json.put("exporting", jsonExporting);
		
		JSONObject jsonTitle = new JSONObject();
		jsonTitle.put("text", "Nombre de photos par categorie");
		json.put("title", jsonTitle);
		
		JSONObject jsonOptions = new JSONObject();
		JSONObject jsonOptionsPie = new JSONObject();
		jsonOptionsPie.put("showInLegend", true);
		jsonOptions.put("pie", jsonOptionsPie);
		json.put("plotOptions", jsonOptions);
		
		JSONArray  jsonSeries = new JSONArray();
		JSONObject jsonSerie = new JSONObject();
		json.put("series", jsonSeries);
		jsonSeries.add(jsonSerie);
		JSONArray  jsonData = new JSONArray();
		jsonSerie.put("name", "Photos");
		jsonSerie.put("data", jsonData);
		
		for (Taxon taxon : TaxonCache.getInstance().getTopLevel()) {
			JSONObject jsonValue = new JSONObject();
			jsonValue.put("name", taxon.getNameFr());
			String color = getTaxonColor(taxon);
			if (color != null) {
				jsonValue.put("color", color);
			}
			jsonValue.put("y", taxon.getPicsCascade().size());
			jsonData.add(jsonValue);
		}
		
		return json;
	}
	
	private String getTaxonColor(Taxon taxon) {
		String color = null;
		
		switch(taxon.getName()) {
		case "Plantae":
			color = "#00ee00";
			break;
		case "Animalia":
			color = "#ee0000";
			break;
		case "Fungi":
			color = "#cdba96";
			break;
		}
		
		return color;
	}

}
