package org.folksource.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.grouplens.common.dto.Dto;

public class TaskDto extends Dto{
	
	public Integer id;
	public String name;
	public String instructions;
	public Boolean required;

	public SubmissionDto[] submissions;
//	public String[] locations;
	public Question[] questions;
	public String type;
	public Integer vQID;
	
	
	//// Methods that create TaskDtos or collections of TaskDtos
	public TaskDto(){
		super();
	}
	public TaskDto(Task t){
		super();
		id = t.id;
		name = t.name;
		instructions = t.instructions;
		required = t.required;
		if (t.submissions == null){submissions = null;}
		else{submissions = SubmissionDto.fromSubmissionArray(t.submissions.toArray(new Submission[t.submissions.size()]));}
//        locations = LocationDto.fromLocationLayerArray(t.getLocationById().toArray(new LocationLayer[t.getLocationById().size()]));
//		locations = LocationDto.fromLocationArray(new Location[0]);
		questions = t.questions.toArray(new Question[t.questions.size()]);
		this.type = t.type;
		this.vQID = t.getDecision_q_id();
	}
	public static List<TaskDto> fromList(List<Task> tasks){
		List<TaskDto> tdtos = new ArrayList<TaskDto>();
		for (Task t : tasks){
			tdtos.add(new TaskDto(t));
		}
		return tdtos;
	}
	//TODO: the naming of this method and the previous are inconsistent
	public static TaskDto[] fromTaskArray(Task[] tasks){
		TaskDto[] tdtos = new TaskDto[tasks.length];
		for (int i = 0; i < tasks.length; i++) {
			tdtos[i] = new TaskDto(tasks[i]);
		}
		return tdtos;
	}
	
	
	//// Methods that create Tasks or collections of Tasks
	public Task toTask(){
		
		Submission[] subs = new Submission[0];
		if (submissions != null){
			subs = SubmissionDto.toSubmissionArray(submissions);
		}
		
		String[] locs = new String[0];
//		if (locations != null) {
//			locs = LocationDto.toLocationArray(locations);
//		}

        System.out.println("QUESTIONS: " + questions.length);
		return new Task(id, name, instructions, required, new LinkedHashSet<Submission>(Arrays.asList(subs)), new LinkedHashSet<Question>(Arrays.asList(questions)), new LinkedHashSet<LocationLayer>(/*Arrays.asList(locs)*/), this.type, this.vQID);
	}
	public static Task[] toTaskArray(TaskDto[] tdtos){
		Task[] tasks = new Task[tdtos.length];
		for (int i = 0; i < tasks.length; i++) {
			tasks[i] = tdtos[i].toTask();
		}
		return tasks;
	}
}
