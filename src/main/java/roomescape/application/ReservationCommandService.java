package roomescape.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateDto;
import roomescape.application.dto.ReservationDto;
import roomescape.application.dto.UserReservationCreateDto;
import roomescape.application.dto.UserWaitingCreateDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.AuthorizationException;
import roomescape.exception.NotFoundException;

@Service
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final TimeService timeService;
    private final ThemeService themeService;
    private final MemberService memberService;
    private final ClockProvider clockProvider;

    public ReservationCommandService(ReservationRepository reservationRepository, TimeService timeService,
                                     ThemeService themeService, MemberService memberService,
                                     ClockProvider clockProvider) {
        this.reservationRepository = reservationRepository;
        this.timeService = timeService;
        this.themeService = themeService;
        this.memberService = memberService;
        this.clockProvider = clockProvider;
    }

    public ReservationDto registerReservationByUser(
            UserReservationCreateDto request,
            Long memberId
    ) {
        return registerReservation(ReservationCreateDto.of(request, memberId));
    }

    public ReservationDto registerReservation(ReservationCreateDto request) {
        validateNotDuplicate(request);
        Theme theme = themeService.getThemeById(request.themeId());
        ReservationTime reservationTime = timeService.getTimeEntityById(request.timeId());
        Member member = memberService.getMemberEntityById(request.memberId());
        Waiting waiting = new Waiting(ReservationStatus.RESERVED);
        Reservation reservation = saveReservation(member, theme, request.date(), reservationTime, waiting);
        return ReservationDto.from(reservation);
    }

    private void validateNotDuplicate(ReservationCreateDto request) {
        boolean duplicated = isDuplicated(
                request.date(),
                request.timeId(),
                request.themeId()
        );
        if (duplicated) {
            throw new IllegalArgumentException("이미 예약된 일시입니다");
        }
    }

    private boolean isDuplicated(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(
                date,
                timeId,
                themeId
        );
    }

    private Reservation saveReservation(Member member, Theme theme, LocalDate request, ReservationTime reservationTime,
                                        Waiting waiting) {
        Reservation reservationWithoutId = Reservation.withoutId(
                member,
                theme,
                request,
                reservationTime,
                waiting
        );
        LocalDateTime now = clockProvider.now();
        validateNotPast(reservationWithoutId, now);
        return reservationRepository.save(reservationWithoutId);
    }

    private void validateNotPast(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new IllegalArgumentException("과거 일시로 예약할 수 없습니다.");
        }
    }

    public ReservationDto registerWaitingByUser(UserWaitingCreateDto request, Long memberId) {
        if (!isDuplicated(request.date(), request.time(), request.theme())) {
            throw new IllegalArgumentException("예약 가능한 일시입니다.");
        }
        Theme theme = themeService.getThemeById(request.theme());
        ReservationTime reservationTime = timeService.getTimeEntityById(request.time());
        Member member = memberService.getMemberEntityById(memberId);
        Waiting waiting = new Waiting(ReservationStatus.WAITING);
        Reservation reservation = saveReservation(member, theme, request.date(), reservationTime, waiting);
        return ReservationDto.from(reservation);
    }

    public void deleteReservation(Long reservationId, Long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약 id가 존재하지 않습니다. id: " + reservationId));
        if (!member.isAdmin() && !member.isSame(reservation.getMember())) {
            throw new AuthorizationException("권한이 없습니다.");
        }
        reservation.cancel();
    }
}
