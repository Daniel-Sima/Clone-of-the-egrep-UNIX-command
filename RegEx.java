import java.util.Scanner;

import javax.print.DocFlavor.STRING;
import javax.print.DocFlavor.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import java.io.*;

/***************************************************************************************/
/***************************************************************************************/
/***************************************************************************************/
/**
 * Classe gerant les RegEx.
 */
public class RegEx {
	// MACROS
	static final int CONCAT = 0xC04CA7;
	static final int ETOILE = 0xE7011E;
	static final int PLUS = 0xFFFFFF;
	static final int ALTERN = 0xA17E54;
	static final int PROTECTION = 0xBADDAD;

	static final int PARENTHESEOUVRANT = 0x16641664;
	static final int PARENTHESEFERMANT = 0x51515151;
	static final int DOT = 0xD07;

	// REGEX
	private static String regEx;

	// TEXT
	private static String text;

	// CONSTRUCTORS
	public RegEx() {
	}

	public RegEx(String regex) {
		this.regEx = regex;
	}

	// TIME
	private static long startDFA, startDOT, startMDFA, startNFDA, startWT, startSearch, endSearch;
	private static long endDFA, endDOT, endMDFA, endNFDA, endWT, endAll, totalDOT;

	// MAIN
	public static void main(String arg[]) throws IOException {
		FileWriter writer;
		System.out.println("Welcome to M2 STL.");

		long startAll = System.currentTimeMillis();

		if (arg.length == 2) {
			regEx = arg[0];
			text = arg[1];
		} else if (arg.length == 1) {
			regEx = arg[0];
		} else {
			Scanner scanner = new Scanner(System.in);
			System.out.print("  >> Please enter a regEx: ");
			regEx = scanner.next();
			scanner.close();
		}
		System.out.println("  >> Parsing regEx \"" + regEx + "\".");
		System.out.println("  >> ...");

		if (regEx.length() < 1) {
			System.err.println("  >> ERROR: empty regEx.");
		} else {
			System.out.print("  >> ASCII codes: [" + (int) regEx.charAt(0));
			for (int i = 1; i < regEx.length(); i++)
				System.out.print("," + (int) regEx.charAt(i));
			System.out.println("].");

			try {
				boolean hasit = false;
				for (int i = 0; i < regEx.length(); i++) { // 1 AR
					char c = regEx.charAt(i);
					if (c == '|' || c == '+' || c == '(' || c == ')' || c == '*' || c == '.') {
						hasit = true;
						break;
					}

					if (arg.length == 1) {
						hasit = true;
					}
				}

				if (hasit) {
					RegExTree ret = parse();
					System.out.println("  >> Tree result: " + ret.toString() + ".");

					/*
					 * Partie Arbre Syntaxique (en utilisant la structure de donnees Automate pour
					 * stocker les transitions)
					 */
					Automata resSyntaxTree = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
					startWT = System.currentTimeMillis();
					resSyntaxTree = resSyntaxTree.toSyntaxTree(ret);
					endWT = System.currentTimeMillis();

					startWT = System.currentTimeMillis();
					writer = new FileWriter("arbre_syntaxique.dot");
					writer.write("digraph {\n");
					for (Automata.Transition t : resSyntaxTree.transitions) {
						writer.write("\t" + t.getStartState() + "->" + t.getEndState() + "\n");
					}

					writer.write("}\n");
					writer.close();

					Process process;
					try {
						process = Runtime.getRuntime().exec("dot -Tpng arbre_syntaxique.dot -o arbre_syntaxique.png");
						process.waitFor();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					endDOT = System.currentTimeMillis();
					totalDOT = endDOT - startDOT;

					/* Partie NDFA */
					Automata res = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
					startNFDA = System.currentTimeMillis();
					res = res.toNDFA(ret);
					endNFDA = System.currentTimeMillis();

					// Affichage du NDFA dans un fichier '.dot'
					startDOT = System.currentTimeMillis();
					writer = new FileWriter("NDFA.dot");
					writer.write("digraph {\n\trankdir=LR;\n\n");
					for (String s : res.finalStates) {
						writer.write("\t" + s + " [shape=doublecircle]\n");
					}
					for (String s : res.initialStates) {
						writer.write("\t" + s + " [style=filled, fillcolor=\"lightblue\"]\n");
					}

					writer.write("\n");

					for (Automata.Transition t : res.transitions) {
						writer.write("\t" + t.getStartState() + "->" + t.getEndState() + " [label= \""
								+ t.getTransitionSymbol() + "\"];\n");
					}

					writer.write("}\n");
					writer.close();

					try {
						process = Runtime.getRuntime().exec("dot -Tpng NDFA.dot -o NDFA.png");
						process.waitFor();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					endDOT = System.currentTimeMillis();
					totalDOT = (endDOT - startDOT);

					/* Partie DFA */
					Automata resDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
					startDFA = System.currentTimeMillis();
					resDFA = resDFA.toDFA(res);
					endDFA = System.currentTimeMillis();

					// Affichage du DFA dans un fichier '.dot'
					startDOT = System.currentTimeMillis();
					writer = new FileWriter("DFA.dot");
					writer.write("digraph {\n\trankdir=LR;\n\n");
					for (String s : resDFA.getFinalStates()) {
						writer.write("\t" + s + " [shape=doublecircle]\n");
					}
					for (String s : resDFA.initialStates) {
						writer.write("\t" + s + " [style=filled, fillcolor=\"lightblue\"]\n");
					}

					writer.write("\n");

					for (Automata.Transition t : resDFA.transitions) {
						writer.write("\t" + t.getStartState() + "->" + t.getEndState() + " [label= \""
								+ t.getTransitionSymbol() + "\"];\n");
					}

					writer.write("}\n");
					writer.close();

					try {
						process = Runtime.getRuntime().exec("dot -Tpng DFA.dot -o DFA.png");
						process.waitFor();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					endDOT = System.currentTimeMillis();
					totalDOT += (endDOT - startDOT);

					/* Partie Min-DFA */
					Automata resMDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
					startMDFA = System.currentTimeMillis();
					resMDFA = resMDFA.toMinDFA(resDFA);
					endMDFA = System.currentTimeMillis();

					// Affichage du Min-DFA dans un fichier '.dot'
					startDOT = System.currentTimeMillis();
					writer = new FileWriter("Min-DFA.dot");
					writer.write("digraph {\n\trankdir=LR;\n\n");
					for (String s : resMDFA.getFinalStates()) {
						writer.write("\t\"" + s + "\" [shape=doublecircle]\n");
					}
					if (resMDFA.initialStates.size() > 1) {
						writer.write("\t\"" + resMDFA.initialStates + "\" [style=filled, fillcolor=\"lightblue\"]\n");
					} else {
						for (String s : resMDFA.initialStates) {
							writer.write("\t" + s + " [style=filled, fillcolor=\"lightblue\"]\n");
						}
					}

					writer.write("\n");

					for (Automata.Transition t : resMDFA.transitions) {
						writer.write("\t" + t.getStartState() + "->" + t.getEndState() + " [label= \""
								+ t.getTransitionSymbol() + "\"];\n");
					}

					writer.write("}\n");
					writer.close();

					try {
						process = Runtime.getRuntime().exec("dot -Tpng Min-DFA.dot -o Min-DFA.png");
						process.waitFor();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					endDOT = System.currentTimeMillis();
					totalDOT += (endDOT - startDOT);

					/* Partie de recherche dans le texte */
					if (arg.length == 2) {
						startSearch = System.currentTimeMillis();
						int cptOcc = resMDFA.search(text, true);
						endSearch = System.currentTimeMillis();
						System.out
								.println("\n  >> We found " + cptOcc + " occurences of pattern with Automates method.");
					}

				} else {
					/* Partie KMP */
					int cpt = processKMP(text, regEx, true);
					System.out.println("\n  >> We found " + cpt + " occurences of pattern with KMP method.");
				}
			} catch (Exception e) {
				System.err.println("  >> ERROR: syntax error for regEx \"" + regEx + "\". | Exception: " + e);
			}
		}

		System.out.println("  >> ...");
		System.out.println("  >> Parsing completed.");
		System.out.println("Goodbye M2 STL. \n\n\n");

		endAll = System.currentTimeMillis();

		/* Affichages temps d'execution */
		System.out.println("Execution times for RegEx '" + regEx + "': ");
		System.out.println("--> Total time Arbre syntaxique = " + (endWT - startWT) + "ms");
		System.out.println("--> Total time NDFA = " + (endNFDA - startNFDA) + "ms");
		System.out.println("--> Total time DFA = " + (endDFA - startDFA) + "ms");
		System.out.println("--> Total time Min-DFA = " + (endMDFA - startMDFA) + "ms");
		System.out.println("--> Total time DOT prints = " + totalDOT + "ms");
		System.out.println("--> Total time text search = " + (endSearch-startSearch) + "ms");
		System.out.println("--> All total times = " + (endAll - startAll) + "ms");
	}

	/***************************************************************************************/
	/**
	 * FROM REGEX TO SYNTAX TREE
	 * 
	 * @return RegExTree parse
	 * @throws Exception
	 */
	static RegExTree parse() throws Exception {
		// BEGIN DEBUG: set conditionnal to true for debug example
		if (false)
			throw new Exception();
		RegExTree example = exampleAhoUllman();
		if (false)
			return example;
		// END DEBUG

		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
		for (int i = 0; i < regEx.length(); i++)
			result.add(new RegExTree(charToRoot(regEx.charAt(i)), new ArrayList<RegExTree>()));

		return parse(result);
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param
	 * @return
	 */
	private static int charToRoot(char c) {
		if (c == '.')
			return DOT;
		if (c == '*')
			return ETOILE;
		if (c == '+')
			return PLUS;
		if (c == '|')
			return ALTERN;
		if (c == '(')
			return PARENTHESEOUVRANT;
		if (c == ')')
			return PARENTHESEFERMANT;
		return (int) c;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private static RegExTree parse(ArrayList<RegExTree> result) throws Exception {
		while (containParenthese(result))
			result = processParenthese(result);
		while (containEtoile(result))
			result = processEtoile(result);
		while (containPlus(result))
			result = processPlus(result);
		while (containConcat(result))
			result = processConcat(result);
		while (containAltern(result))
			result = processAltern(result);

		if (result.size() > 1)
			throw new Exception();

		return removeProtection(result.get(0));
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 */
	private static boolean containParenthese(ArrayList<RegExTree> trees) {
		for (RegExTree t : trees)
			if (t.root == PARENTHESEFERMANT || t.root == PARENTHESEOUVRANT)
				return true;
		return false;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<RegExTree> processParenthese(ArrayList<RegExTree> trees) throws Exception {
		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
		boolean found = false;
		for (RegExTree t : trees) {
			if (!found && t.root == PARENTHESEFERMANT) {
				boolean done = false;
				ArrayList<RegExTree> content = new ArrayList<RegExTree>();
				while (!done && !result.isEmpty())
					if (result.get(result.size() - 1).root == PARENTHESEOUVRANT) {
						done = true;
						result.remove(result.size() - 1);
					} else
						content.add(0, result.remove(result.size() - 1));
				if (!done)
					throw new Exception();
				found = true;
				ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
				subTrees.add(parse(content));
				result.add(new RegExTree(PROTECTION, subTrees));
			} else {
				result.add(t);
			}
		}
		if (!found)
			throw new Exception();
		return result;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 */
	private static boolean containEtoile(ArrayList<RegExTree> trees) {
		for (RegExTree t : trees)
			if (t.root == ETOILE && t.subTrees.isEmpty())
				return true;
		return false;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 */
	private static boolean containPlus(ArrayList<RegExTree> trees) {
		for (RegExTree t : trees)
			if (t.root == PLUS && t.subTrees.isEmpty())
				return true;
		return false;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<RegExTree> processEtoile(ArrayList<RegExTree> trees) throws Exception {
		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
		boolean found = false;
		for (RegExTree t : trees) {
			if (!found && t.root == ETOILE && t.subTrees.isEmpty()) {
				if (result.isEmpty())
					throw new Exception();
				found = true;
				RegExTree last = result.remove(result.size() - 1);
				ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
				subTrees.add(last);
				result.add(new RegExTree(ETOILE, subTrees));
			} else {
				result.add(t);
			}
		}
		return result;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<RegExTree> processPlus(ArrayList<RegExTree> trees) throws Exception {
		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
		boolean found = false;
		for (RegExTree t : trees) {
			if (!found && t.root == PLUS && t.subTrees.isEmpty()) {
				if (result.isEmpty())
					throw new Exception();
				found = true;
				RegExTree last = result.remove(result.size() - 1);
				ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
				subTrees.add(last);
				result.add(new RegExTree(PLUS, subTrees));
			} else {
				result.add(t);
			}
		}
		return result;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 */
	private static boolean containConcat(ArrayList<RegExTree> trees) {
		boolean firstFound = false;
		for (RegExTree t : trees) {
			if (!firstFound && t.root != ALTERN) {
				firstFound = true;
				continue;
			}
			if (firstFound)
				if (t.root != ALTERN)
					return true;
				else
					firstFound = false;
		}
		return false;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<RegExTree> processConcat(ArrayList<RegExTree> trees) throws Exception {
		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
		boolean found = false;
		boolean firstFound = false;
		for (RegExTree t : trees) {
			if (!found && !firstFound && t.root != ALTERN) {
				firstFound = true;
				result.add(t);
				continue;
			}
			if (!found && firstFound && t.root == ALTERN) {
				firstFound = false;
				result.add(t);
				continue;
			}
			if (!found && firstFound && t.root != ALTERN) {
				found = true;
				RegExTree last = result.remove(result.size() - 1);
				ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
				subTrees.add(last);
				subTrees.add(t);
				result.add(new RegExTree(CONCAT, subTrees));
			} else {
				result.add(t);
			}
		}
		return result;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 */
	private static boolean containAltern(ArrayList<RegExTree> trees) {
		for (RegExTree t : trees)
			if (t.root == ALTERN && t.subTrees.isEmpty())
				return true;
		return false;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param trees
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<RegExTree> processAltern(ArrayList<RegExTree> trees) throws Exception {
		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
		boolean found = false;
		RegExTree gauche = null;
		boolean done = false;
		for (RegExTree t : trees) {
			if (!found && t.root == ALTERN && t.subTrees.isEmpty()) {
				if (result.isEmpty())
					throw new Exception();
				found = true;
				gauche = result.remove(result.size() - 1);
				continue;
			}
			if (found && !done) {
				if (gauche == null)
					throw new Exception();
				done = true;
				ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
				subTrees.add(gauche);
				subTrees.add(t);
				result.add(new RegExTree(ALTERN, subTrees));
			} else {
				result.add(t);
			}
		}
		return result;
	}

	/***************************************************************************************/
	/**
	 * 
	 * @param tree
	 * @return
	 * @throws Exception
	 */
	private static RegExTree removeProtection(RegExTree tree) throws Exception {
		if (tree.root == PROTECTION && tree.subTrees.size() != 1)
			throw new Exception();
		if (tree.subTrees.isEmpty())
			return tree;
		if (tree.root == PROTECTION)
			return removeProtection(tree.subTrees.get(0));

		ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
		for (RegExTree t : tree.subTrees)
			subTrees.add(removeProtection(t));
		return new RegExTree(tree.root, subTrees);
	}

	/***************************************************************************************/
	/**
	 * Read file
	 * 
	 * @param path    of the file
	 * @param pattern RegEx
	 * @return TODO
	 */
	public static int processKMP(String path, String pattern, boolean print) {
		File directory = new File(path);
		String strLine = "";
		int cpt = 0;
		if (print) {
			System.out.println("  >> Reading: " + directory.getAbsolutePath() + "\n");
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((strLine = br.readLine()) != null) {
				int[] lps = computeLPSArray(regEx);
				int index = search(strLine, pattern, lps);
				if (index != -1) {
					cpt++;
					if (print) {
						System.out.println("  >> Found: " + strLine);
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
		} catch (IOException e) {
			System.err.println("Unable to read the file.");
		}
		return cpt;
	}

	/***************************************************************************************/
	/**
	 * TODO Compute the LPS (Longest Proper Prefix which is also Suffix) array
	 * 
	 * @param pattern
	 * @return
	 */
	private static int[] computeLPSArray(String pattern) {
		int length = pattern.length();
		int[] lps = new int[length];
		int j = 0;
		for (int i = 1; i < length;) {
			if (pattern.charAt(i) == pattern.charAt(j)) {
				lps[i] = j + 1;
				i++;
				j++;
			} else {
				if (j != 0) {
					j = lps[j - 1];
				} else {
					lps[i] = 0;
					i++;
				}
			}
		}
		return lps;
	}

	/***************************************************************************************/
	/**
	 * TODO Search for the pattern in the given text using KMP algorithm
	 * 
	 * @param text
	 * @param pattern
	 * @param lps
	 * @return
	 */
	private static int search(String text, String pattern, int[] lps) {
		int i = 0;
		int j = 0;
		while (i < text.length()) {
			if (text.charAt(i) == pattern.charAt(j)) {
				i++;
				j++;
				if (j == pattern.length()) {
					// Pattern found, return the starting index of the occurrence
					return i - j;
				}
			} else {
				if (j != 0) {
					j = lps[j - 1];
				} else {
					i++;
				}
			}
		}
		// Pattern not found in the text
		return -1;
	}

	/***************************************************************************************/
	/**
	 * EXAMPLE --> RegEx from Aho-Ullman book Chap.10 Example 10.25
	 * 
	 * @return
	 */
	private static RegExTree exampleAhoUllman() {
		RegExTree a = new RegExTree((int) 'a', new ArrayList<RegExTree>());
		RegExTree b = new RegExTree((int) 'b', new ArrayList<RegExTree>());
		RegExTree c = new RegExTree((int) 'c', new ArrayList<RegExTree>());
		ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
		subTrees.add(c);
		RegExTree cEtoile = new RegExTree(ETOILE, subTrees);
		subTrees = new ArrayList<RegExTree>();
		subTrees.add(b);
		subTrees.add(cEtoile);
		RegExTree dotBCEtoile = new RegExTree(CONCAT, subTrees);
		subTrees = new ArrayList<RegExTree>();
		subTrees.add(a);
		subTrees.add(dotBCEtoile);
		return new RegExTree(ALTERN, subTrees);
	}
}

/***************************************************************************************/
/***************************************************************************************/
/***************************************************************************************/
/**
 * UTILITARY CLASS
 */
class RegExTree {
	protected int root;
	protected ArrayList<RegExTree> subTrees;

	/***************************************************************************************/
	/**
	 * Constructor
	 * 
	 * @param root
	 * @param subTrees
	 */
	public RegExTree(int root, ArrayList<RegExTree> subTrees) {
		this.root = root;
		this.subTrees = subTrees;
	}

	/***************************************************************************************/
	/**
	 * FROM TREE TO PARENTHESIS
	 */
	public String toString() {
		if (subTrees.isEmpty())
			return rootToString();
		String result = rootToString() + "(" + subTrees.get(0).toString();
		for (int i = 1; i < subTrees.size(); i++)
			result += "," + subTrees.get(i).toString();
		return result + ")";
	}

	/***************************************************************************************/
	/**
	 * 
	 * @return
	 */
	public String rootToString() {
		if (root == RegEx.CONCAT)
			return ".";
		if (root == RegEx.ETOILE)
			return "*";
		if (root == RegEx.PLUS)
			return "+";
		if (root == RegEx.ALTERN)
			return "|";
		if (root == RegEx.DOT)
			return ".";
		return Character.toString((char) root);
	}
}

/***************************************************************************************/
/***************************************************************************************/
/***************************************************************************************/
/**
 * Classe manipulant les automates avec ses etats et transitions
 */
class Automata {
	/***************************************************************************************/
	/**
	 * Classe gerant les etats de debut/fin et toutes les transitions sous forme
	 * ['startState' -- 'transitionSymbol' --> 'endState']
	 */
	class Transition {
		private String startState;
		private String transitionSymbol;
		private String endState;

		/***************************************************************************************/
		/**
		 * Constructor
		 * 
		 * @param startState       Etat de debut de la transition
		 * @param transitionSymbol Symbole de la transition
		 * @param endState         Etat de fin de la transition
		 */
		public Transition(String startState, String transitionSymbol, String endState) {
			this.startState = startState;
			this.transitionSymbol = transitionSymbol;
			this.endState = endState;
		}

		/***************************************************************************************/
		/**
		 * Retourne l'etat de debut de la transition
		 * 
		 * @return Chaine de caracteres de l'etat de debut
		 */
		public String getStartState() {
			return this.startState;
		}

		/***************************************************************************************/
		/**
		 * Retourne le symbole de la transition
		 * 
		 * @return Chaine de caracteres du symbole
		 */
		public String getTransitionSymbol() {
			return this.transitionSymbol;
		}

		/***************************************************************************************/
		/**
		 * Retourne l'etat de fin de la transition
		 * 
		 * @return Chaine de caracteres de l'etat de fin
		 */
		public String getEndState() {
			return this.endState;
		}

		/***************************************************************************************/
		/**
		 * Changement du toString() Objet pour un meilleur affichage
		 */
		@Override
		public String toString() {
			return "[" + getStartState() + " -- " + getTransitionSymbol() + " -- >" + getEndState() + "]";
		}

		/***************************************************************************************/
		/**
		 * Changement du equals() Objet pour verifier les doublons selon notre
		 * implementation
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Transition nouvelle = (Transition) o;
			return Objects.equals(startState, nouvelle.getStartState())
					&& Objects.equals(endState, nouvelle.getEndState())
					&& Objects.equals(transitionSymbol, nouvelle.getTransitionSymbol());
		}
	}

	/***************************************************************************************/
	/**
	 * Classe permettant de retourner deux valeurs de type different (K, V)
	 */
	class Pair<K, V> {
		private K key;
		private V value;

		/***************************************************************************************/
		/**
		 * Constructor
		 * 
		 * @param key
		 * @param value
		 */
		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		/***************************************************************************************/
		/**
		 * Retourne la cle
		 * 
		 * @return cle du Pair
		 */
		public K getKey() {
			return key;
		}

		/***************************************************************************************/
		/**
		 * Retourne valeur
		 * 
		 * @return valeur du Pair
		 */
		public V getValue() {
			return value;
		}

		/***************************************************************************************/
		/**
		 * Modifier la cle
		 * 
		 * @param key
		 */
		public void setKey(K key) {
			this.key = key;
		}

		/***************************************************************************************/
		/**
		 * Modifier la valeur
		 * 
		 * @param value
		 */
		public void setValue(V value) {
			this.value = value;
		}
	}

	// ArrayList pusique necessaire pour stocker plusieurs lors du parcours et pour
	// la Min-DFA
	protected ArrayList<String> initialStates;
	protected ArrayList<String> finalStates;
	protected ArrayList<Transition> transitions;
	// Poour garanir que tous les etats ont un numero different (necessaire pour
	// generer le '.dot')
	protected Integer numberStates = 0;

	// DEBUG
	private static boolean DEBUG = false;

	/**
	 * Constructor
	 * 
	 * @param initialState
	 * @param finalStates
	 * @param transitions
	 */
	public Automata(ArrayList<String> initialState, ArrayList<String> finalStates, ArrayList<Transition> transitions) {
		this.initialStates = initialState;
		this.finalStates = finalStates;
		this.transitions = transitions;
	}

	/***************************************************************************************/
	/**
	 * Charge un RegExTree dans la classe 'Automata' pour permettre l'affichage en
	 * fichier '.dot' en utilisant les 'transitions'. Les listes 'initialState' et
	 * 'finalStates' servent a sotcker le premier et dernier etat visite.
	 * 
	 * @param tree RegExTree
	 * @return
	 */
	public Automata toSyntaxTree(RegExTree tree) {
		// Si plus de fils on fait la 'transition' avec son pere
		if (tree.subTrees.isEmpty()) {
			if (!initialStates.isEmpty()) {
				transitions.add(new Transition(initialStates.get(initialStates.size() - 1), null,
						("\"" + tree.rootToString() + "__" + (numberStates++)) + "\""));
			}
			return new Automata(initialStates, finalStates, transitions);
		}

		Automata resAutomata = null;
		for (int i = 0; i < tree.subTrees.size(); i++) {
			// Si on recommence un nouveau parcours de fils, alors on se connecte a son pere
			// s'il y en a un
			if (!(initialStates.isEmpty()) && i == 0) {
				transitions.add(new Transition(initialStates.get(initialStates.size() - 1), null,
						"\"" + tree.rootToString() + "__" + (numberStates) + "\""));
				initialStates.add("\"" + tree.rootToString() + "__" + (numberStates++) + "\"");
			}
			// Cas premier pere
			if (initialStates.isEmpty()) {
				initialStates.add("\"" + tree.rootToString() + "__" + (numberStates++) + "\"");
			}

			// Appel recursif sur le second fils s'il existe
			resAutomata = toSyntaxTree(tree.subTrees.get(i));

			// Si on est a parcourir le second fils (i=1) et que la root est ETOIL ou PLUS
			// alors plus de fils a parcourir car operateurs unaires et on s'efface de la
			// liste
			// de stockage
			if ((!(initialStates.isEmpty()) && i == 1) || (tree.rootToString() == "*")
					|| (tree.rootToString() == "+")) {
				initialStates.remove(initialStates.size() - 1);
			}
		}

		return resAutomata;
	}

	/***************************************************************************************/
	/**
	 * Trouve toutes les transitions entre un etat de depart (present dans
	 * 'statesToSearch') et jusqu'a plus avoir d'etat. Change leur numeros pour le
	 * fichier '.dot'.
	 *
	 * @param accTransitions Accumulateur de transitions
	 * @param statesToSearch Etats qui restent à pacourir
	 * @return Liste avec toutes les transitions depuis le premier etat de
	 *         'statesToSearch'
	 */
	public ArrayList<Transition> findTransitionsBetweenStates(ArrayList<Transition> accTransitions,
			ArrayList<String> statesToSearch) {
		ArrayList<String> newStatesToSearch = new ArrayList<>();
		for (String etat : statesToSearch) {
			for (Transition e : transitions) {
				if (e.getStartState().equals(etat)) {
					// Duplication du numero pour etre sur de ne pas avoir deux fois le meme
					accTransitions.add(new Transition(e.getStartState() + e.getStartState(), e.getTransitionSymbol(),
							e.getEndState() + e.getEndState()));
					if (!newStatesToSearch.contains(e.getEndState())) {
						newStatesToSearch.add(e.getEndState());
					}
				}
			}
		}

		if (!newStatesToSearch.isEmpty()) {
			return findTransitionsBetweenStates(accTransitions, newStatesToSearch);
		}

		return accTransitions;
	}

	/***************************************************************************************/
	/**
	 * Converit un 'RegExTree' en 'Automata' NFDA
	 * 
	 * @param tree RegExTree a convertir
	 * @return Automata convertit depuis 'tree'
	 */
	public Automata toNDFA(RegExTree tree) {
		// Si plus de fils, transition entre le noeud courant et son pere
		if (tree.subTrees.isEmpty()) {
			initialStates.add("" + numberStates);
			finalStates.add("" + (numberStates + 1));
			transitions.add(new Transition("" + numberStates, tree.rootToString(), "" + (numberStates + 1)));
			numberStates += 2;

			return new Automata(initialStates, finalStates, transitions);
		}

		Automata resAutomata = null;
		for (int i = 0; i < tree.subTrees.size(); i++) {
			resAutomata = toNDFA(tree.subTrees.get(i));

			// Application de la regle de "closure"
			if (tree.rootToString() == "*") {
				// R1
				String lastInitialEtat = initialStates.remove(initialStates.size() - 1);
				String lastFinalEtat = finalStates.remove(finalStates.size() - 1);
				transitions.add(new Transition(lastFinalEtat, "ε", lastInitialEtat));
				initialStates.add("" + numberStates);
				transitions.add(new Transition(initialStates.get(initialStates.size() - 1), "ε", lastInitialEtat));
				finalStates.add("" + (numberStates + 1));
				transitions.add(new Transition(lastFinalEtat, "ε", finalStates.get(finalStates.size() - 1)));
				transitions.add(new Transition(initialStates.get(initialStates.size() - 1), "ε",
						finalStates.get(finalStates.size() - 1)));
				numberStates += 2;
			}
			// Application de la regle de "concatenation", applicable que si on est le
			// second fils
			else if ((tree.rootToString() == ".") && (i != 0)) {
				// R2
				String lastInitialEtat = initialStates.remove(initialStates.size() - 1);
				String lastFinalEtat = finalStates.remove(finalStates.size() - 2);
				transitions.add(new Transition(lastFinalEtat, "ε", lastInitialEtat));
			}
			// Application de la regle de "union", applicable que si on est le second fils
			else if ((tree.rootToString() == "|") && (i != 0)) {
				// R2
				String lastInitialEtatR2 = initialStates.remove(initialStates.size() - 1);
				String lastFinalEtatR2 = finalStates.remove(finalStates.size() - 1);
				// R1
				String lastInitialEtatR1 = initialStates.remove(initialStates.size() - 1);
				String lastFinalEtatR1 = finalStates.remove(finalStates.size() - 1);

				initialStates.add("" + numberStates);
				transitions.add(new Transition(initialStates.get(initialStates.size() - 1), "ε", lastInitialEtatR1));
				transitions.add(new Transition(initialStates.get(initialStates.size() - 1), "ε", lastInitialEtatR2));
				finalStates.add("" + (numberStates + 1));
				transitions.add(new Transition(lastFinalEtatR1, "ε", finalStates.get(initialStates.size() - 1)));
				transitions.add(new Transition(lastFinalEtatR2, "ε", finalStates.get(initialStates.size() - 1)));
				numberStates += 2;
			}
			// Application de la regle de "PLUS"
			else if ((tree.rootToString() == "+")) {
				if (DEBUG) {
					System.out.println();
					printTransitions();
				}
				String lastInitialEtat = initialStates.get(initialStates.size() - 1);
				String lastFinalEtat = finalStates.get(finalStates.size() - 1);
				if (DEBUG) {
					System.out.println("lastInitialEtat: " + lastInitialEtat);
					System.out.println("lastFinalEtat: " + lastFinalEtat);
				}

				ArrayList<Transition> plusTransitons = new ArrayList<>();
				ArrayList<String> statesToSearch = new ArrayList<>();
				statesToSearch.add(lastInitialEtat);
				// Recopie de l'automate de l'argument du PLUS ([argument]+)
				plusTransitons = findTransitionsBetweenStates(plusTransitons, statesToSearch);

				transitions.addAll(plusTransitons);
				// Changement des etats finaux et inital
				String newInitialEtat = plusTransitons.get(0).getStartState();
				String newFinalEtat = plusTransitons.get(plusTransitons.size() - 1).getEndState();

				transitions.add(new Transition(lastFinalEtat, "ε", newInitialEtat));
				transitions.add(new Transition(newFinalEtat, "ε", newInitialEtat));

				// Etat ultime de l'automate avec PLUS
				String newFinalFinalEtat = "" + numberStates;
				numberStates++;

				finalStates.remove(finalStates.size() - 1);
				finalStates.add(newFinalFinalEtat);
				transitions.add(new Transition(lastFinalEtat, "ε", newFinalFinalEtat));
				transitions.add(new Transition(newFinalEtat, "ε", newFinalFinalEtat));

				if (DEBUG) {
					System.out.println();
					System.out.println("--------> finalStates: " + finalStates);
					System.out.println("--------> initialStates: " + initialStates);
				}
			}
			// else {
			// }
		}
		return resAutomata;
	}

	/**
	 * Getting epsilon close inital states
	 * 
	 * @return ArrayList with initals states
	 */
	/***************************************************************************************/
	/**
	 * Obtenir les etats epsilon pres.
	 * 
	 * @param startStates Etats a partir des quels on cherche
	 * @return Liste des etats proches a epsilon pres
	 */
	private ArrayList<String> getInitalEpsilonStates(ArrayList<String> startStates) {
		ArrayList<String> res = new ArrayList<>();
		res.addAll(startStates);

		for (String s : startStates) {
			for (Transition e : this.getTransitions()) {
				if ((e.getStartState().equals(s)) && (e.getTransitionSymbol().equals("ε"))
						&& !(res.contains(e.getEndState()))) {
					res.add(e.getEndState());
				}
			}
		}

		if (res.size() != startStates.size()) {
			res = this.getInitalEpsilonStates(res);
		}

		return res;
	}

	/***************************************************************************************/
	/**
	 * Testing all 256 ASCII carcacters to find stats that are acceptable
	 * 
	 * @param states Etats de l'automate
	 * @return Map with ASCI caracter and state number
	 */
	private HashMap<String, ArrayList<String>> getASCII_transitions(ArrayList<String> states) {
		HashMap<String, ArrayList<String>> res = new HashMap<>();

		for (int i = 0; i <= 255; i++) {
			char caractere = (char) i;
			for (Transition e : this.transitions) {
				for (String s : states) {
					if (e.getStartState().equals(s)) {
						if (("" + caractere).equals(e.getTransitionSymbol())) {
							ArrayList<String> etatsASCII = new ArrayList<>();
							etatsASCII.add(e.getEndState());
							etatsASCII = this.getInitalEpsilonStates(etatsASCII);
							res.put("" + caractere, etatsASCII);
						}
					}
				}
			}
		}

		return res;
	}

	/***************************************************************************************/
	/**
	 * Trouver pour chaque ensemble d'etats les transitions et les etats d'arrivee.
	 * Construire une nouvelle liste d'etats avec comme numero les indices du
	 * tableau contenant les listes de hachage avec les transitions.
	 * 
	 * @param states Etats a visiter (etats initiaux au debut)
	 * @param tab    Liste avec toutes les transitions du nouveau etat de depart
	 *               represente par le numero de l'indice de ce tableau
	 * @param cpt    compteur permettant de ne plus realculer les etats deja trouves
	 * @return Liste de tables de hachages avec les tranistion depuis l'indice des
	 *         etats et une liste de ces nouveaux etats
	 */
	private Pair<ArrayList<HashMap<String, ArrayList<String>>>, ArrayList<ArrayList<String>>> findStates_DFA(
			ArrayList<ArrayList<String>> states, ArrayList<HashMap<String, ArrayList<String>>> tab, int cpt) {
		ArrayList<HashMap<String, ArrayList<String>>> tableau = new ArrayList<>(tab);
		ArrayList<ArrayList<String>> newStates = new ArrayList<>(states);
		HashMap<String, ArrayList<String>> res = new HashMap<>();

		for (int i = 0; i < states.size(); i++) {
			if (i >= cpt) { // pour ne pas recalculer
				res = this.getASCII_transitions(states.get(i));
				if (DEBUG) {
					System.out.println("--> Pour: " + states.get(i) + " res: " + res);
				}
				if ((!tableau.contains(res)) && (!newStates.containsAll(res.values()))) {
					newStates.addAll(res.values());
				}
				tableau.add(res);
			}
		}

		if (DEBUG) {
			System.out.println("--> New states: " + newStates);
			System.out.println("--> Tableau: " + tableau);
		}

		if (states.size() != newStates.size()) {
			if (DEBUG) {
				System.err.println("\n");
			}
			return this.findStates_DFA(newStates, tableau, states.size());
		}

		Pair<ArrayList<HashMap<String, ArrayList<String>>>, ArrayList<ArrayList<String>>> resultat = new Pair<>(tableau,
				newStates);
		return resultat;
	}

	/***************************************************************************************/
	/**
	 * Conversion du NDFA en DFA
	 * 
	 * @param NDFA Automate non deterministe
	 * @return Automate deterministe
	 */
	public Automata toDFA(Automata NDFA) {
		if (DEBUG) {
			System.out.println("\n\n----------- DFA ----------- ");
			System.out.println("Inital state: " + NDFA.getInitialStates());
			System.out.println("Final states: " + NDFA.getFinalStates());
			NDFA.printTransitions();
			System.out.println();
		}

		ArrayList<String> startState = new ArrayList<>(NDFA.getInitialStates());
		startState = NDFA.getInitalEpsilonStates(startState);
		ArrayList<ArrayList<String>> states = new ArrayList<>();
		states.add(startState);

		if (DEBUG) {
			System.out.println("--> states: " + states);
			System.out.println("--> startState: " + startState);
		}

		// Tableau final de l'automate
		ArrayList<HashMap<String, ArrayList<String>>> tableau = new ArrayList<>();
		Pair<ArrayList<HashMap<String, ArrayList<String>>>, ArrayList<ArrayList<String>>> pairTableauStates = NDFA
				.findStates_DFA(states, tableau, 0);

		if (DEBUG) {
			System.out.println("\nFinal tableau: " + pairTableauStates.getKey());
			System.out.println("Final states: " + pairTableauStates.getValue());
			System.out.println();
		}

		Automata automataDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		automataDFA.addInitialState("0");
		for (int i = 0; i < pairTableauStates.getValue().size(); i++) {
			HashMap<String, ArrayList<String>> tabTransitions = pairTableauStates.getKey().get(i);

			// Parcours simultane de cle valeur par ensemble
			for (Map.Entry<String, ArrayList<String>> entry : tabTransitions.entrySet()) {
				automataDFA.addTransition(new Transition("" + i, entry.getKey(),
						"" + pairTableauStates.getValue().indexOf(entry.getValue())));

				// Ajout des etats finaux si on en trouve dans le 'tableau'
				if (entry.getValue().contains(NDFA.getFinalStates().get(0))) {
					if (!automataDFA.getFinalStates()
							.contains("" + pairTableauStates.getValue().indexOf(entry.getValue()))) {
						automataDFA.addFinalState("" + pairTableauStates.getValue().indexOf(entry.getValue()));
					}
				}
			}
		}

		if (DEBUG) {
			System.out.print("Final DFA: ");
			automataDFA.printTransitions();
		}

		return automataDFA;
	}

	/***************************************************************************************/
	/**
	 * Decoupe l'ensemble des etats jusqu'a ce que on ne peut plus decouper
	 * parcequ'il y en a qu'un seul ou parceque les deux sont dans le meme ensemble.
	 * 
	 * @param ensembleTotal  Ensembles a analyser (finaux et non finaux au debut)
	 * @param tabTransitions Liste des tables de hachages des transitions par indice
	 *                       de l'etat
	 * @param symboles       Lettres de la RegEx
	 * @param i              Indice de la la chaine 'symboles' (0 au debut)
	 * @return Ensemble des ensembles differents
	 */
	public Set<Set<String>> decoupage(Set<Set<String>> ensembleTotal, ArrayList<HashMap<String, String>> tabTransitions,
			String symboles, int i) {
		if (i == symboles.length()) {
			return ensembleTotal;
		}

		String symbole = "" + symboles.charAt(i);
		Set<Set<String>> ensembleTotalBis = new LinkedHashSet<>(ensembleTotal);

		for (Set<String> ens : ensembleTotal) {
			if (DEBUG)
				System.out.println("==> ens: " + ens + " i: " + i);
			// Si un ensemble est plus grand que 1 on essaye de voir si on peut
			// le separer en regardant leur transitions
			if (!(ens.size() == 1)) {
				int cpt = 0;
				Set<String> dedans = new LinkedHashSet<>();
				Set<String> pasDedans = new LinkedHashSet<>();
				for (HashMap<String, String> table : tabTransitions) {
					if (!table.isEmpty() && ens.contains("" + cpt)) {
						if (table.containsKey(symbole)) {
							dedans.add("" + cpt);
						} else {
							pasDedans.add("" + cpt);
						}
					}
					cpt++;
				}

				// Pour separer les etats qui n'ont pas de transitions
				// sortantes de ceux qui ont
				if (dedans.isEmpty()) {
					ens.removeAll(pasDedans);
					dedans = ens;
				}
				ensembleTotalBis.remove(ens);

				if (!dedans.isEmpty()) {
					ensembleTotalBis.add(dedans);
				}
				if (!pasDedans.isEmpty()) {
					ensembleTotalBis.add(pasDedans);
				}

				if (DEBUG) {
					System.out.println("==> dedans: " + dedans);
					System.out.println("==> pasDedans: " + pasDedans);
				}
			}
		}

		if (DEBUG) {
			System.out.println("==> ensembleTotalBis: " + ensembleTotalBis + " i: " + i + "\n");
		}

		return decoupage(ensembleTotalBis, tabTransitions, symboles, (i + 1));
	}

	/***************************************************************************************/
	/**
	 * Minimisation de l'automate deterministe DFA.
	 * 
	 * @param DFA Automate deterministe non minimise
	 * @return Automate deterministe minimise
	 */
	public Automata toMinDFA(Automata DFA) {
		ArrayList<HashMap<String, String>> tabTransitions = new ArrayList<>();

		if (DEBUG) {
			System.out.println("\n\n----------- Min-DFA -----------");
			DFA.printTransitions();
		}

		// Recuperation du tableau des transitions
		for (Transition e : DFA.getTransitions()) {
			// Si rien n'a ete ajoute encore
			if (tabTransitions.size() == Integer.parseInt(e.getStartState())) {
				HashMap<String, String> val = new HashMap<>();
				val.put(e.getTransitionSymbol(), e.getEndState());
				tabTransitions.add(Integer.parseInt(e.getStartState()), val);
			} else if (tabTransitions.size() < Integer.parseInt(e.getStartState())) {
				// On remplit les cases du tableau jusqu'a la case interesse par des elements
				// nuls
				for (int i = tabTransitions.size(); i <= Integer.parseInt(e.getStartState()); i++) {
					HashMap<String, String> val = new HashMap<>();
					tabTransitions.add(i, val);
					if (i == Integer.parseInt(e.getStartState())) {
						tabTransitions.get(Integer.parseInt(e.getStartState())).put(e.getTransitionSymbol(),
								e.getEndState());
					}
				}
			} else {
				tabTransitions.get(Integer.parseInt(e.getStartState())).put(e.getTransitionSymbol(), e.getEndState());
			}
		}

		if (DEBUG) {
			System.out.println("--> tabTransitions: " + tabTransitions);
		}

		// Recuperation etats finaux
		Set<String> ensembleFinaux = new LinkedHashSet<>();
		for (String finaux : DFA.finalStates) {
			ensembleFinaux.add(finaux);
		}

		// Recuperation etats non finaux
		Set<String> ensembleNonFinaux = new LinkedHashSet<>();
		for (int i = 0; i < tabTransitions.size(); i++) {
			if (!ensembleFinaux.contains("" + i)) {
				ensembleNonFinaux.add("" + i);
			}
		}

		if (DEBUG) {
			System.out.println("--> Set finaux: " + ensembleFinaux);
			System.out.println("--> Set non finaux: " + ensembleNonFinaux);
			System.out.println("--> Symboles: " + DFA.getSymbolesTransition());
		}

		// Separatio en etats distincts pour minimiser
		Set<Set<String>> ensembleTotal = new LinkedHashSet<>();
		ensembleTotal.add(ensembleFinaux);
		ensembleTotal.add(ensembleNonFinaux);
		ensembleTotal = decoupage(ensembleTotal, tabTransitions, DFA.getSymbolesTransition(), 0);
		if (DEBUG) {
			System.out.println("--> ensembleTotal: " + ensembleTotal);
			System.out.println("--> Transitions actuelles: " + transitions);
		}

		// Creation des nouvelles transitions pour l'automate minimise
		for (Set<String> ens : ensembleTotal) {
			Iterator<String> it = ens.iterator();
			while (it.hasNext()) {
				String indexString = it.next();
				int indexInt = Integer.parseInt(indexString);
				if (!(tabTransitions.size() <= indexInt)) {
					for (Map.Entry<String, String> entry : tabTransitions.get(indexInt).entrySet()) {
						Set<String> endSet = getSetFromSets(ensembleTotal, entry.getValue());
						if ((ens.size() == 1) && (endSet.size() == 1)) {
							if (!transitions.contains(new Transition(indexString, entry.getKey(), entry.getValue()))) {
								transitions.add(new Transition(indexString, entry.getKey(), entry.getValue()));
							}
						} else if ((ens.size() > 1) && (endSet.size() == 1)) {
							if (!transitions.contains(
									new Transition("\"" + ens.toString() + "\"", entry.getKey(), entry.getValue()))) {
								transitions.add(
										new Transition("\"" + ens.toString() + "\"", entry.getKey(), entry.getValue()));
							}
						} else if ((ens.size() == 1) && (endSet.size() > 1)) {
							if (!transitions.contains(
									new Transition(indexString, entry.getKey(), "\"" + endSet.toString() + "\""))) {
								transitions.add(
										new Transition(indexString, entry.getKey(), "\"" + endSet.toString() + "\""));
							}
						} else {
							if (!transitions.contains(new Transition("\"" + ens.toString() + "\"", entry.getKey(),
									"\"" + endSet.toString() + "\""))) {
								transitions.add(new Transition("\"" + ens.toString() + "\"", entry.getKey(),
										"\"" + endSet.toString() + "\""));
							}
						}
					}
				}
			}
		}

		if (DEBUG) {
			System.out.println("--> Transitions nouvelles: " + transitions);
		}

		// Recherche set de l'etat initial
		for (Set<String> ens : ensembleTotal) {
			if (ens.contains("0")) {
				List<String> liste = new ArrayList<>(ens);
				initialStates.addAll(liste);
			}
		}

		// Recherche sets des etats finaux
		for (String s : DFA.finalStates) {
			for (Set<String> ens : ensembleTotal) {
				if (ens.contains(s)) {
					if (!finalStates.contains(s) && !finalStates.contains(ens.toString())) {
						List<String> liste = new ArrayList<>();
						if (ens.size() > 1) {
							liste.add(ens.toString());
						} else {
							liste.add(s);
						}
						finalStates.addAll(liste);
					}
				}
			}
		}

		if (DEBUG) {
			System.out.println("=====> initialStates: " + initialStates);
			System.out.println("=====> finalStates: " + finalStates);
			System.out.println("-------------------------------------------\n\n");
		}

		return this;
	}

	/***************************************************************************************/
	/**
	 * Recuperation d'un set parmi un set de sets.
	 * 
	 * @param setOfSets
	 * @param val       Set recherche
	 * @return Set recherche
	 */
	public Set<String> getSetFromSets(Set<Set<String>> setOfSets, String val) {
		for (Set<String> sets : setOfSets) {
			if (sets.contains(val)) {
				return sets;
			}
		}

		return null;
	}

	/***************************************************************************************/
	/**
	 * Recherche dans le text du pattern correspondant au RegEx
	 * 
	 * @param path
	 * @return
	 */
	public int search(String path, boolean print) {
		File directory = new File(path);
		String strLine = "";
		int cpt = 0;
		if (print) {
			System.out.println("  >> Reading: " + directory.getAbsolutePath() + "\n");
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(directory.getAbsolutePath()));

			while ((strLine = br.readLine()) != null) {
				for (String init : this.initialStates) {
					if (processAuto(init, strLine, false)) {
						cpt++;
						if (print) {
							System.out.println("  >> Found: " + strLine);
						}
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
		} catch (IOException e) {
			System.err.println("Unable to read the file.");
		}
		return cpt;
	}

	/***************************************************************************************/
	/**
	 * On process recursivement l'automate sur une ligne de text
	 * 
	 * @param state
	 * @param strLine
	 * @param follow
	 * @return
	 */
	protected boolean processAuto(String state, String strLine, boolean follow) {
		if (state.contains("[")) {
			for (int i = 0; i < state.length(); i++) {
				for (String q : this.finalStates) {
					if (q.equals("" + state.charAt(i)) || q.contains("" + state.charAt(i))) {
						return true;
					}
				}
			}
		} else if (this.finalStates.contains(state)) {
			return true;
		}

		ArrayList<Transition> currTransitions = new ArrayList<>();

		for (Transition t : this.transitions) {
			if (t.startState.equals(state))
				currTransitions.add(t);
		}

		if (strLine.isEmpty()) {
			return false;
		}

		for (Transition t : currTransitions) {
			if (follow) {
				if (t.transitionSymbol.equals("" + strLine.charAt(0))) {
					return processAuto(t.endState, strLine.substring(1, strLine.length()), true);

				}
			} else {
				for (int i = 0; i < strLine.length(); i++) {
					if (t.transitionSymbol.equals("" + strLine.charAt(i))) {
						return processAuto(t.endState, strLine.substring(i + 1, strLine.length()), true);
					}
				}
			}
		}

		if (!strLine.isEmpty()) {
			return processAuto(this.getInitialStates().get(0), strLine.substring(1, strLine.length()), false);
		}

		return false;
	}

	/***************************************************************************************/
	/**
	 * Recuperer les symboles des transitions dans pour faire une String depuis la
	 * DFA, donc plus d'epsilon.
	 * 
	 * @return
	 */
	public String getSymbolesTransition() {
		String res = "";
		for (Transition e : this.getTransitions()) {
			if (!res.contains(e.getTransitionSymbol())) {
				res += e.getTransitionSymbol();
			}
		}

		return res;
	}

	/***************************************************************************************/
	/**
	 * Recuperer les etats initiaux (initial si DFA/Min-DFA)
	 * 
	 * @return Liste des etats (de l'etat) initiaux
	 */
	public ArrayList<String> getInitialStates() {
		return this.initialStates;
	}

	/***************************************************************************************/
	/**
	 * Recuperer les etats finaux
	 * 
	 * @return Liste des etats finaux
	 */
	public ArrayList<String> getFinalStates() {
		return this.finalStates;
	}

	/***************************************************************************************/
	/**
	 * Recuperer toutes les transitions de l'automate.
	 * 
	 * @return Liste des transitions
	 */
	public ArrayList<Transition> getTransitions() {
		return this.transitions;
	}

	/***************************************************************************************/
	/**
	 * Ajouter un etat initial dans la liste des etats initiaux de l'automate.
	 * 
	 * @param initial L'etat a ajouter
	 */
	public void addInitialState(String initial) {
		this.initialStates.add(initial); // ArrayList but only one into for DFA/Min-DFA
	}

	/***************************************************************************************/
	/**
	 * Ajouter un etat final dans la liste des etats finaux de l'automate.
	 * 
	 * @param finalS L'etat a ajouter
	 */
	public void addFinalState(String finalS) {
		this.finalStates.add(finalS);
	}

	/***************************************************************************************/
	/**
	 * Ajouter une liste d'etat finaux dans la liste de l'automate.
	 * 
	 * @param listFinalS La liste a ajouter
	 */
	public void addFinalStates(ArrayList<String> listFinalS) {
		this.finalStates.addAll(listFinalS);
	}

	/***************************************************************************************/
	/**
	 * Ajouter une transition a la liste de transitions de l'automate.
	 * 
	 * @param e Tranition a ajouter
	 */
	public void addTransition(Transition e) {
		this.transitions.add(e);
	}

	/***************************************************************************************/
	/**
	 * Ajouter une liste de transitions a la liste de transitions de l'automate.
	 * 
	 * @param listE Liste a ajouter
	 */
	public void addTransitions(ArrayList<Transition> listE) {
		this.transitions.addAll(listE);
	}

	/***************************************************************************************/
	/**
	 * Affichage de toutes les transitions de l'automate.
	 */
	public void printTransitions() {
		System.out.println("Transitions: ");
		for (Transition e : this.transitions) {
			System.out.println(e.toString());
		}
	}
}
/***************************************************************************************/
/***************************************************************************************/
/***************************************************************************************/