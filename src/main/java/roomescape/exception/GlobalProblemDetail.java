package roomescape.exception;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class GlobalProblemDetail {

    public static ProblemDetail of(HttpStatus status, List<String> messages, String requestUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setProperties(Map.of("errors", messages));
        problemDetail.setType(URI.create(requestUri));
        return problemDetail;
    }
}
