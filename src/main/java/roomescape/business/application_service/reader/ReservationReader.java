package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.MyReservationDto;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationReader {

    private final Reservations reservations;

    /**
     * 예약 확정된 예약들을 핉터링하여 반환합니다.
     *
     * @param themeIdValue 필터링할 테마 아이디
     * @param userIdValue  필터링할 유저 아이디
     * @param dateFrom     필터링 시작 날짜
     * @param dateTo       필터링 종료 날짜
     * @return 예약 확정된 예약 응답들
     */
    public List<ReservationDto> getAll(final String themeIdValue, final String userIdValue, final LocalDate dateFrom, final LocalDate dateTo) {
        List<Reservation> reservations = this.reservations.findAllReservedWithFilter(Id.create(themeIdValue), Id.create(userIdValue), dateFrom, dateTo);
        return ReservationDto.fromEntities(reservations);
    }

    /**
     * 내 예약들을 대기번호와 함께 반환합니다.
     *
     * @param userIdValue 유저 아이디
     * @return 대기번호가 포함된 예약 응답들
     */
    public List<MyReservationDto> getMyReservations(final String userIdValue) {
        Map<Reservation, Integer> reservationsWithWaitingNumber = this.reservations.findAllWithWaitingNumberByUserId(Id.create(userIdValue));
        return MyReservationDto.fromMap(reservationsWithWaitingNumber);
    }
}
