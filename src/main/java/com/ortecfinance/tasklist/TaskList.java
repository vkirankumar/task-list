package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.TaskService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";

    private final TaskService service = new TaskService();
    private final BufferedReader in;
    private final PrintWriter out;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskList(in, out).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        this.in = reader;
        this.out = writer;
//        addTestData();
    }

    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "show":
                show(null);
                break;
            case "add":
                addTask(commandRest[1]);
                break;
            case "check":
                check(commandRest[1]);
                break;
            case "uncheck":
                uncheck(commandRest[1]);
                break;
            case "help":
                help();
                break;
            case "deadline":
                deadline(commandRest[1]);
                break;
            case "today":
                today();
                break;
            case "view-by-deadline":
                viewByDeadline();
                break;
            case "clear-deadline":
                clearDeadline(commandRest[1]);
                break;
            default:
                error(command);
                break;
        }
    }

    private void clearDeadline(String commandLine) {
        String[] subCommand = commandLine.split(" ");
        int taskId = Integer.parseInt(subCommand[0]);
        if (!service.clearDeadline(taskId)) {
            out.printf("Could not find a task with an ID of %d.", taskId);
            out.println();
        }
    }

    private void deadline(String commandLine) {
        String[] subCommand = commandLine.split(" ", 2);
        int taskId = Integer.parseInt(subCommand[0]);
        try {
            LocalDate date = LocalDate.parse(subCommand[1], TaskService.DATE_FORMAT);
            if (!service.setDeadLine(taskId, date)) {
                out.printf("Could not find a task with an ID of %d.", taskId);
                out.println();
            }
        } catch (DateTimeParseException ex) {
            out.printf("Invalid date! Please enter a date in DD-MM-YYYY format => " + subCommand[1]);
            out.println();
        }
    }

    private void show(Map<String, List<Task>> data) {
        Map<String, List<Task>> collection = data == null ? service.getProjects() : data;
        for (Map.Entry<String, List<Task>> project : collection.entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                String deadline = "";
                if (task.getDeadline() != null) {
                    deadline = task.getDeadline().format(TaskService.DATE_FORMAT);
                }
                out.printf("    [%c] %d: %s %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription(), deadline);
            }
            out.println();
        }
    }

    private void today() {
        Map<String, List<Task>> result = service.getTodayTasks();
        if (result.isEmpty()) {
            out.println("No tasks found!!");
        } else {
            show(result);
        }
    }

    private void viewByDeadline() {
        Map<String, Map<String, List<Task>>> sortedData = service.sortByDeadLine();
        sortedData.forEach((key, value) -> {
            if (!key.isEmpty()) {
                //            out.printf("     ");
                out.println(key + ":");
                show(value);
                out.println("--------");
            }
        });
        if (!sortedData.isEmpty() && sortedData.get("") != null) {
            out.println("No Deadlines:");
            show(sortedData.get(""));
            out.println("--------");
        }
    }

    private void addTask(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String projectName) {
        service.addProject(projectName);
    }

    private void addTask(String project, String description) {
        if (!service.addTask(project, description)) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
        }
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        int taskId = Integer.parseInt(idString);
        if (!service.setDone(taskId, done)) {
            out.printf("Could not find a task with an ID of %d.", taskId);
            out.println();
        }
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <Date(DD-MM-YYYY)>");
        out.println("  today (Show tasks that has deadline as today)");
        out.println("  view-by-deadline");
        out.println("  clear-deadline <task ID>");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }

    private void addTestData() {
        service.getProjects().put("Daily Routine",
                List.of(new Task(1, "Breakfast", true, LocalDate.parse("10-11-2026", TaskService.DATE_FORMAT)),
                        new Task(2, "Lunch", false, LocalDate.parse("10-11-1986", TaskService.DATE_FORMAT)),
                        new Task(3, "Breakfast", false, LocalDate.now().minusDays(10)),
                        new Task(4, "Vitamin Supplement", false, LocalDate.now()),
                        new Task(5, "Brunch", true, null)));
        service.getProjects().put("Fitness",
                List.of(new Task(6, "Warm Up", true, LocalDate.parse("10-11-2024", TaskService.DATE_FORMAT)),
                        new Task(7, "Push Up", false, LocalDate.now()),
                        new Task(8, "Abs", false, null)));
    }
}