package roomescape.infrastructure.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class DynamicQueryBuilder {
    private static final String WHERE = "where";
    private static final String AND = " and ";

    private final StringJoiner query;
    private final List<Object> parameters;

    private DynamicQueryBuilder(String initialClause) {
        this.query = new StringJoiner(AND, initialClause + " ", "");
        this.parameters = new ArrayList<>();
        query.setEmptyValue("");
    }

    public static DynamicQueryBuilder where() {
        return new DynamicQueryBuilder(WHERE);
    }

    private DynamicQueryBuilder addFilterOnNonNull(String column, QueryOperator operator, Object value) {
        if (value == null) {
            return this;
        }
        query.add(column + " " + operator.getValue() + " ?");
        parameters.add(value);
        return this;
    }

    public DynamicQueryBuilder equals(String column, Object value) {
        return addFilterOnNonNull(column, QueryOperator.EQUALS, value);
    }

    public DynamicQueryBuilder greaterOrEqualThan(String column, Object value) {
        return addFilterOnNonNull(column, QueryOperator.GREATER_OR_EQUAL_THAN, value);
    }

    public DynamicQueryBuilder lessOrEqualThan(String column, Object value) {
        return addFilterOnNonNull(column, QueryOperator.LESS_OR_EQUAL_THAN, value);
    }

    public String toSql() {
        return query.toString();
    }

    public Object[] getParameters() {
        return parameters.toArray();
    }
}
