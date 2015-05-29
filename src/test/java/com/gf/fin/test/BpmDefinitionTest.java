package com.gf.fin.test;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class BpmDefinitionTest extends BaseTestCase {
	@Resource
	private RepositoryService repositoryService;
	
	@Resource
	private RuntimeService runtimeService;
	
	@Resource
	private TaskService taskService;
	
	@Resource
	private HistoryService historyService;

	@Test
	public void testDeploy() throws IOException {
		Deployment deployment = repositoryService.createDeployment()
				  .addClasspathResource("sample.bpmn20.xml")
				  .deploy();
		System.out.println("deployId:" + deployment.getId());
		// 查询流程定义
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery().deploymentId(deployment.getId())
				.singleResult();

		Long businessKey = new Double(1000000 * Math.random()).longValue();
		// 启动流程
		runtimeService.startProcessInstanceById(processDefinition.getId(),
				businessKey.toString());
		
		// 查询任务实例
		List<Task> taskList = taskService.createTaskQuery()
				.processDefinitionId(processDefinition.getId()).list();
		for (Task task : taskList) {
			System.out.println("task name is " + task.getName()
					+ " ,task key is " + task.getTaskDefinitionKey());
			//认领任务
			taskService.claim(task.getId(), "fozzie");
		}
		
		// Verify Fozzie can now retrieve the task
		taskList = taskService.createTaskQuery().taskAssignee("fozzie").list();
	    for (Task task : taskList) {
	      System.out.println("Task for fozzie: " + task.getName());

	      // Complete the task
	      taskService.complete(task.getId());
	    }

	    System.out.println("Number of tasks for fozzie: "
	            + taskService.createTaskQuery().taskAssignee("fozzie").count());
	    
	    // Retrieve and claim the second task
	    taskList = taskService.createTaskQuery()
				.processDefinitionId(processDefinition.getId()).list();
	    for (Task task : taskList) {
	      System.out.println("Following task is available for management group: " + task.getName());
	      taskService.claim(task.getId(), "kermit");
	    }
	    
	    // Completing the second task ends the process
	    for (Task task : taskList) {
	      taskService.complete(task.getId());
	    }
	    
	    // verify that the process is actually finished
	    HistoricProcessInstance historicProcessInstance =
	      historyService.createHistoricProcessInstanceQuery().processDefinitionId(processDefinition.getId()).singleResult();
	    System.out.println("Process instance end time: " + historicProcessInstance.getEndTime());
	}
}
