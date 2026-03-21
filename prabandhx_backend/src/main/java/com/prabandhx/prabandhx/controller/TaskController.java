package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.TaskDTO;
import com.prabandhx.prabandhx.service.TaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@PreAuthorize("isAuthenticated()")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ===============================
    // CREATE TASK
    // ===============================
    @PostMapping("/project/{projectId}")
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO,
                                        @PathVariable Long projectId) {
        try {
            TaskDTO createdTask = taskService.createTask(taskDTO, projectId);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating task: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET ALL TASKS
    // ===============================
    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET TASK BY ID
    // ===============================
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            TaskDTO task = taskService.getTaskById(id);
            return new ResponseEntity<>(task, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Task not found with id: " + id, 
                                        HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching task: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET TASKS BY PROJECT
    // ===============================
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@PathVariable Long projectId) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByProject(projectId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET TASKS BY ASSIGNEE
    // ===============================
    @GetMapping("/assignee/{userId}")
    public ResponseEntity<?> getTasksByAssignee(@PathVariable Long userId) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByAssignee(userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET TASKS BY STATUS
    // ===============================
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable String status) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByStatus(status);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET TASKS BY PROJECT AND STATUS
    // ===============================
    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<?> getTasksByProjectAndStatus(
            @PathVariable Long projectId,
            @PathVariable String status) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByProjectAndStatus(projectId, status);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET OVERDUE TASKS
    // ===============================
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTasks() {
        try {
            List<TaskDTO> tasks = taskService.getOverdueTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching overdue tasks: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // GET TASKS DUE TODAY
    // ===============================
    @GetMapping("/due-today")
    public ResponseEntity<?> getTasksDueToday() {
        try {
            List<TaskDTO> tasks = taskService.getTasksDueToday();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching tasks due today: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // UPDATE TASK
    // ===============================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Task not found with id: " + id, 
                                        HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating task: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // UPDATE TASK STATUS
    // ===============================
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            TaskDTO updatedTask = taskService.updateTaskStatus(id, status);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Task not found with id: " + id, 
                                        HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating task status: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // ASSIGN TASK TO USER
    // ===============================
    @PatchMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<?> assignTaskToUser(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        try {
            TaskDTO updatedTask = taskService.assignTaskToUser(taskId, userId);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error assigning task: " + e.getMessage(), 
                                        HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error assigning task: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===============================
    // DELETE TASK
    // ===============================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Task not found with id: " + id, 
                                        HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting task: " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}