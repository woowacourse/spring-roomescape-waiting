package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.MemberReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeService reservationTimeService,
                              final ThemeService themeService,
                              final MemberService memberService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
    }

    public ReservationResponse save(final ReservationSaveRequest saveRequest, final Member member) {
        ReservationTime reservationTime = reservationTimeService.getById(saveRequest.timeId());
        Theme theme = themeService.getById(saveRequest.themeId());
        Status status = determineStatus(saveRequest);
        Reservation reservation = saveRequest.toEntity(member, reservationTime, theme, status);
        try {
            return ReservationResponse.from(reservationRepository.save(reservation));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("[ERROR] 중복 예약은 불가능합니다.");
        }
    }

    private Status determineStatus(ReservationSaveRequest saveRequest) {
        if (hasReservation(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId())) {
            return Status.WAITING;
        }
        return Status.RESERVATION;
    }

    private boolean hasReservation(final LocalDate date, final long timeId, final long themeId) {
        return !reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).isEmpty();
    }

    public ReservationResponse save(final AdminReservationSaveRequest adminReservationSaveRequest) {
        Member member = memberService.getById(adminReservationSaveRequest.memberId());
        return save(adminReservationSaveRequest.toReservationSaveRequest(), member);
    }

    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findByFilter(final Long memberId,
                                                  final Long themeId,
                                                  final String status,
                                                  final LocalDate dateFrom,
                                                  final LocalDate dateTo
    ) {
        return reservationRepository.findByThemeIdAndMemberIdAndStatusAndDateBetween(
                        themeId, memberId, Status.from(status), dateFrom, dateTo
                ).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MemberReservationResponse> findByMemberId(final long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        return reservations.stream()
                .map(reservation -> MemberReservationResponse.of(
                        reservation,
                        reservationRepository.countByDateAndTimeIdAndThemeIdAndCreatedAtBefore(
                                reservation.getDate(),
                                reservation.getTime().getId(),
                                reservation.getTheme().getId(),
                                reservation.getCreatedAt()
                        )
                )).toList();
    }

    public void delete(final long id) {
        Reservation reservation = getById(id);
        reservationRepository.deleteById(id);

        Optional<Reservation> reservationWithStatusWaiting = reservationRepository.findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAt(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        );
        reservationWithStatusWaiting.ifPresent(value -> value.setStatus(Status.RESERVATION));
    }

    private Reservation getById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] (themeId : " + id + ") 에 대한 예약이 존재하지 않습니다."));
    }
}
