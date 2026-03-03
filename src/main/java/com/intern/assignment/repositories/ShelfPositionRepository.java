package com.intern.assignment.repositories;

import com.intern.assignment.config.DatabaseConnection;
import com.intern.assignment.entities.Shelf;
import com.intern.assignment.entities.ShelfPosition;
import com.intern.assignment.exceptions.DeviceNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionCannotBeCreatedException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import com.intern.assignment.services.ShelfService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
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
public class ShelfPositionRepository {
    private static final Driver driver = DatabaseConnection.initialise();
    private static final Logger logger = LoggerFactory.getLogger(ShelfPositionRepository.class);
    private final ShelfService shelfService;

    @Autowired
    public ShelfPositionRepository(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    public List<ShelfPosition> createShelfPosition(String deviceId, int numberOfShelfPositions) throws ShelfPositionCannotBeCreatedException {
        try {
            getDeviceById(deviceId);
        } catch (DeviceNotFoundException e) {
            throw new ShelfPositionCannotBeCreatedException(e.getMessage());
        }
        String query = """
                MATCH (device:Device) WHERE elementId(device) = $id AND device.isDeleted = false
                WITH device, range(1, $numberOfShelfPositions) AS positions
                UNWIND positions AS position
                CREATE (shelfPosition:ShelfPosition {
                    deviceId: $id,
                    isDeleted: false
                })
                MERGE (device)-[:HAS]->(shelfPosition)
                RETURN collect(shelfPosition) AS shelfPositions
                """;
        var records = driver.executableQuery(query).withParameters(Map.of(
                "id", deviceId,
                "numberOfShelfPositions", numberOfShelfPositions
        )).execute().records();

        logger.info("Shelf Position Repository: {} Shelf Positions created for {}", numberOfShelfPositions, deviceId);

        return records
                .stream()
                .flatMap(record -> record.get("shelfPositions").asList(Value::asNode).stream())
                .map(node -> {
                    ShelfPosition shelfPosition = new ShelfPosition();
                    shelfPosition.setId(node.elementId());
                    shelfPosition.setDeviceId(node.get("deviceId").asString());
                    shelfPosition.setIsDeleted(node.get("isDeleted").asBoolean());
                    return shelfPosition;
                })
                .toList();
    }

    public void getDeviceById(String deviceId) throws DeviceNotFoundException {
        String query = """
                MATCH (device:Device) WHERE elementId(device) = $id
                RETURN device
                """;
        driver.executableQuery(query).withParameters(Map.of("id", deviceId)).execute().records()
                .stream().findAny()
                .orElseThrow(() -> new DeviceNotFoundException("device with ID: " + deviceId + " could not be found"));
    }

    public List<Map<String,Object>> getShelfPositions(String deviceId) throws DeviceNotFoundException {
        getDeviceById(deviceId);
        String query = """
                MATCH (device:Device) WHERE elementId(device) = $id AND device.isDeleted = false
                MATCH (device)-[:HAS]->(shelfPosition:ShelfPosition) WHERE shelfPosition.isDeleted = false AND elementId(shelfPosition) IS NOT NULL
                RETURN shelfPosition
                """;
        var records = driver.executableQuery(query).withParameters(Map.of("id", deviceId)).execute().records();

        List<Map<String,Object>> shelfPositions = new ArrayList<>();

        records.stream()
                .map(record -> record.get("shelfPosition").asNode())
                .forEach(node -> {
                    ShelfPosition shelfPosition = new ShelfPosition();
                    shelfPosition.setDeviceId(node.get("deviceId").asString());
                    shelfPosition.setId(node.elementId());
                    shelfPosition.setIsDeleted(node.get("isDeleted").asBoolean());
                    Shelf shelf;
                    try {
                        shelf = shelfService.getShelf(shelfPosition.getId());
                    }
                    catch (ShelfPositionNotFoundException exception) {
                        shelf = null;
                    }
                    Map<String,Object> map = new HashMap<>();
                    map.put("shelfPosition", shelfPosition);
                    if(shelf != null) map.put("shelf", shelf);
                    shelfPositions.add(map);
                });

        logger.info("Shelf Position Repository: Shelf Position read requested for device ID: {}", deviceId);

        return shelfPositions;
    }

    public void getShelfPositionById(String shelfPositionId) throws ShelfPositionNotFoundException {
        String query = """
                MATCH (shelfPosition:ShelfPosition) WHERE elementId(shelfPosition) = $id
                RETURN shelfPosition
                """;
        driver.executableQuery(query).withParameters(Map.of("id", shelfPositionId)).execute().records()
                .stream().findAny()
                .orElseThrow(() -> new ShelfPositionNotFoundException("shelfPosition with ID: " + shelfPositionId + " could not be found"));
    }

    public void deleteAllShelfPositions(String deviceId) throws DeviceNotFoundException {
        getDeviceById(deviceId);
        String query = """
                MATCH (device:Device)-[r:HAS]->(shelfPosition:ShelfPosition)
                WHERE elementId(device) = $id
                SET shelfPosition.isDeleted = true
                RETURN shelfPosition
                """;

        List<Record> records = driver.executableQuery(query).withParameters(Map.of("id", deviceId)).execute().records();

        records.forEach(record -> {
            Node node = record.get("shelfPosition").asNode();
            try {
                shelfService.deleteAllShelves(node.elementId());
            } catch (ShelfPositionNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Boolean deleteShelfPosition(String shelfPositionId) throws ShelfPositionNotFoundException {
        getShelfPositionById(shelfPositionId);
        String query = """
                MATCH (device:Device)-[:HAS]-(shelfPosition:ShelfPosition) WHERE elementId(shelfPosition) = $id
                SET shelfPosition.isDeleted = true
                SET device.numberOfShelfPositions = device.numberOfShelfPositions - 1
                WITH shelfPosition
                OPTIONAL MATCH (shelfPosition)-[:HAS]-(shelf:Shelf) SET shelf.isDeleted = true
                RETURN shelfPosition
                """;
        driver.executableQuery(query).withParameters(Map.of("id", shelfPositionId)).execute().records();
        return true;
    }

    public ShelfPosition addShelfPositions(String deviceId) throws DeviceNotFoundException {
        getDeviceById(deviceId);
        String query = """
                MATCH (device:Device) WHERE elementId(device) = $id AND device.isDeleted = false
                SET device.numberOfShelfPositions = device.numberOfShelfPositions + 1
                CREATE (shelfPosition:ShelfPosition {
                    deviceId: $id,
                    isDeleted: false
                })
                MERGE (device)-[:HAS]->(shelfPosition)
                RETURN collect(shelfPosition) AS shelfPositions
                """;
        var records = driver.executableQuery(query).withParameters(Map.of(
                "id", deviceId
        )).execute().records();

        logger.info("Shelf Position Repository: 1 Shelf Positions added for {}", deviceId);

        return records
                .stream()
                .flatMap(record -> record.get("shelfPositions").asList(Value::asNode).stream())
                .map(node -> {
                    ShelfPosition shelfPosition = new ShelfPosition();
                    shelfPosition.setId(node.elementId());
                    shelfPosition.setDeviceId(node.get("deviceId").asString());
                    shelfPosition.setIsDeleted(node.get("isDeleted").asBoolean());
                    return shelfPosition;
                })
                .toList().getFirst();
    }
}
