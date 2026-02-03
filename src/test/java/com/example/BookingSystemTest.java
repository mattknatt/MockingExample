package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    private static final String ROOM_ID = "room_id";
    private static final String BOOKING_ID = "booking_id";
    private static final LocalDateTime CURRENT_TIME = LocalDateTime.of(2026, 1, 28, 12, 0);
    private static final LocalDateTime START_TIME = CURRENT_TIME.plusHours(1);
    private static final LocalDateTime END_TIME = CURRENT_TIME.plusHours(3);
    @Mock
    TimeProvider timeProvider;
    @Mock
    RoomRepository roomRepository;
    @Mock
    NotificationService notificationService;
    @Mock
    Room room;

    @InjectMocks
    BookingSystem bookingSystem;

    private static Stream<Arguments> bookRoom_nullParameters() {
        return Stream.of(
                Arguments.of(null, START_TIME, END_TIME),
                Arguments.of(ROOM_ID, null, null),
                Arguments.of(ROOM_ID, START_TIME, null),
                Arguments.of(ROOM_ID, null, END_TIME),
                Arguments.of(null, null, null),
                Arguments.of(null, START_TIME, null),
                Arguments.of(null, null, END_TIME));

    }

    public static Stream<Arguments> getAvailableRooms_nullParameters() {
        return Stream.of(
                Arguments.of(START_TIME, null),
                Arguments.of(null, END_TIME),
                Arguments.of(null, null));
    }

    @BeforeEach
    void setUp() {

    }

    @ParameterizedTest
    @MethodSource("bookRoom_nullParameters")
    void nullParametersShouldThrowException(String roomId, LocalDateTime startTime, LocalDateTime endTime) {

        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");

    }

    @Test
    void bookRoom_shouldThrowExceptionIfStartTime_isBeforeCurrentTime() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, CURRENT_TIME.minusHours(1), END_TIME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kan inte boka tid i dåtid");

    }

    @Test
    void bookRoom_shouldThrowExceptionIfEndTime_isBeforeStartTime() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, CURRENT_TIME, CURRENT_TIME.minusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");

    }

    @Test
    void bookRoom_shouldThrowExceptionIfRoom_doesNotExist() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rummet existerar inte");
    }

    @Test
    void bookRoom_returnsFalseIfRoomIsNotAvailable() {
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        Mockito.when(room.isAvailable(START_TIME, END_TIME)).thenReturn(false);

        assertThat(bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME)).isFalse();

    }

    @Test
    void bookRoom_addsBookingAndSavesRoom_whenAvailable() {
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        Mockito.when(room.isAvailable(START_TIME, END_TIME)).thenReturn(true);

        boolean result = bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME);

        assertThat(result).isTrue();
        Mockito.verify(room).addBooking(Mockito.any(Booking.class));
        Mockito.verify(roomRepository).save(room);
    }

    @Test
    void bookRoom_shouldReturnTrue_evenIfNotificationFails() throws NotificationException {
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        Mockito.when(room.isAvailable(START_TIME, END_TIME)).thenReturn(true);
        Mockito.doThrow(new NotificationException("Notification failed")).when(notificationService).sendBookingConfirmation(Mockito.any(Booking.class));

        boolean result = bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME);

        assertThat(result).isTrue();
        Mockito.verify(room).addBooking(Mockito.any(Booking.class));
        Mockito.verify(roomRepository).save(room);
        Mockito.verify(notificationService).sendBookingConfirmation(Mockito.any(Booking.class));

    }

    @ParameterizedTest
    @MethodSource("getAvailableRooms_nullParameters")
    void getAvailableRooms_nullParametersThrowsException(LocalDateTime startTime, LocalDateTime endTime) {

        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Måste ange både start- och sluttid");
    }

    @Test
    void getAvailableRooms_endTimeBeforeStartTime_ThrowsException() {

        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(START_TIME, START_TIME.minusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");

    }

    @Test
    void getAvailableRooms_returnsOnlyAvailableRooms() {
        Room availableRoom = Mockito.mock(Room.class);
        Room unavailableRoom = Mockito.mock(Room.class);

        Mockito.when(roomRepository.findAll()).thenReturn(List.of(availableRoom, unavailableRoom));
        Mockito.when(availableRoom.isAvailable(START_TIME, END_TIME)).thenReturn(true);
        Mockito.when(unavailableRoom.isAvailable(START_TIME, END_TIME)).thenReturn(false);

        List<Room> result = bookingSystem.getAvailableRooms(START_TIME, END_TIME);

        assertThat(result).containsExactly(availableRoom);
    }

    @Test
    void cancelBooking_throwsException_ifBookingIdIsNull() {

        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Boknings-id kan inte vara null");

    }

    @Test
    void cancelBooking_returnsFalse_ifNoRoomWithBooking() {

        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        Mockito.when(room.hasBooking(BOOKING_ID)).thenReturn(false);

        boolean result = bookingSystem.cancelBooking(BOOKING_ID);

        assertThat(result).isFalse();

    }

    @Test
    void cancelBooking_throwsException_ifStartTime_isBeforeCurrentTime() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        Booking booking = new Booking(BOOKING_ID, ROOM_ID, CURRENT_TIME.minusHours(1), END_TIME);
        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        Mockito.when(room.hasBooking(BOOKING_ID)).thenReturn(true);
        Mockito.when(room.getBooking(BOOKING_ID)).thenReturn(booking);

        assertThatThrownBy(() -> bookingSystem.cancelBooking(BOOKING_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
    }

    @Test
    void cancelBooking_removesBooking() {

        Booking booking = new Booking(BOOKING_ID, ROOM_ID, START_TIME, END_TIME);
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        Mockito.when(room.hasBooking(BOOKING_ID)).thenReturn(true);
        Mockito.when(room.getBooking(BOOKING_ID)).thenReturn(booking);

        bookingSystem.cancelBooking(BOOKING_ID);
        Mockito.verify(room).removeBooking(BOOKING_ID);

    }
}

