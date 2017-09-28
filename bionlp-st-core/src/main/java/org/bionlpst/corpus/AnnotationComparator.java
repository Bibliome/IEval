package org.bionlpst.corpus;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import org.bionlpst.util.fragment.Fragment;
import org.bionlpst.util.fragment.FragmentComparator;
import org.bionlpst.util.fragment.ImmutableFragment;

/**
 * Annotation comparator for display purposes.
 * @author rbossy
 *
 */
public enum AnnotationComparator implements Comparator<Annotation> {
	INSTANCE {
		@Override
		public int compare(Annotation o1, Annotation o2) {
			ImmutableFragment f1 = TextBoundCollector.INSTANCE.getGlobalFragment(o1);
			ImmutableFragment f2 = TextBoundCollector.INSTANCE.getGlobalFragment(o2);
			int r = FragmentComparator.END_INVERSE_START.compare(f1, f2);
			if (r != 0) {
				return r;
			}
			if (o1.accept(AnnotationDependency.INSTANCE, o2)) {
				return 1;
			}
			if (o2.accept(AnnotationDependency.INSTANCE, o1)) {
				return -1;
			}
			return o1.getKind().compareTo(o2.getKind());
		}
	};
	
	private static enum AnnotationDependency implements AnnotationVisitor<Boolean,Annotation> {
		@SuppressWarnings("hiding")
		INSTANCE {
			@Override
			public Boolean visit(TextBound textBound, Annotation param) {
				return false;
			}

			@Override
			public Boolean visit(Relation relation, Annotation param) {
				for (Annotation arg : relation.getArguments()) {
					if (arg == param || arg.accept(this, param)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Boolean visit(Normalization normalization, Annotation param) {
				Annotation ref = normalization.getAnnotation();
				return ref == param || ref.accept(this, param);
			}

			@Override
			public Boolean visit(Modifier modifier, Annotation param) {
				Annotation ref = modifier.getAnnotation();
				return ref == param || ref.accept(this, param);
			}

			@Override
			public Boolean visit(DummyAnnotation dummy, Annotation param) {
				return false;
			}
		};
	}
	
	private enum TextBoundCollector implements AnnotationVisitor<Collection<TextBound>,Collection<TextBound>> {
		@SuppressWarnings("hiding")
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

		private ImmutableFragment getGlobalFragment(Annotation ann) {
			int start = Integer.MAX_VALUE;
			int end = 0;
			for (Fragment frag : collect(ann)) {
				start = Math.min(start, frag.getStart());
				end = Math.max(end, frag.getEnd());
			}
			return new ImmutableFragment(start, end);
		}
	}
}
