package wvw.xai.jena;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleClauseMatch;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleFunctorMatch;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleTripleMatch;
import com.hp.hpl.jena.util.PrintUtil;

public class DerivationStringPrinter extends DerivationVisitorBase {

	private int indent = 0;
	private PrintWriter out;
	private boolean bindings;

	public DerivationStringPrinter(PrintWriter out, boolean bindings) {
		this.out = out;

		this.bindings = bindings;
	}

	@Override
	protected void reset(InfModel infModel) {
		indent = 0;

		super.reset(infModel);
	}

	@Override
	public void visit(InfModel infModel) {
		super.visit(infModel);

		out.flush();
	}

	@Override
	public Object visit(RuleDerivation deriv) {
		Triple conclusion = deriv.getConclusion();
		List<RuleClauseMatch> matches = deriv.getMatches();

		PrintUtil.printIndent(out, indent);
		out.print(deriv.toString());

		if (bindings) {
			out.print(" concluded " + PrintUtil.print(conclusion));
		}
		out.println(" <-");

		InfGraph infGraph = (InfGraph) infModel.getGraph();

		indent += 4;

		for (int i = 0; i < matches.size(); i++) {
			RuleClauseMatch match = matches.get(i);

			if (match instanceof RuleTripleMatch) {
				RuleTripleMatch tm = (RuleTripleMatch) match;

				Iterator<Derivation> derivations = infGraph.getDerivation(tm.getTriple());
				if (derivations == null || !derivations.hasNext()) {
					PrintUtil.printIndent(out, indent);
					visit(tm);

				} else {
					while (derivations.hasNext()) {
						RuleDerivation deriv2 = (RuleDerivation) derivations.next();
						if (derivLog.containsKey(deriv2)) {
							PrintUtil.printIndent(out, indent);
							out.println("Known " + PrintUtil.print(match) + " - already shown");
						} else {
							derivLog.put(deriv2, null);
							visit(deriv2);
						}
					}
				}

			} else {
				RuleFunctorMatch fm = (RuleFunctorMatch) match;

				PrintUtil.printIndent(out, indent);
				visit(fm);
			}
		}

		indent -= 4;

		return null;
	}

	@Override
	public Object visit(RuleTripleMatch match) {
		out.println("Fact " + PrintUtil.print(match.getTriple()));

		return null;
	}

	@Override
	public Object visit(RuleFunctorMatch match) {
		RuleFunctorMatch fm = (RuleFunctorMatch) match;

		out.println("Functor " + fm.getName() + "("
				+ Arrays.stream(fm.getArgs()).map(a -> PrintUtil.print(a)).collect(Collectors.joining(",")) + ")");

		return null;
	}
}
