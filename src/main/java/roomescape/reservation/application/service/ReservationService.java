package roomescape.reservation.application.service;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.application.exception.MemberNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.exception.NotReservationOwnerException;
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
import roomescape.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.reservation.presentation.dto.MemberReservationResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.SearchConditionsRequest;
import roomescape.reservation.presentation.dto.WaitingReservationRequest;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository timeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository, ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationResponse create(ReservationRequest request, Member member) {
        ReservationTime reservationTime = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (LocalDateTime.now().isAfter(LocalDateTime.of(request.date(), reservationTime.getStartAt()))) {
            throw new ReservationInPastException();
        }

        ReservationStatus status = getReservationStatus(request.date(), reservationTime, theme);

        Reservation newReservation = new Reservation(request.date(),
                reservationTime, theme, member, status, LocalDateTime.now());

        return mapToReservationResponse(reservationRepository.save(newReservation));
    }

    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream()
                .map(this::mapToReservationResponse)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation targetReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        reservationRepository.delete(targetReservation);
    }

    public ReservationResponse createByAdmin(AdminReservationRequest adminReservationRequest) {
        ReservationTime reservationTime = timeRepository.findById(adminReservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(adminReservationRequest.timeId()));

        Theme theme = themeRepository.findById(adminReservationRequest.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(adminReservationRequest.themeId()));

        Member member = memberRepository.findById(adminReservationRequest.memberId())
                .orElseThrow(() -> new MemberNotFoundException(adminReservationRequest.memberId()));

        ReservationStatus status = getReservationStatus(adminReservationRequest.date(), reservationTime, theme);

        Reservation newReservation = new Reservation(adminReservationRequest.date(),
                reservationTime, theme, member, status, LocalDateTime.now());

        return mapToReservationResponse(reservationRepository.save(newReservation));
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
                map(this::mapToReservationResponse)
                .toList();
    }

    public List<MemberReservationResponse> getReservationByMember(Member member) {
        return reservationRepository.findAllByMember(member)
                .stream()
                .map(this::mapToMemberReservationResponse)
                .toList();
    }

    public ReservationResponse createWaitingReservation(WaitingReservationRequest request, Member member) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        Reservation reservation = reservationRepository.save(new Reservation(request.date(),
                time, theme, member, ReservationStatus.WAITING, LocalDateTime.now()));

        return mapToReservationResponse(reservation);
    }

    @Transactional
    public void deleteWaitingReservation(Long reservationId, Member member) {
        Reservation targetReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!targetReservation.getMember().equals(member)) {
            throw new NotReservationOwnerException("예약의 주인이 아닙니다.");
        }

        reservationRepository.delete(targetReservation);
    }

    public List<AdminWaitingReservationResponse> getWaitingReservations() {
        return AdminWaitingReservationResponse.from(reservationRepository.findAllByStatus(ReservationStatus.WAITING));
    }

    private ReservationResponse mapToReservationResponse(
            Reservation reservation) { // TODO : 아래 MemberReservationResponse랑 너무 겹침 -> 리팩토링 필요
        if (reservation.getStatus().isWaiting()) {
            int order = calculateWaitingOrder(reservation);
            return ReservationResponse.fromWaitingReservation(reservation, order);
        }
        return ReservationResponse.fromConfirmedReservation(reservation);
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
        // TODO : findAllByThemeAndDateAnd..로 가져와서 코드 레벨에서 계산하기 - > query의 order by 추가하기 vs stream의 sorted 사용하기 성능 차이 ?
        List<Reservation> reservations = reservationRepository.findAllByDateAndThemeAndTime(
                        reservation.getDate(),
                        reservation.getTheme(),
                        reservation.getTime()
                ).stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .toList();

        return reservations.indexOf(reservation) + 1;
    }
}
