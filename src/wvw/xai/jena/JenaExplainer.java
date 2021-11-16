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

import wvw.utils.IOUtils;
import wvw.utils.jena.JenaKb;

public class JenaExplainer {

	public static void main(String[] args) throws Exception {
		JenaExplainer exp = new JenaExplainer();

		InfModel sa = exp.sleepApneaCase();

//		exp.test(sa);

//		DerivationStringPrinter printer = new DerivationStringPrinter(new PrintWriter(System.out), true);
//		printer.visit(sa);

		DerivationPmlPrinter printer = new DerivationPmlPrinter();
		printer.visit(sa);
		JenaKb pml = printer.getPml();
		pml.printAll("N3");
	}

	public InfModel sleepApneaCase() throws Exception {
		AssetManager assetMan = getAssets();

		Model model = ModelFactory.createDefaultModel();

		// load data
		model.read(assetMan.open("patient.ttl"), "", "TURTLE");
		model.read(assetMan.open("sleep_apnea.ttl"), "", "TURTLE");

		PrintUtil.registerPrefix("", "http://niche.cs.dal.ca/ns/sleep_apnea.owl#");
		PrintUtil.registerPrefix("sa", "http://niche.cs.dal.ca/ns/sleep_apnea.owl#");

		// load & parse rules
		List<Rule> rules = Rule.parseRules(IOUtils.read(assetMan.open("sleep_apnea.jena")));

		// create inf model
		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		infModel.setDerivationLogging(true);

//		infModel.write(System.out, "TURTLE");

		return infModel;
	}

	public void test(InfModel infModel) {
		PrintWriter out = new PrintWriter(System.out);
		for (StmtIterator i = infModel.listStatements(null, null, (RDFNode) null); i.hasNext();) {
			Statement s = i.nextStatement();
			System.out.println(s);

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
			return new FileInputStream(Paths.get("data/" + path).toFile());
		}
	}
}
