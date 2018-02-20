package view;


import java.io.File;
import java.util.List;

import model.StatItem;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;

import view.base.AbstractModule;

import common.view.CyclicNavigationBar;

import controller.ChartExporter;
import controller.Controller;

public class ModuleStats extends AbstractModule<StatItem> {

	//private ProgressBox proBox;
	private CyclicNavigationBar navBar;
	private Label lblChart;
	private Button btnCharts;
	
	private List<File> listCharts;

	public ModuleStats() {
		super();
		
		loadWidgets();
		loadData();
	}

	public void showObjects() {
		vecObjects = Controller.getInstance().getStats();
		reloadTable();
		lblStatus.setText(String.format("%d statistiques", vecObjects.size()));
	}
	
	@Override
	protected void onTableSelection(StatItem obj) {
	}
	
	/**
	 * Get the list of existing charts and display the first one, if any.
	 */
	private void loadCharts() {
		listCharts = ChartExporter.getInstance().getChartFiles();
		
		if (!listCharts.isEmpty()) {
			navBar.reset(listCharts.size());
		}
	}
	
	private void enableWidgets(boolean enabled) {
		btnCharts.setEnabled(enabled);
		navBar.setEnabled(enabled);
		
		btnCharts.update();
		navBar.update();
	}

	@Override
	protected void loadWidgets() {
		((GridData) (tblData.getLayoutData())).widthHint = 400;
		
		initTable(StatItem.getTableHeader(), new double[] {0.3, 0.2});
		btnNew.setVisible(false);
		
		btnCharts = widgetsFactory.createPushButton(cButtons, "Exporter graphiques", 
				"stats", null, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableWidgets(false);
				Runnable runExport = new Runnable() {
					public void run() {
						//Controller.getInstance().exportCharts(proBox);
						Controller.getInstance().exportCharts();
						loadCharts();
					}
				};
				BusyIndicator.showWhile(getDisplay(), runExport);
				enableWidgets(true);
			}
		});
		
		//proBox = new ProgressBox(cRight, 100);
		
		lblChart = widgetsFactory.createPictureLabel(cRight, 600, 800);

		navBar = new CyclicNavigationBar(cRight, 0) {
			@Override
			public void onSelectionChange(int selection) {
				navBar.setStatus("Graphique " + navBar.getIofN());
				lblChart.setImage(new Image(getDisplay(), 
						listCharts.get(selection).getPath()));
			}
		};

		Controller.getInstance().addDataListener(this);
	}

	@Override
	protected void loadData() {
		showObjects();
		loadCharts();
	}

}
