package wvw.xai.jena;

import java.util.HashMap;
import java.util.Map;

import wvw.utils.jena.JenaKb;
import wvw.utils.jena.NS;
import wvw.xai.jena.print.DerivationPmlPrinter;

public class JenaExplainer {

	public static void main(String[] args) throws Exception {
		JenaExplainer exp = new JenaExplainer();

//		exp.dumpCase("sleep-apnea", "sa");
//		exp.dumpCase("copd", "copd");

		exp.explainCase("sleep-apnea", "sa", "patient_prov.ttl", "sleep-apnea_all.ttl", "sentence.jena",
				"sa:hasDiagnosis");
//		exp.explainCase("copd", "copd", "patient_prov.ttl", "copd_yellow1.ttl", "sentence.jena", "copd:isStratified");
//		exp.explainCase("copd", "copd", "patient2_prov.ttl", "copd_yellow-red1.ttl", "sentence.jena",
//				"copd:isStratified");
	}

	public void dumpCase(String caseFolder, String prefix) throws Exception {
//		DerivationStringPrinter printer = new DerivationStringPrinter(new PrintWriter(System.out), true);
		DerivationPmlPrinter printer = new DerivationPmlPrinter(prefix, prefix, true);

		Map<String, String> prefixNs = new HashMap<>() {
			private static final long serialVersionUID = 1L;
			{
				put("prov", NS.prov);
				put(prefix, NS.getNsUri(prefix));
				put("", NS.getNsUri(prefix));
			}
		};

		String casePath = "/data/cases/" + caseFolder;
		DumpUseCaseDerivations.dump(printer, prefixNs, casePath + "rules_prov.jena", casePath + "data.ttl",
				casePath + "patient_prov.ttl");

		JenaKb pml = printer.getPml();
		pml.printAll("N3");
	}

	public void explainCase(String caseFolder, String prefix, String patientFile, String derivFile, String genFile,
			String targetPrp) throws Exception {

		Map<String, String> prefixNs = new HashMap<>() {
			private static final long serialVersionUID = 1L;
			{
				put("prov", NS.prov);
				put("pml", NS.pml);
				put("xpl", NS.xpl);
				put(prefix, NS.getNsUri(prefix));
				put("", NS.getNsUri(prefix));
			}
		};

		String casePath = "data/cases/" + caseFolder + "/";
		String[] dataPaths = { "data/labels.ttl", casePath + "data.ttl", casePath + "res.ttl", casePath + patientFile,
				"out/" + derivFile };

		String[] selectPaths = { "data/select/epm.jena", "data/select/trace.jena", "data/select/add-info.jena",
				"data/gen/descr.jena" };

		String[] genPaths = { "data/gen/" + genFile };

		ExplainUseCaseDerivations.explain(prefixNs, dataPaths, selectPaths, genPaths, targetPrp);
	}
}
