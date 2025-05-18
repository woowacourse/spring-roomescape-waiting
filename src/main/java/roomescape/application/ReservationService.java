package roomescape.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateDto;
import roomescape.application.dto.ReservationDto;
import roomescape.application.dto.ReservationWaitingDto;
import roomescape.application.dto.UserReservationCreateDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeService timeService;
    private final ThemeService themeService;
    private final MemberService memberService;
    private final ClockProvider clockProvider;

    public ReservationService(
            ReservationRepository reservationRepository,
            TimeService timeService,
            ThemeService themeService,
            MemberService memberService,
            ClockProvider clockProvider
    ) {
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
        Theme theme = themeService.getThemeById(request.themeId()).toEntity();
        ReservationTime reservationTime = timeService.getTimeById(request.timeId()).toEntity();
        Member member = memberService.getMemberEntityById(request.memberId());
        Waiting waiting = new Waiting(ReservationStatus.RESERVED);
        Reservation reservationWithoutId = Reservation.withoutId(
                member,
                theme,
                request.date(),
                reservationTime,
                waiting
        );
        LocalDateTime now = clockProvider.now();
        validateNotPast(reservationWithoutId, now);
        Reservation reservation = reservationRepository.save(reservationWithoutId);
        return ReservationDto.from(reservation);
    }

    private void validateNotDuplicate(ReservationCreateDto request) {
        boolean duplicated = reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(),
                request.timeId(),
                request.themeId()
        );
        if (duplicated) {
            throw new IllegalArgumentException("이미 예약된 일시입니다");
        }
    }

    private void validateNotPast(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new IllegalArgumentException("과거 일시로 예약할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ReservationDto.from(reservations);
    }

    @Transactional(readOnly = true)
    public List<ReservationWaitingDto> getReservationsByMember(Long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        List<Reservation> memberReservations = reservationRepository.findByMember(member);
        return memberReservations.stream()
                .map(reservation -> new ReservationWaitingDto(
                                reservation.getId(),
                                reservation.getTheme().getName(),
                                reservation.getDate(),
                                reservation.getTime().getStartAt(),
                                ReservationStatus.name(reservation.getWaiting().getStatus())
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> searchReservationsWith(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository
                .findByMemberAndThemeAndDateRange(
                        memberId,
                        themeId,
                        dateFrom,
                        dateTo
                );
        return ReservationDto.from(reservations);
    }

    public void deleteReservation(Long id) {
        try {
            reservationRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 예약 id가 존재하지 않습니다. id: " + id);
        }
    }
}
