/******************************************************************
 * File:        FRuleEngine.java
 * Created by:  Dave Reynolds
 * Created on:  28-May-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: FRuleEngine.java,v 1.1 2009/06/29 08:55:33 castagna Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.Finder;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.ForwardRuleInfGraphI;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleClauseMatch;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleFunctorMatch;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleTripleMatch;
import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * The processing engine for forward production rules. It neeeds to reference an
 * enclosing ForwardInfGraphI which holds the raw data and deductions.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009/06/29 08:55:33 $
 */
public class FRuleEngine implements FRuleEngineI {

	/** The parent InfGraph which is employing this engine instance */
	protected ForwardRuleInfGraphI infGraph;

	/** Set of rules being used */
	protected List<Rule> rules;

	/**
	 * Map from predicate node to rule + clause, Node_ANY is used for wildcard
	 * predicates
	 */
	protected OneToManyMap<Node, ClausePointer> clauseIndex;

	/** List of predicates used in rules to assist in fast data loading */
	protected HashSet<Node> predicatesUsed;

	private List<Rule> nonMonotonicRules = new ArrayList<>();

	/**
	 * Flag, if true then there is a wildcard predicate in the rule set so that
	 * selective insert is not useful
	 */
	protected boolean wildcardRule;

	/** Set to true to flag that derivations should be logged */
	protected boolean recordDerivations;

	/** performance stats - number of rules passing initial trigger */
	int nRulesTriggered = 0;

	/** performance stats - number of rules fired */
	long nRulesFired = 0;

	/** performance stats - number of rules fired during axiom initialization */
	long nAxiomRulesFired = -1;

	/** True if we have processed the axioms in the rule set */
	boolean processedAxioms = false;

	protected static Logger logger = LoggerFactory.getLogger(FRuleEngine.class);

//  =======================================================================
//  Constructors

	/**
	 * Constructor.
	 * 
	 * @param parent the F or FB infGraph that it using this engine, the parent
	 *               graph holds the deductions graph and source data.
	 * @param rules  the rule set to be processed
	 */
	public FRuleEngine(ForwardRuleInfGraphI parent, List<Rule> rules) {
		infGraph = parent;
		this.rules = rules;
	}

	/**
	 * Constructor. Build an empty engine to which rules must be added using
	 * setRuleStore().
	 * 
	 * @param parent the F or FB infGraph that it using this engine, the parent
	 *               graph holds the deductions graph and source data.
	 */
	public FRuleEngine(ForwardRuleInfGraphI parent) {
		infGraph = parent;
	}

//  =======================================================================
//  Control methods

	/**
	 * Process all available data. This should be called once a deductions graph has
	 * be prepared and loaded with any precomputed deductions. It will process the
	 * rule axioms and all relevant existing exiting data entries.
	 * 
	 * @param ignoreBrules set to true if rules written in backward notation should
	 *                     be ignored
	 * @param inserts      the set of triples to be processed, normally this is the
	 *                     raw data graph but may include additional deductions made
	 *                     by preprocessing hooks
	 */
	public void init(boolean ignoreBrules, Finder inserts) {
		if (clauseIndex == null)
			compile(rules, ignoreBrules);
		findAndProcessAxioms();
		nAxiomRulesFired = nRulesFired;
		logger.debug("Axioms fired " + nAxiomRulesFired + " rules");
		fastInit(inserts);
	}

	/**
	 * Process all available data. This version expects that all the axioms have
	 * already be preprocessed and the clause index already exists.
	 * 
	 * @param inserts the set of triples to be processed, normally this is the raw
	 *                data graph but may include additional deductions made by
	 *                preprocessing hooks
	 */
	public void fastInit(Finder inserts) {
		findAndProcessActions();
		// Create the reasoning context
		BFRuleContext context = new BFRuleContext(infGraph);
		// Insert the data
		if (wildcardRule) {
			for (Iterator<Triple> i = inserts.find(new TriplePattern(null, null, null)); i.hasNext();) {
				context.addTriple(i.next());
			}
		} else {
			for (Iterator<Node> p = predicatesUsed.iterator(); p.hasNext();) {
				Node predicate = p.next();
				for (Iterator<Triple> i = inserts.find(new TriplePattern(null, predicate, null)); i.hasNext();) {
					context.addTriple(i.next());
				}
			}
		}
		// Run the engine
		addSet(context);
	}

