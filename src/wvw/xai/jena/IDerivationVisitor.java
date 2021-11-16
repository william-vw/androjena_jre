package wvw.xai.jena;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleFunctorMatch;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation.RuleTripleMatch;

public interface IDerivationVisitor {

	public void visit(InfModel infModel);

	public Object visit(RuleDerivation deriv);

	public Object visit(RuleTripleMatch match);

	public Object visit(RuleFunctorMatch match);
}
