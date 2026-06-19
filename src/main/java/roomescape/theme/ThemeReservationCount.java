package roomescape.theme;

/**
 * 특정 기간 동안 한 테마가 받은 예약 수. 인기 테마 랭킹의 정렬 기준이 된다.
 */
public record ThemeReservationCount(Theme theme, long count) {
}
