package view;


import model.DataProblem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Link;

import view.base.AbstractModule;
import controller.Controller;
import controller.checks.DataCheckManager;
import controller.checks.ProblemKind;

public class ModuleProblems extends AbstractModule<DataProblem> {

	private Button btnCheckProblems;
	private Link   lnkProblem;
	private SelectionListener listenerLink;
	
	public ModuleProblems() {
		super();
		
		loadWidgets();
		loadData();
	}

	public void showObjects() {
		vecObjects = DataCheckManager.getInstance().getProblems();
		reloadTable();
		lblStatus.setText(String.format("%d problèmes", vecObjects.size()));
		updateProblemLink(null);
	}
	
	@Override
	protected void onTableSelection(DataProblem obj) {
		updateProblemLink(obj);
	}
	
	
	private void enableWidgets(boolean enabled) {
		btnCheckProblems.setEnabled(enabled);
		btnCheckProblems.update();
	}

	@Override
	protected void loadWidgets() {
		((GridData) (tblData.getLayoutData())).widthHint = 400;
		
		initTable(new String[] {"Type", "Description"}, new double[] {0.3, 0.7});
		btnNew.setVisible(false);
		
		btnCheckProblems = widgetsFactory.createPushButton(cButtons, "Vérifier", 
				"system-search", null, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableWidgets(false);
				Runnable runExport = new Runnable() {
					public void run() {
						Controller.getInstance().checkDataQuality();
						loadData();
					}
				};
				BusyIndicator.showWhile(getDisplay(), runExport);
				enableWidgets(true);
			}
		});
		
		lnkProblem = widgetsFactory.createLink(cRight, "Sélectionner un problème", "", null);
		lnkProblem.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		
		Controller.getInstance().addDataListener(this);
	}

	@Override
	protected void loadData() {
		showObjects();
	}
	
	private void updateProblemLink(DataProblem problem) {
		if (listenerLink != null) {
			lnkProblem.removeSelectionListener(listenerLink);
		}
		if (problem == null) {
			lnkProblem.setText("Sélectionner un problème");
			lnkProblem.setToolTipText("");
			listenerLink = null;
		} else {
			lnkProblem.setText("<a>" + problem.getDescription() + "</a>");
			lnkProblem.setToolTipText(problem.getDescription());
			listenerLink = getProblemSelectionListener(problem);
			lnkProblem.addSelectionListener(listenerLink);
		}
		lnkProblem.pack();
	}
	
	private SelectionListener getProblemSelectionListener(DataProblem problem) {
		final int idx = problem.getIdx();
		final Module module = getProblemModule(problem.getKind());
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Panorpa.getInstance().navigate(module, idx);
			}
		};
	}
	
	private Module getProblemModule(ProblemKind kind) {
		if (kind != null) {
			switch(kind) {
			case PIC_NO_LOCATION:
			case PIC_TOO_LARGE:
			case PIC_MISSING:
				return Module.PICS;
			case LOC_NO_DESCR:
				return Module.LOCATIONS;
			case TAX_NO_FRNAME:
				return Module.TAXA;
			default:
				return null;
			}
		}
		return null;
	}

}
