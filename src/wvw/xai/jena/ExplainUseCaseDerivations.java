package wvw.xai.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

import wvw.utils.jena.NS;

public class ExplainUseCaseDerivations {

	public static void explain(Map<String, String> prefixNs, String[] dataPaths, String[] selectPaths,
			String[] genPaths, String targetPrp) throws Exception {

		AssetManager assetMan = new AssetManager();

		Model model = ModelFactory.createDefaultModel();
		// load data
		for (String dataPath : dataPaths) {
			long start = System.currentTimeMillis();
			model.read(assetMan.open(dataPath), "", "TURTLE");
			long end = System.currentTimeMillis();
			System.out.println("load " + dataPath + ": " + (end - start) + "ms");
		}

		PrintUtil.registerPrefixMap(prefixNs);

		long start = System.currentTimeMillis();

		// load & parse rules
		List<Rule> rules = new ArrayList<>();
		for (String selectPath : selectPaths)
			rules.addAll(Rule.rulesFromStream(assetMan.open(selectPath)));

		// create inf model
		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel = ModelFactory.createInfModel(reasoner, model);

		RDFNode nodeSet = nodeSetWithConclusion(targetPrp, infModel);
		infModel.add(infModel.createResource(""), infModel.createProperty(NS.uri("xpl:current")), nodeSet);

		long end = System.currentTimeMillis();
		System.out.println("inf model 1: " + (end - start) + "ms");

		// create inf model (2)
		start = System.currentTimeMillis();

		List<Rule> rules2 = new ArrayList<>();
		for (String genPath : genPaths)
			rules2.addAll(Rule.rulesFromStream(assetMan.open(genPath)));

		GenericRuleReasoner reasoner2 = new GenericRuleReasoner(rules2);
		reasoner2.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel2 = ModelFactory.createInfModel(reasoner2, infModel);

		end = System.currentTimeMillis();
		System.out.println("inf model 2: " + (end - start) + "ms");

		infModel2.write(System.out, "TURTLE");
//		infModel2.getDeductionsModel().write(System.out, "TURTLE");
	}

	private static RDFNode nodeSetWithConclusion(String prp, InfModel infModel) {
		StmtIterator it = infModel.listStatements(null, infModel.createProperty(NS.uri("rdf:predicate")),
				infModel.createResource(NS.uri(prp)));

		Resource stmt = null;
		while (it.hasNext()) {
			stmt = it.next().getSubject();

			Resource object = stmt.getProperty(infModel.createProperty(NS.uri("rdf:object"))).getResource();
			if (!object.hasProperty(infModel.createProperty(NS.uri("xpl:hasHigherStratum")))) {
				break;
			}
		}

		Resource info = (Resource) infModel.listStatements(null, infModel.createProperty(NS.uri("prov:value")), stmt)
				.next().getSubject();

		return infModel.listStatements(null, infModel.createProperty(NS.uri("pml:hasConclusion")), info).next()
				.getSubject();

	}
}
