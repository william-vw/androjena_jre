package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;

public class CopyAll extends CollectAll {

	/**
	 * Return a name for this builtin, normally this will be the name of the functor
	 * that will be used to invoke it.
	 */
	public String getName() {
		return "copyAll";
	}

	/**
	 * This method is invoked when the builtin is called in a rule head. Such a use
	 * is only valid in a forward rule.
	 * 
	 * @param args    the array of argument values for the builtin, this is an array
	 *                of Nodes.
	 * @param length  the length of the argument list, may be less than the length
	 *                of the args array for some rule engines
	 * @param context an execution context giving access to other relevant data
	 */
	public void headAction(Node[] args, int length, RuleContext context) {
		if (length < 3)
			throw new BuiltinException(this, context, "Must have at least 3 arguments to " + getName());

		Node targetNode = getArg(0, args, context);
		Node targetPrp = getArg(1, args, context);

		Node root = getArg(2, args, context);
		Node[] properties = new Node[args.length - 3];
		System.arraycopy(args, 3, properties, 0, properties.length);

		collectAll(root, properties, context);

		// let's do this behind the inf-graph's back
		Graph g = ((InfGraph) context.getGraph()).getRawGraph();
		for (Node node : nodes) {
			g.add(new Triple(targetNode, targetPrp, node));
		}
	}
}