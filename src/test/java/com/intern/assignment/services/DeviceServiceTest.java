package com.intern.assignment.services;

import com.intern.assignment.entities.Device;
import com.intern.assignment.exceptions.DeviceCannotBeCreatedException;
import com.intern.assignment.exceptions.DeviceNotFoundException;
import com.intern.assignment.repositories.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeviceServiceTest {
    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    private static Device createSampleDevice() {
        Device device = new Device();
        device.setId("123");
        device.setDeviceType("demo");
        device.setDeviceName("demo");
        device.setPartNumber("demo");
        device.setIsDeleted(false);
        device.setBuildingName("demo");
        device.setNumberOfShelfPositions(3);
        return device;
    }

    @Test
    void testCreateDevice() throws DeviceCannotBeCreatedException {
        Device device = createSampleDevice();
        Map<String,Object> output = new HashMap<>();
        output.put("device", device);
        when(deviceRepository.createDevice(device)).thenReturn(output);
        Object result = deviceService.createDevice(device).get("device");
        assertNotNull(result);
        assertEquals(result, device);
    }

    @Test
    void testSearchDevices() throws DeviceNotFoundException {
        Device device = createSampleDevice();
        List<Map<String, Object>> repositoryResult =  new ArrayList<>();
        repositoryResult.add(Map.of(
                "device", device
        ));
        when(deviceRepository.searchDevices("123",null,null,null,null,0)).thenReturn(repositoryResult);
        assertEquals(deviceService.searchDevices("123", null,null,null,null,0).getFirst().get("device"), device);
    }

    @Test
    void testUpdateDevice() throws DeviceNotFoundException {
        Device oldDevice = createSampleDevice();
        Device newDevice;
        newDevice = oldDevice;
        String newDeviceName = "new demo";
        newDevice.setDeviceName(newDeviceName);
        when(deviceRepository.updateDevice(oldDevice.getId(), null, newDeviceName, null, null, 0)).thenReturn(newDevice);
        assertEquals(deviceService.updateDevice(oldDevice.getId(), newDeviceName, null, null, null, 0), newDevice);
    }

    @Test
    void testDeleteDevice() throws DeviceNotFoundException {
        Device device = createSampleDevice();
        when(deviceRepository.deleteDevice(device.getId())).thenReturn(true);
        assertTrue(deviceService.deleteDevice(device.getId()));
    }
}
