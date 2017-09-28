package org.bionlpst.evaluation.similarity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Relation;

public class SeeDevFull implements Similarity<Annotation> {
	private static final Map<String,String[]> relationArguments = new HashMap<String,String[]>();
	static {
		relationArguments.put("Binding", new String[] { "Functional_Molecule", "Molecule" });
		relationArguments.put("Primary_Structure_Composition", new String[] { "DNA_Part", "DNA" });
		relationArguments.put("Protein_Complex_Composition", new String[] { "Amino_Acid_Sequence", "Protein_Complex" });
		relationArguments.put("Presence_At_Stage", new String[] { "Functional_Molecule", "Development" });
		relationArguments.put("Presence_In_Genotype", new String[] { "Molecule", "Element", "Genotype" });
		relationArguments.put("Sequence_Identity", new String[] { "Element1", "Element2" });
		relationArguments.put("Interaction", new String[] { "Agent", "Target" });
		relationArguments.put("Functional_Equivalence", new String[] { "Element1", "Element2" });
		relationArguments.put("Involvement_In_Process", new String[] { "Participant", "Process" });
		relationArguments.put("Localization", new String[] { "Functional_Molecule", "Process", "Target_Tissue" });
		relationArguments.put("Family_Membership", new String[] { "Element", "Family" });
		relationArguments.put("Protein_Domain_Composition", new String[] { "Domain", "Product" });
		relationArguments.put("Occurrence_During", new String[] { "Process", "Development" });
		relationArguments.put("Occurrence_In_Genotype", new String[] { "Process", "Genotype" });
		relationArguments.put("Regulation_Of_Accumulation", new String[] { "Agent", "Functional_Molecule" });
		relationArguments.put("Regulation_Of_Development_Phase", new String[] { "Agent", "Development" });
		relationArguments.put("Regulation_Of_Expression", new String[] { "Agent", "DNA" });
		relationArguments.put("Regulation_Of_Molecule_Activity", new String[] { "Agent", "Molecule" });
		relationArguments.put("Regulation_Of_Process", new String[] { "Agent", "Process" });
		relationArguments.put("Regulation_Of_Tissue_Development", new String[] { "Agent", "Target_Tissue" });
		relationArguments.put("Transcription_Or_Translation", new String[] { "Source", "Product" });
	}
	private static final Collection<String> commutativeTypes = new HashSet<String>(Arrays.asList("Sequence_Identity", "Functional_Molecule"));
	private static final Collection<String> optionalArguments = new LinkedHashSet<String>(Arrays.asList("Tissue", "Developmental_Stage", "Organism_Genotype", "Environmental_Factor", "Hormone", "Prerequisite_Event"));

	private static boolean sameType(Relation rela, Relation relb) {
		String typea = rela.getType();
		String typeb = relb.getType();
		return typea.equals(typeb);
	}

	private static boolean sameTwoFirstArguments(Relation rela, Relation relb) {
		String type = rela.getType();
		if (!relationArguments.containsKey(type)) {
			return false;
		}
		String[] roles = relationArguments.get(type);
		String role1;
		String role2;
		if (roles.length == 3) {
			String alt = roles[0];
			if (rela.hasArgument(alt) && relb.hasArgument(alt)) {
				role1 = alt;
			}
			else {
				alt = roles[1];
				if (rela.hasArgument(alt) && relb.hasArgument(alt)) {
					role1 = alt;
				}
				else {
					return false;
				}
			}
			role2 = roles[2];
		}
		else {
			role1 = roles[0];
			if ((!rela.hasArgument(role1)) || (!relb.hasArgument(role1))) {
				return false;
			}
			role2 = roles[1];
		}
		if ((!rela.hasArgument(role2)) || (!relb.hasArgument(role2))) {
			return false;
		}
		Annotation arga1 = rela.getArgument(role1);
		Annotation argb1 = rela.getArgument(role1);
		Annotation arga2 = rela.getArgument(role2);
		Annotation argb2 = rela.getArgument(role2);
		if (commutativeTypes.contains(type)) {
			return (arga1.equals(argb1) && arga2.equals(argb2)) || (arga1.equals(argb2) && arga2.equals(argb1));
		}
		return arga1.equals(argb1) && arga2.equals(argb2);
	}
	
