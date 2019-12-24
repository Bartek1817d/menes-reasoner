package pl.edu.agh.plonka.bartlomiej.menes.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.stripAccents;

public class NameUtils {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public static String generateName(String... keywords) {
        return generateName(Arrays.asList(keywords));
    }

    public static String generateName(Collection<String> keywords) {
        StringBuilder str = new StringBuilder();
        for (String keyword : keywords) {
            str.append(stripAccents(StringUtils.deleteWhitespace(keyword)));
        }
        return str.toString();
    }
}
