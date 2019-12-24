package pl.edu.agh.plonka.bartlomiej.menes.exception;

import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Rule;

public class CreateRuleException extends Exception {

    public CreateRuleException(Rule rule, Throwable cause) {
        super("ERROR_CREATING_RULE" + ' ' + rule.getName(), cause);
    }
}
