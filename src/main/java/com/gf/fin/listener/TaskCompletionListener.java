package com.gf.fin.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class TaskCompletionListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		System.out.println("Task完成触发：" + delegateTask);
	}

}
