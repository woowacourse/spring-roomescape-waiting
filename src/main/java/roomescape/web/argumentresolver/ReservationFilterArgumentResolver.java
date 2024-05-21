package roomescape.web.argumentresolver;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.dto.reservation.ReservationFilter;

@Component
public class ReservationFilterArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(ReservationFilter.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        return createReservationFilter(request);
    }

    private ReservationFilter createReservationFilter(HttpServletRequest request) {
        ReservationFilter filter = new ReservationFilter();
        String memberId = request.getParameter("member");
        String themeId = request.getParameter("theme");
        String dateFrom = request.getParameter("dateFrom");
        String dateTo = request.getParameter("dateTo");

        try {
            if (memberId != null && !memberId.trim().isEmpty()) {
                filter.setMemberId(Long.parseLong(memberId));
            }
            if (themeId != null && !themeId.trim().isEmpty()) {
                filter.setThemeId(Long.parseLong(themeId));
            }
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                filter.setStartDate(LocalDate.parse(dateFrom));
            }
            if (dateTo != null && !dateTo.trim().isEmpty()) {
                filter.setEndDate(LocalDate.parse(dateTo));
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "[ERROR] 요청 형식이 올바르지 않습니다.",
                    new Throwable("memberId = " + memberId +
                            ", themeId = " + themeId +
                            ", dateFrom = " + dateFrom +
                            ", dateTo = " + dateTo)
            );
        }

        return filter;
    }
}