	private static boolean hasNegation(Relation rel) {
		for (Annotation bref : rel.getBackReferences()) {
			if (bref.getType().equals("Negation")) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean minimallyEquivalentRelations(Relation rela, Relation relb) {
		if (rela == null || relb == null) {
			return false;
		}
		return sameType(rela, relb) && sameTwoFirstArguments(rela, relb);
	}
	
	@Override
	public double compute(Annotation a, Annotation b) {
		Relation rela = a.asRelation();
		Relation relb = b.asRelation();
		if (!minimallyEquivalentRelations(rela, relb)) {
			return 0;
		}
		double negationPenalty = hasNegation(rela) == hasNegation(relb) ? 1 : 0.5;
		double optionalArgumentsSimilarity = computeOptionalArgumentsSimilarity(rela, relb);
		double result = negationPenalty * optionalArgumentsSimilarity;
		return result;
	}

	private static double computeOptionalArgumentsSimilarity(Relation rela, Relation relb) {
		double errors = 0;
		Collection<Annotation> all = new HashSet<Annotation>();
		for (String role : optionalArguments) {
			errors += optionalArgumentErrors(rela, relb, role);
			if (rela.hasArgument(role)) {
				all.add(rela.getArgument(role));
			}
			if (relb.hasArgument(role)) {
				all.add(relb.getArgument(role));
			}
		}
		if (all.isEmpty()) {
			return 1;
		}
		return 1 - (errors / all.size());
	}
	
	private static boolean isEquivalent(Annotation a, Annotation b) {
		if (a.equals(b)) {
			return true;
		}
		Relation rela = a.asRelation();
		Relation relb = b.asRelation();
		return minimallyEquivalentRelations(rela, relb);
	}

	private static int optionalArgumentErrors(Relation rela, Relation relb, String role) {
		if (rela.hasArgument(role)) {
			if (relb.hasArgument(role)) {
				Annotation arga = rela.getArgument(role);
				Annotation argb = relb.getArgument(role);
				if (isEquivalent(arga, argb)) {
					return 0;
				}
				return 2;
			}
			return 1;
		}
		if (relb.hasArgument(role)) {
			return 1;
		}
		return 0;
	}

	private static void explainOptionalArgumentErrors(StringBuilder sb, Relation rela, Relation relb, String role) {
		sb.append(role);
		sb.append(": ");
		if (rela.hasArgument(role)) {
			if (relb.hasArgument(role)) {
				Annotation arga = rela.getArgument(role);
				Annotation argb = relb.getArgument(role);
				if (isEquivalent(arga, argb)) {
					sb.append("match");
					return;
				}
				sb.append("wrong (2 errors)");
				return;
			}
			sb.append("missing (1 error)");
			return;
		}
		if (relb.hasArgument(role)) {
			sb.append("extra (1 error)");
			return;
		}
		sb.append("none");
	}
	
	private static void explainNegationPenalty(StringBuilder sb, Relation rela, Relation relb) {
		sb.append("negation: ");
		if (hasNegation(rela)) {
			if (hasNegation(relb)) {
				sb.append("ok (negated)");
				return;
			}
			sb.append("missed (half score)");
			return;
		}
		if (hasNegation(relb)) {
			sb.append("extra (half score)");
			return;
		}
		sb.append("ok (not negated)");
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		Relation rela = a.asRelation();
		Relation relb = b.asRelation();
		if (rela == null || relb == null) {
			sb.append("not an event");
			return;
		}
		if (!sameType(rela, relb)) {
			sb.append("type mismatch");
			return;
		}
		if (!sameTwoFirstArguments(rela, relb)) {
			sb.append("wrong mandatory arguments");
			return;
		}
		explainNegationPenalty(sb, rela, relb);
		sb.append(" and optional arguments: [");
		boolean notFirst = false;
		for (String role : optionalArguments) {
			if (notFirst) {
				sb.append(", ");
			}
			else {
				notFirst = true;
			}
			explainOptionalArgumentErrors(sb, rela, relb, role);
		}
		sb.append("] = ");
		sb.append(computeOptionalArgumentsSimilarity(rela, relb));
	}

}
