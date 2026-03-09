package com.intern.assignment.services;

import com.intern.assignment.entities.ShelfPosition;
import com.intern.assignment.exceptions.DeviceNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionCannotBeCreatedException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import com.intern.assignment.repositories.ShelfPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ShelfPositionTest {
    @Mock
    ShelfPositionRepository shelfPositionRepository;

    @InjectMocks
    ShelfPositionService shelfPositionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private static ShelfPosition createSampleShelfPosition() {
        ShelfPosition shelfPosition = new ShelfPosition();
        shelfPosition.setId("abc");
        shelfPosition.setDeviceId("abc");
        shelfPosition.setIsDeleted(false);
        return shelfPosition;
    }

    @Test
    void testCreateShelfPositions() throws ShelfPositionCannotBeCreatedException {
        List<ShelfPosition> shelfPositions = new ArrayList<>();
        shelfPositions.add(createSampleShelfPosition());
        shelfPositions.add(createSampleShelfPosition());
        shelfPositions.add(createSampleShelfPosition());
        String deviceId = "123";
        Mockito.when(shelfPositionRepository.createShelfPosition(deviceId, 3)).thenReturn(shelfPositions);
        assertEquals(shelfPositionService.createShelfPositions(deviceId, 3), shelfPositions);
    }

    @Test
    void testGetShelfPositions() throws DeviceNotFoundException {
        String deviceId = "123";
        List<ShelfPosition> shelfPositions = new ArrayList<>();
        shelfPositions.add(createSampleShelfPosition());
        shelfPositions.add(createSampleShelfPosition());
        shelfPositions.add(createSampleShelfPosition());
        Map<String,Object> output = Map.of("shelfPositions", shelfPositions);
        List<Map<String,Object>> result = new ArrayList<>();
        result.add(output);
        Mockito.when(shelfPositionRepository.getShelfPositions(deviceId)).thenReturn(result);
        assertEquals(shelfPositionService.getShelfPositions(deviceId), result);
    }

    @Test
    void testDeleteAllShelfPositions() throws DeviceNotFoundException {
        String deviceId = "123";
        shelfPositionService.deleteAllShelfPositions(deviceId);
    }

    @Test
    void testDeleteShelf() throws ShelfPositionNotFoundException {
        String shelfPositionId = "123";
        Mockito.when(shelfPositionRepository.deleteShelfPosition(shelfPositionId)).thenReturn(true);
        assertTrue(shelfPositionService.deleteShelf(shelfPositionId));
    }

    @Test
    void testAddShelfPositions() throws DeviceNotFoundException {
        String deviceId = "123";
        int numberOfShelfPositions = 3;
        Mockito.when(shelfPositionRepository.addShelfPositions(deviceId)).thenReturn(createSampleShelfPosition());
        assertNotNull(shelfPositionService.addShelfPositions(deviceId, numberOfShelfPositions));
    }
}
