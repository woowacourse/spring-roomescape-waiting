package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ErrorTypeHttpStatusMapper {

    public HttpStatus statusOf(ErrorType errorType) {
        return switch (errorType) {
            case INVALID_DOMAIN, INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case RESOURCE_NOT_FOUND, ENDPOINT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RESERVATION_OWNER_MISMATCH, STORE_MANAGEMENT_FORBIDDEN, UNAUTHORIZED -> HttpStatus.FORBIDDEN;
            case DUPLICATE_RESERVATION, RESERVATION_TIME_IN_USE, RESERVATION_NOT_FOUND_FOR_WAITING,
                 RESERVATION_NOT_RESERVED, DUPLICATE_WAITING_RESERVATION, DUPLICATE_USERNAME, DUPLICATE_ORDER_ID ->
                    HttpStatus.CONFLICT;
            case PAST_DATE_TIME_RESERVATION, PAST_RESERVATION_MODIFICATION, NON_PAST_RESERVATION_DELETION ->
                    HttpStatus.UNPROCESSABLE_ENTITY;
            case INVALID_LOGIN, UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case INVALID_REQUEST, PAYMENT_AMOUNT_MISMATCH -> HttpStatus.BAD_REQUEST;
        };
    }
}
