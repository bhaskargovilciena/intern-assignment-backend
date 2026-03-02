package com.intern.assignment.repositories;

import com.intern.assignment.config.DatabaseConnection;
import com.intern.assignment.entities.Shelf;
import com.intern.assignment.entities.ShelfPosition;
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

    public List<ShelfPosition> createShelfPosition(String deviceId, int numberOfShelfPositions) {
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

    public List<Map<String,Object>> getShelfPositions(String deviceId) {
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
                    Shelf shelf = shelfService.getShelf(shelfPosition.getId());
                    Map<String,Object> map = new HashMap<>();
                    map.put("shelfPosition", shelfPosition);
                    if(shelf != null) map.put("shelf", shelf);
                    shelfPositions.add(map);
                });

        logger.info("Shelf Position Repository: Shelf Position read requested for device ID: {}", deviceId);

        return shelfPositions;
    }

    public void deleteAllShelfPositions(String deviceId) {
        String query = """
                MATCH (device:Device)-[r:HAS]->(shelfPosition:ShelfPosition)
                WHERE elementId(device) = $id
                SET shelfPosition.isDeleted = true
                RETURN shelfPosition
                """;

        List<Record> records = driver.executableQuery(query).withParameters(Map.of("id", deviceId)).execute().records();

        records.forEach(record -> {
            Node node = record.get("shelfPosition").asNode();
            shelfService.deleteAllShelves(node.elementId());
        });
    }

    public Boolean deleteShelfPosition(String shelfPositionId) {
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
}
