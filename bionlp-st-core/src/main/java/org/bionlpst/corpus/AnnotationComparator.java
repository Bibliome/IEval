package org.bionlpst.corpus;

import java.util.Comparator;

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
}
