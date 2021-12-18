package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;

public class CountEachLinked extends CollectAll {

	// count = allowed branching factor per "hop" in the graph

	// TODO in practice this is only useful when this is "1" - more useful would be
	// "count" being the final number of branches per first-level node
	// (i.e., first set of nodes after the root node)

	private int count;

	private int cur = 0;

	/**
	 * Return a name for this builtin, normally this will be the name of the functor
	 * that will be used to invoke it.
	 */
	public String getName() {
		return "countEachLinked";
	}

	/**
	 * This method is invoked when the builtin is called in a rule body.
	 * 
	 * @param args    the array of argument values for the builtin, this is an array
	 *                of Nodes, some of which may be Node_RuleVariables.
	 * @param length  the length of the argument list, may be less than the length
	 *                of the args array for some rule engines
	 * @param context an execution context giving access to other relevant data
	 * @return return true if the buildin predicate is deemed to have succeeded in
	 *         the current environment
	 */
	@Override
	public boolean bodyCall(Node[] args, int length, RuleContext context) {
		if (length < 3)
			throw new BuiltinException(this, context, "Must have at least 3 arguments to " + getName());

		Node root = getArg(0, args, context);

		count = (Integer) getArg(args.length - 1, args, context).getLiteralValue();

		Node[] properties = new Node[args.length - 2];
		System.arraycopy(args, 1, properties, 0, properties.length);

		return collectAll(root, properties, context);
	}

//	private Node n0;
//	private Node prp;

	@Override
	protected void expanding(Node n, Node property) {
		cur = 0;

//		n0 = n;
//		prp = property;
	}

	@Override
	protected boolean expanded(Node n, int iteration) {
		cur++;

//		if (iteration > 0)
//			System.out.println("countEach: " + n0 + " - " + prp + " - " + cur);

		return !(iteration > 0 && cur > count);
	}
}