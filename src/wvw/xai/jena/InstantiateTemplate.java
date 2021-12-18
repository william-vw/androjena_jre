package wvw.xai.jena;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.ResultSet;

import wvw.utils.IOUtils;

public class InstantiateTemplate {

	private String template;

	// assumed both are the same size
	private String startDelim = "|~";
	private String endDelim = "~|";

	private Pattern varPat = Pattern.compile("\\?([a-zA-Z0-9]+)\\s+\\|~");
	private Pattern labelPat = Pattern.compile("'([a-zA-Z0-9]+)'\\s+\\|~");

	private Map<String, String> varMap = new HashMap<>();
	private Map<String, String> labelMap = new HashMap<>();

	public InstantiateTemplate(String templatePath) throws ExplException {
		AssetManager assetMan = new AssetManager();

		try {
			template = IOUtils.readStr(assetMan.open(templatePath), false);

		} catch (IOException e) {
			throw new ExplException("error reading template", e);
		}

		processNextCodes(varPat, varMap);
		processNextCodes(labelPat, labelMap);
	}

	private void processNextCodes(Pattern pat, Map<String, String> map) throws ExplException {
		Matcher m = pat.matcher(template);
		while (m.find()) {
			String name = m.group(1);
			String code = getDelimitedCode(m.end());

			System.out.println("name? '" + name + "'");
			System.out.println(code);

			map.put(name, code);
		}
	}

	private String getDelimitedCode(int fromIdx) throws ExplException {
		int cnt = 1;

		int delimLen = startDelim.length();
		int idx = fromIdx;
		while (idx < template.length() - (delimLen - 1)) {
			String str = template.substring(idx, idx + delimLen);

			if (str.equals(startDelim)) {
				cnt++;

			} else if (str.equals(endDelim)) {
				cnt--;
			}

			if (cnt == 0)
				break;
			idx++;
		}

		if (cnt != 0)
			throw new ExplException("unmatched delimiters (count = " + cnt + ")");

		return template.substring(fromIdx, idx);
	}

	public void instantiate(Model m, ResultSet results) {

	}

	public static void main(String[] args) throws Exception {
		InstantiateTemplate it = new InstantiateTemplate("data/templ/graphic.html");
	}
}
