package wvw.xai.jena;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

import wvw.utils.jena.JenaKb;
import wvw.utils.jena.NS;

public class JenaExplainer {

	public static void main(String[] args) throws Exception {
		JenaExplainer exp = new JenaExplainer();

//		exp.dumpSleepApneaCase();
		exp.testExplain();
	}

	public void testExplain() throws Exception {
		AssetManager assetMan = getAssets();

		Model model = ModelFactory.createDefaultModel();
		// load data
		model.read(assetMan.open("data/patient_prov.ttl"), "", "TURTLE");

//		model.read(assetMan.open("out/sleep-apnea_a-b2.ttl"), "", "TURTLE");
//		model.read(assetMan.open("out/sleep-apnea_a-c.ttl"), "", "TURTLE");
		model.read(assetMan.open("out/sleep-apnea_all.ttl"), "", "TURTLE");

		PrintUtil.registerPrefix("prov", NS.prov);
		PrintUtil.registerPrefix("pml", NS.pml);
		PrintUtil.registerPrefix("xpl", NS.xpl);
		PrintUtil.registerPrefix("sa", NS.sa);
		PrintUtil.registerPrefix("", NS.sa);

		// load & parse rules
		List<Rule> rules = Rule.rulesFromStream(assetMan.open("data/select/epm.jena"));

//		rules.addAll(Rule.rulesFromStream(assetMan.open("data/select/abstract.jena")));
		rules.addAll(Rule.rulesFromStream(assetMan.open("data/select/trace.jena")));
////		rules.addAll(Rule.rulesFromStream(assetMan.open("data/select/filter-none.jena")));
//		rules.addAll(Rule.rulesFromStream(assetMan.open("data/select/filter-static.jena")));
		rules.addAll(Rule.rulesFromStream(assetMan.open("data/select/add-info.jena")));
//		
		rules.addAll(Rule.rulesFromStream(assetMan.open("data/gen/descr.jena")));

		// create inf model
		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		infModel.add(infModel.createResource(""), infModel.createProperty(NS.uri("xpl:current")),
				infModel.createResource(NS.uri("pml:nodeSet4")));
//				infModel.createResource(NS.uri("pml:nodeSet6")));
//				infModel.createResource(NS.uri("pml:nodeSet10")));

		// create inf model (2)
//		List<Rule> rules2 = Rule.rulesFromStream(assetMan.open("data/gen/sentence.jena"));

		model.read(assetMan.open("data/res.ttl"), "", "TURTLE");
		List<Rule> rules2 = Rule.rulesFromStream(assetMan.open("data/gen/graphic.jena"));

		GenericRuleReasoner reasoner2 = new GenericRuleReasoner(rules2);
		reasoner2.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel2 = ModelFactory.createInfModel(reasoner2, infModel);

		infModel2.write(System.out, "TURTLE");
//		infModel2.getDeductionsModel().write(System.out, "TURTLE");
	}

	public InfModel dumpSleepApneaCase() throws Exception {
		AssetManager assetMan = getAssets();

		Model model = ModelFactory.createDefaultModel();

		// load data
		model.read(assetMan.open("data/patient_prov.ttl"), "", "TURTLE");
		model.read(assetMan.open("data/sleep-apnea.ttl"), "", "TURTLE");

		PrintUtil.registerPrefix("prov", NS.prov);
		PrintUtil.registerPrefix("sa", "http://niche.cs.dal.ca/ns/sleep_apnea.owl#");
		PrintUtil.registerPrefix("", "http://niche.cs.dal.ca/ns/sleep_apnea.owl#");

		// load & parse rules
		List<Rule> rules = Rule.rulesFromStream(assetMan.open("data/sleep-apnea_prov.jena"));

		// create inf model
		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		infModel.setDerivationLogging(true);

//		infModel.write(System.out, "TURTLE");
//		infModel.getDeductionsModel().write(System.out, "TURTLE");

		// - print derivations as strings
		// (should be same as testOriginal)

//		DerivationStringPrinter printer = new DerivationStringPrinter(new PrintWriter(System.out), true);
//		printer.visit(infModel);

		// - print derivations using PML

		String dataUri = "http://niche.cs.dal.ca/ns/sleep_apnea.owl#";
		String rulesUri = "http://niche.cs.dal.ca/ns/sleep_apnea.jena#";

		DerivationPmlPrinter printer = new DerivationPmlPrinter(dataUri, rulesUri, true);
		printer.visit(infModel);

		JenaKb pml = printer.getPml();
		pml.printAll("N3");

		// - print derivations using Derivation#printTrace
//		testOriginal(infModel);

		return infModel;
	}

	public void testOriginal(InfModel infModel) {
		PrintWriter out = new PrintWriter(System.out);
		for (StmtIterator i = infModel.listStatements(null, null, (RDFNode) null); i.hasNext();) {
			Statement s = i.nextStatement();
//			System.out.println(s);

			Iterator<Derivation> it = infModel.getDerivation(s);
			if (it != null) {
				while (it.hasNext()) {
					Derivation deriv = it.next();
					deriv.printTrace(out, true);
				}
			}
		}
		out.flush();
	}

	public AssetManager getAssets() {
		return new AssetManager();
	}

	protected static class AssetManager {

		public InputStream open(String path) throws IOException {
			return new FileInputStream(Paths.get(path).toFile());
		}
	}
}
