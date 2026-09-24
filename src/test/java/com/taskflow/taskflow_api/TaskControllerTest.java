package com.taskflow.taskflow_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskControllerTest {

    @Autowired
    private TaskController controller;

    @Autowired
    private TaskRepository repository;

    @Test
    void contextLoads() {
        assertNotNull(controller);
    }

    @Test
    void createAndFetchTask() {
        Task task = new Task(null, "Test Task", "Testing description", false);
        Task saved = controller.create(task);

        assertNotNull(saved.getId());
        assertEquals("Test Task", saved.getTitle());

        List<Task> all = controller.getAll();
        assertTrue(all.size() > 0);
    }

    @Test
    void getByIdReturnsTask() {
        Task task = controller.create(new Task(null, "Find Me", "desc", false));
        assertTrue(controller.getById(task.getId()).getStatusCode().is2xxSuccessful());
    }

    @Test
    void deleteRemovesTask() {
        Task task = controller.create(new Task(null, "Delete Me", "desc", false));
        controller.delete(task.getId());
        assertFalse(repository.existsById(task.getId()));
    }

    @Test
    void updateModifiesTask() {
        Task task = controller.create(new Task(null, "Original", "desc", false));
        Task updated = new Task(null, "Updated", "new desc", true);
        controller.update(task.getId(), updated);

        Task fetched = repository.findById(task.getId()).orElseThrow();
        assertEquals("Updated", fetched.getTitle());
        assertTrue(fetched.isCompleted());
    }
}