package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class BookingSystemTest {

    private static final String ROOM_ID = "1";
    private static final LocalDateTime CURRENT_TIME = LocalDateTime.of(2026, 1, 28, 12, 0);
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 1, 28, 15, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2026, 1, 28, 16, 0);

    @Mock TimeProvider timeProvider;
    @Mock RoomRepository roomRepository;
    @Mock NotificationService notificationService;

    @InjectMocks
    BookingSystem bookingSystem;

    private static Stream<Arguments> nullParamsRoomProvider() {
        return Stream.of(
                Arguments.of(ROOM_ID, START_TIME, null),
                Arguments.of(ROOM_ID, null, END_TIME),
                Arguments.of(null, START_TIME, END_TIME));
    }

    @BeforeEach
    void setUp() {

    }

    @ParameterizedTest
    @MethodSource("nullParamsRoomProvider")
    void nullParametersShouldThrowException(String roomId, LocalDateTime startTime, LocalDateTime endTime) {

        assertThatThrownBy(()->bookingSystem.bookRoom(roomId, startTime, endTime))
        .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");

    }



}