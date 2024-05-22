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
        List<Reservation> reservationsInSameTime = reservationRepository.findAllByDateAndTimeAndThemeWithMember(
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
        validateDuplicatedReservation(reservation, reservationsInSameTime);
        validateReservationCapacity(reservation, reservationsInSameTime);
        return reservationRepository.save(reservation);
    }

    private void validateReservationDate(Reservation reservation) {
        LocalDate today = LocalDate.now();
        if (reservation.isBeforeOrOnToday(today)) {
            throw new ViolationException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    private void validateDuplicatedReservation(Reservation reservation, List<Reservation> reservationsInSameTime) {
        boolean existDuplicatedReservation = reservationsInSameTime.stream()
                .map(Reservation::getMember)
                .anyMatch(reservation::hasSameOwner);
        if (existDuplicatedReservation) {
            throw new ViolationException("동일한 사용자의 중복된 예약입니다.");
        }
    }

    private void validateReservationCapacity(Reservation reservation, List<Reservation> reservationsInSameTime) {
        int countInSameTime = reservationsInSameTime.size();
        if (countInSameTime > 0 && reservation.isBooking()) {
            throw new ViolationException("해당 시간대에 예약이 모두 찼습니다.");
        }
        if (countInSameTime == 0 && reservation.isWaiting()) {
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

    public List<Reservation> findReservationsInBookingByMember(Member member) {
        return reservationRepository.findAllByMemberAndStatusWithDetails(member, ReservationStatus.BOOKING);
    }

    public List<WaitingReservation> findWaitingReservationsWithPreviousCountByMember(Member member) {
        return reservationRepository.findWaitingReservationsByMemberWithDetails(member);
    }

    public List<Reservation> findWaitingReservations() {
        return reservationRepository.findAllByStatusWithDetails(ReservationStatus.WAITING);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = findByIdIfNotPresentThrowException(id);
        validateReservationStatus(reservation, ReservationStatus.BOOKING);
        reservationRepository.deleteById(id);

        Optional<Reservation> firstWaitingReservation = reservationRepository.findFirstByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
        firstWaitingReservation.ifPresent(waitingReservation ->
                waitingReservation.updateStatus(ReservationStatus.BOOKING));
    }

    @Transactional
    public void deleteWaitingReservationByMember(Long reservationId, Member member) {
        Reservation reservation = findByIdIfNotPresentThrowException(reservationId);
        validateReservationStatus(reservation, ReservationStatus.WAITING);
        validateOwnerShip(reservation, member);
        reservationRepository.delete(reservation);
    }

    private void validateOwnerShip(Reservation reservation, Member member) {
        Long memberId = member.getId();
        Long ownerId = reservation.getMember().getId();
        if (!memberId.equals(ownerId)) {
            throw new ViolationException("본인의 예약 대기만 삭제할 수 있습니다.");
        }
    }

    @Transactional
    public void deleteWaitingReservationByAdmin(Long reservationId) {
        Reservation reservation = findByIdIfNotPresentThrowException(reservationId);
        validateReservationStatus(reservation, ReservationStatus.WAITING);
        reservationRepository.delete(reservation);
    }

    private Reservation findByIdIfNotPresentThrowException(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("해당 Id의 예약이 없습니다."));
    }

    private void validateReservationStatus(Reservation reservation, ReservationStatus status) {
        if (!reservation.hasSameStatus(status)) {
            throw new ViolationException("상태가 " + status + "인 예약이 아닙니다.");
        }
    }
}
