package roomescape.reservation.repository.param;

public interface SearchParam {

    String addParamToQuery(String query);

    default String setStartQuery(boolean isFirst, String query) {
        if (isFirst) {
            return query + " WHERE ";
        }
        return query + " AND ";
    }
}
