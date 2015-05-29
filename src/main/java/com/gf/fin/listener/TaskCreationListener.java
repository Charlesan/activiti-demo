package com.gf.fin.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class TaskCreationListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		System.out.println("Task创建触发：" + delegateTask);
	}

}
