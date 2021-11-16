package wvw.xai.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleClauseMatch;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleFunctorMatch;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleTripleMatch;

import wvw.utils.jena.JenaKb;

public class DerivationPmlPrinter extends DerivationVisitorBase {

	private int cnt = 0;

	private JenaKb pml;

	@Override
	protected void reset(InfModel infModel) {
		if (pml != null)
			pml.close();

		pml = new JenaKb(ModelFactory.createDefaultModel());

		super.reset(infModel);
	}

	@Override
	public Resource visit(RuleDerivation deriv) {
		if (derivLog.containsKey(deriv))
			return derivLog.get(deriv);

		// - NodeSet
		Resource nodeSet = pml.resource("pml:nodeSet" + cnt++, pml.resource("pml:NodeSet"));

		// - Conclusion
		Triple conclusion = deriv.getConclusion();

		Resource info = reifiedTripleInfo(conclusion);
		pml.add(nodeSet, pml.property("pml:hasConclusion"), info);

		// - InferenceStep

		Resource infStep = pml.blankNode();
		pml.add(nodeSet, pml.property("pml:isConsequentOf"), infStep);

		// -- engine
		pml.add(infStep, pml.property("pml:hasInferenceEngine"), pml.resourceFromUri("http://jena.apache.org"));

		// -- rule
		Rule rule = deriv.getRule();
		pml.add(infStep, pml.property("pml:hasInferenceRule"), pml.resource("pml", rule.getName()));

		// -- antecedents
		List<Resource> antecedents = new ArrayList<>();

		InfGraph infGraph = (InfGraph) infModel.getGraph();

		List<RuleClauseMatch> matches = deriv.getMatches();
		for (int i = 0; i < matches.size(); i++) {
			RuleClauseMatch match = matches.get(i);

			if (match instanceof RuleTripleMatch) {
				RuleTripleMatch tm = (RuleTripleMatch) match;

				Iterator<Derivation> derivs = infGraph.getDerivation(tm.getTriple());
				if (derivs == null || !derivs.hasNext()) {
					Resource antecedent = visit(tm);
					antecedents.add(antecedent);

				} else {
					while (derivs.hasNext()) {
						RuleDerivation deriv2 = (RuleDerivation) derivs.next();
						Resource antecedent = visit(deriv2);

						antecedents.add(antecedent);
					}
				}

			} else {
				RuleFunctorMatch fm = (RuleFunctorMatch) match;
				Resource antecedent = visit(fm);

				antecedents.add(antecedent);
			}
		}

		pml.add(nodeSet, pml.property("pml:hasAntecedentList"),
				pml.list(antecedents.toArray(new RDFNode[antecedents.size()])));

		derivLog.put(deriv, nodeSet);
		return nodeSet;
	}

	@Override
	public Resource visit(RuleTripleMatch match) {
		if (assertLog.containsKey(match))
			return assertLog.get(match);

		Resource nodeSet = pml.resource("pml:nodeSet" + cnt++, pml.resource("pml:NodeSet"));

		Resource info = reifiedTripleInfo(match.getTriple());
		pml.add(nodeSet, pml.property("pml:hasConclusion"), info);

		assertLog.put(match, nodeSet);

		return nodeSet;
	}

	@Override
	public Resource visit(RuleFunctorMatch match) {
		if (assertLog.containsKey(match))
			return assertLog.get(match);

		Resource nodeSet = pml.resource("pml:nodeSet" + cnt++, pml.resource("pml:NodeSet"));

		Resource info = functorInfo(match.getName(), match.getArgs());
		pml.add(nodeSet, pml.property("pml:hasConclusion"), info);

		assertLog.put(match, nodeSet);

		return nodeSet;
	}

	private Resource reifiedTripleInfo(Triple t) {
		Resource info = pml.resource("pml:info" + cnt++, pml.resource("pml:Information"));
		pml.add(info, pml.property("pml:hasReifiedStatement"), pml.reify(t));
		pml.add(info, pml.property("pml:hasFormat"), pml.literal("reified rdf"));

		return info;
	}

	private Resource functorInfo(String name, Node[] args) {
		Resource info = pml.resource("pml:info" + cnt++, pml.resource("pml:Information"));
		pml.add(info, pml.property("pml:hasFunctorName"), pml.literal(name));

		RDFNode[] argNodes = Arrays.stream(args).map(a -> pml.getModel().asRDFNode(a))
				.toArray(size -> new RDFNode[size]);
		pml.add(info, pml.property("pml:hasFunctorArguments"), pml.list(argNodes));

		pml.add(info, pml.property("pml:hasFormat"), pml.literal("functor"));

		return info;
	}

	public JenaKb getPml() {
		return pml;
	}
}
