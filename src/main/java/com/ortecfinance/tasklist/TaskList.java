package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.model.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private final BufferedReader in;
    private final PrintWriter out;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private long lastId = 0;

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
                add(commandRest[1]);
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
                sortByDeadline();
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
        int id = Integer.parseInt(subCommand[0]);
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDeadline(null);
                    return;
                }
            }
        }
        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
    }

    private void deadline(String commandLine) {
        String[] subCommand = commandLine.split(" ", 2);
        int id = Integer.parseInt(subCommand[0]);
        try {
            LocalDate deadline = LocalDate.parse(subCommand[1], DATE_FORMAT);
            for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
                for (Task task : project.getValue()) {
                    if (task.getId() == id) {
                        task.setDeadline(deadline);
                        return;
                    }
                }
            }
            out.printf("Could not find a task with an ID of %d.", id);
        } catch (DateTimeParseException ex) {
            out.printf("Invalid date! Please enter a date in DD-MM-YYYY format => " + subCommand[1]);
        }
        out.println();
    }

    private void show(Map<String, List<Task>> data) {
        Map<String, List<Task>> collection = data == null ? tasks : data;
        for (Map.Entry<String, List<Task>> project : collection.entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                String deadline = "";
                if (task.getDeadline() != null) {
                    deadline = task.getDeadline().format(DATE_FORMAT);
                }
                out.printf("    [%c] %d: %s %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription(), deadline);
            }
            out.println();
        }
    }

    private void today() {
        Map<String, List<Task>> result = tasks.entrySet().stream().flatMap(entry -> entry.getValue()
                .stream().map(task -> Map.entry(entry.getKey(), task))).filter(item ->
                item.getValue().getDeadline() != null && item.getValue().getDeadline().format(DATE_FORMAT).equals(LocalDate.now().format(DATE_FORMAT))
        ).collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
        ));
        if (result.isEmpty()) {
            out.println("No tasks found!!");
        } else {
            show(result);
        }
    }

    private void sortByDeadline() {
        Map<String, Map<String, List<Task>>> sortedData = tasks.entrySet().stream().flatMap(entry -> entry.getValue()
                .stream().map(task -> Map.entry(task.getDeadline() != null ? task.getDeadline().format(DATE_FORMAT) : "",
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

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String name) {
        tasks.put(name, new ArrayList<Task>());
    }

    private void addTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);
        if (projectTasks == null) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
            return;
        }
        projectTasks.add(new Task(nextId(), description, false, null));
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        int id = Integer.parseInt(idString);
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDone(done);
                    return;
                }
            }
        }
        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
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

    private long nextId() {
        return ++lastId;
    }

    private void addTestData() {
        tasks.put("Daily Routine",
                List.of(new Task(1, "Breakfast", true, LocalDate.parse("10-11-2026", DATE_FORMAT)),
                        new Task(2, "Lunch", false, LocalDate.parse("10-11-1986", DATE_FORMAT)),
                        new Task(3, "Breakfast", false, LocalDate.now().minusDays(10)),
                        new Task(4, "Vitamin Supplement", false, LocalDate.now()),
                        new Task(5, "Brunch", true, null)));
        tasks.put("Fitness",
                List.of(new Task(6, "Warm Up", true, LocalDate.parse("10-11-2024", DATE_FORMAT)),
                        new Task(7, "Push Up", false, LocalDate.now()),
                        new Task(8, "Abs", false, null)));
    }
}