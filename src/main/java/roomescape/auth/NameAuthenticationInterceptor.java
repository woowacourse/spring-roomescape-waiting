package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.annotation.Authorized;
import roomescape.auth.exception.MissingAuthorizationHeaderException;
import roomescape.reservation.repository.ReservationRepository;

@Component
public class NameAuthenticationInterceptor implements HandlerInterceptor {

    private static final String NAME_ATTRIBUTE = "name";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final ReservationRepository reservationRepository;

    public NameAuthenticationInterceptor(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        if (!hm.hasMethodAnnotation(Authorized.class)) {
            return true;
        }

        String name = extractName(request);
        request.setAttribute(NAME_ATTRIBUTE, name);

        return true;
    }

    private String extractName(HttpServletRequest request) {
        String name = request.getHeader(AUTHORIZATION_HEADER);

        if (name == null || name.isBlank()) {
            throw new MissingAuthorizationHeaderException();
        }

        return name;
    }
}
