package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationSpecification;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.ReservationSearchRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeInfoResponse;
import roomescape.reservation.dto.response.ReservationTimeInfosResponse;
import roomescape.reservation.dto.response.ReservationsResponse;
import roomescape.reservation.dto.response.WaitingWithRankResponse;
import roomescape.reservation.dto.response.WaitingWithRanksResponse;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.ForbiddenException;
import roomescape.system.exception.model.NotFoundException;
import roomescape.system.exception.model.ValidateException;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeService reservationTimeService;
    private final MemberService memberService;
    private final ThemeService themeService;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationTimeService reservationTimeService,
            final MemberService memberService,
            final ThemeService themeService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationTimeService = reservationTimeService;
        this.memberService = memberService;
        this.themeService = themeService;
    }

    public ReservationsResponse findAllReservations() {
        final List<ReservationResponse> response = reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    public ReservationTimeInfosResponse findReservationsByDateAndThemeId(final LocalDate date, final Long themeId) {
        final List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        final Theme theme = themeService.findThemeById(themeId);
        final List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        final List<ReservationTimeInfoResponse> response = getReservationTimeInfoResponses(
                allTimes, reservations);

        return new ReservationTimeInfosResponse(response);
    }

    private List<ReservationTimeInfoResponse> getReservationTimeInfoResponses(
            final List<ReservationTime> allTimes,
            final List<Reservation> reservations
    ) {
        return allTimes.stream()
                .map(time -> new ReservationTimeInfoResponse(
                        time.getId(),
                        time.getStartAt(),
                        reservations.stream()
                                .anyMatch(reservation -> reservation.getReservationTime() == time))
                )
                .toList();
    }

    public Reservation findReservationById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_NOT_FOUND,
                        String.format("예약(Reservation) 정보가 존재하지 않습니다. [reservationId: %d]", id)));
    }

    @Transactional
    public void removeReservationByIdAndPullIfMemberIsAdmin(final Long targetReservationId, final Long myMemberId) {
        final Member requestMember = memberService.findMemberById(myMemberId);
        final Reservation requestReservation = findReservationById(targetReservationId);

        validateMemberIsAdmin(myMemberId, requestMember, requestReservation);

        reservationRepository.delete(requestReservation);
        final Optional<Reservation> waitingOptional = reservationRepository.findFirstByReservationTimeAndDateAndThemeAndReservationStatusOrderById(
                requestReservation.getReservationTime(),
                requestReservation.getDate(),
                requestReservation.getTheme(),
                ReservationStatus.WAITING
        );

        pull(requestReservation, waitingOptional);
    }

    private void pull(final Reservation requestReservation, final Optional<Reservation> waitingOptional) {
        if (requestReservation.isReserved() && waitingOptional.isPresent()) {
            final Reservation waitingReservation = waitingOptional.get();
            reservationRepository.delete(waitingReservation);
            reservationRepository.save(new Reservation(
                    waitingReservation.getDate(),
                    waitingReservation.getReservationTime(),
                    waitingReservation.getTheme(),
                    waitingReservation.getMember(),
                    ReservationStatus.RESERVED
            ));
        }
    }

    private void validateMemberIsAdmin(final Long myMemberId, final Member requestMember,
                                       final Reservation requestReservation) {
        if (!requestMember.isAdmin() && !requestReservation.getMemberId().equals(myMemberId)) {
            throw new ForbiddenException(
                    ErrorType.PERMISSION_DOES_NOT_EXIST,
                    "예약(Reservation) 정보에 대한 삭제 권한이 존재하지 않습니다."
            );
        }
    }

    public ReservationResponse addReservation(final ReservationRequest request, final Long memberId) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDate requestDate = request.date();

        final ReservationTime requestTime = reservationTimeService.findTimeById(request.timeId());
        final Theme requestTheme = themeService.findThemeById(request.themeId());
        final Member member = memberService.findMemberById(memberId);

        validateDateAndTime(requestDate, requestTime, now);

        final Optional<Reservation> optional = reservationRepository.findFirstByReservationTimeAndDateAndThemeAndReservationStatusOrderById(
                requestTime, requestDate, requestTheme, ReservationStatus.RESERVED
        );
        final ReservationStatus state = optional.isEmpty() ? ReservationStatus.RESERVED : ReservationStatus.WAITING;
        final Reservation saved = reservationRepository.save(
                new Reservation(requestDate, requestTime, requestTheme, member, state));
        return ReservationResponse.from(saved);
    }

    private void validateDateAndTime(
            final LocalDate requestDate,
            final ReservationTime requestReservationTime,
            final LocalDateTime now
    ) {
        if (isReservationInPast(requestDate, requestReservationTime, now)) {
            throw new ValidateException(
                    ErrorType.RESERVATION_PERIOD_IN_PAST,
                    String.format("지난 날짜나 시간은 예약이 불가능합니다. [now: %s %s | request: %s %s]",
                            now.toLocalDate(), now.toLocalTime(), requestDate, requestReservationTime.getStartAt())
            );
        }
    }

    private boolean isReservationInPast(
            final LocalDate requestDate,
            final ReservationTime requestReservationTime,
            final LocalDateTime now
    ) {
        final LocalDate today = now.toLocalDate();
        final LocalTime nowTime = now.toLocalTime();

        if (requestDate.isBefore(today)) {
            return true;
        }
        return requestDate.isEqual(today) && requestReservationTime.getStartAt().isBefore(nowTime);
    }

    public ReservationsResponse findFilteredReservations(final ReservationSearchRequest request) {
        final Specification<Reservation> specification = getReservationSpecification(request);

        final List<ReservationResponse> response = reservationRepository.findAll(specification)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    private Specification<Reservation> getReservationSpecification(
            final ReservationSearchRequest request
    ) {
        Specification<Reservation> specification = (root, query, criteriaBuilder) -> null;
        if (request.themeId() != null) {
            specification = specification.and(
                    ReservationSpecification.withTheme(themeService.findThemeById(request.themeId()))
            );
        }
        if (request.memberId() != null) {
            specification = specification.and(
                    ReservationSpecification.withMember(memberService.findMemberById(request.memberId())));
        }
        if (request.dateFrom() != null) {
            specification = specification.and(ReservationSpecification.withDateFrom(request.dateFrom()));
        }
        if (request.dateTo() != null) {
            specification = specification.and(ReservationSpecification.withDateTo(request.dateTo()));
        }
        if (request.waiting() != null) {
            specification = specification.and(ReservationSpecification.withWaiting(request.waiting()));
        }
        return specification;
    }

    public WaitingWithRanksResponse findWaitingWithRankById(final Long myId) {
        return reservationRepository.findWaitingsWithRankByMemberId(myId)
                .stream()
                .map(WaitingWithRankResponse::from)
                .collect(Collectors.collectingAndThen(Collectors.toList(), WaitingWithRanksResponse::new));
    }

    public void removeReservationByStatus(final Long myId, final Long targetReservationId, final String status) {
        String STATUS_DECLINE = "decline";
        if (!status.equals(STATUS_DECLINE)) {
            return;
        }
        removeReservationByIdAndPullIfMemberIsAdmin(targetReservationId, myId);
    }
}
