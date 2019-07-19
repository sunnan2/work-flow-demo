/**
 * 
 */
package com.flow.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sunnan
 *
 */
@RestController
@RequestMapping("/api/leave")
public class LeaveController {

	@Autowired
	RuntimeService runtimeService;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	RepositoryService repositoryService;
	
	/**
	 * 
	 * @param leaveInfo
	 * @return 申请请假信息表单
	 */
	@PostMapping("/apply/leave")
	public Map<String,Object> applyLeave(Map<String,Object> leaveInfo){
		leaveInfo.put("days", 5);
		leaveInfo.put("userId", 1);
		leaveInfo.put("formId", "1");
		//动态设置办理人可以从数据库中读取
		Map<String,Object> dynamicArg = new HashMap<String, Object>();
		dynamicArg.put("managerial", "2");
		dynamicArg.put("director", "3");
		dynamicArg.put("hr", "4");
		//启动流程实例，字符串"leaveFlow"是BPMN模型文件里process元素的id
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveFlow",(String)leaveInfo.get("formId"),dynamicArg);
		System.out.println("流程实例id=================="+processInstance.getId());
		
		//流程实例启动后，流程会跳转到请假申请节点
		Task vacationApply = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		//设置请假申请任务的执行人也可以配置动态表达式通过变量注入
		taskService.setAssignee(vacationApply.getId(), String.valueOf(leaveInfo.get("userId")));
		//设置流程参数：请假天数和表单ID
		//流程引擎会根据请假天数days>3判断流程走向
		Map<String, Object> args = new HashMap<>();
		args.put("days", leaveInfo.get("days"));
		//设置审批组
		//完成请假申请任务
		taskService.complete(vacationApply.getId(), args);
		Map<String,Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result","success");
		return resultMap;
	}
	
	/**
	 * 查看请假单列表
	 * @return
	 */
	@GetMapping("/qry/leave/list")
	public List<Map<String,Object>> qryLeaveList(){
		List<String> currentRoleList = new ArrayList<String>();
		currentRoleList.add("3");
		//查询用户组的待审批请假流程列表
		List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("leaveFlow").taskCandidateGroupIn(currentRoleList).listPage(0, 10);
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		for (Task task : tasks) {
			Map<String,Object> resMap = new HashMap<String, Object>();
			resMap.put("processInstanceId", task.getProcessInstanceId());
			resMap.put("taskId",task.getId());
			resMap.put("arg",task.getProcessVariables());
			resultList.add(resMap);
		}
		return resultList;
	}
	
	/**
	 * 请假详情信息
	 * @param taskId
	 * @return Map<String,Object>
	 */
	@GetMapping("/qry/leave/detail")
	public Map<String,Object> qryLeaveDetail(Long taskId){
		Task task = taskService.createTaskQuery().taskId(taskId.toString()).singleResult();
		Map<String,Object> resultMap = new HashMap<String, Object>();
		resultMap.put("taskId", task.getId());
		resultMap.put("arg", task.getProcessVariables());
		return resultMap;
	}
	
	@PostMapping("/approval/leave")
	public Map<String,Object> approvalLeave(Long taskId){
		Task task = taskService.createTaskQuery().taskId(taskId.toString()).singleResult();
		taskService.complete(task.getId());
		Map<String,Object> resultMap = new HashMap<String, Object>();
		resultMap.put("success", "true");
		return resultMap;
	}
}
