package roomescape.global.web;

import java.util.Objects;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.Pageable;

public class PageableHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType());
    }

    @Override
    public Pageable resolveArgument(MethodParameter parameter,
                                    ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest,
                                    WebDataBinderFactory binderFactory) {
        int page = parsePage(webRequest);
        int size = parseSize(webRequest);
        return new Pageable(page, size);
    }

    private int parseSize(NativeWebRequest webRequest) {
        try {
            int size = Integer.parseInt(Objects.requireNonNull(webRequest.getParameter("size")));
            if (size <= 0) {
                return DEFAULT_PAGE_SIZE;
            }
            return Math.min(MAX_PAGE_SIZE, size);
        } catch (NumberFormatException | NullPointerException e) {
            return DEFAULT_PAGE_SIZE;
        }
    }

    private int parsePage(NativeWebRequest webRequest) {
        try {
            int page = Integer.parseInt(Objects.requireNonNull(webRequest.getParameter("page")));
            if (page <= 0) {
                return DEFAULT_PAGE_NUMBER;
            }
            return page;
        } catch (NumberFormatException | NullPointerException e) {
            return DEFAULT_PAGE_NUMBER;
        }
    }
}
