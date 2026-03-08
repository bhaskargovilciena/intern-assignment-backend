package com.intern.assignment.controllers;

import com.intern.assignment.entities.Shelf;
import com.intern.assignment.exceptions.ShelfCannotBeCreatedException;
import com.intern.assignment.exceptions.ShelfCannotBeLinkedToShelfPosition;
import com.intern.assignment.exceptions.ShelfNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import com.intern.assignment.services.ShelfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shelf")
public class ShelfController {
    private final ShelfService shelfService;
    private final Logger logger = LoggerFactory.getLogger(ShelfController.class);

    @Autowired
    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Shelf> createShelf(@RequestBody Shelf shelf) throws ShelfCannotBeCreatedException {
        logger.info("Shelf Controller: Shelf creation requested");
        return new ResponseEntity<>(shelfService.createShelf(shelf), HttpStatus.OK);
    }

    @PutMapping("/link")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Shelf> linkShelfToShelfPosition(
            @RequestParam(value = "shelfPositionId") String shelfPositionId,
            @RequestParam(value = "shelfId") String shelfId
    ) throws ShelfNotFoundException, ShelfPositionNotFoundException, ShelfCannotBeLinkedToShelfPosition {
        logger.info("Shelf Controller: shelf link with shelf position requested");
        return new ResponseEntity<>(shelfService.linkShelfToShelfPosition(shelfPositionId, shelfId), HttpStatus.OK);
    }

    @GetMapping("/get")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Shelf> getShelf(@RequestParam (value = "shelfPositionId") String shelfPositionId) throws ShelfPositionNotFoundException {
        logger.info("Shelf Controller: Shelf read requested");
        return new ResponseEntity<>(shelfService.getShelf(shelfPositionId), HttpStatus.OK);
    }

    @GetMapping("/get/available")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<List<Shelf>> getAvailableShelves() {
        logger.info("Shelf Controller: Available shelves requested");
        return new ResponseEntity<>(shelfService.getAvailableShelves(), HttpStatus.OK);
    }

    @PutMapping("/update")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Shelf> updateShelf(
            @RequestParam(value = "shelfId") String shelfId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "partNumber", required = false) String partNumber
    ) throws ShelfNotFoundException {
        logger.info("Shelf Controller: Shelf update requested");
        return new ResponseEntity<>(shelfService.updateShelf(shelfId, name, partNumber), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Boolean> deleteShelf(@RequestParam(value = "shelfId") String shelfId) throws ShelfNotFoundException {
        logger.info("Shelf Controller: Shelf deletion requested for ID: {}", shelfId);
        return new ResponseEntity<>(shelfService.deleteShelf(shelfId),HttpStatus.OK);
    }
}
