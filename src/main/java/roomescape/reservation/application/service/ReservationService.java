package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.util.SystemLocalDateTime;
import roomescape.member.application.exception.MemberNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.exception.ReservationInPastException;
import roomescape.reservation.application.exception.ReservationNotFoundException;
import roomescape.reservation.application.exception.ReservationTimeNotFoundException;
import roomescape.reservation.application.exception.ThemeNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.MemberReservationResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.SearchConditionsRequest;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository timeRepository,
                              ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse create(ReservationRequest request, Member member) {

        ReservationTime reservationTime = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());

        if (SystemLocalDateTime.now().isAfter(LocalDateTime.of(request.date(), reservationTime.getStartAt()))) {
            throw new ReservationInPastException();
        }

        ReservationStatus status = getReservationStatus(request.date(), reservationTime, theme);

        Reservation newReservation = new Reservation(request.date(),
                reservationTime, theme, member, status, SystemLocalDateTime.now());

        return mapToReservationResponse(reservationRepository.save(newReservation));
    }

    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream()
                .map(this::mapToReservationResponse)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {

        Reservation targetReservation = getReservation(id);

        reservationRepository.delete(targetReservation);
    }

    public ReservationResponse createByAdmin(AdminReservationRequest adminReservationRequest) {

        ReservationTime reservationTime = getReservationTime(adminReservationRequest.timeId());
        Theme theme = getTheme(adminReservationRequest.themeId());
        Member member = getMember(adminReservationRequest.memberId());
        ReservationStatus status = getReservationStatus(adminReservationRequest.date(), reservationTime, theme);

        Reservation newReservation = new Reservation(adminReservationRequest.date(),
                reservationTime, theme, member, status, SystemLocalDateTime.now());

        return mapToReservationResponse(reservationRepository.save(newReservation));
    }

    public List<ReservationResponse> getReservationsByConditions(SearchConditionsRequest searchConditionsRequest) {

        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                searchConditionsRequest.themeId(),
                searchConditionsRequest.memberId(),
                searchConditionsRequest.dateFrom(),
                searchConditionsRequest.dateTo()
        );
        return reservations.stream().
                map(this::mapToReservationResponse)
                .toList();
    }

    public List<MemberReservationResponse> getReservationByMember(Member member) {

        return reservationRepository.findAllByMember(member)
                .stream()
                .map(this::mapToMemberReservationResponse)
                .toList();
    }

    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        if (reservation.getStatus().isWaiting()) {
            int order = calculateWaitingOrder(reservation);
            return ReservationResponse.fromWaitingReservation(reservation, order);
        }
        return ReservationResponse.fromConfirmedReservation(reservation);
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException(timeId));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException(themeId));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private ReservationStatus getReservationStatus(LocalDate date, ReservationTime reservationTime, Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme)) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.CONFIRMED;
    }

    private MemberReservationResponse mapToMemberReservationResponse(Reservation reservation) {
        if (reservation.getStatus().isWaiting()) {
            int order = calculateWaitingOrder(reservation);
            return MemberReservationResponse.fromWaitingReservation(reservation, order);
        }
        return MemberReservationResponse.fromConfirmedReservation(reservation);
    }

    private int calculateWaitingOrder(Reservation reservation) {
        return reservationRepository.countReservationsBefore(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getCreatedAt()
        );
    }
}
