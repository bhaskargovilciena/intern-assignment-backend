package com.intern.assignment.repositories;

import com.intern.assignment.config.DatabaseConnection;
import com.intern.assignment.entities.Device;
import com.intern.assignment.services.ShelfPositionService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DeviceRepository {
    private static final Driver driver = DatabaseConnection.initialise();
    private final static Logger logger = LoggerFactory.getLogger(DeviceRepository.class);
    private final ShelfPositionService shelfPositionService;

    @Autowired
    public DeviceRepository(ShelfPositionService shelfPositionService) {
        this.shelfPositionService = shelfPositionService;
    }

    public Map<String,Object> createDevice(Device device) {
        final String query = """
                CREATE (device:Device
                {
                    deviceName:$deviceName,
                    partNumber:$partNumber,
                    buildingName:$buildingName,
                    deviceType:$deviceType,
                    numberOfShelfPositions:$numberOfShelfPositions,
                    isDeleted:false
                }
                )
                RETURN device
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("deviceName", device.getDeviceName());
        map.put("partNumber", device.getPartNumber());
        map.put("buildingName", device.getBuildingName());
        map.put("deviceType", device.getDeviceType());
        map.put("numberOfShelfPositions", device.getNumberOfShelfPositions());

        var record = driver.executableQuery(query).withParameters(map).execute().records()
                .stream()
                .map(r -> r.get("device").asNode())
                .findFirst().orElseThrow(() -> new RuntimeException("Device could not be created"));
        logger.info("Device Repository: Device with ID: {} created successfully", record.elementId());
        device.setId(record.elementId()); // setting ID of the device created to show at the client
        device.setIsDeleted(record.get("isDeleted").asBoolean()); // setting the isDeleted property from the device
        Map<String,Object> result = new HashMap<>();
        result.put("device", device);
        result.put("shelfPositions", shelfPositionService.createShelfPositions(device.getId(), device.getNumberOfShelfPositions()));

        return result;
    }

    public List<Map<String,Object>> searchDevices(String id, String buildingName, String deviceName, String partNumber, String deviceType, int numberOfShelfPositions) {
        String query = "MATCH (device:Device) WHERE device.isDeleted=false ";
        Map<String, Object> params = new HashMap<>();

        if(id != null) {
            query += "AND elementId(device) = $id";
            params.put("id", id);
        }
        if(buildingName != null) {
            query += "AND device.buildingName = $buildingName ";
            params.put("buildingName", buildingName);
        }
        if(deviceName != null) {
            query += "AND device.deviceName = $deviceName ";
            params.put("deviceName", deviceName);
        }
        if(deviceType != null) {
            query += "AND device.deviceType = $deviceType ";
            params.put("deviceType", deviceType);
        }
        if(partNumber != null) {
            query += "AND device.partNumber = $partNumber ";
            params.put("partNumber",partNumber);
        }
        if(numberOfShelfPositions != 0) {
            query += "AND device.numberOfShelfPositions = $numberOfShelfPositions ";
            params.put("numberOfShelfPositions", numberOfShelfPositions);
        }

        query += "MATCH (device)-[:HAS]->(shelfPosition:ShelfPosition) RETURN device, collect(shelfPosition) as shelfPositions";
        var records = driver.executableQuery(query).withParameters(params).execute().records();

        List<Map<String,Object>> devices = new ArrayList<>();
        records.forEach(record -> {
            Node node = record.get("device").asNode();
            Device device = new Device();
            device.setDeviceName(node.get("deviceName").asString());
            device.setDeviceType(node.get("deviceType").asString());
            device.setBuildingName(node.get("buildingName").asString());
            device.setPartNumber(node.get("partNumber").asString());
            device.setNumberOfShelfPositions(node.get("numberOfShelfPositions").asInt());
            device.setId(node.elementId());
            device.setIsDeleted(node.get("isDeleted").asBoolean());
            List<Map<String,Object>> shelfPositions = shelfPositionService.getShelfPositions(device.getId());
            devices.add(Map.of(
              "device", device,
              "shelfPositions", shelfPositions
            ));
        });
        logger.info("Device Repository: Search devices function accessed with query: {}", query);

        return devices;
    }

    public Device updateDevice(String id, String buildingName, String deviceName, String partNumber, String deviceType, int numberOfShelfPositions) {
        StringBuilder queryBuilder = new StringBuilder("MATCH (device:Device) WHERE elementId(device) = $id AND device.isDeleted=false SET ");
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        if(buildingName != null) {
            queryBuilder.append("device.buildingName = $buildingName, ");
            params.put("buildingName", buildingName);
        }
        if(deviceName != null) {
            queryBuilder.append("device.deviceName = $deviceName, ");
            params.put("deviceName", deviceName);
        }
        if(partNumber != null) {
            queryBuilder.append("device.partNumber = $partNumber, ");
            params.put("partNumber", partNumber);
        }
        if(deviceType != null) {
            queryBuilder.append("device.deviceType = $deviceType, ");
            params.put("deviceType", deviceType);
        }
        if(numberOfShelfPositions != 0) {
            queryBuilder.append("device.numberOfShelfPositions = $numberOfShelfPositions, ");
            params.put("numberOfShelfPositions", numberOfShelfPositions);
            shelfPositionService.createShelfPositions(id, numberOfShelfPositions);
        }

        queryBuilder.setLength(queryBuilder.length() - 2);

        queryBuilder.append(" RETURN device");

        var records = driver.executableQuery(queryBuilder.toString()).withParameters(params).execute().records();

        Device device = new Device();

        records.forEach(record -> {
            Node node = record.get("device").asNode();
            device.setId(node.elementId());
            device.setDeviceName(node.get("deviceName").asString());
            device.setDeviceType(node.get("deviceType").asString());
            device.setBuildingName(node.get("buildingName").asString());
            device.setPartNumber(node.get("partNumber").asString());
            device.setNumberOfShelfPositions(node.get("numberOfShelfPositions").asInt());
            device.setIsDeleted(node.get("isDeleted").asBoolean());
        });
        logger.info("Device Repository: Update device function accessed with ID: {}", id);
        return device;
    }

    public boolean deleteDevice(String deviceId) {
        String query = """
                MATCH (device:Device) WHERE elementId(device) = $id
                SET device.isDeleted = true
                RETURN device
                """;
        List<Record> records = driver.executableQuery(query).withParameters(Map.of("id", deviceId)).execute().records();

        records.forEach(record -> {
            Node node = record.get("device").asNode();
            shelfPositionService.deleteAllShelfPositions(node.elementId());
        });

        return true;
    }
}
