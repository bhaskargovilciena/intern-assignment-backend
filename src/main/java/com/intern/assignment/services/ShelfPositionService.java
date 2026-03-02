package com.intern.assignment.services;

import com.intern.assignment.entities.ShelfPosition;
import com.intern.assignment.repositories.ShelfPositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShelfPositionService {
    private final ShelfPositionRepository shelfPositionRepository;
    private static final Logger logger = LoggerFactory.getLogger(ShelfPositionService.class);

    @Autowired
    public ShelfPositionService(ShelfPositionRepository shelfPositionRepository) {
        this.shelfPositionRepository = shelfPositionRepository;
    }

    public List<ShelfPosition> createShelfPositions(String deviceId, int numberOfShelfPositions) {
        logger.info("Shelf Position Service: Shelf Positions creation request for device ID: {} accepted and forwarded to repository", deviceId);
        return shelfPositionRepository.createShelfPosition(deviceId, numberOfShelfPositions);
    }

    public List<Map<String,Object>> getShelfPositions(String deviceId) {
        logger.info("Shelf Position Service: Shelf Position reads request accepted for device ID: {} and forwarded to repository", deviceId);
        return shelfPositionRepository.getShelfPositions(deviceId);
    }

    public void deleteAllShelfPositions(String deviceId) {
        logger.info("Shelf Position Service: Shelf Position delete requested and accepted for device ID: {}", deviceId);
        shelfPositionRepository.deleteAllShelfPositions(deviceId);
    }
}
