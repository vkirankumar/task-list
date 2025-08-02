package com.ortecfinance.tasklist.api.controller;

import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping
    public Map<String, List<Task>> getProjects() {
        return taskService.getProjects();
    }

    @PostMapping
    public ResponseEntity<String> addProject(@RequestBody String projectName) {
        taskService.addProject(projectName);
        return ResponseEntity.ok(String.format("Project %s created successfully", projectName));
    }

    @PostMapping("/{projectName}/tasks")
    public ResponseEntity<String> addTask(@PathVariable String projectName, @RequestBody String taskName) {
        if (taskService.addTask(projectName, taskName)) {
            return ResponseEntity.ok(String.format("Task %s created for project '%s' successfully", taskName, projectName));
        }
        return ResponseEntity.badRequest().body("Invalid project name!");
    }

    @GetMapping("/view_by_deadline")
    public Map<String, Map<String, List<Task>>> getTasksByDeadline() {
        return taskService.sortByDeadLine();
    }

    @PutMapping("/tasks/{taskId}/deadline")
    public ResponseEntity<String> updateTaskDeadline(@PathVariable String taskId, @RequestBody String date) {
        if (taskService.setDeadLine(Integer.parseInt(taskId), date)) {
            return ResponseEntity.ok("Success");
        } else {
            return ResponseEntity.badRequest().body("Invalid date or task id");
        }
    }
}