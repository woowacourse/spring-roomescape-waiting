package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.exception.DuplicateReservationException;
import roomescape.service.exception.InvalidSearchDateException;
import roomescape.service.exception.PreviousTimeException;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByMember(final LoginMember member) {
        return reservationRepository.findAllByMemberId(member.id());
    }

    public List<Reservation> searchReservations(final ReservationSearchCondition condition) {
        validateDateRange(condition);
        return reservationRepository.searchReservations(condition);
    }

    public Reservation addReservation(final CreateReservationRequest reservationRequest) {
        final ReservationTime time = reservationTimeRepository.fetchById(reservationRequest.timeId());
        final Theme theme = themeRepository.fetchById(reservationRequest.themeId());
        final Member member = memberRepository.fetchById(reservationRequest.memberId());

        final Reservation reservation = new Reservation(null, member, reservationRequest.date(), time, theme);

        validateDuplicate(theme, time, reservation);
        final LocalDateTime reservationDateTime = reservation.getDate().atTime(time.getStartAt());
        validateBeforeDay(reservationDateTime);

        return reservationRepository.save(reservation);
    }

    public void deleteReservation(final long id) {
        final Reservation fetchReservation = reservationRepository.fetchById(id);
        reservationRepository.deleteById(fetchReservation.getId());
    }

    private void validateBeforeDay(final LocalDateTime reservationDateTime) {
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new PreviousTimeException("지난 시간으로 예약할 수 없습니다.");
        }
    }

    private void validateDuplicate(final Theme theme, final ReservationTime time, final Reservation reservation) {
        final boolean isExistsReservation = reservationRepository
                .existsByThemeIdAndTimeIdAndDate(theme.getId(), time.getId(), reservation.getDate());
        if (isExistsReservation) {
            throw new DuplicateReservationException("중복된 시간으로 예약이 불가합니다.");
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
