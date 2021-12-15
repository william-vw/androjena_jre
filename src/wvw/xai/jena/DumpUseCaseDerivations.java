package wvw.xai.jena;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import wvw.xai.jena.print.DerivationVisitorBase;

public class DumpUseCaseDerivations {

	public static InfModel dump(DerivationVisitorBase printer, Map<String, String> prefixNs, String rulesPath,
			String... dataPaths) throws Exception {

		AssetManager assetMan = new AssetManager();

		Model model = ModelFactory.createDefaultModel();

		// load data
		for (String dataPath : dataPaths)
			model.read(assetMan.open(dataPath), "", "TURTLE");

		PrintUtil.registerPrefixMap(prefixNs);

		// load & parse rules
		List<Rule> rules = Rule.rulesFromStream(assetMan.open(rulesPath));

		// create inf model
		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		infModel.setDerivationLogging(true);

//		infModel.write(System.out, "TURTLE");
//		infModel.getDeductionsModel().write(System.out, "TURTLE");

		printer.visit(infModel);

		// (print derivations using Derivation#printTrace)
//		testOriginal(infModel);

		return infModel;
	}

	public static void dumpOriginal(InfModel infModel) {
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
}
