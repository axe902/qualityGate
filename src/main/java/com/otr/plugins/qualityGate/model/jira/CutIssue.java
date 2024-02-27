package com.otr.plugins.qualityGate.model.jira;

import net.rcarz.jiraclient.Issue;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Truncated representation of jira issue {@link Issue}
 *
 * @param key    issue key {@link Issue#getKey()}
 * @param type   name of issue type {@link Issue#getIssueType()}
 * @param status name of issue status {@link Issue#getStatus()}
 */
public record CutIssue(String key, String type, String status, List<String> patch, String source) {

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        Optional.ofNullable(key).ifPresent(v -> map.put("key", v));
        Optional.ofNullable(type).ifPresent(v -> map.put("type", v));
        Optional.ofNullable(status).ifPresent(v -> map.put("status", v));
        Optional.ofNullable(patch).ifPresent(v -> map.put("patch", StringUtils.join(patch, "\n")));
        Optional.ofNullable(source).ifPresent(v -> map.put("source", v));
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CutIssue cutIssue = (CutIssue) o;
        return Objects.equals(key, cutIssue.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
