package org.bionlpst.corpus;

import java.util.Collection;
import java.util.HashSet;

import org.bionlpst.util.fragment.Fragment;
import org.bionlpst.util.fragment.ImmutableFragment;

public enum TextBoundCollector implements AnnotationVisitor<Collection<TextBound>,Collection<TextBound>> {
	INSTANCE {
		@Override
		public Collection<TextBound> visit(TextBound textBound, Collection<TextBound> param) {
			param.add(textBound);
			return param;
		}

		@Override
		public Collection<TextBound> visit(Relation relation, Collection<TextBound> param) {
			for (Annotation arg : relation.getArguments()) {
				arg.accept(this, param);
			}
			return param;
		}

		@Override
		public Collection<TextBound> visit(Normalization normalization, Collection<TextBound> param) {
			return normalization.getAnnotation().accept(this, param);
		}

		@Override
		public Collection<TextBound> visit(Modifier modifier, Collection<TextBound> param) {
			return modifier.getAnnotation().accept(this, param);
		}

		@Override
		public Collection<TextBound> visit(DummyAnnotation dummy, Collection<TextBound> param) {
			return param;
		}
	};

	private Collection<TextBound> collect(Annotation ann) {
		return ann.accept(this, new HashSet<TextBound>());
	}

	public ImmutableFragment getGlobalFragment(Annotation ann) {
		int start = Integer.MAX_VALUE;
		int end = 0;
		for (Fragment frag : collect(ann)) {
			start = Math.min(start, frag.getStart());
			end = Math.max(end, frag.getEnd());
		}
		return new ImmutableFragment(start, end);
	}
}