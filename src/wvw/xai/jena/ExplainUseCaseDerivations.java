package wvw.xai.jena;

import java.io.IOException;
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
			String[] genPaths, String targetPrp) throws ExplException {

		AssetManager assetMan = new AssetManager();
		Model model = ModelFactory.createDefaultModel();

		try {
			// load data
			for (String dataPath : dataPaths) {
				long start = System.currentTimeMillis();
				model.read(assetMan.open(dataPath), "", "TURTLE");
				long end = System.currentTimeMillis();
				System.out.println("load " + dataPath + ": " + (end - start) + "ms");
			}

		} catch (IOException e) {
			throw new ExplException("error loading data", e);
		}

		PrintUtil.registerPrefixMap(prefixNs);

		long start = System.currentTimeMillis();

		// load & parse rules
		List<Rule> rules = new ArrayList<>();
		try {
			for (String selectPath : selectPaths)
				rules.addAll(Rule.rulesFromStream(assetMan.open(selectPath)));
			
			for (String genPath : genPaths)
				rules.addAll(Rule.rulesFromStream(assetMan.open(genPath)));

		} catch (IOException e) {
			throw new ExplException("error loading rules", e);
		}

		// create inf model
		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel = ModelFactory.createInfModel(reasoner, model);

		RDFNode nodeSet = nodeSetWithConclusion(targetPrp, infModel);
		infModel.add(infModel.createResource(""), infModel.createProperty(NS.uri("xpl:current")), nodeSet);

//		long end = System.currentTimeMillis();
//		System.out.println("inf model 1: " + (end - start) + "ms");
//
//		// create inf model (2)
//		start = System.currentTimeMillis();
//
//		List<Rule> rules2 = new ArrayList<>();
//		try {
//			for (String genPath : genPaths)
//				rules2.addAll(Rule.rulesFromStream(assetMan.open(genPath)));
//
//		} catch (IOException e) {
//			throw new ExplException("error loading rules", e);
//		}
//
//		GenericRuleReasoner reasoner2 = new GenericRuleReasoner(rules2);
//		reasoner2.setMode(GenericRuleReasoner.FORWARD);
//
//		InfModel infModel2 = ModelFactory.createInfModel(reasoner2, infModel);

		long end = System.currentTimeMillis();
		System.out.println("inf model: " + (end - start) + "ms");

		infModel.write(System.out, "TURTLE");
//		infModel.getDeductionsModel().write(System.out, "TURTLE");
	}

	private static RDFNode nodeSetWithConclusion(String prp, InfModel infModel) throws ExplException {
		Resource prpRes = infModel.createResource(NS.uri(prp));
		StmtIterator it = infModel.listStatements(null, infModel.createProperty(NS.uri("rdf:predicate")), prpRes);

		if (!it.hasNext())
			throw new ExplException("no conclusion found with property '" + prpRes + "'");

		Resource stmt = null;
		while (it.hasNext()) {
			stmt = it.next().getSubject();

			Resource object = stmt.getProperty(infModel.createProperty(NS.uri("rdf:object"))).getResource();
			if (!object.hasProperty(infModel.createProperty(NS.uri("xpl:hasHigherStratum")))) {
				break;
			}
		}

		Resource info = infModel.listStatements(null, infModel.createProperty(NS.uri("prov:value")), stmt).next()
				.getSubject();
		return infModel.listStatements(null, infModel.createProperty(NS.uri("pml:hasConclusion")), info).next()
				.getSubject();

	}
}
