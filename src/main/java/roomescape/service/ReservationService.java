package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponseDto createReservation(ReservationCreateDto dto) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + dto.timeId()));

        validateDuplicate(dto.date(), reservationTime.startAt(), dto.themeId());
        Reservation.validateReservableTime(dto.date(), reservationTime.startAt());

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + dto.themeId()));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + dto.memberId()));

        Reservation requestReservation = Reservation.createWithoutId(member, dto.date(), reservationTime, theme);
        Reservation newReservation = reservationRepository.save(requestReservation);

        return ReservationResponseDto.of(newReservation, newReservation.getTime(), theme);
    }

    private void validateDuplicate(LocalDate date, LocalTime time, long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateTimeAndThemeId(date, time, themeId);
        if (!reservations.isEmpty()) {
            throw new DuplicateContentException("[ERROR] 이미 예약이 존재합니다. 다른 예약 일정을 선택해주세요.");
        }
    }

    public List<ReservationResponseDto> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(), reservation.getTheme()))
                .toList();
    }

    public List<ReservationResponseDto> findReservationBetween(long themeId, long memberId, LocalDate from, LocalDate to) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findReservationsByPeriodAndMemberAndTheme(themeId, memberId, from, to);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(), reservation.getTheme()))
                .toList();
    }

    public void deleteReservation(Long id) {
        int deletedReservationCount = reservationRepository.deleteById(id);

        if (deletedReservationCount == 0) {
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
    }
}
