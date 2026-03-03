package com.intern.assignment.controllers;

import com.intern.assignment.entities.Device;
import com.intern.assignment.exceptions.DeviceNotFoundException;
import com.intern.assignment.services.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/device")
public class DeviceController {
    private final DeviceService deviceService;
    private final Logger logger = LoggerFactory.getLogger(DeviceController.class);
    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Map<String,Object>> createDevice(@RequestBody Device device) {
        logger.info("Device Controller: Device Creation Requested");
        return new ResponseEntity<>(deviceService.createDevice(device), HttpStatus.OK);
    }

    @GetMapping("/search")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<List<Map<String,Object>>> searchDevices(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "buildingName", required = false) String buildingName,
            @RequestParam(value = "partNumber", required = false) String partNumber,
            @RequestParam(value = "numberOfShelfPositions", defaultValue = "0") int numberOfShelfPositions
    ) throws DeviceNotFoundException {
        logger.info("Device Controller: Search Device function called");
        return new ResponseEntity<>(deviceService.searchDevices(id, deviceName, buildingName, partNumber, deviceType, numberOfShelfPositions), HttpStatus.OK);
    }

    @PutMapping("/update")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Device> updateDevice(
            @RequestParam(value = "id") String id,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "buildingName", required = false) String buildingName,
            @RequestParam(value = "partNumber", required = false) String partNumber,
            @RequestParam(value = "numberOfShelfPositions", defaultValue = "0") int numberOfShelfPositions
    ) throws DeviceNotFoundException {
        logger.info("Device Controller: Update Device function called");
        return new ResponseEntity<>(deviceService.updateDevice(id, deviceName, buildingName, partNumber, deviceType, numberOfShelfPositions), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Boolean> deleteDevice(@RequestParam(value = "id") String deviceId) throws DeviceNotFoundException {
        return new ResponseEntity<>(deviceService.deleteDevice(deviceId), HttpStatus.OK);
    }
}
