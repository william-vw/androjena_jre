package wvw.xai.jena;

import java.util.HashMap;
import java.util.Map;

import wvw.utils.jena.JenaKb;
import wvw.utils.jena.NS;
import wvw.xai.jena.print.DerivationPmlPrinter;

public class JenaExplainer {

	public static void main(String[] args) throws Exception {
		JenaExplainer exp = new JenaExplainer();

//		exp.dumpSleepApneaCase();
//		exp.dumpCopdCase();

//		exp.explainSleepApneaCase();
//		exp.explainCopdCase("data/cases/copd/patient_prov.ttl", "out/copd_yellow.ttl", "pml:nodeSet20");
		exp.explainCopdCase("data/cases/copd/patient2_prov.ttl", "out/copd_yellow-red1.ttl", "pml:nodeSet0");
	}

	public void dumpSleepApneaCase() throws Exception {
//		DerivationStringPrinter printer = new DerivationStringPrinter(new PrintWriter(System.out), true);
		DerivationPmlPrinter printer = new DerivationPmlPrinter("http://niche.cs.dal.ca/ns/sleep_apnea.owl#",
				"http://niche.cs.dal.ca/ns/sleep_apnea.jena#", true);

		Map<String, String> prefixNs = new HashMap<>() {
			private static final long serialVersionUID = 1L;
			{
				put("prov", NS.prov);
				put("sa", NS.sa);
				put("", NS.sa);
			}
		};

		DumpUseCaseDerivations.dump(printer, prefixNs, "data/cases/sleep-apnea/rules_prov.jena",
				"data/cases/sleep-apnea/data.ttl", "data/cases/sleep-apnea/patient_prov.ttl");

		JenaKb pml = printer.getPml();
		pml.printAll("N3");
	}

	public void dumpCopdCase() throws Exception {
//		DerivationStringPrinter printer = new DerivationStringPrinter(new PrintWriter(System.out), true);
		DerivationPmlPrinter printer = new DerivationPmlPrinter("http://niche.cs.dal.ca/ns/copd.owl#",
				"http://niche.cs.dal.ca/ns/copd.jena#", true);

		Map<String, String> prefixNs = new HashMap<>() {
			private static final long serialVersionUID = 1L;
			{
				put("prov", NS.prov);
				put("copd", NS.copd);
				put("", NS.copd);
			}
		};

		DumpUseCaseDerivations.dump(printer, prefixNs, "data/cases/copd/rules_prov.jena",
				"data/cases/copd/patient2_prov.ttl");

		JenaKb pml = printer.getPml();
		pml.printAll("N3");
	}

	public void explainSleepApneaCase(String patientPath, String derivPath, String curNodeSet) throws Exception {
		Map<String, String> prefixNs = new HashMap<>() {
			private static final long serialVersionUID = 1L;
			{
				put("prov", NS.prov);
				put("pml", NS.pml);
				put("xpl", NS.xpl);
				put("sa", NS.sa);
				put("", NS.sa);
			}
		};

		String[] dataPaths = { "data/cases/sleep-apnea/data.ttl", "data/cases/sleep-apnea/res.ttl", patientPath,
				derivPath };

		String[] selectPaths = { "data/select/epm.jena", "data/select/trace.jena", "data/select/add-info.jena",
				"data/gen/descr.jena" };

		String[] genPaths = { "data/gen/sentence.jena" };

		ExplainUseCaseDerivations.explain(prefixNs, dataPaths, selectPaths, genPaths, curNodeSet);
	}

	public void explainCopdCase(String patientPath, String derivPath, String curNodeSet) throws Exception {
		Map<String, String> prefixNs = new HashMap<>() {
			private static final long serialVersionUID = 1L;
			{
				put("prov", NS.prov);
				put("pml", NS.pml);
				put("xpl", NS.xpl);
				put("copd", NS.copd);
				put("", NS.copd);
			}
		};

		String[] dataPaths = { "data/cases/copd/data.ttl", patientPath, derivPath
//			, "data/cases/copd/res.ttl" 
		};

		String[] selectPaths = { "data/select/epm.jena", "data/select/trace.jena", "data/select/add-info.jena",
				"data/gen/descr.jena" };

		String[] genPaths = { "data/gen/sentence.jena" };

		ExplainUseCaseDerivations.explain(prefixNs, dataPaths, selectPaths, genPaths, curNodeSet);
	}

}