	/**
	 * Add one triple to the data graph, run any rules triggered by the new data
	 * item, recursively adding any generated triples.
	 */
	public synchronized void add(Triple t) {
		BFRuleContext context = new BFRuleContext(infGraph);
		context.addTriple(t);
		addSet(context);
	}

	/**
	 * Remove one triple to the data graph.
	 * 
	 * @return true if the effects could be correctly propagated or false if not (in
	 *         which case the entire engine should be restarted).
	 */
	public synchronized boolean delete(Triple t) {
		// Incremental delete not supported
		return false;
	}

	/**
	 * Return the number of rules fired since this rule engine instance was created
	 * and initialized
	 */
	public long getNRulesFired() {
		return nRulesFired;
	}

	/**
	 * Return true if the internal engine state means that tracing is worthwhile. It
	 * will return false during the axiom bootstrap phase.
	 */
	public boolean shouldTrace() {
//        return processedAxioms;
		return true;
	}

	/**
	 * Set to true to enable derivation caching
	 */
	public void setDerivationLogging(boolean recordDerivations) {
		this.recordDerivations = recordDerivations;
	}

	/**
	 * Access the precomputed internal rule form. Used when precomputing the
	 * internal axiom closures.
	 */
	public Object getRuleStore() {
		return new RuleStore(clauseIndex, predicatesUsed, wildcardRule);
	}

	/**
	 * Set the internal rule from from a precomputed state.
	 */
	public void setRuleStore(Object ruleStore) {
		RuleStore rs = (RuleStore) ruleStore;
		clauseIndex = rs.clauseIndex;
		predicatesUsed = rs.predicatesUsed;
		wildcardRule = rs.wildcardRule;
	}

//  =======================================================================
//  Internal methods

	/**
	 * Add a set of new triple to the data graph, run any rules triggered by the new
	 * data item, recursively adding any generated triples. Technically the triples
	 * having been physically added to either the base or deduction graphs and the
	 * job of this function is just to process the stack of additions firing any
	 * relevant rules.
	 * 
	 * @param context a context containing a set of new triples to be added
	 */
	public void addSet(BFRuleContext context) {
		Triple t;

		// non-monotonic rules
		while (context.hasNextTriple()) {

			// monotonic rules
			while (context.hasNextTriple()) {
				t = context.getNextTriple();

				if (infGraph.shouldTrace()) {
					logger.info("Processing: " + PrintUtil.print(t));
				}
				// Check for rule triggers
				HashSet<Rule> firedRules = new HashSet<Rule>();
				Iterator<ClausePointer> i1 = clauseIndex.getAll(t.getPredicate());
				Iterator<ClausePointer> i2 = clauseIndex.getAll(Node.ANY);
				Iterator<ClausePointer> i = new ConcatenatedIterator<ClausePointer>(i1, i2);
				while (i.hasNext()) {
					ClausePointer cp = i.next();
					if (firedRules.contains(cp.rule))
						continue;
					context.resetEnv(cp.rule.getNumVars());
					TriplePattern trigger = (TriplePattern) cp.rule.getBodyElement(cp.index);
					if (match(trigger, t, context.getEnvStack())) {
						nRulesTriggered++;
						context.setRule(cp.rule);
						if (matchRuleBody(cp.index, context)) {
							firedRules.add(cp.rule);
							nRulesFired++;
						}
					}
				}
			}

			for (Rule r : nonMonotonicRules) {
//				System.out.println("non-mon: " + r);
				context.resetEnv(r.getNumVars());
				context.setRule(r);
				if (matchRuleBody(context)) {
					nRulesFired++;
				}
			}
		}
	}

	/**
	 * Compile a list of rules into the internal rule store representation.
	 * 
	 * @param rules        the list of Rule objects
	 * @param ignoreBrules set to true if rules written in backward notation should
	 *                     be ignored
	 * @return an object that can be installed into the engine using setRuleStore.
	 */
	public void compile(List<Rule> rules, boolean ignoreBrules) {
		clauseIndex = new OneToManyMap<Node, ClausePointer>();
		predicatesUsed = new HashSet<Node>();
		wildcardRule = false;

		for (Iterator<Rule> i = rules.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (ignoreBrules && r.isBackward())
				continue;

			if (!r.isMonotonic()) {
				nonMonotonicRules.add(r);
			} else {
				Object[] body = r.getBody();
				for (int j = 0; j < body.length; j++) {
					if (body[j] instanceof TriplePattern) {
						Node predicate = ((TriplePattern) body[j]).getPredicate();
						ClausePointer cp = new ClausePointer(r, j);
						if (predicate.isVariable()) {
							clauseIndex.put(Node.ANY, cp);
							wildcardRule = true;
						} else {
							clauseIndex.put(predicate, cp);
							if (!wildcardRule) {
								predicatesUsed.add(predicate);
							}
						}
					}
				}
			}
		}

		if (wildcardRule)
			predicatesUsed = null;
	}

