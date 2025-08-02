# Task List
 - Task list is a small spring-boot application to add and maintain a schedule as projects and it respective tasks.
 - It enables users to add tasks with a deadline, view them sorted for today and mark them as done/undone.
 - The app has two modes, Web and console.
**********************************************************************************************************************
### REST API Endpoints:
- `POST /projects`: Create a new project
- `GET /projects`: Returns all projects and underlying tasks
- `POST /projects/{project_id}/tasks`: Create a new task for a project
- `PUT /projects/{project_id}/tasks/{task_id}/deadline`: Update the deadline for a task
- `GET /projects/view_by_deadline`: Get all tasks grouped by deadline (or also by project if you did the optional part)

### Console App Commands:
- show
- add project <project name>
- add task <project name> <task description>
- check <task ID>
- uncheck <task ID>
- deadline <task ID> <Date(DD-MM-YYYY)>
- today (Show tasks that has deadline as today)
- view-by-deadline
- clear-deadline <task ID>
***********************************************************************************************************************

## Running the application
### Console Mode:
```
mvn spring-boot:run -Dspring-boot.run.arguments="console"
```

### Web mode:
```
mvn spring-boot:run
```
***********************************************************************************************************************