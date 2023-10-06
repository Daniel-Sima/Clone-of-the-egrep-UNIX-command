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
public class RegEx {
  //MACROS
  static final int CONCAT = 0xC04CA7;
  static final int ETOILE = 0xE7011E;
  static final int PLUS = 0xFFFFFF;
  static final int ALTERN = 0xA17E54;
  static final int PROTECTION = 0xBADDAD;

  static final int PARENTHESEOUVRANT = 0x16641664;
  static final int PARENTHESEFERMANT = 0x51515151;
  static final int DOT = 0xD07;
  
  //REGEX
  private static String regEx;
  //TEXT
  private static String text;
  
  //CONSTRUCTOR
  public RegEx(){}

  //TIME
  private static long startDFA,startDOT,startMDFA,startNFDA,startWT;
  private static long endDFA,endDOT,endMDFA,endNFDA,endWT;

  //MAIN
  public static void main(String arg[]) {
    System.out.println("Welcome to Bogota, Mr. Thomas Anderson.");
    System.out.println(arg[0]);

    long startAll = System.currentTimeMillis();   

    if(arg.length==2){
      regEx = arg[0];
      text = arg[1];
    } else if (arg.length==1) {
      regEx = arg[0];
    } else {
      Scanner scanner = new Scanner(System.in);
      System.out.print("  >> Please enter a regEx: ");
      regEx = scanner.next();
    }
    System.out.println("  >> Parsing regEx \""+regEx+"\".");
    System.out.println("  >> ...");
    
    if (regEx.length()<1) {
      System.err.println("  >> ERROR: empty regEx.");
    } else {
      System.out.print("  >> ASCII codes: ["+(int)regEx.charAt(0));
      for (int i=1;i<regEx.length();i++) System.out.print(","+(int)regEx.charAt(i));
      System.out.println("].");
      try {
        boolean hasit = false;
        for (int i=1;i<regEx.length();i++){
          char c = regEx.charAt(i);
          if(c=='|' || c=='+' || c=='(' || c==')' || c=='*' || c=='.'){
            hasit=true;
            break;
          }
        }
        if(hasit){   
        
          RegExTree ret = parse();
          System.out.println("  >> Tree result: "+ret.toString()+".");

          // Print Arbre Syntaxique 
          Automata resSyntaxTree = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()); 
          resSyntaxTree = resSyntaxTree.toSyntaxTree(ret);
          /* 
          startWT = System.currentTimeMillis();
          FileWriter writer = new FileWriter("arbre_syntaxique.dot");
          writer.write("digraph {\n");
          for (Automata.Transition t : resSyntaxTree.transitions){
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
          endWT = System.currentTimeMillis();
          */
          // Print NDFA
          Automata res = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
          res = res.toNDFA(ret);
          /*
          startNFDA = System.currentTimeMillis();
          writer = new FileWriter("NDFA.dot");
          writer.write("digraph {\n\trankdir=LR;\n\n");
          for (String s : res.finalStates){
            writer.write("\t" + s + " [shape=doublecircle]\n");
          }
          for (String s : res.initialStates){
            writer.write("\t" + s + " [style=filled, fillcolor=\"lightblue\"]\n");
          }

          writer.write("\n");

          for (Automata.Transition t : res.transitions){
            writer.write("\t" + t.getStartState() + "->" + t.getEndState() + " [label= \"" +t.getTransitionSymbol() + "\"];\n");
          }
          
          writer.write("}\n");
          writer.close();
          
          try {
            process = Runtime.getRuntime().exec("dot -Tpng NDFA.dot -o NDFA.png");
            process.waitFor();
          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
          }
          endNFDA = System.currentTimeMillis();
                  */
          // Print DFA
          Automata resDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
          resDFA = resDFA.toDFA(res);
          /*
          startDFA = System.currentTimeMillis();
          writer = new FileWriter("DFA.dot");
          writer.write("digraph {\n\trankdir=LR;\n\n");
          for (String s : resDFA.getFinalStates()){
            writer.write("\t" + s + " [shape=doublecircle]\n");
          }
          for (String s : resDFA.initialStates){
            writer.write("\t" + s + " [style=filled, fillcolor=\"lightblue\"]\n");
          }

          writer.write("\n");

          for (Automata.Transition t : resDFA.transitions){
            writer.write("\t" + t.getStartState() + "->" + t.getEndState() + " [label= \"" +t.getTransitionSymbol() + "\"];\n");
          }
          
          writer.write("}\n");
          writer.close();
          
          try {
            process = Runtime.getRuntime().exec("dot -Tpng DFA.dot -o DFA.png");
            process.waitFor();
          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
          }
          endDFA = System.currentTimeMillis();
          */

          // Print Min-DFA
          //System.out.println("/*********************************** Min-DFA ***********************************/");
          Automata resMDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
          resMDFA = resMDFA.toMinDFA(resDFA);

          /* 
          startMDFA = System.currentTimeMillis();
          writer = new FileWriter("Min-DFA.dot");
          writer.write("digraph {\n\trankdir=LR;\n\n");
          for (String s : resMDFA.getFinalStates()){
            writer.write("\t" + s + " [shape=doublecircle]\n");
          }
          for (String s : resMDFA.initialStates){
            writer.write("\t" + s + " [style=filled, fillcolor=\"lightblue\"]\n");
          }
          
          writer.write("\n");

          for (Automata.Transition t : resMDFA.transitions){
            writer.write("\t" + t.getStartState() + "->" + t.getEndState() + " [label= \"" +t.getTransitionSymbol() + "\"];\n");
          }
          
          writer.write("}\n");
          writer.close();
          endMDFA = System.currentTimeMillis();
          */
          if(arg.length==2){
            int cptOcc = resMDFA.search(text);
            System.out.println("We found "+cptOcc+" occurences of pattern.");
          }
          /*
          startDOT = System.currentTimeMillis();
          try {
            process = Runtime.getRuntime().exec("dot -Tpng Min-DFA.dot -o Min-DFA.png");
            process.waitFor();
          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
          }
          endDOT = System.currentTimeMillis();
          */
        }else{
          int cpt = processKMP(text, regEx);
          System.out.println("We found "+cpt+" occurences of pattern.");
        } 
      } catch (Exception e) {
        System.err.println("  >> ERROR: syntax error for regEx \""+regEx+"\"." );
      }    
    }

