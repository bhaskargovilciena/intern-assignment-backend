package com.intern.assignment.controllers;

import com.intern.assignment.services.ShelfPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/shelf-position")
public class ShelfPositionController {
    private final ShelfPositionService shelfPositionService;

    @Autowired
    public ShelfPositionController(ShelfPositionService shelfPositionService) {
        this.shelfPositionService = shelfPositionService;
    }

    @DeleteMapping("/delete")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Boolean> deleteShelfPosition(@RequestParam (value = "id") String shelfPositionId) {
        return new ResponseEntity<>(shelfPositionService.deleteShelf(shelfPositionId), HttpStatus.OK);
    }
}
