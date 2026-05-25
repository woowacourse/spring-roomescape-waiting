package roomescape.reservation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ForbiddenException;
import roomescape.reservation.exception.MissingAuthorizationHeaderException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;

@Component
public class ReservationOwnerInterceptor implements HandlerInterceptor {

    private final ReservationRepository reservationRepository;

    public ReservationOwnerInterceptor(ReservationRepository reservationRepository) {
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
        Long id = extractReservationId(request);

        authorize(id, name);
        return true;
    }

    private String extractName(HttpServletRequest request) {
        String name = request.getHeader("Authorization");

        if (name == null || name.isBlank()) {
            throw new MissingAuthorizationHeaderException();
        }

        return name;
    }

    private Long extractReservationId(HttpServletRequest request) {
        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
        );

        return Long.valueOf(uriVars.get("id"));
    }

    private void authorize(Long id, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservation.hasSameName(name)) {
            throw new ForbiddenException();
        }
    }
}
