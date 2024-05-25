package roomescape.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roomescape.exception.AuthorizationExpiredException;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.security.service.MemberAuthService;
import roomescape.reservation.dto.ReservationCreateRequest;

public class ReservationArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberAuthService memberAuthService;

    public ReservationArgumentResolver(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType()
                .equals(ReservationCreateRequest.class);
    }

    @Override
    public ReservationCreateRequest resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Cookie[] cookies = Objects.requireNonNull(request).getCookies();

        if (memberAuthService.isLoginMember(cookies)) {
            MemberProfileInfo payload = memberAuthService.extractPayload(cookies);
            ReservationCreateRequest reservationRequest = convertToRequestBody(request);
            return new ReservationCreateRequest(
                    payload.id(),
                    reservationRequest.themeId(),
                    reservationRequest.timeId(),
                    LocalDate.from(reservationRequest.date())
            );
        }
        throw new AuthorizationExpiredException();
    }

    private ReservationCreateRequest convertToRequestBody(HttpServletRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            return objectMapper.readValue(reader, ReservationCreateRequest.class);
        } catch (IOException e) {
            throw new AuthorizationExpiredException();
        }
    }
}
