package wvw.utils.jena;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NS {

	public static Map<String, String> map;

	public static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String owl = "http://www.w3.org/2002/07/owl#";
	public static final String dc = "http://purl.org/dc/terms/";
	public static final String fhir = "http://hl7.org/fhir/";
	public static final String log = "http://www.w3.org/2000/10/swap/log#";
	public static final String state = "http://niche.cs.dal.ca/ns/state.owl#";
	public static final String cond = "http://niche.cs.dal.ca/ns/condition/base.owl#";
	public static final String wf = "http://niche.cs.dal.ca/ns/workflow/base.owl#";
	public static final String cig = "http://niche.cs.dal.ca/ns/cig/cig.owl#";
	public static final String in = "http://niche.cs.dal.ca/ns/cig/input/fhir.owl#";
	public static final String ckd = "http://niche.cs.dal.ca/ns/cig/kidney_statins.owl#";
	public static final String pml = "http://provenanceweb.org/ns/pml#";
	public static final String sa = "http://niche.cs.dal.ca/ns/sleep_apnea.owl#";

	static {
		map = new HashMap<String, String>();

		map.put("rdf", rdf);
		map.put("rdfs", rdfs);
		map.put("owl", owl);
		map.put("dc", dc);
		map.put("fhir", fhir);
		map.put("log", log);
		map.put("state", state);
		map.put("cond", cond);
		map.put("wf", wf);
		map.put("cig", cig);
		map.put("in", in);
		map.put("ckd", ckd);
		map.put("pml", pml);
		map.put("sa", sa);
	}

	public static String toQname(String uri) {
		Iterator<String> nsIt = map.keySet().iterator();
		while (nsIt.hasNext()) {
			String prefix = nsIt.next();
			String ns = map.get(prefix);

			if (uri.contains(ns))
				return uri.replace(ns, prefix + ":");
		}

		return uri;
	}

	public static String getNsUri(String ns) {
		if (map.containsKey(ns))
			return map.get(ns);
		else
			return null;
	}

	public static String toUri(String uri) {
		if (!uri.contains(":"))
			return null;

		String prefix = uri.substring(0, uri.indexOf(":"));
		String ln = uri.substring(uri.indexOf(":") + 1);

		Iterator<String> nsIt = map.keySet().iterator();
		while (nsIt.hasNext()) {
			String nsPrefix = nsIt.next();
			String nsUri = map.get(prefix);

			if (prefix.equals(nsPrefix))
				return nsUri + ln;
		}

		return uri;
	}

	public static boolean containsNsUri(String uri) {
		Iterator<String> nsIt = map.keySet().iterator();
		while (nsIt.hasNext()) {
			String prefix = nsIt.next();
			String ns = map.get(prefix);

			if (uri.startsWith(ns))
				return true;
		}

		return false;
	}

	public static boolean containsNsPrefix(String qName) {
		Iterator<String> nsIt = map.keySet().iterator();
		while (nsIt.hasNext()) {
			String prefix = nsIt.next();

			if (qName.startsWith(prefix + ":"))
				return true;
		}

		return false;
	}

	public static String localName(String uri) {
		int idx = uri.lastIndexOf("/");
		int idx2 = uri.lastIndexOf("#");

		if (idx2 > idx)
			return uri.substring(idx2 + 1);
		else
			return uri.substring(idx + 1);
	}
}
