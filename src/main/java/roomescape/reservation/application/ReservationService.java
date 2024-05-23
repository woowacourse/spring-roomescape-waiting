package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Theme;

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
        boolean existReservationInSameTime = reservationRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
        if (existReservationInSameTime) {
            throw new ViolationException("해당 시간대에 예약이 모두 찼습니다.");
        }
    }

    @Transactional
    public Reservation createWaitingReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate fromDate, LocalDate toDate) {
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, fromDate, toDate);
    }

    public List<Reservation> findAllByMember(Member loginMember) {
        return reservationRepository.findAllByMember(loginMember);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}
