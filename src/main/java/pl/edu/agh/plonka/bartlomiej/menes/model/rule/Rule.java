package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Rule {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private String name;
    private Collection<AbstractAtom> declarationAtoms = new HashSet<>();
    private Collection<AbstractAtom> bodyAtoms = new HashSet<>();
    private Collection<AbstractAtom> headAtoms = new HashSet<>();

    public Rule() {
    }

    public Rule(String name) {
        this.name = name;
    }

    public Rule(String name, Collection<AbstractAtom> declarationAtoms, Collection<AbstractAtom> bodyAtoms, Collection<AbstractAtom> headAtoms) {
        this.name = name;
        this.declarationAtoms.addAll(declarationAtoms);
        this.bodyAtoms.addAll(bodyAtoms);
        this.headAtoms.addAll(headAtoms);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<AbstractAtom> getDeclarationAtoms() {
        return declarationAtoms;
    }

    public void setDeclarationAtoms(Collection<AbstractAtom> declarationAtoms) {
        this.declarationAtoms.clear();
        this.declarationAtoms.addAll(declarationAtoms);
    }

    public void addDeclarationAtom(AbstractAtom declarationAtom) {
        declarationAtoms.add(declarationAtom);
    }

    public Collection<AbstractAtom> getBodyAtoms() {
        return bodyAtoms;
    }

    public void setBodyAtoms(Collection<AbstractAtom> bodyAtoms) {
        this.bodyAtoms.clear();
        this.bodyAtoms.addAll(bodyAtoms);
    }

    public void addBodyAtom(AbstractAtom inputAtom) {
        bodyAtoms.add(inputAtom);
    }

    public void addBodyAtoms(Collection<AbstractAtom> inputAtoms) {
        bodyAtoms.addAll(inputAtoms);
    }

    public Collection<AbstractAtom> getHeadAtoms() {
        return headAtoms;
    }

    public void setHeadAtoms(Collection<AbstractAtom> headAtoms) {
        this.headAtoms.clear();
        this.headAtoms.addAll(headAtoms);
    }

    public void addHeadAtom(AbstractAtom headAtom) {
        headAtoms.add(headAtom);
    }

    public void addHeadAtoms(Collection<AbstractAtom> headAtoms) {
        this.headAtoms.addAll(headAtoms);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rule other = (Rule) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        Collection<AbstractAtom> bodyAtoms = new ArrayList<>(this.declarationAtoms);
        bodyAtoms.addAll(this.bodyAtoms);
        for (AbstractAtom atom : bodyAtoms) {
            str.append(atom);
            str.append(" ^ ");
        }
        if (!bodyAtoms.isEmpty())
            str.setLength(str.length() - 3);
        str.append(" -> ");
        for (AbstractAtom atom : headAtoms) {
            str.append(atom);
            str.append(" ^ ");
        }
        if (!headAtoms.isEmpty())
            str.setLength(str.length() - 3);
        return str.toString();
    }
}
