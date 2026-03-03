package com.intern.assignment.services;

import com.intern.assignment.entities.ShelfPosition;
import com.intern.assignment.exceptions.DeviceNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionCannotBeCreatedException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import com.intern.assignment.repositories.ShelfPositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<ShelfPosition> createShelfPositions(String deviceId, int numberOfShelfPositions) throws ShelfPositionCannotBeCreatedException {
        logger.info("Shelf Position Service: Shelf Positions creation request for device ID: {} accepted and forwarded to repository", deviceId);
        return shelfPositionRepository.createShelfPosition(deviceId, numberOfShelfPositions);
    }

    public List<Map<String,Object>> getShelfPositions(String deviceId) throws DeviceNotFoundException {
        logger.info("Shelf Position Service: Shelf Position reads request accepted for device ID: {} and forwarded to repository", deviceId);
        return shelfPositionRepository.getShelfPositions(deviceId);
    }

    public void deleteAllShelfPositions(String deviceId) throws DeviceNotFoundException {
        logger.info("Shelf Position Service: Shelf Position delete requested and accepted for device ID: {}", deviceId);
        shelfPositionRepository.deleteAllShelfPositions(deviceId);
    }

    public Boolean deleteShelf(String shelfPositionId) throws ShelfPositionNotFoundException {
        logger.info("Shelf Position Service: Shelf Position delete requested and accepted for shelf position id: {}", shelfPositionId);
        return shelfPositionRepository.deleteShelfPosition(shelfPositionId);
    }

    public List<ShelfPosition> addShelfPositions(String deviceId, int numberOfShelfPositions) throws DeviceNotFoundException {
        logger.info("Shelf Position Service: Shelf positions added for device ID: {}", deviceId);
        List<ShelfPosition> result = new ArrayList<>();
        for(int i=0;i<numberOfShelfPositions;i++) result.add(shelfPositionRepository.addShelfPositions(deviceId));
        return result;
    }
}
