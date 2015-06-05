package com.gf.fin.test;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.gf.fin.service.ProcessControlService;

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
				.addClasspathResource("sample.bpmn20.xml").deploy();
		System.out.println("deployId:" + deployment.getId());
		// 查询流程定义
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();

		Long businessKey1 = new Double(1000000 * Math.random()).longValue();
		// 启动流程1
		ProcessInstance processInstance1 = runtimeService
				.startProcessInstanceById(processDefinition.getId(),
						businessKey1.toString());
		
		Long businessKey2 = new Double(1000000 * Math.random()).longValue();
		// 启动流程2
		ProcessInstance processInstance2 = runtimeService
				.startProcessInstanceById(processDefinition.getId(),
						businessKey2.toString());

		// 查询任务实例
		List<Task> taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance1.getProcessInstanceId())
				.list();
		// 认领p1填写报表任务
		taskService.claim(taskList.get(0).getId(), "fozzie");
		// 查询任务实例
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance2.getProcessInstanceId())
				.list();
		// 认领p2填写报表任务
		taskService.claim(taskList.get(0).getId(), "fozzie");

		// Verify Fozzie can now retrieve the task
		taskList = taskService.createTaskQuery().taskAssignee("fozzie").list();
		// Complete the task
		for (Task task : taskList) {
			taskService.complete(task.getId());
		}

		// 认领p1的审批1任务
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance1.getProcessInstanceId())
				.list();
		taskService.claim(taskList.get(0).getId(), "kermit");
		String taskId = taskList.get(0).getId();
		
		// 认领p2的审批1任务
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance2.getProcessInstanceId())
				.list();
		taskService.claim(taskList.get(0).getId(), "kermit");

		ProcessControlService processControlService = new ProcessControlService();
		processControlService.setTaskService(taskService);
		processControlService.setRuntimeService(runtimeService);
		processControlService.setRepositoryService(repositoryService);
		processControlService.setHistoryService(historyService);
		List<ActivityImpl> backActivities = null;
		try {
			backActivities = processControlService.findBackAvtivity(taskId);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		// 查询p1流程当前任务
//		taskList = taskService.createTaskQuery()
//				.processInstanceId(processInstance1.getProcessInstanceId())
//				.list();
//		System.out.println("Current task(p1): " + taskList.get(0).getName());
//		
//		// 查询p2流程当前任务
//		taskList = taskService.createTaskQuery()
//				.processInstanceId(processInstance2.getProcessInstanceId())
//				.list();
//		System.out.println("Current task(p2): " + taskList.get(0).getName());
		
		
		// p1驳回
		try {
			processControlService.backProcess(taskId, backActivities.get(0)
					.getId(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//p1提交任务
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance1.getProcessInstanceId())
				.list();
		taskService.complete(taskList.get(0).getId());
		//p1提交任务
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance1.getProcessInstanceId())
				.list();
		taskService.complete(taskList.get(0).getId());
		//p1提交任务
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance1.getProcessInstanceId())
				.list();
		taskService.complete(taskList.get(0).getId());
		
		//p2提交任务
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance2.getProcessInstanceId())
				.list();
		taskService.complete(taskList.get(0).getId());
		//p2提交任务
		taskList = taskService.createTaskQuery()
				.processInstanceId(processInstance2.getProcessInstanceId())
				.list();
		taskService.complete(taskList.get(0).getId());

//		// 查询p1流程当前任务
//		taskList = taskService.createTaskQuery()
//				.processInstanceId(processInstance1.getProcessInstanceId())
//				.list();
//		System.out.println("Current task(p1): " + taskList.get(0).getName());
		
//		// 查询p2流程当前任务
//		taskList = taskService.createTaskQuery()
//				.processInstanceId(processInstance2.getProcessInstanceId())
//				.list();
//		System.out.println("Current task(p2): " + taskList.get(0).getName());
		 // verify that the process is actually finished
		
		//查询p1结束时间
		 HistoricProcessInstance historicProcessInstance1 =
		 historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
		 System.out.println("Process instance end time: " +
		 historicProcessInstance1.getEndTime());
		
		//查询p2结束时间
		 HistoricProcessInstance historicProcessInstance2 =
		 historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
		 System.out.println("Process instance end time: " +
		 historicProcessInstance2.getEndTime());
	}
}
