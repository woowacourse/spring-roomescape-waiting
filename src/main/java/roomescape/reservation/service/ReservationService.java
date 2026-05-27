package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.dao.ReservationDAO;
import roomescape.reservation.dao.ReservationTimeDAO;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ThemeSimpleResponse;
import roomescape.reservation.dto.response.TimeResponse;

@Service
public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final ReservationTimeDAO reservationTimeDAO;

    public ReservationService(ReservationDAO reservationDAO, ReservationTimeDAO reservationTimeDAO) {
        this.reservationDAO = reservationDAO;
        this.reservationTimeDAO = reservationTimeDAO;
    }

    public ReservationCreateResponse create(ReservationRequest request) {
        reservationTimeDAO.findById(request.timeId());
        boolean isExistSlot = reservationDAO.findByDateTimeTheme(request.date(), request.timeId(), request.themeId());
        /*
        중복 예약 금지
         */
        boolean isAlreadyExist = reservationDAO.findByNameAndDateAndTimeAndTheme(request.name(), request.date(), request.timeId(), request.themeId());
        if (isAlreadyExist) {
            throw new IllegalStateException("[ERROR] 이미 예약된 예약을 중복 예약할 수 없습니다.");
        }

        ReservationStatus status = ReservationStatus.RESERVED;
        if (isExistSlot) {
            status = ReservationStatus.WAITING;
        }
        Reservation reservation = reservationDAO.insert(request.name(), LocalDate.parse(request.date()), request.timeId(), request.themeId(), status);

        return ReservationCreateResponse.from(reservation);
    }

    public List<ReservationResponse> findAll() {
        return reservationDAO.findAll().stream()
                .map(reservation -> ReservationResponse.of(
                        reservation.getId(),
                        reservation.getName(),
                        reservation.getDate(),
                        TimeResponse.from(reservation.getTime()),
                        ThemeSimpleResponse.from(reservation.getTheme())
                )).toList();
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationDAO.findById(id);
        return ReservationResponse.of(reservation.getId(), reservation.getName(), reservation.getDate(), TimeResponse.from(reservation.getTime()), ThemeSimpleResponse.from(reservation.getTheme()));
    }

    public void delete(Long id) {
        reservationDAO.delete(id);
    }

    public boolean existsByTimeId(Long timeId) {
        return reservationDAO.existsByTimeId(timeId);
    }
}
