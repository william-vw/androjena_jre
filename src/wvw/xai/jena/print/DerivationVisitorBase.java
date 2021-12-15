package wvw.xai.jena.print;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleClauseMatch;

public abstract class DerivationVisitorBase implements IDerivationVisitor {

	protected InfModel infModel;
	protected Map<RuleDerivation, Resource> derivLog = new HashMap<>();
	protected Map<RuleClauseMatch, Resource> assertLog = new HashMap<>();

	@Override
	public void visit(InfModel infModel) {
		reset(infModel);

		StmtIterator stmtIt = infModel.listStatements(null, null, (RDFNode) null);
		while (stmtIt.hasNext()) {
			Statement s = stmtIt.nextStatement();
			// System.out.println(s);

			Iterator<Derivation> derivIt = infModel.getDerivation(s);
			if (derivIt != null) {
				while (derivIt.hasNext()) {

					RuleDerivation deriv = (RuleDerivation) derivIt.next();
					visit(deriv);

					if (derivIt.hasNext())
						System.out.println("INFO multiple derivations: " + s);
					
					// skip multiple derivations
//					break;
				}
			}
		}
	}

	protected void reset(InfModel infModel) {
		this.infModel = infModel;

		derivLog.clear();
		assertLog.clear();
	}
}
