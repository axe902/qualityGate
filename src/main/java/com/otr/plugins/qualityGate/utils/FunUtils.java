package com.otr.plugins.qualityGate.utils;

import com.otr.plugins.qualityGate.model.jira.CutIssue;
import lombok.experimental.UtilityClass;
import net.rcarz.jiraclient.Issue;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@UtilityClass
public class FunUtils {
    private static final UnaryOperator<String> CAST = s -> s.replace(" ", "").toLowerCase(Locale.ROOT);
    private static final PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("#{", "}");

    public static final Function<Issue, CutIssue> CUT_ISSUE = issue -> new CutIssue(
            issue.getKey(),
            issue.getIssueType().getName(),
            issue.getStatus().getName(),
            getPatchFromIssue(issue),
            getSourceFromIssue(issue));

    public static List<String> getPatchFromIssue(Issue issue) {
        Object patchField = Optional.ofNullable(issue.getField("customfield_28582")).orElse(issue.getField("customfield_28588"));
        if (JSONUtils.isNull(patchField)) {
            return null;
        }

        List<String> result = null;
        if (JSONUtils.isArray(patchField)) {
            result = (List<String>) ((JSONArray) patchField).stream().map(o -> ((JSONObject) o).getString("key")).collect(Collectors.toList());
        } else if (JSONUtils.isObject(patchField)) {
            result = Collections.singletonList(((JSONObject) patchField).getString("key"));
        }

        return result;
    }

    public static String getSourceFromIssue(Issue issue) {
        Object sourceField = issue.getField("customfield_28581");
        if (JSONUtils.isNull(sourceField)) {
            return null;
        }

        if (JSONUtils.isObject(sourceField)) {
            return ((JSONObject) sourceField).getString("key");
        }
        return null;
    }

    public String canonical(String s) {
        return CAST.apply(s);
    }

    public String resolve(String value, PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver) {
        return placeholderHelper.replacePlaceholders(value, placeholderResolver);
    }
}
