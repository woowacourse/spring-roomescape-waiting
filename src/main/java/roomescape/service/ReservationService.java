package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.dto.LoginMember;
import roomescape.dto.request.MemberReservationRequest;
import roomescape.dto.request.ReservationFilterRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationMineResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@Service
public class ReservationService {

    private final MemberRepository memberRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository, TimeSlotRepository timeSlotRepository,
                              ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findEntireReservationList() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findDistinctReservations(ReservationFilterRequest request) {
        Member member = getMemberById(request.memberId());
        Theme theme = getThemeById(request.themeId());
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(
                        member,
                        theme,
                        request.dateFrom(),
                        request.dateTo())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse create(ReservationRequest request, LocalDateTime now) {
        Member member = getMemberById(request.memberId());
        TimeSlot timeSlot = getTimeSlotById(request.timeId());
        Theme theme = getThemeById(request.themeId());
        Reservation reservation = request.toEntity(member, timeSlot, theme);
        reservation.validatePast(now);
        validateDuplicatedReservation(request.date(), timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public ReservationResponse create(LoginMember loginMember, MemberReservationRequest request, LocalDateTime now) {
        Member member = getMemberById(loginMember.id());
        TimeSlot timeSlot = getTimeSlotById(request.timeId());
        Theme theme = getThemeById(request.themeId());
        Reservation reservation = request.toEntity(member, timeSlot, theme);
        reservation.validatePast(now);
        validateDuplicatedReservation(request.date(), timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public List<ReservationMineResponse> findMyReservations(LoginMember loginMember) {
        Member member = getMemberById(loginMember.id());
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        return reservations.stream()
                .map(ReservationMineResponse::from)
                .toList();
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    private void validateDuplicatedReservation(LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, timeSlot, theme)) {
            throw new IllegalArgumentException("예약이 종료되었습니다");
        }
    }

    private Member getMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 입니다"));
    }

    private TimeSlot getTimeSlotById(long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시간 입니다"));
    }

    private Theme getThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마 입니다"));
    }
}
