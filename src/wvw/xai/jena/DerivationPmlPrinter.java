package wvw.xai.jena;

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

	private String dataUri;
	private String rulesUri;

	private int cnt = 0;

	private JenaKb pml;

	public DerivationPmlPrinter(String dataUri, String rulesUri) {
		this.dataUri = dataUri;
		this.rulesUri = rulesUri;
	}

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
		// (unfortunately, pml:NodeSet is not listed in pml-3.0.ttl,
		// although it is mentioned in the comments)
		Resource nodeSet = pml.resource("pml:nodeSet" + cnt++, pml.resource("pml:NodeSet")); // (prov:Entity)

		// -- conclusion
		Triple conclusion = deriv.getConclusion();

		Resource info = reifiedTripleInfo(conclusion);
		// TODO should not be a sub-property of prov:generated
		// (entity generating an entity)
		pml.add(nodeSet, pml.property("pml:hasConclusion"), info);

		// - InferenceStep

		Resource infStep = pml.blankNode(pml.resource("pml:Derivation")); // pml:InferenceStep, prov:Activity
		pml.add(nodeSet, pml.property("pml:isConsequentOf"), infStep); // prov:wasGeneratedBy

		sourceOfActivity(infStep, rulesUri);

		// -- engine
		Resource engine = pml.resourceFromUri("http://jena.apache.org", pml.resource("pml:InferenceEngine"));

		pml.add(infStep, pml.property("prov:wasAssociatedWith"), engine);

		// -- rule
		Rule rule = deriv.getRule();

		ruleAsPlanOfActivity(infStep, rule, engine);

		// -- antecedents
//		List<Resource> antecedents = new ArrayList<>();

		InfGraph infGraph = (InfGraph) infModel.getGraph();

		List<RuleClauseMatch> matches = deriv.getMatches();
		for (int i = 0; i < matches.size(); i++) {
			RuleClauseMatch match = matches.get(i);

			if (match instanceof RuleTripleMatch) {
				RuleTripleMatch tm = (RuleTripleMatch) match;

				Iterator<Derivation> derivs = infGraph.getDerivation(tm.getTriple());
				if (derivs == null || !derivs.hasNext()) {
					Resource antecedent = visit(tm);

					qualifiedAntecedentOf(infStep, antecedent);
//					antecedents.add(antecedent);

				} else {
					while (derivs.hasNext()) {
						RuleDerivation deriv2 = (RuleDerivation) derivs.next();
						Resource antecedent = visit(deriv2);

						qualifiedAntecedentOf(infStep, antecedent);
//						antecedents.add(antecedent);
					}
				}

			} else {
				RuleFunctorMatch fm = (RuleFunctorMatch) match;
				Resource antecedent = visit(fm);

				qualifiedAntecedentOf(infStep, antecedent);
//				antecedents.add(antecedent);
			}
		}

//		pml.add(infStep, pml.property("pml:hasAntecedentList"),
//				pml.list(antecedents.toArray(new RDFNode[antecedents.size()])));

		derivLog.put(deriv, nodeSet);
		return nodeSet;
	}

	@Override
	public Resource visit(RuleTripleMatch match) {
		if (assertLog.containsKey(match))
			return assertLog.get(match);

		// - NodeSet
		Resource nodeSet = pml.resource("pml:nodeSet" + cnt++, pml.resource("pml:NodeSet")); // (prov:Entity)

		// -- conclusion

		Resource info = reifiedTripleInfo(match.getTriple());
		pml.add(nodeSet, pml.property("pml:hasConclusion"), info); // prov:generated

		// - InferenceStep

		Resource infStep = pml.blankNode(pml.resource("pml:Assertion")); // pml:InferenceStep, prov:Activity
		pml.add(nodeSet, pml.property("pml:isConsequentOf"), infStep); // prov:wasGeneratedBy

		sourceOfActivity(infStep, dataUri);

		assertLog.put(match, nodeSet);

		return nodeSet;
	}

	@Override
	public Resource visit(RuleFunctorMatch match) {
		if (assertLog.containsKey(match))
			return assertLog.get(match);

		Resource nodeSet = pml.resource("pml:nodeSet" + cnt++, pml.resource("pml:NodeSet"));

		// -- conclusion

		Resource info = functorInfo(match.getName(), match.getArgs());
		pml.add(nodeSet, pml.property("pml:hasConclusion"), info);

		// - InferenceStep

		Resource infStep = pml.blankNode(pml.resource("pml:Assertion")); // pml:InferenceStep, prov:Activity
		pml.add(nodeSet, pml.property("pml:isConsequentOf"), infStep); // prov:wasGeneratedBy

		sourceOfActivity(infStep, dataUri);

		assertLog.put(match, nodeSet);

		return nodeSet;
	}

	private void ruleAsPlanOfActivity(Resource infStep, Rule rule, Resource engine) {
		Resource qa = pml.blankNode();

		pml.add(infStep, pml.property("prov:qualifiedAssociation"), qa);
		pml.add(qa, pml.property("prov:agent"), engine);
		pml.add(qa, pml.property("prov:hadPlan"), pml.resource("pml", rule.getName(), pml.resource("pml:Rule")));
	}

	private void qualifiedAntecedentOf(Resource infStep, Resource antecedent) {
		Resource antUsage = pml.blankNode();

//		pml.add(infStep, pml.property("pml:qualifiedAntecedent"), antUsage); // prov:qualifiedUsage
		// personally prefer this for consistency with source provenance
		pml.add(infStep, pml.property("prov:qualifiedUsage"), antUsage);

		pml.add(antUsage, pml.property("prov:entity"), antecedent);
		pml.add(antUsage, pml.property("prov:hadRole"), pml.resource("pml:Antecedent"));
	}

	private void sourceOfActivity(Resource infStep, String uri) {
		Resource srcUsage = pml.blankNode();
		pml.add(infStep, pml.property("prov:qualifiedUsage"), srcUsage);

		pml.add(srcUsage, pml.property("prov:entity"), pml.resource(uri));
		pml.add(srcUsage, pml.property("prov:hadRole"), pml.resource("pml:Source"));
	}

	private Resource reifiedTripleInfo(Triple t) {
		Resource info = pml.resource("pml:info" + cnt++, pml.resource("pml:Information"));
		pml.add(info, pml.property("prov:value"), pml.reify(t));
		pml.add(info, pml.property("dc:format"), pml.literal("RDF"));

		return info;
	}

	private Resource functorInfo(String name, Node[] args) {
		Resource info = pml.resource("pml:info" + cnt++, pml.resource("pml:Information"));

		Resource value = pml.blankNode();
		pml.add(info, pml.property("prov:value"), value);

		pml.add(value, pml.property("pml:functorName"), pml.literal(name));

		RDFNode[] argNodes = Arrays.stream(args).map(a -> pml.getModel().asRDFNode(a))
				.toArray(size -> new RDFNode[size]);
		pml.add(value, pml.property("pml:functorArgs"), pml.list(argNodes));

		pml.add(info, pml.property("dc:format"), pml.literal("functor"));

		return info;
	}

	public JenaKb getPml() {
		return pml;
	}
}
