package com.hp.hpl.jena.reasoner.rulesys.builtins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;

public abstract class CollectAll extends BaseBuiltin {

	/**
	 * Return the expected number of arguments for this functor or 0 if the number
	 * is flexible.
	 */
	@Override
	public int getArgLength() {
		return 0;
	}

	protected List<Node> collectAll(Node root, Node[] properties, RuleContext context) {
		List<Node> expanding = new ArrayList<>();
		expanding.add(root);

		Set<Node> expanded = new HashSet<>();
		for (int i = 0; i < properties.length; i++) {
			Node prp = getArg(i, properties, context);
			// System.out.println("expanding? " + prp + " - " + expanding);

			Iterator<Node> it0 = expanding.iterator();
			while (it0.hasNext()) {
				Node n0 = it0.next();
				it0.remove();

				Iterator<Triple> it = context.getGraph().find(n0, prp, null);
				while (it.hasNext()) {
					Node n = it.next().getObject();
					if (!expanded.contains(n))
						expanded.add(n);
				}
			}

			// System.out.println("expanded? " + expanded);
			expanding.addAll(expanded);
			expanded.clear();
		}
		// System.out.println("");

		return expanding;
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
