package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationDto;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.exception.DuplicateReservationException;
import roomescape.service.exception.InvalidSearchDateException;
import roomescape.service.exception.PreviousTimeException;
import roomescape.service.exception.ReservationNotFoundException;

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

    public List<Reservation> getReservedReservations() {
        return reservationRepository.findAllByStatus(Status.RESERVED);
    }

    public List<Reservation> getWaitingReservations() {
        return reservationRepository.findAllByStatus(Status.WAITING);
    }

    public List<Reservation> getReservationsByMember(final LoginMember member) {
        return reservationRepository.findAllByMemberId(member.id());
    }

    public List<Reservation> searchReservations(final ReservationSearchCondition condition) {
        validateDateRange(condition);
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                condition.themeId(), condition.memberId(),
                condition.dateFrom(), condition.dateTo());
    }

    public Reservation addReservation(final CreateReservationDto reservationDto) {
        validateDuplicate(reservationDto);
        validateDuplicateWaiting(reservationDto);
        final LocalDate date = reservationDto.date();
        final ReservationTime time = reservationTimeRepository
                .findByIdOrThrow(reservationDto.timeId());
        validateBeforeDay(date, time);

        final Theme theme = themeRepository.findByIdOrThrow(reservationDto.themeId());
        final Member member = memberRepository.findByEmailOrThrow(reservationDto.memberId());
        final Reservation reservation = new Reservation(null, member, date, time, theme,
                reservationDto.status());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation setWaitingReservationReserved(final long id) {
        final Reservation waitingReservation = reservationRepository.findByIdOrThrow(id);
        if (waitingReservation.isReserved()) {
            throw new ReservationNotFoundException("해당 시간에 예약이 존재합니다.");
        }
        Reservation reservation = addReservation(new CreateReservationDto(
                waitingReservation.getMember().getId(),
                waitingReservation.getTheme().getId(),
                waitingReservation.getDate(),
                waitingReservation.getTime().getId(),
                Status.RESERVED));
        reservationRepository.deleteById(waitingReservation.getId());
        return reservation;
    }

    @Transactional
    public void deleteReservation(final long id) {
        final Reservation fetchReservation = reservationRepository.findByIdOrThrow(id);
        reservationRepository.deleteById(fetchReservation.getId());
        Optional<Reservation> firstWaitingReservation = reservationRepository
                .findFirstByTimeIdAndThemeIdAndDateAndStatus(
                        fetchReservation.getTime().getId(),
                        fetchReservation.getTheme().getId(),
                        fetchReservation.getDate(),
                        Status.WAITING
                );
        firstWaitingReservation.ifPresent(
                reservation -> setWaitingReservationReserved(reservation.getId()));
    }

    private void validateDateRange(final ReservationSearchCondition request) {
        if (request.dateFrom() == null || request.dateTo() == null) {
            return;
        }
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new InvalidSearchDateException("from은 to보다 이전 날짜여야 합니다.");
        }
    }

    private void validateDuplicate(final CreateReservationDto reservationDto) {
        final boolean isExistReservation =
                reservationRepository.existsByThemeIdAndTimeIdAndDateAndStatus(
                        reservationDto.themeId(),
                        reservationDto.timeId(),
                        reservationDto.date(),
                        Status.RESERVED);
        if (isExistReservation && reservationDto.status() == Status.RESERVED) {
            throw new DuplicateReservationException("중복된 시간으로 예약이 불가합니다.");
        }
        if (!isExistReservation && reservationDto.status() == Status.WAITING) {
            throw new DuplicateReservationException("예약이 존재하지 않을 경우 예약대기할 수 없습니다.");
        }
    }

    private void validateDuplicateWaiting(final CreateReservationDto reservationDto) {
        final boolean isExistReservation =
                reservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDateAndStatus(
                        reservationDto.memberId(),
                        reservationDto.themeId(),
                        reservationDto.timeId(),
                        reservationDto.date(),
                        Status.WAITING);
        if (isExistReservation && reservationDto.status() == Status.WAITING) {
            throw new DuplicateReservationException("중복된 시간으로 예약대기가 불가합니다.");
        }
    }

    private void validateBeforeDay(final LocalDate date, final ReservationTime time) {
        final LocalDateTime reservationDateTime = date.atTime(time.getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new PreviousTimeException("지난 시간으로 예약할 수 없습니다.");
        }
    }
}
