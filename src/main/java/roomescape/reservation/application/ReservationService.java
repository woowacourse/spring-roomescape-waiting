package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ViolationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation create(Reservation reservation) {
        validateReservationDate(reservation);
        validateDuplicatedReservation(reservation);
        return reservationRepository.save(reservation);
    }

    private void validateReservationDate(Reservation reservation) {
        LocalDate today = LocalDate.now();
        if (reservation.isBeforeOrOnToday(today)) {
            throw new ViolationException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    private void validateDuplicatedReservation(Reservation reservation) {
        boolean existReservationInSameTime = reservationRepository.existByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getReservationTimeId(), reservation.getThemeId());
        if (existReservationInSameTime) {
            throw new ViolationException("해당 시간대에 예약이 모두 찼습니다.");
        }
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId,
                                                                               LocalDate fromDate, LocalDate toDate) {
        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(memberId, themeId, fromDate, toDate);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}
