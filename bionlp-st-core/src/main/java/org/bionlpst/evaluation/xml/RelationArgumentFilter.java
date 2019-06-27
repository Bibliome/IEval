package org.bionlpst.evaluation.xml;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Relation;
import org.bionlpst.util.Filter;

public class RelationArgumentFilter implements Filter<Annotation> {
	private final String role;
	private final Filter<Annotation> filter;
	private boolean acceptNotRelation;
	private boolean acceptNoArg;
	
	public RelationArgumentFilter(String role, Filter<Annotation> filter, boolean acceptNotRelation, boolean acceptNoArg) {
		super();
		this.role = role;
		this.filter = filter;
		this.acceptNotRelation = acceptNotRelation;
		this.acceptNoArg = acceptNoArg;
	}

	@Override
	public boolean accept(Annotation item) {
		Relation rel = item.asRelation();
		if (rel == null) {
			return acceptNotRelation;
		}
		if (!rel.hasArgument(role)) {
			return acceptNoArg;
		}
		Annotation arg = rel.getArgument(role);
		return filter.accept(arg);
	}

	@Override
	public Filter<Annotation> reduce() {
		return new RelationArgumentFilter(role, filter.reduce(), acceptNotRelation, acceptNoArg);
	}

	@Override
	public void init() {
		filter.init();
	}
}
