package org.peakaboo.curvefit.peak.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.peakaboo.curvefit.peak.transition.PrimaryTransitionSeries;

public class CombinedPeakTable implements PeakTable {

	List<PrimaryTransitionSeries> series;
	PeakTable[] members;
	
	public CombinedPeakTable(PeakTable... members) {
		this.members = members;
	}
	
	private void load() {
		series = new ArrayList<>();
		
		Set<PrimaryTransitionSeries> merged = new HashSet<>();
		for (PeakTable member : members) {
			//add if not already present
			merged.addAll(member.getAll());
		}
		
		series.addAll(merged);
		
		series.sort((t1, t2) -> {
			int shellDiff = t1.getShell().shell() - t2.getShell().shell();
			if (shellDiff != 0) {
				return shellDiff;
			}
			
			return t1.getElement().atomicNumber() - t2.getElement().atomicNumber();			
		});
		
	}

	@Override
	public List<PrimaryTransitionSeries> getAll() {
		if (series == null) {
			load();
		}
		
		List<PrimaryTransitionSeries> copy = new ArrayList<>();
		for (PrimaryTransitionSeries ts : series) {
			copy.add(new PrimaryTransitionSeries(ts));
		}
		return copy;
		
	}
	
}
