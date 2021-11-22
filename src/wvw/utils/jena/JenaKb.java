package wvw.utils.jena;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import wvw.utils.IOUtils;

public class JenaKb {

	private Model model;

	public JenaKb(Model model) {
		this.model = model;
	}

	public JenaKb from(String base, String... paths) throws IOException {
		for (String path : paths)
			model.read(new FileInputStream(path), base);

		return this;
	}

	@SuppressWarnings("rawtypes")
	public JenaKb fromClsRes(Class cls, String base, String... paths) throws IOException, URISyntaxException {

		for (String path : paths)
			model.read(IOUtils.getResourceInputStream(cls, path), base);

		return this;
	}

	public JenaKb from(String base, InputStream in) {
		model.read(in, base);

		return this;
	}

	public JenaKb fromString(String base, String rdf) {
		return from(base, new ByteArrayInputStream(rdf.getBytes()));
	}

	public Model getModel() {
		return model;
	}

	public Resource resource() {
		return model.createResource();
	}

	public Resource resource(String ns, String ln) {
		return model.createResource(NS.getNsUri(ns) + ln);
	}

	public Resource resource(String qName) {
		return model.createResource(NS.toUri(qName));
	}

	public Resource resourceFromUri(String uri) {
		return model.createResource(uri);
	}

	public Resource resource(Resource type) {
		return model.createResource(type);
	}

	public Resource resource(String ns, String ln, Resource type) {
		return model.createResource(NS.getNsUri(ns) + ln, type);
	}

	public Resource resource(String qName, Resource type) {
		return model.createResource(NS.toUri(qName), type);
	}

	public Resource resourceFromUri(String uri, Resource type) {
		return model.createResource(uri, type);
	}

	public Resource blankNode() {
		return model.createResource(new AnonId());
	}
	
	public Resource blankNode(Resource type) {
		Resource ret = model.createResource(new AnonId());
		model.add(ret, RDF.type, type);
		
		return ret;
	}

	public Resource blankNode(String id) {
		return model.createResource(new AnonId(id));
	}
	
	public Resource blankNode(String id, Resource type) {
		Resource ret = model.createResource(new AnonId(id));
		model.add(ret, RDF.type, type);
		
		return ret;
	}
	
	public Resource blankNode(AnonId id) {
		return model.createResource(id);
	}

	public Property property(String qName) {
		return model.createProperty(NS.toUri(qName));
	}

	public Property propertyFromUri(String uri) {
		return model.createProperty(uri);
	}

	public Literal literal(String value) {
		return model.createLiteral(value);
	}

	public Literal literal(java.sql.Date date) {
		return model.createTypedLiteral(toCal(date));
	}

	public Literal literal(Object value) {
		return model.createTypedLiteral(value);
	}

	public RDFList list(RDFNode... elements) {
		return model.createList(elements);
	}

	public Resource toResource(Node arg) {
		if (arg.isURI())
			return resourceFromUri(arg.getURI());
		else
			return blankNode(arg.getBlankNodeId());
	}

	public StmtIterator list(Resource s, Property p, RDFNode o) {
		return model.listStatements(s, p, o);
	}

	public List<Statement> listAll() {
		return model.listStatements().toList();
	}

	public boolean contains(Resource s, Property p, RDFNode o) {
		return model.contains(s, p, o);
	}

	public Statement statement(Triple t) {
		return model.asStatement(t);
	}

	public ReifiedStatement reify(Statement s) {
		return model.createReifiedStatement(s);
	}

	public ReifiedStatement reify(Triple t) {
		return model.createReifiedStatement(statement(t));
	}

	public void add(Statement stmt) {
		model.add(stmt);
	}

	public void add(Resource s, Property p, RDFNode o) {
		model.add(s, p, o);
	}

	public void add(Resource s, Property p, Object o) {
		model.addLiteral(s, p, model.createTypedLiteral(o));
	}

	public void addAll(List<Statement> stmts) {
		model.add(stmts);
	}

	public void remove(Statement stmt) {
		model.remove(stmt);
	}

	public void remove(Resource s, Property p, RDFNode o) {
		model.remove(s, p, o);
	}

	public void remove(Resource s, Property p, Object o) {
		model.remove(s, p, model.createTypedLiteral(o));
	}

	public boolean hasProperty(Resource subj, Property prp) {
		return model.getProperty(subj, prp) != null;
	}

	public RDFNode getObject(Resource subj, Property prp) {
		return (subj.hasProperty(prp) ? subj.getProperty(prp).getObject() : null);
	}

	public Resource getObjectResource(Resource subj, Property prp) {
		RDFNode object = getObject(subj, prp);

		return (object != null ? (Resource) object : null);
	}

	public Literal getObjectLiteral(Resource subj, Property prp) {
		RDFNode object = getObject(subj, prp);

		return (object != null ? (Literal) object : null);
	}

	public Integer getObjectInt(Resource subj, Property prp) {
		return (Integer) getObjectPrimitive(subj, prp, l -> l.getInt());
	}

	public Double getObjectDouble(Resource subj, Property prp) {
		return (Double) getObjectPrimitive(subj, prp, l -> l.getDouble());
	}

	public Boolean getObjectBool(Resource subj, Property prp) {
		return (Boolean) getObjectPrimitive(subj, prp, l -> l.getBoolean());
	}

	public Object getObjectPrimitive(Resource subj, Property prp, Function<Literal, Object> fn) {
		Literal l = getObjectLiteral(subj, prp);
		if (l != null)
			return fn.apply(l);
		else
			return null;
	}

	public void setObjectInt(Resource subj, Property prp, int value) {
		if (hasProperty(subj, prp))
			model.getProperty(subj, prp).changeLiteralObject(value);
		else
			model.add(subj, prp, model.createTypedLiteral(value));
	}

	public LocalDate getObjectDate(Resource subj, Property prp) {
		return toDate((XSDDateTime) getObject(subj, prp));
	}

	public List<RDFNode> collectObjects(Resource subj, Property... path) {
		List<RDFNode> objs = new ArrayList<>();
		getObjects(subj, path, 0, objs);

		return objs;
	}

	public void printAll(String format) {
		model.write(System.out, format);
	}

	public void printDerivations() {
		((InfModel) model).getDeductionsModel().write(System.out);

//		Log.i("");
//
//		StmtIterator stmtIt = model.getDeductionsModel().listStatements();
//		boolean found = stmtIt.hasNext();
//
//		while (stmtIt.hasNext()) {
//			Log.i("derivation:");
//			Log.i(stmtIt.next() + "\n");
//		}
//
//		if (!found)
//			Log.i("derivations: none\n");
	}

	public void close() {
		model.close();
	}

	private void getObjects(Resource subj, Property[] path, int idx, List<RDFNode> collect) {
		StmtIterator it = subj.listProperties(path[idx]);

		while (it.hasNext()) {
			RDFNode obj = it.next().getObject();
			if (idx == path.length - 1)
				collect.add(obj);
			else
				getObjects((Resource) obj, path, idx + 1, collect);
		}
	}

	private Calendar toCal(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}

//	private LocalDate toDate(String str) {
//		DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
//		return LocalDate.parse(str, formatter);
//	}

	private LocalDate toDate(XSDDateTime dateTime) {
		Calendar cal = dateTime.asCalendar();

		return LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()).toLocalDate();
	}
}