	/**
	 * Scan the rules for any axioms and insert those
	 */
	protected void findAndProcessAxioms() {
		BFRuleContext context = new BFRuleContext(infGraph);
		for (Iterator<Rule> i = rules.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.bodyLength() == 0) {
				// An axiom
				for (int j = 0; j < r.headLength(); j++) {
					Object head = r.getHeadElement(j);
					if (head instanceof TriplePattern) {
						TriplePattern h = (TriplePattern) head;
						Triple t = new Triple(h.getSubject(), h.getPredicate(), h.getObject());
						context.addTriple(t);
						infGraph.getDeductionsGraph().add(t);
					}
				}
			}
		}
		addSet(context);
		processedAxioms = true;
	}

	/**
	 * Scan the rules for any actions and run those
	 */
	protected void findAndProcessActions() {
		BFRuleContext context = new BFRuleContext(infGraph);
		for (Iterator<Rule> i = rules.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.bodyLength() == 0) {
				// An axiom
				for (int j = 0; j < r.headLength(); j++) {
					Object head = r.getHeadElement(j);
					if (head instanceof Functor) {
						Functor f = (Functor) head;
						Builtin imp = f.getImplementor();
						if (imp != null) {
							context.setRule(r);
							imp.headAction(f.getArgs(), f.getArgLength(), context);
						} else {
							throw new ReasonerException(
									"Invoking undefined Functor " + f.getName() + " in " + r.toShortString());
						}
					}
				}
			}
		}
	}

	/**
	 * Match the rest of a set of rule clauses once an initial rule trigger has
	 * fired. Carries out any actions as a side effect.
	 * 
	 * @param trigger the index of the clause which has already be successfully
	 *                matched
	 * @param context a context containing a set of new triples to be added
	 * @return true if the rule actually fires
	 */

	private boolean matchRuleBody(int trigger, BFRuleContext context) {
		Rule r = context.getRule();
		List<ClauseEntry> body = new ArrayList<>(Arrays.asList(r.getBody()));
		body.remove(trigger);

		return matchRuleBody(body, context);
	}

	private boolean matchRuleBody(BFRuleContext context) {
		Rule r = context.getRule();
		List<ClauseEntry> body = new ArrayList<>(Arrays.asList(r.getBody()));

		return matchRuleBody(body, context);
	}

	private boolean matchRuleBody(List<ClauseEntry> body, BFRuleContext context) {
		// Create an ordered list of body clauses to process, best at the end

		int len = body.size();
		List<ClauseEntry> clauses = new ArrayList<ClauseEntry>(len);

		if (len == 1) {
			clauses.add(body.get(0));
			// No clauses to add, just fall through to clause matcher
		} else {
			// Pick most bound remaining clause as the best one to go first
			int bestscore = 0;
			int best = -1;
			for (int i = 0; i < len; i++) {
				BindingStack env = context.getEnvStack();
				if (body.get(i) instanceof TriplePattern) {
					TriplePattern clause = (TriplePattern) body.get(i);
					int score = scoreNodeBoundness(clause.getSubject(), env) * 3
							+ scoreNodeBoundness(clause.getPredicate(), env) * 2
							+ scoreNodeBoundness(clause.getObject(), env) * 3;
					if (score > bestscore) {
						bestscore = score;
						best = i;
					}
				}
			}

			for (int i = 0; i < len; i++) {
				if (i == best)
					continue;
				if (body.get(i) instanceof TriplePattern) {
					clauses.add(body.get(i));
				}
			}
			if (best != -1)
				clauses.add(body.get(best));
		}

		// Call a recursive clause matcher in the ordered list of clauses
		boolean matched = matchClauseList(clauses, context);
		if (matched) {
			// We have new deductions stashed which now want to be
			// asserted as deductions and then added to processing stack
			context.flushPending();
		}
		return matched;
	}

	/**
	 * Match each of a list of clauses in turn. For all bindings for which all
	 * clauses match check the remaining clause guards and fire the rule actions.
	 * 
	 * @param clauses the list of clauses to match, start with last clause
	 * @param context a context containing a set of new triples to be added
	 * @return true if the rule actually fires
	 */
	private boolean matchClauseList(List<ClauseEntry> clauses, BFRuleContext context) {
		Rule rule = context.getRule();
		BindingStack env = context.getEnvStack();
		int index = clauses.size() - 1;
		if (index == -1) {
			// Check any non-pattern clauses
			for (int i = 0; i < rule.bodyLength(); i++) {
				Object clause = rule.getBodyElement(i);
				if (clause instanceof Functor) {
					// Fire a built in
					if (!((Functor) clause).evalAsBodyClause(context)) {
						return false; // guard failed
					}
				}
			}
			// Now fire the rule
			if (infGraph.shouldTrace()) {
				logger.info("Fired rule: " + rule.toShortString() + " = " + rule.instantiate(env));
			}

			List<RuleClauseMatch> matchList = null;
			if (recordDerivations) {
				// Create derivation record
				matchList = new ArrayList<>(rule.bodyLength());
				for (int i = 0; i < rule.bodyLength(); i++) {
					Object clause = rule.getBodyElement(i);
					if (clause instanceof TriplePattern) {
						matchList.add(new RuleTripleMatch(env.instantiate((TriplePattern) clause)));

					} else if (clause instanceof Functor) {
						Functor functor = (Functor) clause;
						String name = functor.getName();
						Node[] args = functor.getBoundArgs(env);

						matchList.add(new RuleFunctorMatch(name, args));
					}
				}
			}
			for (int i = 0; i < rule.headLength(); i++) {
				Object hClause = rule.getHeadElement(i);
				if (hClause instanceof TriplePattern) {
					Triple t = env.instantiate((TriplePattern) hClause);
//					System.out.println("inst: " + t);
					if (!t.getSubject().isLiteral()) {
						// Only add the result if it is legal at the RDF level.
						// E.g. RDFS rules can create assertions about literals
						// that we can't record in RDF
						if (!context.contains(t)) {
							context.add(t);
						}
						// edit wvw (always record derivations; keep duplicate inferences with different
						// derivation trees)
						if (recordDerivations) {
							infGraph.logDerivation(t, new RuleDerivation(rule, t, matchList, infGraph));
						}
					}
				} else if (hClause instanceof Functor) {
					Functor f = (Functor) hClause;
					Builtin imp = f.getImplementor();
					if (imp != null) {
						imp.headAction(f.getBoundArgs(env), f.getArgLength(), context);
					} else {
						throw new ReasonerException(
								"Invoking undefined Functor " + f.getName() + " in " + rule.toShortString());
					}
				} else if (hClause instanceof Rule) {
					Rule r = (Rule) hClause;
					if (r.isBackward()) {
						infGraph.addBRule(r.instantiate(env));
					} else {
						throw new ReasonerException("Found non-backward subrule : " + r);
					}
				}
			}
			return true;
		}
		// More clauses left to match ...
		List<ClauseEntry> clausesCopy = new ArrayList<ClauseEntry>(clauses);
		TriplePattern clause = (TriplePattern) clausesCopy.remove(index);
		Node objPattern = env.getBinding(clause.getObject());
		if (Functor.isFunctor(objPattern)) {
			// Can't search on functor patterns so leave that as a wildcard
			objPattern = null;
		}
		Iterator<Triple> i = infGraph.findDataMatches(env.getBinding(clause.getSubject()),
				env.getBinding(clause.getPredicate()), objPattern);
		boolean foundMatch = false;
		while (i.hasNext()) {
			Triple t = i.next();
			// Add the bindings to the environment
			env.push();
			boolean match = match(clause.getPredicate(), t.getPredicate(), env) && match(clause.getObject(), t.getObject(), env)
					&& match(clause.getSubject(), t.getSubject(), env);
			if (match) {
				foundMatch |= matchClauseList(clausesCopy, context);
			}
			env.unwind();
		}
		return foundMatch;
	}

	/**
	 * Score a Node in terms of groundedness - heuristic. Treats a variable as
	 * better than a wildcard because it constrains later clauses. Treats rdf:type
	 * as worse than any other ground node because that tends to link to lots of
	 * expensive rules.
	 */
	public static int scoreNodeBoundness(Node n, BindingEnvironment env) {
		if (n instanceof Node_ANY) {
			return 0;
		} else if (n instanceof Node_RuleVariable) {
			Node val = env.getGroundVersion(n);
			if (val == null) {
				return 1;
			} else if (val.equals(RDF.type.asNode())) {
				return 2;
			} else {
				return 3;
			}
		} else {
			return 3;
		}
	}

