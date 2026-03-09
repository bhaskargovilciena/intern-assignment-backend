package com.intern.assignment.services;

import com.intern.assignment.entities.Shelf;
import com.intern.assignment.exceptions.ShelfCannotBeCreatedException;
import com.intern.assignment.exceptions.ShelfCannotBeLinkedToShelfPosition;
import com.intern.assignment.exceptions.ShelfNotFoundException;
import com.intern.assignment.exceptions.ShelfPositionNotFoundException;
import com.intern.assignment.repositories.ShelfRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShelfServiceTest {
    @Mock
    ShelfRepository shelfRepository;

    @InjectMocks
    ShelfService shelfService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private static Shelf createSampleShelf() {
        Shelf shelf = new Shelf();
        shelf.setId("123");
        shelf.setIsDeleted(false);
        shelf.setPartNumber("demo");
        shelf.setName("demo");
        return shelf;
    }

    @Test
    void testCreateShelf() throws ShelfCannotBeCreatedException {
        Shelf shelf = createSampleShelf();
        Mockito.when(shelfRepository.createShelf(shelf)).thenReturn(shelf);
        assertEquals(shelfService.createShelf(shelf), shelf);
    }

    @Test
    void testLinkShelfToShelfPosition() throws ShelfPositionNotFoundException, ShelfNotFoundException, ShelfCannotBeLinkedToShelfPosition {
        Shelf shelf = createSampleShelf();
        String shelfPositionId = "abc";
        Mockito.when(shelfRepository.linkShelfToShelfPosition(shelfPositionId, shelf.getId())).thenReturn(shelf);
        assertEquals(shelfService.linkShelfToShelfPosition(shelfPositionId, shelf.getId()), shelf);
    }

    @Test
    void testGetShelf() throws ShelfPositionNotFoundException {
        Shelf shelf = createSampleShelf();
        String shelfPositionId = "abc";
        Mockito.when(shelfRepository.getShelf(shelfPositionId)).thenReturn(shelf);
        assertEquals(shelfService.getShelf(shelfPositionId), shelf);
    }

    @Test
    void testUpdateShelf() throws ShelfNotFoundException {
        Shelf oldShelf = createSampleShelf();
        String newName = "new name";
        Shelf newShelf;
        newShelf = oldShelf;
        newShelf.setName(newName);
        Mockito.when(shelfRepository.updateShelf(oldShelf.getId(), newName, null)).thenReturn(newShelf);
        assertEquals(shelfService.updateShelf(oldShelf.getId(), newName, null), newShelf);
    }

    @Test
    void testDeleteAllShelves() throws ShelfPositionNotFoundException {
        String shelfPositionId = "abc";
        shelfService.deleteAllShelves(shelfPositionId);
    }

    @Test
    void testDeleteShelf() throws ShelfNotFoundException {
        Shelf shelf = createSampleShelf();
        Mockito.when(shelfRepository.deleteShelf(shelf.getId())).thenReturn(true);
        assertTrue(shelfService.deleteShelf(shelf.getId()));
    }

    @Test
    void getAvailableShelves() {
        List<Shelf> shelves = new ArrayList<>();
        shelves.add(createSampleShelf());
        shelves.add(createSampleShelf());
        Mockito.when(shelfRepository.getAllAvailableShelves()).thenReturn(shelves);
        assertEquals(shelfService.getAvailableShelves(), shelves);
    }
}