    System.out.println("  >> ...");
    System.out.println("  >> Parsing completed.");
    System.out.println("Goodbye Mr. Anderson.");

    long endAll = System.currentTimeMillis(); 
    
    long totalTime = endAll-startAll;
    System.out.println("Execution time : "+totalTime+"ms"); 
  }

  //FROM REGEX TO SYNTAX TREE
  private static RegExTree parse() throws Exception {
    //BEGIN DEBUG: set conditionnal to true for debug example
    if (false) throw new Exception();
    RegExTree example = exampleAhoUllman();
    if (false) return example;
    //END DEBUG

    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    for (int i=0;i<regEx.length();i++) result.add(new RegExTree(charToRoot(regEx.charAt(i)),new ArrayList<RegExTree>()));
    
    return parse(result);
  }
  private static int charToRoot(char c) {
    if (c=='.') return DOT;
    if (c=='*') return ETOILE;
    if (c=='+') return PLUS;
    if (c=='|') return ALTERN;
    if (c=='(') return PARENTHESEOUVRANT;
    if (c==')') return PARENTHESEFERMANT; 
    return (int)c;
  }
  private static RegExTree parse(ArrayList<RegExTree> result) throws Exception {
    while (containParenthese(result)) result=processParenthese(result);
    while (containEtoile(result)) result=processEtoile(result);
    while (containPlus(result)) result=processPlus(result);
    while (containConcat(result)) result=processConcat(result);
    while (containAltern(result)) result=processAltern(result);

    if (result.size()>1) throw new Exception();

    return removeProtection(result.get(0));
  }
  private static boolean containParenthese(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==PARENTHESEFERMANT || t.root==PARENTHESEOUVRANT) return true;
    return false;
  }
  private static ArrayList<RegExTree> processParenthese(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    for (RegExTree t: trees) {
      if (!found && t.root==PARENTHESEFERMANT) {
        boolean done = false;
        ArrayList<RegExTree> content = new ArrayList<RegExTree>();
        while (!done && !result.isEmpty())
          if (result.get(result.size()-1).root==PARENTHESEOUVRANT) { done = true; result.remove(result.size()-1); }
          else content.add(0,result.remove(result.size()-1));
        if (!done) throw new Exception();
        found = true;
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
        subTrees.add(parse(content));
        result.add(new RegExTree(PROTECTION, subTrees));
      } else {
        result.add(t);
      }
    }
    if (!found) throw new Exception();
    return result;
  }

  private static boolean containEtoile(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==ETOILE && t.subTrees.isEmpty()) return true;
    return false;
  }

  private static boolean containPlus(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==PLUS && t.subTrees.isEmpty()) return true;
    return false;
  }

  private static ArrayList<RegExTree> processEtoile(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    for (RegExTree t: trees) {
      if (!found && t.root==ETOILE && t.subTrees.isEmpty()) {
        if (result.isEmpty()) throw new Exception();
        found = true;
        RegExTree last = result.remove(result.size()-1);
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
        subTrees.add(last);
        result.add(new RegExTree(ETOILE, subTrees));
      } else {
        result.add(t);
      }
    }
    return result;
  }

  private static ArrayList<RegExTree> processPlus(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    for (RegExTree t: trees) {
      if (!found && t.root==PLUS && t.subTrees.isEmpty()) {
        if (result.isEmpty()) throw new Exception();
        found = true;
        RegExTree last = result.remove(result.size()-1);
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
        subTrees.add(last);
        result.add(new RegExTree(PLUS, subTrees));
      } else {
        result.add(t);
      }
    }
    return result;
  }

  private static boolean containConcat(ArrayList<RegExTree> trees) {
    boolean firstFound = false;
    for (RegExTree t: trees) {
      if (!firstFound && t.root!=ALTERN) { firstFound = true; continue; }
      if (firstFound) if (t.root!=ALTERN) return true; else firstFound = false;
    }
    return false;
  }
  private static ArrayList<RegExTree> processConcat(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    boolean firstFound = false;
    for (RegExTree t: trees) {
      if (!found && !firstFound && t.root!=ALTERN) {
        firstFound = true;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root==ALTERN) {
        firstFound = false;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root!=ALTERN) {
        found = true;
        RegExTree last = result.remove(result.size()-1);
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
  private static boolean containAltern(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==ALTERN && t.subTrees.isEmpty()) return true;
    return false;
  }
  private static ArrayList<RegExTree> processAltern(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    RegExTree gauche = null;
    boolean done = false;
    for (RegExTree t: trees) {
      if (!found && t.root==ALTERN && t.subTrees.isEmpty()) {
        if (result.isEmpty()) throw new Exception();
        found = true;
        gauche = result.remove(result.size()-1);
        continue;
      }
      if (found && !done) {
        if (gauche==null) throw new Exception();
        done=true;
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
  private static RegExTree removeProtection(RegExTree tree) throws Exception {
    if (tree.root==PROTECTION && tree.subTrees.size()!=1) throw new Exception();
    if (tree.subTrees.isEmpty()) return tree;
    if (tree.root==PROTECTION) return removeProtection(tree.subTrees.get(0));

    ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
    for (RegExTree t: tree.subTrees) subTrees.add(removeProtection(t));
    return new RegExTree(tree.root, subTrees);
  }
  
  //read file
  public static int processKMP(String path, String pattern){
    File directory = new File(path);
    String strLine = "";
    int cpt = 0;
    System.out.println("Reading: "+directory.getAbsolutePath());
    try {
        BufferedReader br = new BufferedReader(new FileReader(path));
        while ((strLine = br.readLine()) != null)
        {
            int[] lps = computeLPSArray(regEx);
            int index = search(strLine, pattern, lps);
            if (index != -1) {
              cpt++;
              System.out.println("found: "+strLine);
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

  // Compute the LPS (Longest Proper Prefix which is also Suffix) array
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

  // Search for the pattern in the given text using KMP algorithm
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

  //EXAMPLE
  // --> RegEx from Aho-Ullman book Chap.10 Example 10.25
  private static RegExTree exampleAhoUllman() {
    RegExTree a = new RegExTree((int)'a', new ArrayList<RegExTree>());
    RegExTree b = new RegExTree((int)'b', new ArrayList<RegExTree>());
    RegExTree c = new RegExTree((int)'c', new ArrayList<RegExTree>());
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

//UTILITARY CLASS
class RegExTree {
  protected int root;
  protected ArrayList<RegExTree> subTrees;

  public RegExTree(int root, ArrayList<RegExTree> subTrees) {
    this.root = root;
    this.subTrees = subTrees;
  }

  //FROM TREE TO PARENTHESIS
  public String toString() {
    if (subTrees.isEmpty()) return rootToString();
    String result = rootToString()+"("+subTrees.get(0).toString();
    for (int i=1;i<subTrees.size();i++) result+=","+subTrees.get(i).toString();
    return result+")";
  }

  public String rootToString() {
    if (root==RegEx.CONCAT) return ".";
    if (root==RegEx.ETOILE) return "*";
    if (root==RegEx.PLUS) return "+";
    if (root==RegEx.ALTERN) return "|";
    if (root==RegEx.DOT) return ".";
    return Character.toString((char)root);
  }
}

class Automata {
  class Transition {
    private String startState;
    private String transitionSymbol;    
    private String endState;

    public Transition(String startState, String transitionSymbol, String endState) {
      this.startState = startState;
      this.transitionSymbol = transitionSymbol;
      this.endState = endState;
    }

    public String getStartState(){
      return this.startState;
    }

    public String getTransitionSymbol(){
      return this.transitionSymbol;
    }

    public String getEndState(){
      return this.endState;
    }

    public String toString(){
      return "[" + getStartState() + " -- " + getTransitionSymbol() +" -- >" + getEndState() + "]";
    }

    /** Pour verifier les doublons selon notre implementation */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition nouvelle = (Transition) o;
        return Objects.equals(startState, nouvelle.getStartState()) &&
               Objects.equals(endState, nouvelle.getEndState()) &&
               Objects.equals(transitionSymbol, nouvelle.getTransitionSymbol());
    }
  }

  /**
   * Classe permettant de retourner deux valeurs de type different
   */
  class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value){
      this.key = key;
      this.value = value;
    }

    public K getKey(){
      return key;
    }

     public V getValue(){
      return value;
    }

    public void setKey(K key){
      this.key = key;
    }

    public void setValue(V value){
      this.value = value;
    }
  }

  protected ArrayList<String> initialStates; // AR
  protected ArrayList<String> finalStates;
  protected ArrayList<Transition> transitions;  
  protected Integer numberStates = 0;


  public Automata (ArrayList<String> initialState, ArrayList<String> finalStates, ArrayList<Transition> transitions){
    this.initialStates = initialState;
    this.finalStates = finalStates;
    this.transitions = transitions;
  }

   /** RegExTree to dot */
  public Automata toSyntaxTree(RegExTree tree){
    if (tree.subTrees.isEmpty()){
      transitions.add(new Transition(initialStates.get(initialStates.size()-1), null, ("\""+tree.rootToString()+"__"+(numberStates++))+"\""));
      return new Automata(initialStates, finalStates, transitions);
    } 

    Automata resAutomata = null;
    for (int i=0; i<tree.subTrees.size(); i++){
      if (!(initialStates.isEmpty()) && i == 0) {
        transitions.add(new Transition(initialStates.get(initialStates.size()-1), null, "\""+tree.rootToString()+"__"+(numberStates)+"\""));
        initialStates.add("\""+tree.rootToString()+"__"+(numberStates++)+"\"");
      }  
      if (initialStates.isEmpty()){
        initialStates.add("\""+tree.rootToString()+"__"+(numberStates++)+"\"");
      }

      resAutomata = toSyntaxTree(tree.subTrees.get(i));
      
      if ((!(initialStates.isEmpty()) && i == 1) || (tree.rootToString() == "*") || (tree.rootToString() == "+")) {
        initialStates.remove(initialStates.size()-1);
      } 
    }

    return resAutomata;
  }

   /**
    * Trouve toute les transitions entre un etat de depart (present dans 'statesToSearch') et de fin.
    * Change leur numeros.
    * A REVOIR CAS PARTICULIERS
    *
    * @param startState
    * @param accTransitions
    * @param statesToSearch
    * @return
    */
  public ArrayList<Transition> findTransitionsBetweenStates(ArrayList<Transition> accTransitions, ArrayList<String> statesToSearch){
    ArrayList<String> newStatesToSearch = new ArrayList<>();
    for (String etat : statesToSearch){
      for (Transition e : transitions){
        if (e.getStartState().equals(etat)){
          accTransitions.add(new Transition(e.getStartState()+e.getStartState(), e.getTransitionSymbol(), e.getEndState()+e.getEndState()));
          if (!newStatesToSearch.contains(e.getEndState())){
            newStatesToSearch.add(e.getEndState());
          }
        }
      }
    }

    if (!newStatesToSearch.isEmpty()){
      return findTransitionsBetweenStates(accTransitions, newStatesToSearch);
    }

    return accTransitions;
  }

   /** From tree to automata (NFDA) */
  public Automata toNDFA(RegExTree tree) {
    if (tree.subTrees.isEmpty()) {
      initialStates.add(""+numberStates);
      finalStates.add(""+(numberStates+1));
      transitions.add(new Transition(""+numberStates, tree.rootToString(), ""+(numberStates+1)));
      numberStates += 2;

      return new Automata(initialStates, finalStates, transitions);
    }

    Automata resAutomata = null;
    for (int i=0; i<tree.subTrees.size(); i++){
      resAutomata = toNDFA(tree.subTrees.get(i));
      
      if (tree.rootToString() == "*"){
        // R1
        String lastInitialEtat = initialStates.remove(initialStates.size()-1);
        String lastFinalEtat = finalStates.remove(finalStates.size()-1);
        transitions.add(new Transition(lastFinalEtat, "ε", lastInitialEtat));
        initialStates.add(""+numberStates);
        transitions.add(new Transition(initialStates.get(initialStates.size()-1), "ε", lastInitialEtat));
        finalStates.add(""+(numberStates+1));
        transitions.add(new Transition(lastFinalEtat, "ε", finalStates.get(finalStates.size()-1)));
        transitions.add(new Transition(initialStates.get(initialStates.size()-1), "ε", finalStates.get(finalStates.size()-1)));
        numberStates += 2;
      } else if ((tree.rootToString() == ".") && (i != 0)){
        // R2
        String lastInitialEtat = initialStates.remove(initialStates.size()-1);
        String lastFinalEtat = finalStates.remove(finalStates.size()-2); 
        transitions.add(new Transition(lastFinalEtat, "ε", lastInitialEtat));
      } else if ((tree.rootToString() == "|") && (i != 0)) {
        // R2
        String lastInitialEtatR2 = initialStates.remove(initialStates.size()-1);
        String lastFinalEtatR2 = finalStates.remove(finalStates.size()-1);
        // R1
        String lastInitialEtatR1 = initialStates.remove(initialStates.size()-1);
        String lastFinalEtatR1 = finalStates.remove(finalStates.size()-1);

        initialStates.add(""+numberStates);
        transitions.add(new Transition(initialStates.get(initialStates.size()-1), "ε", lastInitialEtatR1));
        transitions.add(new Transition(initialStates.get(initialStates.size()-1), "ε", lastInitialEtatR2));
        finalStates.add(""+(numberStates+1));
        transitions.add(new Transition(lastFinalEtatR1, "ε", finalStates.get(initialStates.size()-1)));
        transitions.add(new Transition(lastFinalEtatR2, "ε", finalStates.get(initialStates.size()-1)));
        numberStates += 2;
      } else if ((tree.rootToString() == "+")) {
        System.err.println("/////////////////////////////////////////////////////");
        //printTransitions();
        String lastInitialEtat = initialStates.get(initialStates.size()-1);
        String lastFinalEtat = finalStates.get(finalStates.size()-1);
        //System.out.println("lastInitialEtat: "+lastInitialEtat);
        //System.out.println("lastFinalEtat: "+lastFinalEtat);

        ArrayList<Transition> plusTransitons = new ArrayList<>();
        ArrayList<String> statesToSearch = new ArrayList<>(); 
        statesToSearch.add(lastInitialEtat);
        plusTransitons = findTransitionsBetweenStates(plusTransitons, statesToSearch);

        
        transitions.addAll(plusTransitons);
        String newInitialEtat = plusTransitons.get(0).getStartState();
        String newFinalEtat = plusTransitons.get(plusTransitons.size()-1).getEndState();

        transitions.add(new Transition(lastFinalEtat, "ε", newInitialEtat));
        transitions.add(new Transition(newFinalEtat, "ε", newInitialEtat));

        // Etat ultime de l'automate avec PLUS
        String newFinalFinalEtat = ""+numberStates; numberStates++;
        //System.out.println("====> ce: "+newFinalFinalEtat);
        finalStates.remove(finalStates.size()-1);
        finalStates.add(newFinalFinalEtat);
        transitions.add(new Transition(lastFinalEtat, "ε", newFinalFinalEtat));
        transitions.add(new Transition(newFinalEtat, "ε", newFinalFinalEtat));



        //System.out.println("--------> finalStates: "+finalStates);
        //System.out.println("--------> initialStates: "+initialStates);
        //System.err.println("/////////////////////////////////////////////////////");
      } else {
        System.out.println("===> negliger?: "+tree.rootToString());
      }
    }  
    return resAutomata;
  }

  /**
   * Getting epsilon close inital states
   * 
   * @return ArrayList with initals states
   */
  private ArrayList<String> getInitalEpsilonStates(ArrayList<String> startStates){
    ArrayList<String> res = new ArrayList<>();
    res.addAll(startStates);
    
    for (String s : startStates){
      for (Transition e : this.getTransitions()){
        if ((e.getStartState().equals(s)) && (e.getTransitionSymbol().equals("ε")) && !(res.contains(e.getEndState()))){
          res.add(e.getEndState());
        }
      }
    }

    if (res.size() != startStates.size()){
      res = this.getInitalEpsilonStates(res);
    }
    
    return res;
  }

  /**
   * Testing all 256 ASCII carcacters to find stats that are acceptable
   * 
   * @return Map with ASCI caracter and state number
   */
  private HashMap<String, ArrayList<String>> getASCII_transitions(ArrayList<String> states){
    HashMap<String, ArrayList<String>> res = new HashMap<>();

    for (int i = 0; i <= 255; i++) {
      char caractere = (char) i;
      for (Transition e : this.transitions){
        for (String s : states){
          if (e.getStartState().equals(s)){ // ATTENTION .equals important
            if ((""+caractere).equals(e.getTransitionSymbol())) {
              ArrayList<String> etatsASCII = new ArrayList<>();
              etatsASCII.add(e.getEndState());
              etatsASCII = this.getInitalEpsilonStates(etatsASCII);
              res.put(""+caractere, etatsASCII);
            }
          }
        }
      }
    }

    return res;
  }

  private  Pair<ArrayList<HashMap<String, ArrayList<String>>>, ArrayList<ArrayList<String>>>  findStates_DFA(ArrayList<ArrayList<String>> states, ArrayList<HashMap<String, ArrayList<String>>> tab, int cpt){
    ArrayList<HashMap<String, ArrayList<String>>> tableau = new ArrayList<>(tab);
    ArrayList<ArrayList<String>> newStates = new ArrayList<>(states);

    HashMap<String, ArrayList<String>> res = new HashMap<>();
    
    for (int i=0; i < states.size(); i++){
      if (i >= cpt){
        res = this.getASCII_transitions(states.get(i));
        //System.out.println("--> Pour: "+states.get(i)+" res: "+res);
        if ((!tableau.contains(res)) && (!newStates.containsAll(res.values()))){
          newStates.addAll(res.values());
        }
        tableau.add(res);
      }
    }

    //System.out.println("--> New states: "+newStates);
    //System.out.println("--> Tableau: "+tableau);

    if (states.size() != newStates.size()){
      System.err.println("\n");
      return this.findStates_DFA(newStates, tableau, states.size());
    } 
    
    Pair<ArrayList<HashMap<String, ArrayList<String>>>, ArrayList<ArrayList<String>>> resultat =  new Pair<>(tableau, newStates);
    return resultat;
  }

  /**
   * From NDFA to DFA
   * 
   * @param NDFA
   * @return
   */
  public Automata toDFA(Automata NDFA) {
    //System.out.println("Inital state: "+NDFA.getInitialStates());    
    //System.out.println("Final states: "+NDFA.getFinalStates());
    //NDFA.printTransitions();
    /*----------------------------------------------- */
    //System.out.println("-----------------------------------------------");
    ArrayList<String> startState = new ArrayList<>(NDFA.getInitialStates());
    startState = NDFA.getInitalEpsilonStates(startState);
    ArrayList<ArrayList<String>> states = new ArrayList<>();
    states.add(startState);

    //System.out.println("===> states: "+states);
    //System.out.println("==> startState: "+startState);
    
    // HashMap<String, ArrayList<String>> res = NDFA.getASCII_transitions(states.get(0));
    // System.out.println("===> res: "+res);
    // // Tableau final de l'automate
    ArrayList<HashMap<String, ArrayList<String>>> tableau = new ArrayList<>();
    // tableau.add(res);
    Pair<ArrayList<HashMap<String, ArrayList<String>>>, ArrayList<ArrayList<String>>> pairTableauStates = NDFA.findStates_DFA(states, tableau, 0);
    //System.out.println("\nFinal tableau: "+pairTableauStates.getKey());
    //System.out.println("Final states: "+pairTableauStates.getValue());
    //System.out.println("-----------------------------------------------");

    Automata automataDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    automataDFA.addInitialState("0");
    for (int i=0; i<pairTableauStates.getValue().size(); i++) {
      HashMap<String, ArrayList<String>> tabTransitions = pairTableauStates.getKey().get(i);
      
      // Parcours simultane de cle valeur par ensemble
      for (Map.Entry<String, ArrayList<String>> entry : tabTransitions.entrySet()) {
        automataDFA.addTransition(new Transition(""+i, entry.getKey(), ""+pairTableauStates.getValue().indexOf(entry.getValue())));
        
        // Ajout des etats finaux si on en trouve dans le 'tableau'
        if (entry.getValue().contains(NDFA.getFinalStates().get(0))) { // AR
          if (!automataDFA.getFinalStates().contains(""+pairTableauStates.getValue().indexOf(entry.getValue()))){
            automataDFA.addFinalState(""+pairTableauStates.getValue().indexOf(entry.getValue()));
          }
        }
      }
    }
    /*
    System.out.print("Final DFA: ");
    automataDFA.printTransitions();
    System.out.println("-----------------------------------------------");
    */
    return automataDFA;
  }


  /**
   * Decoupe l'ensemble des etats jusqu'a ce que on ne peut plus decouper
   * parcequ'il y en a qu'un seul ou parceque les deux sont dans le 
   * meme ensemble.
   * 
   * @param ensembleTotal
   * @param tabTransitions
   * @return
   */
  public Set<Set<String>> decoupage(Set<Set<String>> ensembleTotal,  ArrayList<HashMap<String, String>> tabTransitions, String symboles, int i){
    if (i == symboles.length()){
      return ensembleTotal;
    }

    String symbole = ""+symboles.charAt(i); 
    Set<Set<String>> ensembleTotalBis = new LinkedHashSet<>(ensembleTotal);

    for (Set<String> ens : ensembleTotal){
      //System.out.println("==> ens: "+ens+" i: "+i);
      if (!(ens.size() == 1)){
        int cpt = 0;
        Set<String> dedans = new LinkedHashSet<>();
        Set<String> pasDedans = new LinkedHashSet<>();
        for (HashMap<String, String> table : tabTransitions){
          if (ens.contains(""+cpt)) {
            if (table.containsKey(symbole)){
              dedans.add(""+cpt);
            } else {
              pasDedans.add(""+cpt);
            }
          }
          cpt++;
        }
        ensembleTotalBis.remove(ens);
        if (!dedans.isEmpty()){
          ensembleTotalBis.add(dedans);
        }
        if (!pasDedans.isEmpty()){
          ensembleTotalBis.add(pasDedans);
        }
        //System.out.println("==> dedans: "+dedans);        
        //System.out.println("==> pasDedans: "+pasDedans);

      }
    }

    //System.out.println("==> ensembleTotalBis: "+ensembleTotalBis+" i: "+i+"\n");
    return decoupage(ensembleTotalBis, tabTransitions, symboles, (i+1));
  }

  /**
   * Minimisation de l'automate deterministe DFA.
   * 
   * @param DFA
   * @return
   */
  public Automata toMinDFA(Automata DFA){
    ArrayList<HashMap<String, String>> tabTransitions = new ArrayList<>();

    // Recuperation du tableau des transitions
    for (Transition e : DFA.getTransitions()){
      // Si rien n'a ete ajoute encore
      if (tabTransitions.size() == Integer.parseInt(e.getStartState())){
        HashMap<String, String> val = new HashMap<>();
        val.put(e.getTransitionSymbol(), e.getEndState());
        tabTransitions.add(Integer.parseInt(e.getStartState()), val);
      } else {
        tabTransitions.get(Integer.parseInt(e.getStartState())).put(e.getTransitionSymbol(), e.getEndState());
      }
    }

    Set<String> ensembleFinaux = new LinkedHashSet<>();
    for (String finaux : DFA.finalStates){
      ensembleFinaux.add(finaux);
    }

    Set<String> ensembleNonFinaux = new LinkedHashSet<>();
    for (int i=0; i<tabTransitions.size(); i++){
      if (!ensembleFinaux.contains(""+i)){
        ensembleNonFinaux.add(""+i);
      }
    }
    /* 
    System.out.println(tabTransitions);
    System.out.println("--> Set finaux: "+ensembleFinaux);    
    System.out.println("--> Set non finaux: "+ensembleNonFinaux);
    System.out.println("--> Symboles: "+DFA.getSymbolesTransition());
    */
    Set<Set<String>> ensembleTotal = new LinkedHashSet<>();
    ensembleTotal.add(ensembleFinaux); ensembleTotal.add(ensembleNonFinaux);
    ensembleTotal = decoupage(ensembleTotal, tabTransitions, DFA.getSymbolesTransition(), 0);
    /*
    System.out.println("--> ensembleTotal: "+ensembleTotal);
    System.out.println("--> Transitions actuelles: "+transitions);
    */
    for (Set<String> ens : ensembleTotal){
       Iterator<String> it = ens.iterator();
        while (it.hasNext()) {
          String indexString = it.next();
          int indexInt = Integer.parseInt(indexString);
          if (!(tabTransitions.size() <= indexInt)){
            for (Map.Entry<String, String> entry : tabTransitions.get(indexInt).entrySet()) {
              Set<String> endSet = getSetFromSets(ensembleTotal, entry.getValue());
              if ((ens.size() == 1) && (endSet.size() == 1)){
                if (!transitions.contains(new Transition(indexString, entry.getKey(), entry.getValue()))){
                  transitions.add(new Transition(indexString, entry.getKey(), entry.getValue()));
                }
              } else if ((ens.size() > 1) && (endSet.size() == 1)) {
                if (!transitions.contains(new Transition("\""+ens.toString()+"\"", entry.getKey(), entry.getValue()))){
                  transitions.add(new Transition("\""+ens.toString()+"\"", entry.getKey(), entry.getValue()));
                }
              } else if ((ens.size() == 1) && (endSet.size() > 1)) {
                if (!transitions.contains(new Transition(indexString, entry.getKey(), "\""+endSet.toString()+"\""))){
                  transitions.add(new Transition(indexString, entry.getKey(), "\""+endSet.toString()+"\""));
                }
              } else {
                if (!transitions.contains(new Transition("\""+ens.toString()+"\"", entry.getKey(), "\""+endSet.toString()+"\""))){
                  transitions.add(new Transition("\""+ens.toString()+"\"", entry.getKey(), "\""+endSet.toString()+"\""));
                } 
              }
            }
          }
        }
    }

    //System.out.println("--> Transitions nouvelles: "+transitions);

    // Recherche set de l'etat initial
    for (Set<String> ens : ensembleTotal){
      if (ens.contains("0")){
        List<String> liste = new ArrayList<>(ens);
        initialStates.addAll(liste);
      }
    }

    // Recherche sets des etats finaux
    for (String s : DFA.finalStates){
      for (Set<String> ens : ensembleTotal){
        if (ens.contains(s)){
          List<String> liste = new ArrayList<>(ens);
          finalStates.addAll(liste);
        }
      }
    }

    return this;
  }

  /**
   * 
   * @param setOfSets
   * @param val
   * @return
   */
  public Set<String> getSetFromSets(Set<Set<String>> setOfSets, String val){
    for (Set<String> sets : setOfSets){
      if (sets.contains(val)){
        return sets;
      }
    }

    return null;
  }

  /**
   * 
   * @param path
   * @return
   */
  public int search(String path){
    File directory = new File(path);
    String strLine = "";
    int cpt=0;
    System.out.println("Reading: "+directory.getAbsolutePath());
    try {
        BufferedReader br = new BufferedReader(new FileReader(directory.getAbsolutePath()));
        
        while ((strLine = br.readLine()) != null)
        {
          for (String init : this.initialStates) {
            if(processAuto(init, strLine, false)){
              cpt++;
              System.out.println(strLine);
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

  protected boolean processAuto(String state, String strLine, boolean follow){
    if(state.contains("[")){
      for (int i=0; i<state.length(); i++) {
        if(this.finalStates.contains(""+state.charAt(i))){
          //System.out.println(state+" "+strLine);
          return true;
        }
      }
    } else if(this.finalStates.contains(state)){
      //System.out.println(state+" "+strLine);
      return true;
    }
    if(strLine.isEmpty()) return false;
    ArrayList<Transition> currTransitions = new ArrayList<>();
    
    for (Transition t : this.transitions) {
      if(t.startState.equals(state)) currTransitions.add(t);
    }
    for(Transition t : currTransitions){
      if(follow){
        //System.out.println();
        //System.out.println(currTransitions+"\n"+strLine);
        if(t.transitionSymbol.equals(""+strLine.charAt(0))){
          return processAuto(t.endState, strLine.substring(1,strLine.length()), true);
        }
      }else{
        for(int i=0; i<strLine.length(); i++){
          if(t.transitionSymbol.equals(""+strLine.charAt(i))){
            //System.out.println(strLine.substring(i+1,strLine.length()));
            return processAuto(t.endState, strLine.substring(i+1,strLine.length()), true);
          }
        }
      }
      
    }
    return false;
  }


  /**
   * 
   * 
   * @return
   */
  public String getSymbolesTransition(){
    String res = "";
    for (Transition e : this.getTransitions()){
      if (!res.contains(e.getTransitionSymbol())){
        res += e.getTransitionSymbol();
      }
    }

    return res;
  }

  public ArrayList<String> getInitialStates(){
    return this.initialStates;
  }

  public ArrayList<String> getFinalStates(){
    return this.finalStates;
  }

  public ArrayList<Transition> getTransitions(){
    return this.transitions;
  }

  public void addInitialState(String initial){
    this.initialStates.add(initial); // ArrayList but only one into
  }

  public void addFinalState(String finalS){
    this.finalStates.add(finalS);
  }

  public void addFinalStates(ArrayList<String> listFinalS){
    this.finalStates.addAll(listFinalS);
  }

  public void addTransition(Transition e){
    this.transitions.add(e);
  }

  public void addTransitions(ArrayList<Transition> listE) {
    this.transitions.addAll(listE);
  }

  public void printTransitions(){
    System.out.println("Transitions: ");
    for (Transition e : this.transitions){
      System.out.println(e.toString());
    }
  }
}