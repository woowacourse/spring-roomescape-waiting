package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              MemberRepository memberRepository,
                              ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Reservation create(Reservation reservation) {
        validateReservationDate(reservation);
        validateReservationCapacity(reservation);
        validateDuplicatedReservation(reservation);
        return reservationRepository.save(reservation);
    }

    private void validateReservationDate(Reservation reservation) {
        LocalDate today = LocalDate.now();
        if (reservation.isBeforeOrOnToday(today)) {
            throw new ViolationException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    private void validateReservationCapacity(Reservation reservation) {
        boolean existInSameTime = reservationRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
        if (existInSameTime && reservation.isBooking()) {
            throw new ViolationException("해당 시간대에 예약이 모두 찼습니다.");
        }
        if (!existInSameTime && reservation.isWaiting()) {
            throw new ViolationException("해당 시간대에 예약이 가능합니다. 대기 말고 예약을 해주세요.");
        }
    }

    private void validateDuplicatedReservation(Reservation reservation) {
        boolean existDuplicatedReservation = reservationRepository.existsByDateAndTimeAndThemeAndMember(
                reservation.getDate(), reservation.getTime(), reservation.getTheme(), reservation.getMember());
        if (existDuplicatedReservation) {
            throw new ViolationException("동일한 사용자의 중복된 예약입니다.");
        }
    }

    public List<Reservation> findReservations() {
        return reservationRepository.findAllByStatusWithDetails(ReservationStatus.BOOKING);
    }

    public List<Reservation> findReservationsByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId,
                                                                                LocalDate fromDate, LocalDate toDate) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 Id의 사용자가 없습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당 Id의 테마가 없습니다."));
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, fromDate, toDate);
    }

    public List<Reservation> findReservationsInBookingByMember(Member member) {
        return reservationRepository.findAllByMemberAndStatusWithDetails(member, ReservationStatus.BOOKING);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.findById(id).ifPresent(reservation -> {
            validateInBooking(reservation);
            reservationRepository.delete(reservation);
            approveFirstWaitingReservation(reservation);
        });
    }

    private void validateInBooking(Reservation reservation) {
        if (!reservation.isBooking()) {
            throw new ViolationException("예약 상태가 예약 중이 아닙니다.");
        }
    }

    private void approveFirstWaitingReservation(Reservation deletedReservation) {
        Optional<Reservation> firstWaitingReservation = reservationRepository.findFirstByDateAndTimeAndTheme(
                deletedReservation.getDate(), deletedReservation.getTime(), deletedReservation.getTheme());
        firstWaitingReservation.ifPresent(Reservation::changeToBooking);
    }
}
