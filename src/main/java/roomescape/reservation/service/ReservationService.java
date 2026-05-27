package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import roomescape.reservation.dao.ReservationDAO;
import roomescape.reservation.dao.ReservationTimeDAO;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
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
                        ThemeSimpleResponse.from(reservation.getTheme()),
                    reservation.getStatus()

                )).toList();
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationDAO.findById(id);
        return ReservationResponse.of(reservation.getId(), reservation.getName(),
            reservation.getDate(), TimeResponse.from(reservation.getTime()),
            ThemeSimpleResponse.from(reservation.getTheme()),
            reservation.getStatus());
    }

    public void delete(Long id) {
        reservationDAO.delete(id);
    }

    public boolean existsByTimeId(Long timeId) {
        return reservationDAO.existsByTimeId(timeId);
    }

    public void deleteByNameAndReservationId(String name, Long reservationId) {
        boolean isExistReservation = reservationDAO.existsByNameAndReservationId(name, reservationId);
        if (!isExistReservation) {
            throw new IllegalStateException("해당 예약이 이미 존재하지 않습니다.");
        }

        reservationDAO.deleteByNameAndReservationId(name, reservationId);
    }

    public void updateMyReservation(UpdateMyReservation updateMyReservation, String name, Long reservationId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        validateReservationAuthority(name, reservation);
        isReservationExists(updateMyReservation.date(), updateMyReservation.timeId(), reservation.getTheme().getId());
        reservationDAO.updateReservation(updateMyReservation.date(), updateMyReservation.timeId(), name, reservationId);
    }

    public List<ReservationResponse> findAllByName(String name) {
        return reservationDAO.findAllByName(name).stream()
            .map(r -> ReservationResponse.of(r.getId(), r.getName(), r.getDate(),
                TimeResponse.from(r.getTime()), ThemeSimpleResponse.from(r.getTheme()),
                r.getStatus()))
            .toList();
    }

    private static void validateReservationAuthority(String name, Reservation reservation) {
        if (!Objects.equals(reservation.getName(), name)) {
            throw new IllegalStateException("다른 사람의 예약은 변경할 수 없습니다.");
        }
    }

    private void isReservationExists(LocalDate date, Long timeId, Long themeId) {
        boolean reservationExist = reservationDAO.existsByTimeIdAndThemeId(date, timeId, themeId);
        if (reservationExist) {
            throw new IllegalStateException("해당 시간대는 이미 예약이 완료되었습니다.");
        }
    }
}
