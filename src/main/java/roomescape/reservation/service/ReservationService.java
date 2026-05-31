package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ThemeSimpleResponse;
import roomescape.reservation.dto.response.TimeResponse;
import roomescape.theme.domain.Theme;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;

    public ReservationService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public ReservationCreateResponse create(ReservationRequest request, ReservationTime time, Theme theme) {
        boolean isExistSlot = reservationDao.findByDateTimeTheme(request.date(), time.getId(), theme.getId());

        boolean isAlreadyExist = reservationDao.findByNameAndDateAndTimeAndTheme(request.name(), request.date(), time.getId(), theme.getId());
        if (isAlreadyExist) {
            throw new IllegalStateException("[ERROR] 이미 예약된 예약을 중복 예약할 수 없습니다.");
        }

        ReservationStatus status = ReservationStatus.RESERVED;
        if (isExistSlot) {
            status = ReservationStatus.WAITING;
        }
        Reservation reservation = reservationDao.insert(request.name(), LocalDate.parse(request.date()), time, theme, status);

        return ReservationCreateResponse.from(reservation);
    }

    public List<ReservationResponse> findAll() {
        return reservationDao.findAll().stream()
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
        Reservation reservation = reservationDao.findById(id);
        return ReservationResponse.of(reservation.getId(), reservation.getName(),
            reservation.getDate(), TimeResponse.from(reservation.getTime()),
            ThemeSimpleResponse.from(reservation.getTheme()),
            reservation.getStatus());
    }

    public void delete(Long id) {
        reservationDao.delete(id);
    }

    public boolean existsByTimeId(Long timeId) {
        return reservationDao.existsByTimeId(timeId);
    }

    public void deleteByNameAndReservationId(String name, Long reservationId) {
        boolean isExistReservation = reservationDao.existsByNameAndReservationId(name, reservationId);
        if (!isExistReservation) {
            throw new IllegalStateException("해당 예약이 이미 존재하지 않습니다.");
        }

        reservationDao.deleteByNameAndReservationId(name, reservationId);
    }

    public void updateMyReservation(UpdateMyReservation updateMyReservation, String name, Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        validateReservationAuthority(name, reservation);
        isReservationExists(updateMyReservation.date(), updateMyReservation.timeId(), reservation.getTheme().getId());
        reservationDao.updateReservation(updateMyReservation.date(), updateMyReservation.timeId(), name, reservationId);
    }

    public List<MyReservationResponse> findAllByName(String name) {
        return reservationDao.findAllByName(name);
    }

    private static void validateReservationAuthority(String name, Reservation reservation) {
        if (!Objects.equals(reservation.getName(), name)) {
            throw new IllegalStateException("다른 사람의 예약은 변경할 수 없습니다.");
        }
    }

    private void isReservationExists(LocalDate date, Long timeId, Long themeId) {
        boolean reservationExist = reservationDao.existsByTimeIdAndThemeId(date, timeId, themeId);
        if (reservationExist) {
            throw new IllegalStateException("해당 시간대는 이미 예약이 완료되었습니다.");
        }
    }
}
