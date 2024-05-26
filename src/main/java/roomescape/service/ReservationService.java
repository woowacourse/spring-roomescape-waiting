package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationMineResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.repository.*;

@Service
public class ReservationService {

    private final MemberRepository memberRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository, TimeSlotRepository timeSlotRepository,
                              ReservationRepository reservationRepository, WaitingRepository waitingRepository, ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findDistinctReservations(Long memberId, Long themeId,
                                                              String dateFrom, String dateTo) {
        LocalDate from = LocalDate.parse(dateFrom);
        LocalDate to = LocalDate.parse(dateTo);
        Member member = findMemberById(memberId);
        Theme theme = findThemeById(themeId);
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, from, to)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse create(ReservationRequest reservationRequest) {
        Member member = findMemberById(reservationRequest.memberId());
        TimeSlot timeSlot = findTimeSlotById(reservationRequest.timeId());
        Theme theme = findThemeById(reservationRequest.themeId());

        validate(reservationRequest.date(), timeSlot, theme, member);

        Reservation reservation = reservationRequest.toEntity(member, timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public List<ReservationMineResponse> findMyReservations(LoginMember loginMember) {
        Member member = findMemberById(loginMember.id());
        List<Reservation> reservations = reservationRepository.findAllByMemberOrderByDateAsc(member);
        return reservations.stream()
                .map(ReservationMineResponse::from)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        Reservation currentReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 예약 내역이 존재하지 않습니다."));

        reservationRepository.deleteById(id);

        updateWaitingToReservation(currentReservation);
    }

    private void updateWaitingToReservation(final Reservation reservation) {

        Optional<Waiting> firstWaiting = waitingRepository
                .findFirstByDateAndTimeAndTheme(reservation.getDate(), reservation.getTime(), reservation.getTheme());

        if (firstWaiting.isPresent()) {
            Reservation newReservation =
                    new Reservation(firstWaiting.get().getMember(), reservation.getDate(),
                            reservation.getTime(), reservation.getTheme());
            waitingRepository.delete(firstWaiting.get());
            reservationRepository.save(newReservation);
        }
    }

    private void validate(LocalDate date, TimeSlot timeSlot, Theme theme, Member member) {
        validateReservation(date, timeSlot);
        validateDuplicatedReservation(date, timeSlot, theme, member);
    }

    private void validateReservation(LocalDate date, TimeSlot time) {
        if (time == null || (time.isTimeBeforeNow() && !date.isAfter(LocalDate.now()))) {
            throw new IllegalArgumentException("[ERROR] 지나간 날짜와 시간으로 예약할 수 없습니다");
        }
    }

    private void validateDuplicatedReservation(LocalDate date, TimeSlot timeSlot, Theme theme, Member member) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndMember(date, timeSlot, theme, member)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 완료되었습니다");
        }

        if (reservationRepository.existsByDateAndTimeAndTheme(date, timeSlot, theme)) {
            throw new IllegalArgumentException("[ERROR] 예약이 종료되었습니다");
        }

        if (reservationRepository.existsByDateAndTimeAndMember(date, timeSlot, member)) {
            throw new IllegalArgumentException("[ERROR] 동일한 시간대에 예약을 두 개 이상 할 수 없습니다.");
        }
    }

    private Member findMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 회원 입니다"));
    }

    private TimeSlot findTimeSlotById(long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 시간 입니다"));
    }

    private Theme findThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 테마 입니다"));
    }
}
