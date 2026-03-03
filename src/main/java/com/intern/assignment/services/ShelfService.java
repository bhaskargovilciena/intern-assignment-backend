package com.intern.assignment.services;

import com.intern.assignment.entities.Shelf;
import com.intern.assignment.exceptions.ShelfNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import com.intern.assignment.exceptions.ShelfCannotBeCreatedException;
import com.intern.assignment.repositories.ShelfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShelfService {
    private final ShelfRepository shelfRepository;
    private final Logger logger = LoggerFactory.getLogger(ShelfService.class);

    @Autowired
    public ShelfService(ShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }

    public Shelf createShelf(String shelfPositionId, Shelf shelf) throws ShelfCannotBeCreatedException {
        if(shelf.getName().isBlank()) throw new ShelfCannotBeCreatedException("shelf name cannot be empty");
        if(shelf.getPartNumber().isBlank()) throw new ShelfCannotBeCreatedException("shelf part number cannot be blank");
        logger.info("Shelf Service: Shelf creation requested and forwarded to repository");
        return shelfRepository.createShelf(shelfPositionId, shelf);
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
}
