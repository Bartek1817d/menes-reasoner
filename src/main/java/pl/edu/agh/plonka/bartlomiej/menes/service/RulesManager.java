package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.slf4j.Logger;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;
import pl.edu.agh.plonka.bartlomiej.menes.exception.CreateRuleException;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import pl.edu.agh.plonka.bartlomiej.menes.model.OntologyClass;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.*;
import pl.edu.agh.plonka.bartlomiej.menes.utils.Others;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Others.findEntity;

public class RulesManager {

    private static final Logger LOG = getLogger(RulesManager.class);

    private final SWRLAPIOWLOntology ruleOntology;

    RulesManager(SWRLAPIOWLOntology ruleOntology) {
        this.ruleOntology = ruleOntology;
    }

    public void addRule(Rule rule) throws CreateRuleException {
        try {
            ruleOntology.createSWRLRule(rule.getName(), rule.toString());
        } catch (SWRLParseException | SWRLBuiltInException e) {
            throw new CreateRuleException(rule, e);
        }
    }

    public void deleteRule(Rule rule) {
        ruleOntology.deleteSWRLRule(rule.getName());
    }

    public void deleteRules(Collection<Rule> rules) {
        rules.forEach(rule -> ruleOntology.deleteSWRLRule(rule.getName()));
    }

    public void deleteRules() {
        ruleOntology.reset();
    }

    public Collection<Rule> loadRules(Set<OntologyClass> classes, Set<Entity> entities) {
        Collection<Rule> rules = new ArrayList<>();
        for (SWRLAPIRule swrlRule : ruleOntology.getSWRLRules()) {
            Rule rule = new Rule(swrlRule.getRuleName());
            for (SWRLAtom atom : swrlRule.getBody()) {
                AbstractAtom bodyAtom = parseSWRLAtom(atom, classes, entities);
                if (isDeclarationAtom(bodyAtom))
                    rule.addDeclarationAtom(bodyAtom);
                else
                    rule.addBodyAtom(bodyAtom);
            }
            for (SWRLAtom atom : swrlRule.getHead()) {
                rule.addHeadAtom(parseSWRLAtom(atom, classes, entities));
            }
            rules.add(rule);
        }
        return rules;
    }

    @SuppressWarnings("unchecked")
    private AbstractAtom parseSWRLAtom(SWRLAtom swrlAtom, Set<OntologyClass> classes, Set<Entity> entities) {
        String str = swrlAtom.toString();
        Pattern atomPattern = Pattern
                .compile("^(?<atomType>\\p{Alpha}+)\\(<\\S+#(?<atomID>\\w+)> (?<atomArguments>.+)\\)$");
        Pattern argumentPattern = Pattern.compile(
                "((?<argumentType>\\p{Alpha}*)\\(?<\\S+#(?<argumentID>\\w+)>\\)?)|(\"(?<value>\\d+)\"\\^\\^xsd:(?<valueType>[a-z]+))");
        Matcher atomMatcher = atomPattern.matcher(str);
        if (atomMatcher.find()) {

            String atomType = atomMatcher.group("atomType");
            String atomID = atomMatcher.group("atomID");
            String atomArguments = atomMatcher.group("atomArguments");
            Matcher argumentMatcher = argumentPattern.matcher(atomArguments);

            // class declaration
            if (atomType.equals("ClassAtom")) {
                return parseClassAtom(atomID, argumentMatcher, classes, entities);
            } else if (atomType.equals("ObjectPropertyAtom") || atomType.equals("DataPropertyAtom")
                    || atomType.equals("BuiltInAtom")) { // property
                return parseTwoArgumentsAtom(atomType, atomID, argumentMatcher, entities);
            }
        }
        return null;
    }

    private AbstractAtom parseClassAtom(String atomID,
                                        Matcher argumentMatcher,
                                        Set<OntologyClass> classes,
                                        Set<Entity> entities) {
        if (argumentMatcher.find()) {
            String argumentType = argumentMatcher.group("argumentType");
            String argumentID = argumentMatcher.group("argumentID");
            if (argumentType.equals("Variable"))
                return new ClassDeclarationAtom<>(findEntity(atomID, classes), new Variable(argumentID));
            if (Others.containsEntity(argumentID, entities))
                return new ClassDeclarationAtom<>(findEntity(atomID, classes), findEntity(argumentID, entities));
        }
        return null;
    }

    private AbstractAtom parseTwoArgumentsAtom(String atomType,
                                               String atomID,
                                               Matcher argumentMatcher,
                                               Set<Entity> entities) {
        int i = 0;
        @SuppressWarnings("rawtypes")
        TwoArgumentsAtom atom;
        if (atomType.equals("BuiltInAtom"))
            atom = new TwoArgumentsAtom<>(atomID, "swrlb");
        else
            atom = new TwoArgumentsAtom<>(atomID);
        while (argumentMatcher.find()) {
            i += 1;
            String argumentType = argumentMatcher.group("argumentType");
            String argumentID = argumentMatcher.group("argumentID");
            String value = argumentMatcher.group("value");
            String valueType = argumentMatcher.group("valueType");

            if (argumentType != null && argumentID != null) {
                if (argumentType.equals("Variable")) {
                    setAtomArgument(atom, new Variable(argumentID), i);
                } else if (argumentType.equals("")) {
                    Entity entity = findEntity(argumentID, entities);
                    setAtomArgument(atom, entity, i);
                }
            } else if (valueType != null && value != null && StringUtils.isNumeric(value)) {
                int intVal = Integer.parseInt(value);
                setAtomArgument(atom, intVal, i);
            }
        }
        if (i == 2 && atom.getArgument1() != null && atom.getArgument2() != null)
            return atom;
        else
            return null;
    }

    private <T> void setAtomArgument(TwoArgumentsAtom atom, T argument, int i) {
        switch (i) {
            case 1:
                atom.setArgument1(argument);
            case 2:
                atom.setArgument2(argument);
        }
    }

    private boolean isDeclarationAtom(AbstractAtom atom) {
        if (atom instanceof ClassDeclarationAtom) {
            return true;
        }
        if (atom instanceof TwoArgumentsAtom) {
            TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) atom;
            return twoArgumentsAtom.getArgument1() instanceof Variable && twoArgumentsAtom.getArgument2() instanceof Variable;
        }
        return false;
    }
}
