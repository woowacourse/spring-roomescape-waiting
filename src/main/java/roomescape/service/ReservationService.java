package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

@Service
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;

    public ReservationService(final JpaReservationRepository reservationRepository,
                              final JpaReservationTimeRepository reservationTimeRepository,
                              final JpaThemeRepository themeRepository,
                              final JpaMemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponseDto createReservation(ReservationCreateDto dto) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + dto.timeId()));

        validateDuplicate(dto.date(), dto.timeId(), dto.themeId());
        Reservation.validateReservableTime(dto.date(), reservationTime.getStartAt());

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + dto.themeId()));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + dto.memberId()));

        Reservation requestReservation = Reservation.createWithoutId(member, dto.date(), reservationTime, theme);
        Reservation newReservation = reservationRepository.save(requestReservation);

        return ReservationResponseDto.of(newReservation, newReservation.getTime(), theme);
    }

    private void validateDuplicate(LocalDate date, long timeId, long themeId) {
        List<Reservation> reservations = reservationRepository.findReservationsByDateAndTimeIdAndThemeId(date, timeId,
                themeId);
        if (!reservations.isEmpty()) {
            throw new DuplicateContentException("[ERROR] 이미 예약이 존재합니다. 다른 예약 일정을 선택해주세요.");
        }
    }

    public List<ReservationResponseDto> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public List<ReservationResponseDto> findReservationBetween(long themeId, long memberId, LocalDate from,
                                                               LocalDate to) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberId(
                from, to, themeId, memberId);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        reservationRepository.deleteById(id);
    }

    public List<MyReservationResponseDto> findMyReservations(LoginInfo loginInfo) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(loginInfo.id());
        return reservations.stream().map(reservation -> new MyReservationResponseDto(reservation)).toList();
    }
}
