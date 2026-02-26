package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.entity.Task;
import com.prabandhx.prabandhx.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

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