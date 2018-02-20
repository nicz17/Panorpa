package view;

import java.util.Vector;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import controller.Controller;

import model.Taxon;
import model.TaxonRank;
import view.base.AbstractSelector;

public class TaxonSelector extends AbstractSelector<Taxon> {
	
	/** The rank of taxons to load. */
	private TaxonRank rank;

	public TaxonSelector(String name, Composite parent, ModifyListener listener) {
		super(name, parent, true, listener);
		rank = null;
	}
	
	public void setRank(TaxonRank rank) {
		boolean needReload = (this.rank != rank);
		this.rank = rank;
		if (needReload) {
			load();
		}
	}

	@Override
	protected Vector<Taxon> getData() {
		Vector<Taxon> data = Controller.getInstance().getTaxons(rank);
		data.add(null);
		return data;
	}

	@Override
	public String getDisplayValue(Taxon taxon) {
		if (taxon == null) {
			return "(aucun)";
		} else {
			return taxon.getName();
		}
	}

	@Override
	protected Taxon newValueFromText(String text) {
		return null;
	}

}
