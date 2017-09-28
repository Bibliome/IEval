package org.bionlpst.evaluation.xml;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Relation;
import org.bionlpst.util.Filter;

public class RelationArgumentFilter implements Filter<Annotation> {
	private final String role;
	private final Filter<Annotation> filter;
	
	public RelationArgumentFilter(String role, Filter<Annotation> filter) {
		super();
		this.role = role;
		this.filter = filter;
	}

	@Override
	public boolean accept(Annotation item) {
		Relation rel = item.asRelation();
		if (rel == null) {
			return false;
		}
		if (!rel.hasArgument(role)) {
			return false;
		}
		Annotation arg = rel.getArgument(role);
		return filter.accept(arg);
	}

	@Override
	public Filter<Annotation> reduce() {
		return new RelationArgumentFilter(role, filter.reduce());
	}
}
