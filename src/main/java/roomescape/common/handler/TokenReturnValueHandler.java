package roomescape.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.auth.annotation.SendToken;
import roomescape.member.controller.dto.response.TokenDto;

public class TokenReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(SendToken.class);
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (response == null || request == null) {
            return;
        }

        if (!(returnValue instanceof ResponseEntity<?> responseEntity)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (!(responseEntity.getBody() instanceof TokenDto tokenDto)) {
            response.setStatus(responseEntity.getStatusCode().value());
            return;
        }

        response.setStatus(responseEntity.getStatusCode().value());
        sendToken(tokenDto, request, response);
    }

    private void sendToken(TokenDto tokenDto, HttpServletRequest request, HttpServletResponse response) {
        if (isRoomEscapeApp(request)) {
            response.addHeader(HttpHeaders.SET_COOKIE, createTokenCookie(tokenDto).toString());
            return;
        }
        response.setHeader(HttpHeaders.AUTHORIZATION, tokenDto.token());
    }

    private boolean isRoomEscapeApp(HttpServletRequest request) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        return userAgent != null && userAgent.contains("room-escape-app");
    }

    private ResponseCookie createTokenCookie(TokenDto tokenDto) {
        return ResponseCookie.from("Authorization", tokenDto.token())
                .httpOnly(true)
                .path("/")
                .sameSite("None")
                .secure(false)
                .build();
    }

}
