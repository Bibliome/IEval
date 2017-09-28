package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BootstrapConfig {
	private final Random random;
	private final int resamples;
	
	public BootstrapConfig(Random random, int resamples) {
		super();
		this.random = random;
		this.resamples = resamples;
	}

	public Random getRandom() {
		return random;
	}

	public int getResamples() {
		return resamples;
	}
	
	public <T> List<Pair<T>> resample(List<Pair<T>> pairs, List<Pair<T>> sample) {
		final int n = pairs.size();
		if (sample == null) {
			sample = new ArrayList<Pair<T>>(n);
		}
		else {
			sample.clear();
		}
		for (int i = 0; i < n; ++i) {
			int j = random.nextInt(n);
			Pair<T> p = pairs.get(j);
			sample.add(p);
		}
		return sample;
	}
}
