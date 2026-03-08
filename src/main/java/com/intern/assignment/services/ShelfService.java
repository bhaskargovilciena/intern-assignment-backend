package com.intern.assignment.services;

import com.intern.assignment.entities.Shelf;
import com.intern.assignment.exceptions.ShelfCannotBeLinkedToShelfPosition;
import com.intern.assignment.exceptions.ShelfNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import com.intern.assignment.exceptions.ShelfCannotBeCreatedException;
import com.intern.assignment.repositories.ShelfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShelfService {
    private final ShelfRepository shelfRepository;
    private final Logger logger = LoggerFactory.getLogger(ShelfService.class);

    @Autowired
    public ShelfService(ShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }

    public Shelf createShelf(Shelf shelf) throws ShelfCannotBeCreatedException {
        if(shelf.getName().isBlank()) throw new ShelfCannotBeCreatedException("shelf name cannot be empty");
        if(shelf.getPartNumber().isBlank()) throw new ShelfCannotBeCreatedException("shelf part number cannot be blank");
        logger.info("Shelf Service: Shelf creation requested and forwarded to repository");
        return shelfRepository.createShelf(shelf);
    }

    public Shelf linkShelfToShelfPosition(String shelfPositionId, String shelfId) throws ShelfPositionNotFoundException, ShelfNotFoundException, ShelfCannotBeLinkedToShelfPosition {
        if(shelfPositionId.isBlank()) throw new ShelfPositionNotFoundException("shelf position id can't be null");
        if(shelfId.isBlank()) throw new ShelfNotFoundException("shelf id can't be null");
        logger.info("Shelf Service: Shelf with ID: {} to link with shelf position id: {}", shelfId, shelfPositionId);
        return shelfRepository.linkShelfToShelfPosition(shelfPositionId, shelfId);
    }

    public Shelf getShelf(String shelfPositionId) throws ShelfPositionNotFoundException {
        logger.info("Shelf Service: Shelf read requested and forwarded to repository");
        return shelfRepository.getShelf(shelfPositionId);
    }

    public Shelf updateShelf(String shelfId, String name, String partNumber) throws ShelfNotFoundException {
        logger.info("Shelf Service: Shelf updated requested and forwarded to repository");
        return shelfRepository.updateShelf(shelfId, name, partNumber);
    }

    public void deleteAllShelves(String shelfPositionId) throws ShelfPositionNotFoundException {
        logger.info("Shelf Service: Shelf deletion requested and forwarded to repository");
        shelfRepository.deleteAllShelves(shelfPositionId);
    }

    public Boolean deleteShelf(String shelfId) throws ShelfNotFoundException {
        logger.info("Shelf Service: Shelf deletion requested and forwarded to repository for ID: {}", shelfId);
        return shelfRepository.deleteShelf(shelfId);
    }

    public List<Shelf> getAvailableShelves() {
        logger.info("Shelf Service: All available shelves requested");
        return shelfRepository.getAllAvailableShelves();
    }
}
