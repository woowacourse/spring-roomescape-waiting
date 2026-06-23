package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidReservationStateException;
import roomescape.exception.UnauthorizedReservationException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ThemeSimpleResponse;
import roomescape.reservation.dto.response.TimeResponse;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
    }

    public ReservationCreateResponse create(ReservationRequest request) {
        reservationTimeDao.findById(request.timeId());
        boolean isExistSlot = reservationDao.findByDateTimeThemeStatus(request.date(), request.timeId(), request.themeId());

        boolean isAlreadyExist = reservationDao.findByNameAndDateAndTimeAndTheme(request.name(), request.date(), request.timeId(), request.themeId());
        if (isAlreadyExist) {
            throw new DuplicateReservationException("이미 예약된 예약을 중복 예약할 수 없습니다.");
        }

        ReservationStatus status = ReservationStatus.PENDING;
        if (isExistSlot) {
            status = ReservationStatus.WAITING;
        }

        Reservation reservation = reservationDao.insert(request.name(), LocalDate.parse(request.date()), request.timeId(), request.themeId(), status);
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reservation deleteMyReservation(Long id, String name) {
        Reservation reservation = reservationDao.findById(id);
        validateReservationAuthority(name, reservation);
        validateIsNotReserved(reservation, ReservationStatus.RESERVED, "예약 상태의 예약만 취소할 수 있습니다.");
        reservationDao.delete(id);

        return reservation;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void promoteFirstWaiting(Reservation reservation) {
        boolean slotTaken = reservationDao.findByDateTimeThemeStatus(
            reservation.getDate().toString(), reservation.getTime().getId(), reservation.getTheme().getId()
        );
        if (slotTaken) {
            throw new IllegalStateException("해당 슬롯에 이미 확정 예약이 존재합니다.");
        }
        reservationDao.findFirstWaitingByDateTimeTheme(
            reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        ).ifPresent(waiting -> {
            waiting.promote(ReservationStatus.RESERVED);
            reservationDao.updateStatus(waiting.getId(), ReservationStatus.RESERVED);
        });
    }

    public boolean existsByTimeId(Long timeId) {
        return reservationDao.existsByTimeId(timeId);
    }

    public void deleteWaitingByNameAndReservationId(String name, Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        validateReservationAuthority(name, reservation);
        validateIsNotReserved(reservation, ReservationStatus.WAITING, "대기 상태의 예약만 취소할 수 있습니다.");
        reservationDao.delete(reservationId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reservation updateMyReservation(UpdateMyReservation updateMyReservation, String name, Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        validateReservationAuthority(name, reservation);
        isReservationExists(updateMyReservation.date(), updateMyReservation.timeId(), reservation.getTheme().getId());
        reservationDao.updateReservation(updateMyReservation.date(), updateMyReservation.timeId(), name, reservationId);

        return reservation;
    }

    public List<MyReservationResponse> findAllByName(String name) {
        return reservationDao.findAllByName(name).stream()
                .map(r -> new MyReservationResponse(
                        r.id(),
                        r.name(),
                        r.date(),
                        new TimeResponse(r.timeId(), r.startAt()),
                        new ThemeSimpleResponse(r.themeId(), r.themeName()),
                        r.status(),
                        r.waitRank()
                ))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void restoreReservation(Long id) {
        Reservation reservation = reservationDao.findById(id);
        reservation.restore();
        reservationDao.updateStatus(id, ReservationStatus.RESERVED);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void revertReservationUpdate(Long reservationId, LocalDate originalDate,
        Long originalTimeId, String name) {
        reservationDao.updateReservation(originalDate, originalTimeId, name, reservationId);
    }

    private static void validateIsNotReserved(Reservation reservation, ReservationStatus reserved,
        String message) {
        if (reservation.getStatus() != reserved) {
            throw new InvalidReservationStateException(message);
        }
    }

    private static void validateReservationAuthority(String name, Reservation reservation) {
        if (!Objects.equals(reservation.getName(), name)) {
            throw new UnauthorizedReservationException("다른 사람의 예약은 변경할 수 없습니다.");
        }
    }

    public void confirm(Long reservationId) {
        reservationDao.updateStatus(reservationId, ReservationStatus.RESERVED);
    }

    private void isReservationExists(LocalDate date, Long timeId, Long themeId) {
        boolean reservationExist = reservationDao.existsByTimeIdAndThemeId(date, timeId, themeId);
        if (reservationExist) {
            throw new DuplicateReservationException("해당 시간대는 이미 예약이 완료되었습니다.");
        }
    }
}
