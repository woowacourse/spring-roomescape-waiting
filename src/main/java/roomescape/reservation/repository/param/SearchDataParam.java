package roomescape.reservation.repository.param;

public class SearchDataParam<T> implements SearchParam {

    private final boolean isFirst;
    private final String paramName;;
    private final T paramValue;;

    public SearchDataParam(final boolean isFirst, final String paramName, final T paramValue) {
        this.isFirst = isFirst;
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    @Override
    public String addParamToQuery(final String query) {
        return setStartQuery(isFirst, query) + paramName + " = " + paramValue;
    }
}
