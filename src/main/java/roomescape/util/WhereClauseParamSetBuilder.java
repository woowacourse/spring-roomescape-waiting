package roomescape.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WhereClauseParamSetBuilder {

    private WhereClauseParamSetBuilder() {
    }

    public static WhereClauseParamSet makeFrom(Map<String, Object> inputParams) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        inputParams.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> {
                    conditions.add(entry.getKey());
                    params.add(entry.getValue());
                });
        return new WhereClauseParamSet(conditions, params);
    }
}
