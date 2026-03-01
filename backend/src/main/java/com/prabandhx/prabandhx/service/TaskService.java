package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.entity.*;
import com.prabandhx.prabandhx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public Task createTask(Task task, Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow();

        task.setProject(project);

        return taskRepository.save(task);
    }

    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }
}