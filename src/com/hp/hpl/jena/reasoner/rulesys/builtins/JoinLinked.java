package com.hp.hpl.jena.reasoner.rulesys.builtins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;

public class JoinLinked extends CollectAll {

	/**
	 * Return a name for this builtin, normally this will be the name of the functor
	 * that will be used to invoke it.
	 */
	public String getName() {
		return "joinLinked";
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
		if (length < 5)
			throw new BuiltinException(this, context, "Must have at least 5 arguments to " + getName());

		Node root = getArg(0, args, context);
		String separator1 = getArg(1, args, context).getLiteralValue().toString();
		String separator2 = getArg(2, args, context).getLiteralValue().toString();
		String separator3 = getArg(3, args, context).getLiteralValue().toString();

		Node[] properties = new Node[args.length - 5];
		System.arraycopy(args, 4, properties, 0, properties.length);

		List<Node> nodes = collectAll(root, properties, context);

		// if needed, wait until all data is available
		if (nodes.isEmpty())
			return false;

		List<String> strs = new ArrayList<>();

		Iterator<Node> it = nodes.iterator();
		while (it.hasNext()) {
			String str = lex(it.next(), context);
			if (!str.isBlank())
				strs.add(str);
		}

		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < strs.size(); i++) {
			String str = strs.get(i);

			if (nodes.size() > 2 && i == nodes.size() - 1)
				buff.append(separator2);
			else if (i > 0) {
				if (nodes.size() == 2)
					buff.append(separator3);
				else
					buff.append(separator1);
			}

			buff.append(str);
		}
		Node result = Node.createLiteral(buff.toString());
		return context.getEnv().bind(args[length - 1], result);
	}

	/**
	 * Return the appropriate lexical form of a node
	 */
	protected String lex(Node n, RuleContext context) {
		if (n.isBlank()) {
			return n.getBlankNodeLabel();
		} else if (n.isURI()) {
			return n.getURI();
		} else if (n.isLiteral()) {
			return n.getLiteralLexicalForm();
		} else {
			throw new BuiltinException(this, context, "Illegal node type: " + n);
		}
	}

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP All
 * rights reserved.
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
