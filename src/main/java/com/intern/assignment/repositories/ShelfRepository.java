package com.intern.assignment.repositories;

import com.intern.assignment.config.DatabaseConnection;
import com.intern.assignment.entities.Shelf;
import com.intern.assignment.exceptions.ShelfCannotBeLinkedToShelfPosition;
import com.intern.assignment.exceptions.ShelfNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ShelfRepository {
    private static final Driver driver = DatabaseConnection.initialise();
    private static final Logger logger = LoggerFactory.getLogger(ShelfRepository.class);

    public Shelf createShelf(Shelf shelf) {
        String query = """
                MERGE (shelf:Shelf {
                    name: $name,
                    partNumber: $partNumber,
                    isDeleted: false
                })
                RETURN shelf
                """;
        var records = driver.executableQuery(query).withParameters(Map.of(
                "name", shelf.getName(),
                "partNumber", shelf.getPartNumber()
        )).execute().records();

        records.forEach(record -> {
            Node node = record.get("shelf").asNode();
            shelf.setId(node.elementId());
            shelf.setIsDeleted(node.get("isDeleted").asBoolean());
        });
        logger.info("Shelf Repository: Shelf created with ID {}", shelf.getId());
        return shelf;
    }

    public Shelf linkShelfToShelfPosition(String shelfPositionId, String shelfId) throws ShelfCannotBeLinkedToShelfPosition, ShelfPositionNotFoundException, ShelfNotFoundException {
        getShelfById(shelfId);
        if(getShelf(shelfPositionId) != null) throw new ShelfCannotBeLinkedToShelfPosition("shelf position already has a shelf");
        String query = """
                MATCH (shelfPosition:ShelfPosition) WHERE elementId(shelfPosition) = $shelfPositionId
                MATCH (shelf:Shelf) WHERE elementId(shelf) = $shelfId
                MERGE (shelfPosition)-[:HAS]-(shelf)
                RETURN shelf
                """;
        Shelf shelf = new Shelf();
        driver.executableQuery(query).withParameters(Map.of(
                "shelfPositionId", shelfPositionId,
                "shelfId", shelfId
        )).execute().records().forEach(r -> {
            Node node = r.get("shelf").asNode();
            shelf.setIsDeleted(node.get("isDeleted").asBoolean());
            shelf.setId(node.elementId());
            shelf.setName(node.get("name").asString());
            shelf.setPartNumber(node.get("partNumber").asString());
        });
        return shelf;
    }

    public Shelf getShelf(String shelfPositionId) throws ShelfPositionNotFoundException {
        getShelfPositionById(shelfPositionId);
        String query = """
                MATCH (shelfPosition:ShelfPosition) WHERE elementId(shelfPosition) = $id AND shelfPosition.isDeleted = false
                MATCH (shelfPosition)-[:HAS]->(shelf:Shelf) WHERE shelf.isDeleted = false AND elementId(shelf) IS NOT NULL
                RETURN shelf
                """;
        var records = driver.executableQuery(query).withParameters(Map.of("id", shelfPositionId)).execute().records();
        Shelf shelf = new Shelf();
        records.forEach(record -> {
            Node node = record.get("shelf").asNode();
            shelf.setId(node.elementId());
            shelf.setPartNumber(node.get("partNumber").asString());
            shelf.setName(node.get("name").asString());
            shelf.setIsDeleted(node.get("isDeleted").asBoolean());
        });
        logger.info("Shelf Repository: Shelf reads performed for shelf position ID: {}", shelfPositionId);
        if(shelf.getId() == null) return null;
        return shelf;
    }

    public void getShelfById(String shelfId) throws ShelfNotFoundException {
        String query = """
                MATCH (shelf:Shelf) WHERE elementId(shelf) = $id
                RETURN shelf
                """;
        driver.executableQuery(query).withParameters(Map.of("id", shelfId)).execute().records()
                .stream().findAny()
                .orElseThrow(() -> new ShelfNotFoundException("shelf with ID: " + shelfId + " could not be found"));
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

    public Shelf updateShelf(String shelfId, String name, String partNumber) throws ShelfNotFoundException {
        getShelfById(shelfId);
        StringBuilder query = new StringBuilder("MATCH (shelf:Shelf) WHERE elementId(shelf) = $id AND shelf.isDeleted = false SET ");
        Map<String, Object> params = new HashMap<>();
        params.put("id", shelfId);

        if(name != null) {
            query.append("shelf.name = $name, ");
            params.put("name", name);
        }
        if(partNumber != null) {
            query.append("shelf.partNumber = $partNumber, ");
            params.put("partNumber", partNumber);
        }

        query.setLength(query.length() - 2);
        query.append(" RETURN shelf");

        var records = driver.executableQuery(query.toString()).withParameters(params).execute().records();
        Shelf shelf = new Shelf();
        records.forEach(record -> {
            Node node = record.get("shelf").asNode();
            shelf.setName(node.get("name").asString());
            shelf.setId(node.elementId());
            shelf.setPartNumber(node.get("partNumber").asString());
            shelf.setIsDeleted(node.get("isDeleted").asBoolean());
        });
        logger.info("Shelf Repository: Shelf updated for ID: {}",shelfId);
        return shelf;
    }

    public void deleteAllShelves(String shelfPositionId) throws ShelfPositionNotFoundException {
        getShelfPositionById(shelfPositionId);
        String query = """
                MATCH (shelfPosition:ShelfPosition)-[r:HAS]->(shelf:Shelf)
                WHERE elementId(shelfPosition) = $id
                SET shelf.isDeleted = true
                DELETE r
                """;

        driver.executableQuery(query).withParameters(Map.of("id", shelfPositionId)).execute();
    }

    public Boolean deleteShelf(String shelfId) throws ShelfNotFoundException {
        getShelfById(shelfId);
        String query = """
                MATCH (:ShelfPosition)-[r:HAS]->(shelf:Shelf) WHERE elementId(shelf) = $shelfId
                SET shelf.isDeleted = true
                DELETE r
                """;
        driver.executableQuery(query).withParameters(Map.of("shelfId", shelfId)).execute().records();

        return true;
    }

    public List<Shelf> getAllAvailableShelves() {
        String query = """
                MATCH (shelf:Shelf {isDeleted : false})
                WHERE NOT (shelf)<-[:HAS]-(:ShelfPosition)
                RETURN collect(shelf) as shelves
                """;
        List<Shelf> shelves = new ArrayList<>();
        driver.executableQuery(query).execute().records().stream()
                .flatMap(record -> record.get("shelves").asList(Value::asNode).stream())
                .forEach(node -> {
                    Shelf shelf =  new Shelf();
                    shelf.setName(node.get("name").asString());
                    shelf.setId(node.elementId());
                    shelf.setIsDeleted(node.get("isDeleted").asBoolean());
                    shelf.setPartNumber(node.get("partNumber").asString());
                    shelves.add(shelf);
                });
        return shelves;
    }
}
