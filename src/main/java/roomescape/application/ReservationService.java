package roomescape.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.dto.ReservationWithRankDto;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedException;

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
        Member member = memberRepository.getById(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        Theme theme = themeRepository.getById(themeId);

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
            throw new BadRequestException("해당 날짜/시간에 이미 예약이 존재합니다.");
        }
    }

    @Transactional
    public ReservationResponse addReservationWaiting(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    ) {
        Member member = memberRepository.getById(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        Theme theme = themeRepository.getById(themeId);

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
            throw new BadRequestException("예약이 존재하지 않아 예약 대기를 할 수 없습니다.");
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
            throw new BadRequestException("이미 예약을 하셨습니다.");
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
            throw new BadRequestException("이미 예약 대기를 하셨습니다.");
        }
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new DomainNotFoundException("해당 id의 예약이 존재하지 않습니다.");
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
                .getByIdAndStatus(reservationId, ReservationStatus.WAITING);

        if (!reservation.getMember().getId().equals(memberId)) {
            throw new UnauthorizedException("자신의 예약 대기만 취소할 수 있습니다.");
        }

        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationResponse> getReservationWaitings() {
        List<Reservation> reservations = reservationRepository.findAllByStatus(ReservationStatus.WAITING);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse approveReservationWaiting(Long id) {
        Reservation reservation = reservationRepository
                .getById(id);

        validateReservationNotWaiting(reservation);
        validateReservationExists(reservation);

        reservation.updateToReserved();

        return ReservationResponse.from(reservation);
    }

    private void validateReservationExists(Reservation reservation) {
        boolean reservationExists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                ReservationStatus.RESERVED
        );

        if (reservationExists) {
            throw new BadRequestException("해당 날짜/시간에 이미 예약이 존재합니다.");
        }
    }

    @Transactional
    public void rejectReservationWaiting(Long id) {
        Reservation reservation = reservationRepository.getById(id);

        validateReservationNotWaiting(reservation);

        reservationRepository.deleteById(id);
    }

    private void validateReservationNotWaiting(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new BadRequestException("예약 대기 상태에서만 승인할 수 있습니다.");
        }
    }
}
