package roomescape.common.utils;

public enum ExecuteResult {

    SUCCESS, FAIL;

    public static ExecuteResult of(final int result) {
        if (result == 0) {
            return FAIL;
        }
        return SUCCESS;
    }
}