//    /**
//     * Instantiate a triple pattern against the current environment.
//     * This version handles unbound varibles by turning them into bNodes.
//     * @param clause the triple pattern to match
//     * @param env the current binding environment
//     * @return a new, instantiated triple
//     */
//    public static Triple instantiate(TriplePattern pattern, BindingStack env) {
//        Node s = env.getBinding(pattern.getSubject());
//        if (s == null) s = Node.createAnon();
//        Node p = env.getBinding(pattern.getPredicate());
//        if (p == null) p = Node.createAnon();
//        Node o = env.getBinding(pattern.getObject());
//        if (o == null) o = Node.createAnon();
//        return new Triple(s, p, o);
//    }

	/**
	 * Test if a TriplePattern matches a Triple in the given binding environment. If
	 * it does then the binding environment is modified the reflect any additional
	 * bindings.
	 * 
	 * @return true if the pattern matches the triple
	 */
	public static boolean match(TriplePattern pattern, Triple triple, BindingStack env) {
		env.push();
		boolean matchOK = match(pattern.getPredicate(), triple.getPredicate(), env)
				&& match(pattern.getObject(), triple.getObject(), env)
				&& match(pattern.getSubject(), triple.getSubject(), env);
		if (matchOK) {
			env.commit();
			return true;
		} else {
			env.unwind();
			return false;
		}
	}

	/**
	 * Test if a pattern Node matches a Triple Node in the given binding
	 * environment. If it does then the binding environment is modified the reflect
	 * any additional bindings.
	 * 
	 * @return true if the pattern matches the node
	 */
	public static boolean match(Node pattern, Node node, BindingStack env) {
		if (pattern instanceof Node_RuleVariable) {
			int index = ((Node_RuleVariable) pattern).getIndex();
			return env.bind(index, node);
		} else if (pattern instanceof Node_ANY) {
			return true;
		} else if (Functor.isFunctor(pattern)) {
			if (!Functor.isFunctor(node))
				return false;
			Functor patternF = (Functor) pattern.getLiteralValue();
			Functor nodeF = (Functor) node.getLiteralValue();
			if (!patternF.getName().equals(nodeF.getName()))
				return false;
			Node[] patternArgs = patternF.getArgs();
			Node[] nodeArgs = nodeF.getArgs();
//            if (patternF.isGround()) {
//                return Arrays.equals(patternArgs, nodeArgs);
//            } else {
			if (patternArgs.length != nodeArgs.length)
				return false;
			// Compatible functor shapes so bind an embedded variables in the pattern
			env.push();
			boolean matchOK = true;
			for (int i = 0; i < patternArgs.length; i++) {
				if (!match(patternArgs[i], nodeArgs[i], env)) {
					matchOK = false;
					break;
				}
			}
			if (matchOK) {
				env.commit();
				return true;
			} else {
				env.unwind();
				return false;
			}
//            }
		} else {
			return pattern.sameValueAs(node);
		}
	}

