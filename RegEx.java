import java.util.Scanner;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.*; 
import java.lang.*;
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
  
  //CONSTRUCTOR
  public RegEx(){}

  //MAIN
  public static void main(String arg[]) {
    System.out.println("Welcome to Bogota, Mr. Thomas Anderson.");
    if (arg.length!=0) {
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
        RegExTree ret = parse();
        System.out.println("  >> Tree result: "+ret.toString()+".");

        Automata resSyntaxTree = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()); 
        resSyntaxTree = resSyntaxTree.toSyntaxTree(ret);

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




        Automata res = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        res = res.toNDFA(ret);
        
        writer = new FileWriter("automate.dot");
			  writer.write("digraph {\nrankdir=LR;\n");
        for (String s : res.finalStates){
          writer.write("\t" + s + " [shape=doublecircle]\n");
        }

        writer.write("\n");

        for (Automata.Transition t : res.transitions){
          writer.write("\t" + t.getStartState() + "->" + t.getEndState() + " [label= \"" +t.getTransitionSymbol() + "\"];\n");
        }
        
			  writer.write("}\n");
        writer.close();

		    try {
			    process = Runtime.getRuntime().exec("dot -Tpng automate.dot -o automate.png");
          process.waitFor();
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        System.err.println("  >> ERROR: syntax error for regEx \""+regEx+"\". PONG" );
      }
    }

    System.out.println("  >> ...");
    System.out.println("  >> Parsing completed.");
    System.out.println("Goodbye Mr. Anderson.");
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
      transitions.add(new Transition(initialStates.get(initialStates.size()-1), null, tree.rootToString()+"__"+(numberStates++)));
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
        System.out.println("====> Initial states: "+initialStates);
        System.out.println("====> Final states: "+finalStates);
        String lastInitialEtat = initialStates.get(initialStates.size()-1);
        String lastFinalEtat = finalStates.get(finalStates.size()-1);
        System.out.println("lastInitialEtat: "+lastInitialEtat);
        System.out.println("lastFinalEtat: "+lastFinalEtat);
        transitions.add(new Transition(lastFinalEtat, "ε\", constraint=\"false", lastInitialEtat));
      } else {
        System.out.println("===> negliger?: "+tree.rootToString());
      }
    }  
    return resAutomata;
  }

  public ArrayList<String> getInitialStates(){
    return this.initialStates;
  }

  public ArrayList<String> getFinalStates(){
    return this.finalStates;
  }

  public void printTransitions(){
    for (Transition e : this.transitions){
      System.out.println(e.toString());
    }
  }
}
