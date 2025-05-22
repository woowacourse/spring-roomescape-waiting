package roomescape.service.reservation;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.dto.admin.AdminWaitingReservationResponse;
import roomescape.dto.reservation.MemberReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.WaitingReservationRequest;
import roomescape.dto.search.SearchConditionsRequest;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.ReservationAlreadyExistsException;
import roomescape.exception.reservation.ReservationInPastException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservation.ReservationStatusRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationStatusRepository statusRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository timeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository, ReservationTimeRepository reservationTimeRepository,
                              ReservationStatusRepository statusRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.statusRepository = statusRepository;
    }

    public ReservationResponse create(ReservationRequest request, Member member) {
        ReservationTime reservationTime = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (LocalDateTime.now().isAfter(LocalDateTime.of(request.date(), reservationTime.getStartAt()))) {
            throw new ReservationInPastException();
        }

        if (reservationRepository.existsByDateAndTimeAndTheme(request.date(), reservationTime, theme)) {
            throw new ReservationAlreadyExistsException();
        }

        ReservationStatus status = new ReservationStatus(1L);
        statusRepository.save(status);

        Reservation newReservation = new Reservation(request.date(),
                reservationTime, theme, member, status);

        return ReservationResponse.from(reservationRepository.save(newReservation));
    }

    public List<ReservationResponse> getAll() {
        return ReservationResponse.from(reservationRepository.findAll());
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation targetReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        updateWaitingReservationPriorities(targetReservation);
        reservationRepository.delete(targetReservation);
    }

    public ReservationResponse createByAdmin(AdminReservationRequest adminReservationRequest) {
        ReservationTime reservationTime = timeRepository.findById(adminReservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(adminReservationRequest.timeId()));

        Theme theme = themeRepository.findById(adminReservationRequest.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(adminReservationRequest.themeId()));

        Member member = memberRepository.findById(adminReservationRequest.memberId())
                .orElseThrow(() -> new MemberNotFoundException(adminReservationRequest.memberId()));

        ReservationStatus status = new ReservationStatus(1L);
        statusRepository.save(status);

        Reservation newReservation = new Reservation(adminReservationRequest.date(),
                reservationTime, theme, member, status);

        return ReservationResponse.from(reservationRepository.save(newReservation));
    }

    public List<ReservationResponse> getReservationsByConditions(
            @Valid SearchConditionsRequest searchConditionsRequest) {

        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                searchConditionsRequest.themeId(),
                searchConditionsRequest.memberId(),
                searchConditionsRequest.dateFrom(),
                searchConditionsRequest.dateTo()
        );
        return reservations.stream().
                map(ReservationResponse::from)
                .toList();
    }

    public List<MemberReservationResponse> getReservationByMember(Member member) {
        return reservationRepository.findAllByMember(member)
                .stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    public ReservationResponse createWaitingReservation(WaitingReservationRequest request, Member member) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        long waitingCount = reservationRepository.countByDateAndTimeAndTheme(request.date(), time, theme);

        ReservationStatus status = new ReservationStatus(waitingCount + 1);
        statusRepository.save(status);

        Reservation reservation = reservationRepository.save(new Reservation(request.date(),
                time, theme, member, status));
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void deleteWaitingReservation(Long reservationId, Member member) {
        Reservation targetReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!targetReservation.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("예약의 주인이 아닙니다.");
        }

        updateWaitingReservationPriorities(targetReservation);
        reservationRepository.delete(targetReservation);
    }

    public List<AdminWaitingReservationResponse> getWaitingReservations() {
        return AdminWaitingReservationResponse.from(reservationRepository.findAllWaiting());
    }

    private void updateWaitingReservationPriorities(Reservation targetReservation) {
        reservationRepository.updateAllWaitingReservationsAfterPriority(
                targetReservation.getDate(),
                targetReservation.getTime(),
                targetReservation.getTheme(),
                targetReservation.getStatus().getPriority()
        );
    }
}