//=======================================================================
// Inner classes

	/**
	 * Structure used in the clause index to indicate a particular clause in a rule.
	 * This is used purely as an internal data structure so we just use direct field
	 * access.
	 */
	protected static class ClausePointer {

		/** The rule containing this clause */
		protected Rule rule;

		/** The index of the clause in the rule body */
		protected int index;

		/** constructor */
		ClausePointer(Rule rule, int index) {
			this.rule = rule;
			this.index = index;
		}

		/** Get the clause pointed to */
		TriplePattern getClause() {
			return (TriplePattern) rule.getBodyElement(index);
		}
	}

	/**
	 * Structure used to wrap up processed rule indexes.
	 */
	public static class RuleStore {

		/**
		 * Map from predicate node to rule + clause, Node_ANY is used for wildcard
		 * predicates
		 */
		protected OneToManyMap<Node, ClausePointer> clauseIndex;

		/** List of predicates used in rules to assist in fast data loading */
		protected HashSet<Node> predicatesUsed;

		/**
		 * Flag, if true then there is a wildcard predicate in the rule set so that
		 * selective insert is not useful
		 */
		protected boolean wildcardRule;

		/** Constructor */
		RuleStore(OneToManyMap<Node, ClausePointer> clauseIndex, HashSet<Node> predicatesUsed, boolean wildcardRule) {
			this.clauseIndex = clauseIndex;
			this.predicatesUsed = predicatesUsed;
			this.wildcardRule = wildcardRule;
		}
	}

}

/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard
 * Development Company, LP All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */