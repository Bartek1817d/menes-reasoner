package pl.edu.agh.plonka.bartlomiej.menes.model;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class Entity {

    private final Logger LOG = getLogger(Entity.class);

    private String id;
    private String label;
    private String comment;
    private final Set<Entity> classes = new HashSet<>();

    private Map<String, String> languageLabelMap = new HashMap<>();
    private Map<String, String> languageCommentMap = new HashMap<>();

    public Entity() {
    }

    public Entity(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        this.languageLabelMap.put("en", label);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        this.languageCommentMap.put("en", comment);
    }

    public Set<Entity> getClasses() {
        return classes;
    }

    public void setClasses(Set<Entity> classes) {
        this.classes.clear();
        this.classes.addAll(classes);
    }

    public void addClasses(Set<Entity> classes) {
        this.classes.addAll(classes);
    }

    public void addClass(Entity cls) {
        this.classes.add(cls);
    }

    @Override
    public String toString() {
        return isNotBlank(label) ? label : id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Entity other = (Entity) obj;
        if (id == null) {
            return other.id == null;
        } else {
            return id.equals(other.id);
        }
    }

    public Map<String, String> getLanguageLabelMap() {
        return languageLabelMap;
    }

    public void setLanguageLabelMap(Map<String, String> languageLabelMap) {
        this.languageLabelMap = languageLabelMap;
    }

    public Map<String, String> getLanguageCommentMap() {
        return languageCommentMap;
    }

    public void setLanguageCommentMap(Map<String, String> languageCommentMap) {
        this.languageCommentMap = languageCommentMap;
    }

    public void setLanguage() {
        if (languageLabelMap.containsKey("en"))
            this.label = languageLabelMap.get("en");
        else
            this.label = id;
        if (languageCommentMap.containsKey("en"))
            this.comment = languageCommentMap.get("en");
    }
}
