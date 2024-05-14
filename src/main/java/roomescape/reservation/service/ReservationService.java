package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.ForbiddenException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MemberReservationResponse;
import roomescape.reservation.dto.response.MemberReservationsResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeInfoResponse;
import roomescape.reservation.dto.response.ReservationTimeInfosResponse;
import roomescape.reservation.dto.response.ReservationsResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeRepository themeRepository;
    private final MemberService memberService;
    private final ThemeService themeService;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository, ReservationTimeService reservationTimeService,
                              final ThemeRepository themeRepository,
                              final MemberService memberService, ThemeService themeService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeRepository = themeRepository;
        this.memberService = memberService;
        this.themeService = themeService;
    }

    public ReservationsResponse findAllReservations() {
        List<ReservationResponse> response = reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    public ReservationTimeInfosResponse findReservationsByDateAndThemeId(final LocalDate date, final Long themeId) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        Theme theme = themeService.findThemeById(themeId);
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        List<ReservationTimeInfoResponse> response = new ArrayList<>();
        for (ReservationTime time : allTimes) {
            boolean alreadyBooked = false;
            for (Reservation reservation : reservations) {
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
        Member member = memberService.findMemberById(requestMemberId);
        Reservation reservation = findReservationById(reservationId);
        Long reservationMemberId = reservation.getMember().getId();

        if (member.isRole(Role.ADMIN) || reservationMemberId.equals(requestMemberId)) {
            reservationRepository.deleteById(reservation.getId());
        } else {
            throw new ForbiddenException(ErrorType.PERMISSION_DOES_NOT_EXIST,
                    "예약(Reservation) 정보에 대한 삭제 권한이 존재하지 않습니다.");
        }
    }

    public ReservationResponse addReservation(final ReservationRequest request, final Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate requestDate = request.date();

        ReservationTime requestReservationTime = reservationTimeService.findTimeById(request.timeId());
        Theme theme = themeService.findThemeById(request.themeId());
        Member member = memberService.findMemberById(memberId);

        validateDateAndTime(requestDate, requestReservationTime, now);
        validateReservationDuplicate(request, theme);

        // TODO: 예약대기 추가 시, ReservationStatus 값 설정 로직 추가
        Reservation savedReservation = reservationRepository.save(request.toEntity(requestReservationTime, theme, member, ReservationStatus.RESERVED));
        return ReservationResponse.from(savedReservation);
    }

    private void validateDateAndTime(final LocalDate requestDate, final ReservationTime requestReservationTime, final LocalDateTime now) {
        if (isReservationInPast(requestDate, requestReservationTime, now)) {
            throw new ValidateException(ErrorType.RESERVATION_PERIOD_IN_PAST,
                    String.format("지난 날짜나 시간은 예약이 불가능합니다. [now: %s %s | request: %s %s]",
                            now.toLocalDate(), now.toLocalTime(), requestDate, requestReservationTime.getStartAt()));
        }
    }

    private boolean isReservationInPast(final LocalDate requestDate, final ReservationTime requestReservationTime, final LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (requestDate.isBefore(today)) {
            return true;
        }
        if (requestDate.isEqual(today) && requestReservationTime.getStartAt().isBefore(nowTime)) {
            return true;
        }
        return false;
    }

    private void validateReservationDuplicate(final ReservationRequest reservationRequest, final Theme theme) {
        ReservationTime time = reservationTimeService.findTimeById(reservationRequest.timeId());

        List<Reservation> duplicateTimeReservations = reservationRepository.findByReservationTimeAndDateAndTheme(
                time, reservationRequest.date(), theme);

        if (duplicateTimeReservations.size() > 0) {
            throw new DataDuplicateException(ErrorType.RESERVATION_DUPLICATED,
                    String.format("이미 해당 날짜/시간/테마에 예약이 존재합니다. [values: %s]", reservationRequest));
        }
    }

    public ReservationsResponse findReservationsByThemeIdAndMemberIdBetweenDate(
            final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo) {
        Member member = memberService.findMemberById(memberId);
        Theme theme = themeService.findThemeById(themeId);
        // TODO: [STEP6 필터링 로직] 동적 쿼리로 변경
        List<ReservationResponse> response = reservationRepository.findByThemeIdAndMemberIdBetweenDate(theme, member, dateFrom, dateTo).stream()
                .map(ReservationResponse::from)
                .toList();
        return new ReservationsResponse(response);
    }

    public MemberReservationsResponse findReservationByMemberId(final Long memberId) {
        Member member = memberService.findMemberById(memberId);
        List<Reservation> reservations = reservationRepository.findByMember(member);
        List<MemberReservationResponse> responses = new ArrayList<>();
        for (Reservation reservation : reservations) {
            responses.add(MemberReservationResponse.from(reservation));
        }
        return new MemberReservationsResponse(responses);
    }
}
