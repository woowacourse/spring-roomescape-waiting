package roomescape.util;

import java.util.List;

public record WhereClauseParamSet(List<String> conditions, List<Object> params) {

    public String getWhereClause() {
        if (conditions.isEmpty()) return "";
        return "WHERE " + String.join(" AND ", conditions);
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }
}
