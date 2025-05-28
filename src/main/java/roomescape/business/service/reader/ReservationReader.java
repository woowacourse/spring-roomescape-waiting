package roomescape.business.service.reader;

import roomescape.business.dto.MyReservationDto;
import roomescape.business.dto.ReservationDto;

import java.time.LocalDate;
import java.util.List;

public interface ReservationReader {

    /**
     * 예약 확정된 예약들을 핉터링하여 반환합니다.
     *
     * @param themeIdValue 필터링할 테마 아이디
     * @param userIdValue  필터링할 유저 아이디
     * @param dateFrom     필터링 시작 날짜
     * @param dateTo       필터링 종료 날짜
     * @return 예약 확정된 예약 응답들
     */
    List<ReservationDto> getAll(String themeIdValue, String userIdValue, LocalDate dateFrom, LocalDate dateTo);

    /**
     * 내 예약들을 대기번호와 함께 반환합니다.
     *
     * @param userIdValue 유저 아이디
     * @return 대기번호가 포함된 예약 응답들
     */
    List<MyReservationDto> getMyReservations(String userIdValue);
}
