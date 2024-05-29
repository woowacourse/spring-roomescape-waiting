package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.*;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
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
        validateReservationDate(reservation);
        validateDuplicatedWaitingReservation(reservation);

        Waiting waiting = Waiting.from(reservation);
        waitingRepository.save(waiting);

        return reservationRepository.save(reservation);
    }

    private void validateDuplicatedWaitingReservation(Reservation reservation) {
        boolean existReservationInSameTime = reservationRepository.existsByMemberAndDateAndTimeAndTheme(
                reservation.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
        if (existReservationInSameTime) {
            throw new ViolationException("이미 예약 또는 대기를 신청하셨습니다.");
        }
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
    public Reservation deleteReservation(Long id) {
        Reservation reservation = findReservation(id);
        reservationRepository.deleteById(id);

        return reservation;
    }

    @Transactional
    public void deleteWaitingReservation(Long id, Member loginMember) {
        Reservation reservation = findReservation(id);
        validateDeletePermission(reservation, loginMember);
        reservationRepository.deleteById(id);

        waitingRepository.deleteByMemberAndDateAndTimeAndTheme(
                reservation.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    private Reservation findReservation(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 예약이 없습니다."));
    }

    private void validateDeletePermission(Reservation reservation, Member loginMember) {
        if (!loginMember.isAdmin() && !reservation.isMemberMatch(loginMember)) {
            throw new ViolationException("예약 대기 삭제는 예약을 한 사용자 또는 관리자만 가능합니다.");
        }
    }
}
