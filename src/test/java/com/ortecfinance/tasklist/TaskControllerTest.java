package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.TaskService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Task Controller Test")
class TaskControllerTest {

	@Autowired
    MockMvc mvc;

	@Autowired
    WebApplicationContext webApplicationContext;

	@Autowired
	TaskService taskService;

	private final String GET_ALL_PROJECTS_RESPONSE = "{\"Daily Routine\":[{\"id\":1,\"description\":\"Breakfast\",\"done\":true,\"deadline\":\"2026-11-10\"},{\"id\":2,\"description\":\"Lunch\",\"done\":false,\"deadline\":\"1986-11-10\"},{\"id\":3,\"description\":\"Breakfast\",\"done\":false,\"deadline\":\"2025-07-23\"},{\"id\":4,\"description\":\"Vitamin Supplement\",\"done\":false,\"deadline\":\"2025-08-02\"},{\"id\":5,\"description\":\"Brunch\",\"done\":true,\"deadline\":null}],\"Fitness\":[{\"id\":6,\"description\":\"Warm Up\",\"done\":true,\"deadline\":\"2024-11-10\"},{\"id\":7,\"description\":\"Push Up\",\"done\":false,\"deadline\":\"2025-08-02\"},{\"id\":8,\"description\":\"Abs\",\"done\":false,\"deadline\":null}]}";
	private final String GET_BY_DEADLINE_RESPONSE = "{\"\":{\"Fitness\":[{\"id\":8,\"description\":\"Abs\",\"done\":false,\"deadline\":null}]},\"10-11-2024\":{\"Fitness\":[{\"id\":6,\"description\":\"Warm Up\",\"done\":true,\"deadline\":\"2024-11-10\"}]},\"02-08-2025\":{\"Daily Routine\":[{\"id\":4,\"description\":\"Vitamin Supplement\",\"done\":false,\"deadline\":\"2025-08-02\"}],\"Fitness\":[{\"id\":7,\"description\":\"Push Up\",\"done\":false,\"deadline\":\"2025-08-02\"}]},\"10-11-2026\":{\"Daily Routine\":[{\"id\":1,\"description\":\"Breakfast\",\"done\":true,\"deadline\":\"2026-11-10\"}]},\"15-11-2025\":{\"Daily Routine\":[{\"id\":5,\"description\":\"Brunch\",\"done\":true,\"deadline\":\"2025-11-15\"}]},\"23-07-2025\":{\"Daily Routine\":[{\"id\":3,\"description\":\"Breakfast\",\"done\":false,\"deadline\":\"2025-07-23\"}]},\"10-11-1986\":{\"Daily Routine\":[{\"id\":2,\"description\":\"Lunch\",\"done\":false,\"deadline\":\"1986-11-10\"}]}}";
	private final String API_PREFIX = "/projects";
	private final String deadline = "15-11-2025";

	@BeforeEach
    void setupBeforeAll() {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();
		setUpMockData();
	}

	@Test
	@DisplayName("Get All Projects with their task list.")
	void getAllProjectsWithTasks_ValidRequest_SuccessResponse() throws Exception {
		MvcResult result = mvc.perform(MockMvcRequestBuilders.get(API_PREFIX)
				.contentType(MediaType.APPLICATION_JSON)).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
		Assertions.assertEquals(GET_ALL_PROJECTS_RESPONSE, result.getResponse().getContentAsString());
    }

	@Test
	@DisplayName("Add a new project - Success")
	void addNewProject_ValidRequest_SuccessResponse() throws Exception {
		String projectName = "WallPainting";
		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(API_PREFIX)
				.contentType(MediaType.APPLICATION_JSON).content(projectName)).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
        Assertions.assertEquals(result.getResponse()
                .getContentAsString(), String.format("Project %s created successfully", projectName));
	}

	@Test
	@DisplayName("Add a new project - Failure")
	void addNewProject_InvalidRequest_ErrorResponse() throws Exception {
		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(API_PREFIX)
				.contentType(MediaType.APPLICATION_JSON)).andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());
	}

	@Test
	@DisplayName("Add a new task to an existing project - Success")
	void addNewTaskToProject_ValidRequest_SuccessResponse() throws Exception {
		String projectName = "Fitness";
		String taskName = "Bench Press";
		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(API_PREFIX + "/" + projectName + "/tasks")
				.contentType(MediaType.APPLICATION_JSON).content(taskName)).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
        Assertions.assertEquals(result.getResponse()
                .getContentAsString(), String.format("Task %s created for project '%s' successfully", taskName, projectName));
	}

	@Test
	@DisplayName("Set deadline date to a task - Success")
	void setDeadlineForTask_ValidRequest_SuccessResponse() throws Exception {
		String taskId = "5";

		MvcResult result = mvc.perform(MockMvcRequestBuilders.put(API_PREFIX + "/tasks/" + taskId + "/deadline")
				.contentType(MediaType.APPLICATION_JSON).content(deadline)).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
	}

	@Test
	@DisplayName("Set deadline date to a task - Failure Invalid Date")
	void setDeadlineForTask_InvalidValidRequest_ErrorResponse() throws Exception {
		String taskId = "5";
		MvcResult result = mvc.perform(MockMvcRequestBuilders.put(API_PREFIX + "/tasks/" + taskId + "/deadline")
				.contentType(MediaType.APPLICATION_JSON)).andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());
	}

	@Test
	@DisplayName("View projects and its tasks by deadline date")
	void viewTasksAndProejctsByDeadline_ValidRequest_SuccessResponse() throws Exception {
		String taskId = "5";
		MvcResult result = mvc.perform(MockMvcRequestBuilders.put(API_PREFIX + "/tasks/" + taskId + "/deadline")
				.contentType(MediaType.APPLICATION_JSON).content(deadline)).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
		result = mvc.perform(MockMvcRequestBuilders.get(API_PREFIX + "/view_by_deadline")
				.contentType(MediaType.APPLICATION_JSON)).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
		Assertions.assertEquals(GET_BY_DEADLINE_RESPONSE, result.getResponse().getContentAsString());
	}

	void setUpMockData() {
		List<Task> tasks = new ArrayList<>();
		tasks.add(new Task(1, "Breakfast", true, LocalDate.parse("10-11-2026", TaskService.DATE_FORMAT)));
		tasks.add(new Task(2, "Lunch", false, LocalDate.parse("10-11-1986", TaskService.DATE_FORMAT)));
		tasks.add(new Task(3, "Breakfast", false, LocalDate.now().minusDays(10)));
		tasks.add(new Task(4, "Vitamin Supplement", false, LocalDate.now()));
		tasks.add(new Task(5, "Brunch", true, null));
		taskService.getProjects().put("Daily Routine",tasks);
		tasks = new ArrayList<>();
		tasks.add(new Task(6, "Warm Up", true, LocalDate.parse("10-11-2024", TaskService.DATE_FORMAT)));
		tasks.add(new Task(7, "Push Up", false, LocalDate.now()));
		tasks.add(new Task(8, "Abs", false, null));
		taskService.getProjects().put("Fitness", tasks);
		taskService.setId(8);
	}

}
