package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.model.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    public final Map<String, List<Task>> dataProvider = new LinkedHashMap<>();
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private long lastId = 0;

    public void addProject(String projectName) {
        dataProvider.computeIfAbsent(projectName, k -> new ArrayList<>());
    }

    public boolean addTask(String projectName, String task) {
        if (dataProvider.get(projectName) == null) {
            return false;
        }
        dataProvider.get(projectName).add(new Task(nextId(), task, false, null));
        return true;
    }

    public boolean setDone(int taskId, boolean isDone) {
        for (Map.Entry<String, List<Task>> project : dataProvider.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == taskId) {
                    task.setDone(isDone);
                    return true;
                }
            }
        }
        return false;
    }

    public Map<String, List<Task>> getTodayTasks() {
        return dataProvider.entrySet().stream().flatMap(entry -> entry.getValue()
                .stream().map(task -> Map.entry(entry.getKey(), task))).filter(item ->
                item.getValue().getDeadline() != null && item.getValue().getDeadline()
                        .format(DATE_FORMAT).equals(LocalDate.now()
                                .format(DATE_FORMAT))
        ).collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
        ));
    }

    public Map<String, Map<String, List<Task>>> sortByDeadLine() {
        return dataProvider.entrySet().stream().flatMap(entry -> entry.getValue()
                        .stream().map(task -> Map.entry(task.getDeadline() != null ? task.getDeadline()
                                        .format(DATE_FORMAT) : "",
                                new AbstractMap.SimpleEntry<>(entry.getKey(), task))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue,
                                Collectors.groupingBy(
                                        Map.Entry::getKey,
                                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                                )
                        )
                ));
    }

    public boolean clearDeadline(int taskId) {
        for (Map.Entry<String, List<Task>> project : dataProvider.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == taskId) {
                    task.setDeadline(null);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setDeadLine(int taskId, LocalDate date) {
        for (Map.Entry<String, List<Task>> project : dataProvider.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == taskId) {
                    task.setDeadline(date);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setDeadLine(int taskId, String date) {
        return this.setDeadLine(taskId, LocalDate.parse(date, DATE_FORMAT));
    }

    public Map<String, List<Task>> getProjects() {
        return dataProvider;
    }

    private long nextId() {
        return ++lastId;
    }
}
