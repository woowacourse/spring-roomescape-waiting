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
import roomescape.reservation.domain.WaitingReservation;

import java.time.LocalDate;
import java.util.List;

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
        validateReservationTimeSlot(reservation);
        return reservationRepository.save(reservation);
    }

    private void validateReservationDate(Reservation reservation) {
        LocalDate today = LocalDate.now();
        if (reservation.isBeforeOrOnToday(today)) {
            throw new ViolationException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    private void validateReservationTimeSlot(Reservation reservation) {
        boolean existReservationInSameTime = reservationRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
        if (existReservationInSameTime && reservation.isBooking()) {
            throw new ViolationException("해당 시간대에 예약이 모두 찼습니다.");
        }
        if (!existReservationInSameTime && reservation.isWaiting()) {
            throw new ViolationException("해당 시간대에 예약이 가능합니다. 대기 말고 예약을 해주세요.");
        }
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAllWithDetails();
    }

    public List<Reservation> findReservationsByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId,
                                                                                LocalDate fromDate, LocalDate toDate) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 Id의 사용자가 없습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당 Id의 테마가 없습니다."));
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, fromDate, toDate);
    }

    public List<Reservation> findAllInBookingByMember(Member member) {
        return reservationRepository.findAllByMemberAndStatusWithDetails(member, ReservationStatus.BOOKING);
    }

    public List<WaitingReservation> findAllInWaitingWithPreviousCountByMember(Member member) {
        return reservationRepository.findWaitingReservationsByMemberWithDetails(member);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}
