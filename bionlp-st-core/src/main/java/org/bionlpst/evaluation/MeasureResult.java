package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeasureResult {
	private final Measure measure;
	private final Number result;
	private final List<Number> resamples = new ArrayList<Number>();
	
	MeasureResult(Measure measure, Number result) {
		super();
		this.measure = measure;
		this.result = result;
	}

	public Measure getMeasure() {
		return measure;
	}

	public Number getResult() {
		return result;
	}

	public List<Number> getResamples() {
		return Collections.unmodifiableList(resamples);
	}
	
	public void addResample(Number n) {
		resamples.add(n);
	}
	
	public static class ConfidenceInterval {
		public final double p;
		public final double lo;
		public final double hi;
		
		public ConfidenceInterval(double p, double lo, double hi) {
			super();
			this.p = p;
			this.lo = lo;
			this.hi = hi;
		}
	}

	public ConfidenceInterval getConfidenceInterval(double p) {
		if (resamples.isEmpty()) {
			return null;
		}
		List<Double> copy = new ArrayList<Double>(resamples.size());
		for (Number n : resamples) {
			copy.add(n.doubleValue());
		}
		Collections.sort(copy);
		int confidenceN = (int) Math.round(p * resamples.size());
		int loN = (resamples.size() - confidenceN) / 2;
		int hiN = loN + confidenceN;
		return new ConfidenceInterval(p, copy.get(loN).doubleValue(), copy.get(hiN).doubleValue());
	}
}
