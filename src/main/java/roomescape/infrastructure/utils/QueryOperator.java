package roomescape.infrastructure.utils;

public enum QueryOperator {
    EQUALS("="),
    GREATER_OR_EQUAL_THAN(">="),
    LESS_OR_EQUAL_THAN("<="),
    ;

    private final String value;

    QueryOperator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
