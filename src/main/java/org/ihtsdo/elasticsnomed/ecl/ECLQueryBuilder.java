package org.ihtsdo.elasticsnomed.ecl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ihtsdo.elasticsnomed.ecl.domain.*;
import org.ihtsdo.elasticsnomed.ecl.generated.parser.ECLLexer;
import org.ihtsdo.elasticsnomed.ecl.generated.parser.ECLListener;
import org.ihtsdo.elasticsnomed.ecl.generated.parser.ECLParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ECLQueryBuilder {

	ExpressionConstraint createQuery(String ecl) throws ECLException {
		ANTLRInputStream inputStream = new ANTLRInputStream(ecl);
		final ECLLexer lexer = new ECLLexer(inputStream);
		final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		final ECLParser parser = new ECLParser(tokenStream);
//		final List<RecognitionException> exceptions = new ArrayList<>();
//		parser.setErrorHandler(getErrorHandler(exceptions));

		ParserRuleContext tree;
		try {
			tree = parser.expressionconstraint();
		} catch (NullPointerException e) {
			throw new ECLException("Failed to parse ECL '" + ecl + "'", e);
		}
		final ParseTreeWalker walker = new ParseTreeWalker();
		final ECLListenerImpl listener = new ECLListenerImpl();
		walker.walk(listener, tree);

		return listener.getRootExpressionConstraint();
	}

	private static final class ECLListenerImpl implements ECLListener {

		private ExpressionConstraint rootExpressionConstraint;

		@Override
		public void enterExpressionconstraint(ECLParser.ExpressionconstraintContext ctx) {
		}

		@Override
		public void exitExpressionconstraint(ECLParser.ExpressionconstraintContext ctx) {
		}

		@Override
		public void enterRefinedexpressionconstraint(ECLParser.RefinedexpressionconstraintContext ctx) {
			rootExpressionConstraint = new RefinedExpressionConstraint(build(ctx.subexpressionconstraint()), build(ctx.eclrefinement()));
		}

		@Override
		public void exitRefinedexpressionconstraint(ECLParser.RefinedexpressionconstraintContext ctx) {
		}

		@Override
		public void enterCompoundexpressionconstraint(ECLParser.CompoundexpressionconstraintContext ctx) {
			throw new UnsupportedOperationException("Compoundexpressionconstraint");

		}

		@Override
		public void exitCompoundexpressionconstraint(ECLParser.CompoundexpressionconstraintContext ctx) {

		}

		@Override
		public void enterConjunctionexpressionconstraint(ECLParser.ConjunctionexpressionconstraintContext ctx) {
			throw new UnsupportedOperationException("Conjunctionexpressionconstraint is not supported.");
		}

		@Override
		public void exitConjunctionexpressionconstraint(ECLParser.ConjunctionexpressionconstraintContext ctx) {

		}

		@Override
		public void enterDisjunctionexpressionconstraint(ECLParser.DisjunctionexpressionconstraintContext ctx) {
			throw new UnsupportedOperationException("Disjunctionexpressionconstraint is not supported.");
		}

		@Override
		public void exitDisjunctionexpressionconstraint(ECLParser.DisjunctionexpressionconstraintContext ctx) {

		}

		@Override
		public void enterExclusionexpressionconstraint(ECLParser.ExclusionexpressionconstraintContext ctx) {
			throw new UnsupportedOperationException("Exclusionexpressionconstraint is not supported.");
		}

		@Override
		public void exitExclusionexpressionconstraint(ECLParser.ExclusionexpressionconstraintContext ctx) {

		}

		@Override
		public void enterDottedexpressionconstraint(ECLParser.DottedexpressionconstraintContext ctx) {
			throw new UnsupportedOperationException("Dottedexpressionconstraint is not supported.");
		}

		@Override
		public void exitDottedexpressionconstraint(ECLParser.DottedexpressionconstraintContext ctx) {

		}

		@Override
		public void enterDottedexpressionattribute(ECLParser.DottedexpressionattributeContext ctx) {
		}

		@Override
		public void exitDottedexpressionattribute(ECLParser.DottedexpressionattributeContext ctx) {

		}

		private SubExpressionConstraint build(ECLParser.SubexpressionconstraintContext ctx) {
			Operator operator = ctx.constraintoperator() != null ? Operator.textLookup(ctx.constraintoperator().getText()) : null;

//			boolean memberOf = ctx.memberof() != null;
			if (ctx.memberof() != null) {
				throw new UnsupportedOperationException("MemberOf is not supported.");
			}

			SubExpressionConstraint subExpressionConstraint = new SubExpressionConstraint(operator);

			ECLParser.EclfocusconceptContext eclfocusconcept = ctx.eclfocusconcept();
			if (eclfocusconcept.wildcard() != null) {
				subExpressionConstraint.wildcard();
			}
			if (eclfocusconcept.eclconceptreference() != null) {
				subExpressionConstraint.setConceptId(eclfocusconcept.eclconceptreference().conceptid().getText());
			}

			if (ctx.expressionconstraint() != null) {
				throw new UnsupportedOperationException("Nested expression constraint is not supported.");
			}

			return subExpressionConstraint;
		}

		@Override
		public void enterSubexpressionconstraint(ECLParser.SubexpressionconstraintContext ctx) {
			if (rootExpressionConstraint == null) {
				rootExpressionConstraint = build(ctx);
			}
		}

		@Override
		public void exitSubexpressionconstraint(ECLParser.SubexpressionconstraintContext ctx) {

		}

		@Override
		public void enterEclfocusconcept(ECLParser.EclfocusconceptContext ctx) {

		}

		@Override
		public void exitEclfocusconcept(ECLParser.EclfocusconceptContext ctx) {

		}

		@Override
		public void enterDot(ECLParser.DotContext ctx) {

		}

		@Override
		public void exitDot(ECLParser.DotContext ctx) {

		}

		@Override
		public void enterMemberof(ECLParser.MemberofContext ctx) {

		}

		@Override
		public void exitMemberof(ECLParser.MemberofContext ctx) {

		}

		@Override
		public void enterEclconceptreference(ECLParser.EclconceptreferenceContext ctx) {
		}

		@Override
		public void exitEclconceptreference(ECLParser.EclconceptreferenceContext ctx) {

		}

		@Override
		public void enterConceptid(ECLParser.ConceptidContext ctx) {

		}

		@Override
		public void exitConceptid(ECLParser.ConceptidContext ctx) {

		}

		@Override
		public void enterTerm(ECLParser.TermContext ctx) {

		}

		@Override
		public void exitTerm(ECLParser.TermContext ctx) {

		}

		@Override
		public void enterWildcard(ECLParser.WildcardContext ctx) {

		}

		@Override
		public void exitWildcard(ECLParser.WildcardContext ctx) {

		}

		@Override
		public void enterConstraintoperator(ECLParser.ConstraintoperatorContext ctx) {

		}

		@Override
		public void exitConstraintoperator(ECLParser.ConstraintoperatorContext ctx) {

		}

		@Override
		public void enterDescendantof(ECLParser.DescendantofContext ctx) {

		}

		@Override
		public void exitDescendantof(ECLParser.DescendantofContext ctx) {

		}

		@Override
		public void enterDescendantorselfof(ECLParser.DescendantorselfofContext ctx) {

		}

		@Override
		public void exitDescendantorselfof(ECLParser.DescendantorselfofContext ctx) {

		}

		@Override
		public void enterChildof(ECLParser.ChildofContext ctx) {

		}

		@Override
		public void exitChildof(ECLParser.ChildofContext ctx) {

		}

		@Override
		public void enterAncestorof(ECLParser.AncestorofContext ctx) {

		}

		@Override
		public void exitAncestorof(ECLParser.AncestorofContext ctx) {

		}

		@Override
		public void enterAncestororselfof(ECLParser.AncestororselfofContext ctx) {

		}

		@Override
		public void exitAncestororselfof(ECLParser.AncestororselfofContext ctx) {

		}

		@Override
		public void enterParentof(ECLParser.ParentofContext ctx) {

		}

		@Override
		public void exitParentof(ECLParser.ParentofContext ctx) {

		}

		@Override
		public void enterConjunction(ECLParser.ConjunctionContext ctx) {

		}

		@Override
		public void exitConjunction(ECLParser.ConjunctionContext ctx) {

		}

		@Override
		public void enterDisjunction(ECLParser.DisjunctionContext ctx) {

		}

		@Override
		public void exitDisjunction(ECLParser.DisjunctionContext ctx) {

		}

		@Override
		public void enterExclusion(ECLParser.ExclusionContext ctx) {

		}

		@Override
		public void exitExclusion(ECLParser.ExclusionContext ctx) {

		}

		@Override
		public void enterEclrefinement(ECLParser.EclrefinementContext ctx) {
		}

		private EclRefinement build(ECLParser.EclrefinementContext ctx) {
			if (ctx == null) {
				return null;
			}
			EclRefinement refinement = new EclRefinement();
			refinement.setSubRefinement(build(ctx.subrefinement()));
			if (ctx.conjunctionrefinementset() != null) {
				throw new UnsupportedOperationException("ConjunctionRefinementSet is not supported.");
			}
			if (ctx.disjunctionrefinementset() != null) {
				throw new UnsupportedOperationException("DisjunctionRefinementSet is not supported.");
			}
			return refinement;
		}

		@Override
		public void exitEclrefinement(ECLParser.EclrefinementContext ctx) {

		}

		@Override
		public void enterConjunctionrefinementset(ECLParser.ConjunctionrefinementsetContext ctx) {

		}

		public List<SubRefinement> build(ECLParser.ConjunctionrefinementsetContext ctx) {
			List<SubRefinement> refinements = new ArrayList<>();
			for (ECLParser.SubrefinementContext subrefinementContext : ctx.subrefinement()) {
				refinements.add(build(subrefinementContext));
			}
			return refinements;
		}

		@Override
		public void exitConjunctionrefinementset(ECLParser.ConjunctionrefinementsetContext ctx) {

		}

		@Override
		public void enterDisjunctionrefinementset(ECLParser.DisjunctionrefinementsetContext ctx) {

		}

		@Override
		public void exitDisjunctionrefinementset(ECLParser.DisjunctionrefinementsetContext ctx) {

		}

		@Override
		public void enterSubrefinement(ECLParser.SubrefinementContext ctx) {
		}

		private SubRefinement build(ECLParser.SubrefinementContext ctx) {
			SubRefinement subRefinement = new SubRefinement();
			subRefinement.setEclAttributeSet(build(ctx.eclattributeset()));

//			subRefinement.setEclAttributeGroup(build(ctx.eclattributegroup()));
			if (ctx.eclattributegroup() != null) {
				throw new UnsupportedOperationException("EclAttributeGroup is not supported.");
			}

//			subRefinement.setEclRefinement(build(ctx.eclrefinement()));
			if (ctx.eclrefinement() != null) {
				throw new UnsupportedOperationException("EclRefinement is not supported.");
			}

			return subRefinement;
		}

		@Override
		public void exitSubrefinement(ECLParser.SubrefinementContext ctx) {

		}

		@Override
		public void enterEclattributeset(ECLParser.EclattributesetContext ctx) {

		}

		private EclAttributeSet build(ECLParser.EclattributesetContext ctx) {
			if (ctx == null) return null;
			EclAttributeSet eclAttributeSet = new EclAttributeSet();
			eclAttributeSet.setSubAttributeSet(build(ctx.subattributeset()));
			ECLParser.ConjunctionattributesetContext conjunctionattributeset = ctx.conjunctionattributeset();
			if (conjunctionattributeset != null) {
				throw new UnsupportedOperationException("ConjunctionAttributeSet is not supported.");
//				for (ECLParser.SubattributesetContext subattributesetContext : conjunctionattributeset.subattributeset()) {
//					eclAttributeSet.addConjunctionAttributeSet(build(subattributesetContext));
//				}
			}
			ECLParser.DisjunctionattributesetContext disjunctionattributeset = ctx.disjunctionattributeset();
			if (disjunctionattributeset != null) {
				throw new UnsupportedOperationException("DisjunctionAttributeSet is not supported.");
//				for (ECLParser.SubattributesetContext subattributesetContext : disjunctionattributeset.subattributeset()) {
//					eclAttributeSet.addDisjunctionAttributeSet(build(subattributesetContext));
//				}
			}
			return eclAttributeSet;
		}

		@Override
		public void exitEclattributeset(ECLParser.EclattributesetContext ctx) {

		}

		@Override
		public void enterConjunctionattributeset(ECLParser.ConjunctionattributesetContext ctx) {

		}

		@Override
		public void exitConjunctionattributeset(ECLParser.ConjunctionattributesetContext ctx) {

		}

		@Override
		public void enterDisjunctionattributeset(ECLParser.DisjunctionattributesetContext ctx) {

		}

		@Override
		public void exitDisjunctionattributeset(ECLParser.DisjunctionattributesetContext ctx) {

		}

		@Override
		public void enterSubattributeset(ECLParser.SubattributesetContext ctx) {

		}

		private SubAttributeSet build(ECLParser.SubattributesetContext ctx) {
			if (ctx == null) return null;
			SubAttributeSet subAttributeSet = new SubAttributeSet();
			subAttributeSet.setAttribute(build(ctx.eclattribute()));

//			subAttributeSet.setAttributeSet(build(ctx.eclattributeset()));
			if (ctx.eclattributeset() != null) {
				throw new UnsupportedOperationException("EclAttributeSet is not supported.");
			}

			return subAttributeSet;
		}

		@Override
		public void exitSubattributeset(ECLParser.SubattributesetContext ctx) {

		}

		@Override
		public void enterEclattributegroup(ECLParser.EclattributegroupContext ctx) {

		}

		private EclAttributeSet build(ECLParser.EclattributegroupContext ctx) {
			return null;
		}

		@Override
		public void exitEclattributegroup(ECLParser.EclattributegroupContext ctx) {

		}

		@Override
		public void enterEclattribute(ECLParser.EclattributeContext ctx) {

		}

		private EclAttribute build(ECLParser.EclattributeContext ctx) {
			if (ctx == null) return null;
			EclAttribute attribute = new EclAttribute();

			// TODO cardinality
			ECLParser.CardinalityContext cardinality = ctx.cardinality();
			if (cardinality != null) {
				throw new UnsupportedOperationException("Cardinality is not supported.");
			}

			// TODO reverseflag
			ECLParser.ReverseflagContext reverseflag = ctx.reverseflag();
			if (reverseflag != null) {
				throw new UnsupportedOperationException("The reverse flag is not supported.");
			}

			attribute.setAttributeName(build(ctx.eclattributename().subexpressionconstraint()));
			ECLParser.ExpressioncomparisonoperatorContext expressioncomparisonoperator = ctx.expressioncomparisonoperator();
			if (expressioncomparisonoperator == null) {
				throw new UnsupportedOperationException("Only the expressionComparisonOperator is supported, not the numericComparisonOperator or the stringComparisonOperator.");
			}
			attribute.setExpressionComparisonOperator(expressioncomparisonoperator.getText());
			attribute.setValue(build(ctx.subexpressionconstraint()));

			return attribute;
		}

		@Override
		public void exitEclattribute(ECLParser.EclattributeContext ctx) {

		}

		@Override
		public void enterCardinality(ECLParser.CardinalityContext ctx) {

		}

		@Override
		public void exitCardinality(ECLParser.CardinalityContext ctx) {

		}

		@Override
		public void enterMinvalue(ECLParser.MinvalueContext ctx) {

		}

		@Override
		public void exitMinvalue(ECLParser.MinvalueContext ctx) {

		}

		@Override
		public void enterTo(ECLParser.ToContext ctx) {

		}

		@Override
		public void exitTo(ECLParser.ToContext ctx) {

		}

		@Override
		public void enterMaxvalue(ECLParser.MaxvalueContext ctx) {

		}

		@Override
		public void exitMaxvalue(ECLParser.MaxvalueContext ctx) {

		}

		@Override
		public void enterMany(ECLParser.ManyContext ctx) {

		}

		@Override
		public void exitMany(ECLParser.ManyContext ctx) {

		}

		@Override
		public void enterReverseflag(ECLParser.ReverseflagContext ctx) {

		}

		@Override
		public void exitReverseflag(ECLParser.ReverseflagContext ctx) {

		}

		@Override
		public void enterEclattributename(ECLParser.EclattributenameContext ctx) {

		}

		@Override
		public void exitEclattributename(ECLParser.EclattributenameContext ctx) {

		}

		@Override
		public void enterExpressioncomparisonoperator(ECLParser.ExpressioncomparisonoperatorContext ctx) {

		}

		@Override
		public void exitExpressioncomparisonoperator(ECLParser.ExpressioncomparisonoperatorContext ctx) {

		}

		@Override
		public void enterNumericcomparisonoperator(ECLParser.NumericcomparisonoperatorContext ctx) {

		}

		@Override
		public void exitNumericcomparisonoperator(ECLParser.NumericcomparisonoperatorContext ctx) {

		}

		@Override
		public void enterStringcomparisonoperator(ECLParser.StringcomparisonoperatorContext ctx) {

		}

		@Override
		public void exitStringcomparisonoperator(ECLParser.StringcomparisonoperatorContext ctx) {

		}

		@Override
		public void enterNumericvalue(ECLParser.NumericvalueContext ctx) {

		}

		@Override
		public void exitNumericvalue(ECLParser.NumericvalueContext ctx) {

		}

		@Override
		public void enterStringvalue(ECLParser.StringvalueContext ctx) {

		}

		@Override
		public void exitStringvalue(ECLParser.StringvalueContext ctx) {

		}

		@Override
		public void enterIntegervalue(ECLParser.IntegervalueContext ctx) {

		}

		@Override
		public void exitIntegervalue(ECLParser.IntegervalueContext ctx) {

		}

		@Override
		public void enterDecimalvalue(ECLParser.DecimalvalueContext ctx) {

		}

		@Override
		public void exitDecimalvalue(ECLParser.DecimalvalueContext ctx) {

		}

		@Override
		public void enterNonnegativeintegervalue(ECLParser.NonnegativeintegervalueContext ctx) {

		}

		@Override
		public void exitNonnegativeintegervalue(ECLParser.NonnegativeintegervalueContext ctx) {

		}

		@Override
		public void enterSctid(ECLParser.SctidContext ctx) {

		}

		@Override
		public void exitSctid(ECLParser.SctidContext ctx) {

		}

		@Override
		public void enterWs(ECLParser.WsContext ctx) {

		}

		@Override
		public void exitWs(ECLParser.WsContext ctx) {

		}

		@Override
		public void enterMws(ECLParser.MwsContext ctx) {

		}

		@Override
		public void exitMws(ECLParser.MwsContext ctx) {

		}

		@Override
		public void enterComment(ECLParser.CommentContext ctx) {

		}

		@Override
		public void exitComment(ECLParser.CommentContext ctx) {

		}

		@Override
		public void enterNonstarchar(ECLParser.NonstarcharContext ctx) {

		}

		@Override
		public void exitNonstarchar(ECLParser.NonstarcharContext ctx) {

		}

		@Override
		public void enterStarwithnonfslash(ECLParser.StarwithnonfslashContext ctx) {

		}

		@Override
		public void exitStarwithnonfslash(ECLParser.StarwithnonfslashContext ctx) {

		}

		@Override
		public void enterNonfslash(ECLParser.NonfslashContext ctx) {

		}

		@Override
		public void exitNonfslash(ECLParser.NonfslashContext ctx) {

		}

		@Override
		public void enterSp(ECLParser.SpContext ctx) {

		}

		@Override
		public void exitSp(ECLParser.SpContext ctx) {

		}

		@Override
		public void enterHtab(ECLParser.HtabContext ctx) {

		}

		@Override
		public void exitHtab(ECLParser.HtabContext ctx) {

		}

		@Override
		public void enterCr(ECLParser.CrContext ctx) {

		}

		@Override
		public void exitCr(ECLParser.CrContext ctx) {

		}

		@Override
		public void enterLf(ECLParser.LfContext ctx) {

		}

		@Override
		public void exitLf(ECLParser.LfContext ctx) {

		}

		@Override
		public void enterQm(ECLParser.QmContext ctx) {

		}

		@Override
		public void exitQm(ECLParser.QmContext ctx) {

		}

		@Override
		public void enterBs(ECLParser.BsContext ctx) {

		}

		@Override
		public void exitBs(ECLParser.BsContext ctx) {

		}

		@Override
		public void enterDigit(ECLParser.DigitContext ctx) {

		}

		@Override
		public void exitDigit(ECLParser.DigitContext ctx) {

		}

		@Override
		public void enterZero(ECLParser.ZeroContext ctx) {

		}

		@Override
		public void exitZero(ECLParser.ZeroContext ctx) {

		}

		@Override
		public void enterDigitnonzero(ECLParser.DigitnonzeroContext ctx) {

		}

		@Override
		public void exitDigitnonzero(ECLParser.DigitnonzeroContext ctx) {

		}

		@Override
		public void enterNonwsnonpipe(ECLParser.NonwsnonpipeContext ctx) {

		}

		@Override
		public void exitNonwsnonpipe(ECLParser.NonwsnonpipeContext ctx) {

		}

		@Override
		public void enterAnynonescapedchar(ECLParser.AnynonescapedcharContext ctx) {

		}

		@Override
		public void exitAnynonescapedchar(ECLParser.AnynonescapedcharContext ctx) {

		}

		@Override
		public void enterEscapedchar(ECLParser.EscapedcharContext ctx) {

		}

		@Override
		public void exitEscapedchar(ECLParser.EscapedcharContext ctx) {

		}

		@Override
		public void enterUtf8_2(ECLParser.Utf8_2Context ctx) {

		}

		@Override
		public void exitUtf8_2(ECLParser.Utf8_2Context ctx) {

		}

		@Override
		public void enterUtf8_3(ECLParser.Utf8_3Context ctx) {

		}

		@Override
		public void exitUtf8_3(ECLParser.Utf8_3Context ctx) {

		}

		@Override
		public void enterUtf8_4(ECLParser.Utf8_4Context ctx) {

		}

		@Override
		public void exitUtf8_4(ECLParser.Utf8_4Context ctx) {

		}

		@Override
		public void enterUtf8_tail(ECLParser.Utf8_tailContext ctx) {

		}

		@Override
		public void exitUtf8_tail(ECLParser.Utf8_tailContext ctx) {

		}

		@Override
		public void visitTerminal(TerminalNode terminalNode) {

		}

		@Override
		public void visitErrorNode(ErrorNode errorNode) {

		}

		@Override
		public void enterEveryRule(ParserRuleContext parserRuleContext) {

		}

		@Override
		public void exitEveryRule(ParserRuleContext parserRuleContext) {

		}

		ExpressionConstraint getRootExpressionConstraint() {
			return rootExpressionConstraint;
		}
	}
}
