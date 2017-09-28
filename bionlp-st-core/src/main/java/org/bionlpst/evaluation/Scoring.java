package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Filter;
import org.bionlpst.util.Util;

/**
 * A scoring computes different measures on pairings.
 * @author rbossy
 *
 * @param <T>
 */
public class Scoring<T> {
	private final String name;
	private final Filter<Pair<T>> postFilter;
	private final Similarity<T> similarity;
	private final List<Measure> measures = new ArrayList<Measure>();

	/**
	 * Creates a new scoring.
	 * @param postFilter pair filter.
	 * @param similarity pair similarity function.
	 */
	public Scoring(String name, Filter<Pair<T>> postFilter, Similarity<T> similarity) {
		super();
		this.name = name;
		this.postFilter = postFilter;
		this.similarity = similarity;
	}

	/**
	 * Creates a new scoring.
	 * @param postFilter pair filter.
	 * @param similarity pair similarity function.
	 * @param measures named measures.
	 */
	public Scoring(String name, Filter<Pair<T>> postFilter, Similarity<T> similarity, Collection<Measure> measures) {
		this(name, postFilter, similarity);
		this.measures.addAll(measures);
	}
	
	public String getName() {
		return name;
	}

	public Filter<Pair<T>> getPostFilter() {
		return postFilter;
	}

	public Similarity<T> getSimilarity() {
		return similarity;
	}

	public List<Measure> getMeasures() {
		return Collections.unmodifiableList(measures);
	}
	
	public void addMeasure(Measure measure) {
		measures.add(measure);
	}

	@Deprecated
	public ScoringResult<T> getResult(Collection<Pair<T>> pairs) {
		List<Pair<T>> filtered = Util.filter(postFilter, pairs);
		List<MeasureResult> measureResults = new ArrayList<MeasureResult>(measures.size());
		for (Measure m : measures) {
			Number n = m.compute(similarity, filtered);
			measureResults.add(new MeasureResult(m, n));
		}
		return new ScoringResult<T>(this, measureResults);
	}

	public ScoringResult<T> getResult(Collection<Pair<T>> pairs, BootstrapConfig bootstrap) {
		List<Pair<T>> filtered = Util.filter(postFilter, pairs);
		List<MeasureResult> measureResults = new ArrayList<MeasureResult>(measures.size());
		for (Measure m : measures) {
			Number n = m.compute(similarity, filtered);
			measureResults.add(new MeasureResult(m, n));
		}
		if (bootstrap != null) {
			List<Pair<T>> sample = new ArrayList<Pair<T>>(filtered.size());
			for (int i = 0; i < bootstrap.getResamples(); ++i) {
				bootstrap.resample(filtered, sample);
				for (MeasureResult mr : measureResults) {
					Measure m = mr.getMeasure();
					Number n = m.compute(similarity, sample);
					mr.addResample(n);
				}
			}
		}
		return new ScoringResult<T>(this, measureResults);
	}
}
