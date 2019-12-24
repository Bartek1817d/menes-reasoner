package pl.edu.agh.plonka.bartlomiej.menes.exception;

import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Rule;

import static java.lang.String.format;

public class RuleAlreadyExistsException extends Exception {

    public RuleAlreadyExistsException(Rule rule) {
        super(format("RULE_ALREADY_EXISTS %s", rule.getName()));
    }
}
