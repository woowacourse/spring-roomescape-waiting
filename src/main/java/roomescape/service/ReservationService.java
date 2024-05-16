package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.exception.UnauthorizedException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWithRankDto;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public List<ReservationResponse> getReservationsByConditions(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository
                .findAllByConditions(memberId, themeId, dateFrom, dateTo);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse addReservation(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    ) {
        Member member = memberRepository.getByIdentifier(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getByIdentifier(timeId);
        Theme theme = themeRepository.getByIdentifier(themeId);

        Reservation reservation = Reservation.create(
                LocalDateTime.now(clock),
                date,
                member,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        );

        validateDuplicatedReservation(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    private void validateDuplicatedReservation(Reservation reservation) {
        if (reservationRepository.existsByReservation(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            throw new IllegalArgumentException("해당 날짜/시간에 이미 예약이 존재합니다.");
        }
    }

    @Transactional
    public ReservationResponse addReservationWaiting(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    ) {
        Member member = memberRepository.getByIdentifier(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getByIdentifier(timeId);
        Theme theme = themeRepository.getByIdentifier(themeId);

        Reservation reservation = Reservation.create(
                LocalDateTime.now(clock),
                date,
                member,
                reservationTime,
                theme,
                ReservationStatus.WAITING
        );

        validateReservationNotExists(reservation);
        validateAlreadyReserved(reservation);
        validateReservationWaitingExists(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    private void validateReservationNotExists(Reservation reservation) {
        boolean reservationNotExists = !reservationRepository.existsByReservation(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );

        if (reservationNotExists) {
            throw new IllegalArgumentException("예약이 존재하지 않아 예약 대기를 할 수 없습니다.");
        }
    }

    private void validateAlreadyReserved(Reservation reservation) {
        boolean reservationExists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId(),
                ReservationStatus.RESERVED
        );

        if (reservationExists) {
            throw new IllegalArgumentException("이미 예약을 하셨습니다.");
        }
    }

    private void validateReservationWaitingExists(Reservation reservation) {
        boolean reservationWaitingExists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId(),
                ReservationStatus.WAITING
        );

        if (reservationWaitingExists) {
            throw new IllegalArgumentException("이미 예약 대기를 하셨습니다.");
        }
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NoSuchElementException("해당 id의 예약이 존재하지 않습니다.");
        }

        reservationRepository.deleteById(id);
    }

    public List<MyReservationResponse> getMyReservationWithRanks(long memberId) {
        List<ReservationWithRankDto> reservations = reservationRepository
                .findReservationWithRanksByMemberId(memberId);

        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservationWaitingById(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository
                .findByIdAndStatus(reservationId, ReservationStatus.WAITING)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 예약 대기가 존재하지 않습니다."));

        if (!reservation.getMember().getId().equals(memberId)) {
            throw new UnauthorizedException("자신의 예약 대기만 취소할 수 있습니다.");
        }

        reservationRepository.deleteById(reservationId);
    }
}
