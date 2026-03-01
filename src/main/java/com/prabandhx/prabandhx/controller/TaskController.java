package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.entity.Task;
import com.prabandhx.prabandhx.service.TaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@PreAuthorize("isAuthenticated()")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{projectId}")
    public Task create(@RequestBody Task task,
                       @PathVariable Long projectId) {
        return taskService.createTask(task, projectId);
    }

    @GetMapping("/{projectId}")
    public List<Task> getByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
    }
}