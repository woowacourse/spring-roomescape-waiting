package roomescape.reservation.repository.param;

import java.time.LocalDate;

public class SearchDateParam implements SearchParam {

    private final boolean isFirst;
    private final String paramName;
    private final LocalDate from;
    private final LocalDate to;

    public SearchDateParam(final boolean isFirst, final String paramName, final LocalDate from, final LocalDate to) {
        this.isFirst = isFirst;
        this.paramName = paramName;
        this.from = from;
        this.to = to;
    }

    @Override
    public String addParamToQuery(final String query) {
        String generateQuery = setStartQuery(isFirst, query);

        if (from != null && to != null) {
            return generateQuery + paramName + " BETWEEN " + "\'" + from + "\'" + " AND " + "\'" + to + "\'";
        }

        if (from != null) {
            return generateQuery + paramName + " >= " + "\'" + from + "\'";
        }

        if (to != null) {
            return generateQuery + paramName + " <= " + "\'" + to + "\'";
        }

        return query;
    }
}
