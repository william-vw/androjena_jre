package com.hp.hpl.jena.reasoner.rulesys.builtins;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;

public class GetAny extends CollectAll {

	/**
	 * Return a name for this builtin, normally this will be the name of the functor
	 * that will be used to invoke it.
	 */
	public String getName() {
		return "getAny";
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

		Node[] properties = new Node[args.length - 2];
		System.arraycopy(args, 1, properties, 0, properties.length);

		List<Node> nodes = collectAll(root, properties, context);
		
		Node result = (!nodes.isEmpty() ? nodes.get(0) : Node.createLiteral(""));
		return context.getEnv().bind(args[args.length - 1], result);
	}
}