package org.bionlpst.evaluation.similarity;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Relation;
import org.bionlpst.util.Util;

public class SameTypeAndArgumentsSimilarity implements Similarity<Annotation> {
	private final Collection<String> commutativeTypes;
	private final boolean resolveEquivalences;
	private final Map<String,TypeConversion> typeConversion;

	public SameTypeAndArgumentsSimilarity(Collection<String> commutativeTypes, boolean resolveEquivalences) {
		this(commutativeTypes, resolveEquivalences, new HashMap<String,TypeConversion>());
	}

	public SameTypeAndArgumentsSimilarity(Collection<String> commutativeTypes, boolean resolveEquivalences, BufferedReader reader) throws IOException {
		this(commutativeTypes, resolveEquivalences, getTypeConversion(reader));
	}
	
	private static Map<String,TypeConversion> getTypeConversion(BufferedReader reader) throws IOException {
		Map<String,TypeConversion> result = new HashMap<String,TypeConversion>();
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			List<String> columns = Util.split(line, '\t');
			String type = columns.get(0);
			String newType = columns.get(1);
			Map<String,String> roleConversion = getRoleConversion(columns.subList(2, columns.size()));
			TypeConversion conversion = new TypeConversion(newType, roleConversion);
			result.put(type, conversion);
		}
		return result;
	}
	
	private static Map<String,String> getRoleConversion(List<String> columns) {
		Map<String,String> result = new HashMap<String,String>();
		for (int i = 0; i < columns.size(); i += 2) {
			result.put(columns.get(i), columns.get(i+1));
		}
		return result;
	}
	
	public SameTypeAndArgumentsSimilarity(Collection<String> commutativeTypes, boolean resolveEquivalences, Map<String,TypeConversion> typeConversion) {
		super();
		this.commutativeTypes = commutativeTypes;
		this.resolveEquivalences = resolveEquivalences;
		this.typeConversion = typeConversion;
	}

	public static class TypeConversion {
		private final String newType;
		private final Map<String,String> roleConversion;
		private final Map<String,String> roleDeconversion;

		public TypeConversion(String newType, Map<String,String> roleConversion) {
			super();
			this.newType = newType;
			this.roleConversion = roleConversion;
			this.roleDeconversion = new HashMap<String,String>();
			for (Map.Entry<String,String> e : roleConversion.entrySet()) {
				roleDeconversion.put(e.getValue(), e.getKey());
			}
		}
	}
	
	private static String convertType(TypeConversion conversion, String type) {
		if (conversion == null) {
			return type;
		}
		return conversion.newType;
	}
	
	private static String convertRole(TypeConversion conversion, String role) {
		if (conversion == null) {
			return role;
		}
		if (conversion.roleConversion.containsKey(role)) {
			return conversion.roleConversion.get(role);
		}
		return role;
	}

	@Override
	public double compute(Annotation a, Annotation b) {
		String aType = a.getType();
		String bType = b.getType();
		TypeConversion aConversion = typeConversion.get(aType);
		TypeConversion bConversion = typeConversion.get(bType);
		String aNewType = convertType(aConversion, aType);
		String bNewType = convertType(bConversion, bType);
		if (!aNewType.equals(bNewType)) {
			return 0;
		}
		Relation ar = a.asRelation();
		if (ar == null) {
			return 0;
		}
		Relation br = b.asRelation();
		if (br == null) {
			return 0;
		}
		if (sameArguments(ar, br, aConversion, bConversion)) {
			return 1;
		}
		return 0;
	}
	
	private boolean sameArguments(Relation ar, Relation br, TypeConversion aConversion, TypeConversion bConversion) {
		String type = convertType(aConversion, ar.getType());
		if (commutativeTypes.contains(type)) {
			return sameCommutativeArguments(ar, br);
		}
		return sameNonCommutativeArguments(ar, br, aConversion, bConversion);
	}
	
	private boolean sameCommutativeArguments(Relation ar, Relation br) {
		Collection<Annotation> argsa = new ArrayList<Annotation>(ar.getArguments());
		Collection<Annotation> argsb = new ArrayList<Annotation>(br.getArguments());
		for (Annotation arga : argsa) {
			Annotation argb = findEquivalent(arga, argsb);
			if (argb == null) {
				return false;
			}
			argsb.remove(argb);
		}
		return argsb.isEmpty();
	}
	
	private Annotation findEquivalent(Annotation arga, Collection<Annotation> argsb) {
		for (Annotation argb : argsb) {
			if (isEquivalent(arga, argb)) {
				return argb;
			}
		}
		return null;
	}
	
	private static boolean hasArgument(Relation rel, String role, TypeConversion conversion) {
		if (conversion == null) {
			return rel.hasArgument(role);
		}
		if (conversion.roleDeconversion.containsKey(role)) {
			String concreteRole = conversion.roleDeconversion.get(role);
			return rel.hasArgument(concreteRole);
		}
		return rel.hasArgument(role);
	}
	
	private static Annotation getArgument(Relation rel, String role, TypeConversion conversion) {
		if (conversion == null) {
			return rel.getArgument(role);
		}
		if (conversion.roleDeconversion.containsKey(role)) {
			String concreteRole = conversion.roleDeconversion.get(role);
			return rel.getArgument(concreteRole);
		}
		return rel.getArgument(role);
	}
	
	private static void collectRoles(Relation rel, TypeConversion conversion, Collection<String> roles) {
		for (String role : rel.getRoles()) {
			String newRole = convertRole(conversion, role);
			roles.add(newRole);
		}
	}

	private boolean sameNonCommutativeArguments(Relation ar, Relation br, TypeConversion aConversion, TypeConversion bConversion) {
		Collection<String> roles = new HashSet<String>();
		collectRoles(ar, aConversion, roles);
		collectRoles(br, bConversion, roles);
		for (String role : roles) {
			if (!hasArgument(ar, role, aConversion)) {
				return false;
			}
			if (!hasArgument(br, role, bConversion)) {
				return false;
			}
			Annotation arga = getArgument(ar, role, aConversion);
			Annotation argb = getArgument(br, role, bConversion);
			if (!isEquivalent(arga, argb)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isEquivalent(Annotation arga, Annotation argb) {
		if (resolveEquivalences) {
			return arga.getEquivalents().contains(argb);
		}
		return arga.equals(argb);
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		String aType = a.getType();
		String bType = b.getType();
		TypeConversion aConversion = typeConversion.get(aType);
		TypeConversion bConversion = typeConversion.get(bType);
		String aNewType = convertType(aConversion, aType);
		String bNewType = convertType(bConversion, bType);
		if (!aNewType.equals(bNewType)) {
			sb.append(a.getType());
			sb.append(" != ");
			sb.append(b.getType());
			sb.append(" -> ");
			return;
		}
		Relation ar = a.asRelation();
		if (ar == null) {
			sb.append(a.getId());
			sb.append(" not relation -> 0");
			return;
		}
		Relation br = b.asRelation();
		if (br == null) {
			sb.append(b.getId());
			sb.append(" not relation -> 0");
			return;
		}
		String comm = commutativeTypes.contains(aNewType) ? " (commutative)" : "";
		if (sameArguments(ar, br, aConversion, bConversion)) {
			sb.append("same args");
			sb.append(comm);
			sb.append(" -> 1");
			return;
		}
		sb.append("different args");
		sb.append(comm);
		sb.append(" -> 0");
	}
}
