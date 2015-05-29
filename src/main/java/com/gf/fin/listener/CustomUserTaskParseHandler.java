package com.gf.fin.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.parse.BpmnParseHandler;

/**
 * 自定义的任务解析器
 * 
 * @author admin
 *
 */
public class CustomUserTaskParseHandler implements BpmnParseHandler {

	@Override
	public Collection<Class<? extends BaseElement>> getHandledTypes() {
		Set<Class<? extends BaseElement>> types = new HashSet<Class<? extends BaseElement>>();
		types.add(UserTask.class); //对UserTask节点进行自定义解析
		return types;
	}

	@Override
	public void parse(BpmnParse bpmnParse, BaseElement baseElement) {
		// 调用默认的任务解析器  
        new UserTaskParseHandler().parse(bpmnParse, baseElement);  
        TaskDefinition taskDefinition = (TaskDefinition) bpmnParse  
                .getCurrentActivity().getProperty(  
                        UserTaskParseHandler.PROPERTY_TASK_DEFINITION);
        
        // 自定义任务解析（添加监听器）  
        ActivitiListener customTaskCreationListener = new ActivitiListener();
        ActivitiListener customTaskCompletionListener = new ActivitiListener();
        
        // 绑定监听事件  
        customTaskCreationListener.setEvent(TaskListener.EVENTNAME_CREATE);
        customTaskCompletionListener.setEvent(TaskListener.EVENTNAME_COMPLETE);
        
        // 设置监听器的实现类  
        customTaskCreationListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);  
        customTaskCreationListener.setImplementation("com.gf.fin.listener.TaskCreationListener");  
        customTaskCompletionListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        customTaskCompletionListener.setImplementation("com.gf.fin.listener.TaskCompletionListener");
        
        // 将监听器绑定到任务  
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, bpmnParse  
                .getListenerFactory().createClassDelegateTaskListener(customTaskCreationListener));
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, bpmnParse  
                .getListenerFactory().createClassDelegateTaskListener(customTaskCompletionListener));
	}

}
