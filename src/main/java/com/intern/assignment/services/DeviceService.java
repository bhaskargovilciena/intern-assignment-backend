package com.intern.assignment.services;

import com.intern.assignment.entities.Device;
import com.intern.assignment.exceptions.DeviceCannotBeCreatedException;
import com.intern.assignment.exceptions.DeviceNotFoundException;
import com.intern.assignment.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    @Autowired
    public DeviceService(DeviceRepository deviceRepository) { this.deviceRepository = deviceRepository; }

    public Map<String,Object> createDevice(Device device) throws DeviceCannotBeCreatedException {
        if(device.getDeviceName().isBlank()) throw new DeviceCannotBeCreatedException("please give device name");
        if(device.getDeviceType().isBlank()) throw new DeviceCannotBeCreatedException("please give device type");
        if(device.getBuildingName().isBlank()) throw new DeviceCannotBeCreatedException("please give building name");
        if(device.getNumberOfShelfPositions() == 0) throw new DeviceCannotBeCreatedException("shelf positions can't be 0");
        if(device.getPartNumber().isBlank()) throw new DeviceCannotBeCreatedException("please give part number");
        logger.info("Device Service: Device creation requested and forwarded to Device Repository");
        return deviceRepository.createDevice(device);
    }

    public List<Map<String,Object>> searchDevices(String id, String deviceName, String buildingName, String partNumber, String deviceType, int numberOfShelfPositions) throws DeviceNotFoundException {
        logger.info("Device Service: Search Devices function called and passed to repository");
        return deviceRepository.searchDevices(id, buildingName, deviceName, partNumber, deviceType, numberOfShelfPositions);
    }

    public Device updateDevice(String id, String deviceName, String buildingName, String partNumber, String deviceType, int numberOfShelfPositions) throws DeviceNotFoundException {
        logger.info("Device Service: Update Devices function called and passed to repository");
        return deviceRepository.updateDevice(id, buildingName, deviceName, partNumber, deviceType, numberOfShelfPositions);
    }

    public boolean deleteDevice(String deviceId) throws DeviceNotFoundException {
        logger.info("Device Service: Delete device function called and passed to repository");
        return deviceRepository.deleteDevice(deviceId);
    }
}
