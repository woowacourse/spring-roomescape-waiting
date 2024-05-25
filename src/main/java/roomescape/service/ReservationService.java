package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.controller.time.dto.IsMineRequest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.service.exception.DeletingException;
import roomescape.service.exception.DuplicateReservationException;
import roomescape.service.exception.InvalidSearchDateException;
import roomescape.service.exception.PreviousTimeException;
import roomescape.service.exception.UserDeleteReservationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservations() {
        final Set<ReservationInfo> preReservations = new HashSet<>();

        return reservationRepository.findAll()
                .stream()
                .filter(reservation -> preReservations.add(ReservationInfo.from(reservation)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationRankResponse> getMyReservation(final LoginMember member) {
        return reservationRepository.findMyReservation(member.id());
    }

    @Transactional(readOnly = true)
    public List<Reservation> searchReservations(final ReservationSearchCondition condition) {
        validateDateRange(condition);
        return reservationRepository.findReservationsByCondition(condition.dateFrom(), condition.dateTo(),
                condition.themeId(), condition.memberId());
    }

    @Transactional
    public Reservation addReservation(final CreateReservationRequest request) {
        final ReservationTime time = reservationTimeRepository.fetchById(request.timeId());
        final Theme theme = themeRepository.fetchById(request.themeId());
        final Member member = memberRepository.fetchById(request.memberId());
        final LocalDate date = request.date();

        final Reservation reservation = new Reservation(null, member, date, time, theme);

        final LocalDateTime reservationDateTime = reservation.getDate().atTime(time.getStartAt());
        validateBeforeDay(reservationDateTime);
        validateReservation(member, theme, time, date);

        return reservationRepository.save(reservation);
    }

    private void validateReservation(final Member member, final Theme theme, final ReservationTime time, final LocalDate date) {
        final boolean bookedAlready = reservationRepository
                .existsByMemberAndThemeAndTimeAndDate(member, theme, time, date);
        if (bookedAlready) {
            throw new DuplicateReservationException("같은 테마, 날짜, 시간으로 예약이 존재합니다.");
        }
    }

    @Transactional
    public void deleteReservation(final long id) {
        final Reservation fetchReservation = reservationRepository.fetchById(id);
        reservationRepository.deleteById(fetchReservation.getId());
    }

    @Transactional
    public void deleteWaitReservation(final long reservationId, final long memberId) {
        final Reservation fetchReservation = reservationRepository.fetchById(reservationId);
        if (!fetchReservation.isOwn(memberId)) {
            throw new DeletingException("다른 회원의 예약 대기는 취소 불가합니다.");
        }

        final boolean isWaitReservation = reservationRepository.existsByIdBeforeAndThemeIdAndTimeIdAndDate(reservationId,
                fetchReservation.getTheme().getId(), fetchReservation.getTime().getId(), fetchReservation.getDate());

        if (!isWaitReservation) {
            throw new UserDeleteReservationException("유저는 예약 대기만 삭제 가능합니다.");
        }
        reservationRepository.deleteById(fetchReservation.getId());
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllWaiting() {
        return reservationRepository.findAllWaiting();
    }

    @Transactional(readOnly = true)
    public boolean isMyReservation(final IsMineRequest request, final LoginMember loginMember) {
        return reservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(loginMember.id(), request.themeId(),
                request.timeId(), request.date());
    }

    private void validateBeforeDay(final LocalDateTime reservationDateTime) {
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new PreviousTimeException("지난 시간으로 예약할 수 없습니다.");
        }
    }

    private void validateDateRange(final ReservationSearchCondition request) {
        if (request.dateFrom() == null || request.dateTo() == null) {
            return;
        }
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new InvalidSearchDateException("from은 to보다 이전 날짜여야 합니다.");
        }
    }
}
