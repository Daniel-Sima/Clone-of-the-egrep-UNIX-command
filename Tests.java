import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class Tests {

	@org.junit.jupiter.api.Test
	void test_regex_1() {
		RegEx regEx = new RegEx("a|bc*");
		try {
			@SuppressWarnings("static-access")
			RegExTree ret = regEx.parse();

			/*********************************************************/
			/******************* Test Arbre **************************/
			/*********************************************************/
			Automata resSyntaxTree = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			resSyntaxTree = resSyntaxTree.toSyntaxTree(ret);

			// Car pas de etat initiaux ou finaux dans l'arbre
			assertEquals(new ArrayList<String>(), resSyntaxTree.getInitialStates());
			assertEquals(new ArrayList<String>(), resSyntaxTree.getFinalStates());

			ArrayList<Automata.Transition> transitions = new ArrayList<>();
			Automata automata = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			transitions.add(automata.new Transition("\"|__0\"", null, "\"a__1\""));
			transitions.add(automata.new Transition("\"|__0\"", null, "\".__2\""));
			transitions.add(automata.new Transition("\".__2\"", null, "\"b__3\""));
			transitions.add(automata.new Transition("\".__2\"", null, "\"*__4\""));
			transitions.add(automata.new Transition("\"*__4\"", null, "\"c__5\""));
			automata.addTransitions(transitions);
			assertEquals(automata.getTransitions(), resSyntaxTree.getTransitions());

			/*********************************************************/
			/******************* Test NDFA ***************************/
			/*********************************************************/
			Automata res = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			res = res.toNDFA(ret);

			ArrayList<String> initial = new ArrayList<String>();
			initial.add("8");
			ArrayList<String> finals = new ArrayList<String>();
			finals.add("9");
			assertEquals(initial, res.getInitialStates());
			assertEquals(finals, res.getFinalStates());

			transitions = new ArrayList<>();
			transitions.add(automata.new Transition("0", "a", "1"));
			transitions.add(automata.new Transition("2", "b", "3"));
			transitions.add(automata.new Transition("4", "c", "5"));
			transitions.add(automata.new Transition("5", "ε", "4"));
			transitions.add(automata.new Transition("6", "ε", "4"));
			transitions.add(automata.new Transition("5", "ε", "7"));
			transitions.add(automata.new Transition("6", "ε", "7"));
			transitions.add(automata.new Transition("3", "ε", "6"));
			transitions.add(automata.new Transition("8", "ε", "0"));
			transitions.add(automata.new Transition("8", "ε", "2"));
			transitions.add(automata.new Transition("1", "ε", "9"));
			transitions.add(automata.new Transition("7", "ε", "9"));
			automata.addTransitions(transitions);
			assertEquals(transitions, res.getTransitions());

			/*********************************************************/
			/******************* Test DFA ****************************/
			/*********************************************************/
			Automata resDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			resDFA = resDFA.toDFA(res);

			initial = new ArrayList<String>();
			initial.add("0");
			finals = new ArrayList<String>();
			finals.add("1");
			finals.add("2");
			finals.add("3");
			assertEquals(initial, resDFA.getInitialStates());
			assertEquals(finals, resDFA.getFinalStates());

			transitions = new ArrayList<>();
			transitions.add(automata.new Transition("0", "a", "1"));
			transitions.add(automata.new Transition("0", "b", "2"));
			transitions.add(automata.new Transition("2", "c", "3"));
			transitions.add(automata.new Transition("3", "c", "3"));
			automata.addTransitions(transitions);
			assertEquals(transitions, resDFA.getTransitions());

			/*********************************************************/
			/******************* Test Min-DFA ************************/
			/*********************************************************/
			Automata resMDFA = new Automata(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			resMDFA = resMDFA.toMinDFA(resDFA);
			
			initial = new ArrayList<String>();
			initial.add("0");
			finals = new ArrayList<String>();
			finals.add("1");
			finals.add("[2, 3]");
			assertEquals(initial, resMDFA.getInitialStates());
			assertEquals(finals, resMDFA.getFinalStates());
			
			transitions = new ArrayList<>();
			transitions.add(automata.new Transition("0", "a", "1"));
			transitions.add(automata.new Transition("0", "b", "\"[2, 3]\""));
			transitions.add(automata.new Transition("\"[2, 3]\"", "c", "\"[2, 3]\""));
			automata.addTransitions(transitions);
			assertEquals(transitions, resMDFA.getTransitions());

		} catch (Exception e) {
			e.printStackTrace();
		}

//		fail("pas encore");
	}

}
