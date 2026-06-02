package roomescape.reservation.service;

@FunctionalInterface
interface RetryableOperation<T> {
    T execute();
}
