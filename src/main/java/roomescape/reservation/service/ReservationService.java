package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.ForbiddenException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationSpecification;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.FilteredReservationRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MemberReservationResponse;
import roomescape.reservation.dto.response.MemberReservationsResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeInfoResponse;
import roomescape.reservation.dto.response.ReservationTimeInfosResponse;
import roomescape.reservation.dto.response.ReservationsResponse;
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

        final List<ReservationTimeInfoResponse> response = new ArrayList<>();
        for (final ReservationTime time : allTimes) {
            boolean alreadyBooked = false;
            for (final Reservation reservation : reservations) {
                if (reservation.getReservationTime() == time) {
                    alreadyBooked = true;
                    break;
                }
            }
            response.add(new ReservationTimeInfoResponse(time.getId(), time.getStartAt(), alreadyBooked));
        }

        return new ReservationTimeInfosResponse(response);
    }

    public Reservation findReservationById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_NOT_FOUND,
                        String.format("예약(Reservation) 정보가 존재하지 않습니다. [reservationId: %d]", id)));
    }

    public void removeReservationById(final Long reservationId, final Long requestMemberId) {
        final Member member = memberService.findMemberById(requestMemberId);
        final Reservation reservation = findReservationById(reservationId);
        final Long reservationMemberId = reservation.getMember().getId();

        if (member.isAdmin() || reservationMemberId.equals(requestMemberId)) {
            reservationRepository.deleteById(reservation.getId());
            return;
        }
        throw new ForbiddenException(
                ErrorType.PERMISSION_DOES_NOT_EXIST,
                "예약(Reservation) 정보에 대한 삭제 권한이 존재하지 않습니다."
        );
    }

    public ReservationResponse addReservation(final ReservationRequest request, final Long memberId) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDate requestDate = request.date();

        final ReservationTime requestReservationTime = reservationTimeService.findTimeById(request.timeId());
        final Theme theme = themeService.findThemeById(request.themeId());
        final Member member = memberService.findMemberById(memberId);

        validateDateAndTime(requestDate, requestReservationTime, now);
        validateReservationDuplicate(request, theme);

        // TODO: 예약대기 추가 시, ReservationStatus 값 설정 로직 추가
        final Reservation savedReservation = reservationRepository.save(
                request.toEntity(requestReservationTime, theme, member, ReservationStatus.RESERVED));
        return ReservationResponse.from(savedReservation);
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
                            now.toLocalDate(), now.toLocalTime(), requestDate, requestReservationTime.getStartAt()));
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

    private void validateReservationDuplicate(
            final ReservationRequest reservationRequest,
            final Theme theme
    ) {
        final ReservationTime time = reservationTimeService.findTimeById(reservationRequest.timeId());

        final List<Reservation> duplicateTimeReservations = reservationRepository.findByReservationTimeAndDateAndTheme(
                time, reservationRequest.date(), theme);

        if (!duplicateTimeReservations.isEmpty()) {
            throw new DataDuplicateException(ErrorType.RESERVATION_DUPLICATED,
                    String.format("이미 해당 날짜/시간/테마에 예약이 존재합니다. [values: %s]", reservationRequest));
        }
    }

    public ReservationsResponse findFilteredReservations(final FilteredReservationRequest request) {
        final Specification<Reservation> specification = getReservationSpecification(request);

        final List<ReservationResponse> response = reservationRepository.findAll(specification)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    private Specification<Reservation> getReservationSpecification(
            final FilteredReservationRequest request
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
        return specification;
    }

    public MemberReservationsResponse findReservationByMemberId(final Long memberId) {
        final Member member = memberService.findMemberById(memberId);
        final List<Reservation> reservations = reservationRepository.findByMember(member);
        final List<MemberReservationResponse> responses = new ArrayList<>();
        for (final Reservation reservation : reservations) {
            responses.add(MemberReservationResponse.from(reservation));
        }
        return new MemberReservationsResponse(responses);
    }
}
