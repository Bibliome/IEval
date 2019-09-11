package org.bionlpst.corpus.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.AnnotationVisitor;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.DummyAnnotation;
import org.bionlpst.corpus.Equivalence;
import org.bionlpst.corpus.Modifier;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.util.fragment.Fragment;

public enum BioNLPSTWriter {
	;
	
	public static void write(Corpus corpus, File outputDir) throws FileNotFoundException {
		outputDir.mkdirs();
		for (Document doc : corpus.getDocuments()) {
			writeTxt(doc, outputDir);
			writeAnnotations(doc, outputDir, AnnotationSetSelector.INPUT);
			writeAnnotations(doc, outputDir, AnnotationSetSelector.REFERENCE);
		}
	}
	
	private static PrintStream open(Document doc, File outputDir, String ext) throws FileNotFoundException {
		File f = new File(outputDir, doc.getId() + ext);
		return new PrintStream(f);
	}

	private static void writeTxt(Document doc, File outputDir) throws FileNotFoundException {
		try (PrintStream ps = open(doc, outputDir, ".txt")) {
			ps.print(doc.getContents());
		}
	}
	
	private static String ext(AnnotationSetSelector selector) {
		switch (selector) {
			case INPUT: return ".a1";
			case PREDICTION:
			case REFERENCE: return ".a2";
		}
		throw new RuntimeException();
	}
	
	private static void writeAnnotations(Document doc, File outputDir, AnnotationSetSelector selector) throws FileNotFoundException {
		AnnotationSet aset = selector.getAnnotationSet(doc);
		try (PrintStream ps = open(doc, outputDir, ext(selector))) {
			for (Annotation a : aset.getAnnotations()) {
				if (a.getKind().equals(AnnotationKind.DUMMY)) {
					continue;
				}
				ps.printf("%s\t%s ", a.getId(), a.getType());
				a.accept(AnnotationWriter.INSTANCE, ps);
				ps.println();
			}
			if (selector.equals(AnnotationSetSelector.REFERENCE)) {
				for (Equivalence equiv : doc.getEquivalences()) {
					ps.print("*\t");
					boolean notFirst = false;
					for (Annotation a : equiv.getAnnotations()) {
						if (notFirst) {
							ps.print(' ');
						}
						else {
							notFirst = true;
						}
						ps.print(a.getId());
					}
					ps.println();
				}
			}
		}
	}
	
	private static enum AnnotationWriter implements AnnotationVisitor<Void,PrintStream> {
		INSTANCE;
		
		@Override
		public Void visit(TextBound textBound, PrintStream param) {
			boolean notFirst = false;
			for (Fragment f : textBound.getFragments()) {
				if (notFirst) {
					param.print(';');
				}
				else {
					notFirst = true;
				}
				param.printf("%d %d", f.getStart(), f.getEnd());
			}
			param.printf("\t%s", textBound.getForm());
			return null;
		}

		@Override
		public Void visit(Relation relation, PrintStream param) {
			boolean notFirst = false;
			for (Map.Entry<String,Annotation> e : relation.getArgumentMap().entrySet()) {
				if (notFirst) {
					param.print(' ');
				}
				else {
					notFirst = true;
				}
				param.printf("%s:%s", e.getKey(), e.getValue().getId());
			}
			return null;
		}

		@Override
		public Void visit(Normalization normalization, PrintStream param) {
			param.printf("Annotation:%s Referent:%s", normalization.getAnnotation().getId(), normalization.getReferent());
			return null;
		}

		@Override
		public Void visit(Modifier modifier, PrintStream param) {
			param.printf("Annotation:%s", modifier.getAnnotation().getId());
			return null;
		}

		@Override
		public Void visit(DummyAnnotation dummy, PrintStream param) {
			throw new RuntimeException();
		}
	}
}
