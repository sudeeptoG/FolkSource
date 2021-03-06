package org.folksource.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.folksource.model.Answer;
import org.folksource.model.AnswerDto;
import org.folksource.model.MediaAudioAnswer;
import org.folksource.model.MediaPhotoAnswer;
import org.folksource.model.MediaVideoAnswer;
import org.folksource.util.AnswerService;
import org.folksource.util.SubmissionService;
import org.grouplens.common.dto.DtoContainer;

import com.opensymphony.xwork2.ModelDriven;

public class AnswerController implements ModelDriven<DtoContainer<AnswerDto>>{

	private DtoContainer<AnswerDto> content = new DtoContainer<AnswerDto>(AnswerDto.class, false);
	
	//MediaAnswer fields
	String mimeType;
	File media;
	/** The base directory to save uploaded photos. */
	public static final String BASE_DIR = "tmp/";
	//public static final String BASE_DIR = "/export/scratch/FolkSource/files/";

	
	@Override
	public DtoContainer<AnswerDto> getModel() {
		return content;
	}
	
	/**
	 * The answer api endpoint should only be used to send media answers to the server.
	 * All others should be part of submission objects that get sent to the submission endpoint.
	 * @return
	 */
	public String create() {
		HttpServletRequest req = ServletActionContext.getRequest();
		Map<String, String[]> params = req.getParameterMap();
		
		String answer_type = params.get("answer_type")[0];
		Integer q_id = Integer.parseInt(params.get("q_id")[0]);
		Integer sub_id = Integer.parseInt(params.get("sub_id")[0]);
//		System.out.println(params.get("mimeType")[0]);
//		System.out.println(params.get("fileName")[0]);
		Integer id = 0;
		String path = saveMedia(sub_id, q_id);
		
		Answer a;
		if(answer_type.equals("media_audio"))
			a = new MediaAudioAnswer(id, answer_type, q_id, sub_id, path, getMediaContentType());
		if(answer_type.equals("media_video"))
			a = new MediaVideoAnswer(id, answer_type, q_id, sub_id, path, getMediaContentType());
		else
			a = new MediaPhotoAnswer(id, answer_type, q_id, sub_id, path, getMediaContentType());
		
		AnswerService.save(a);
		
		content.set(new AnswerDto(a));
		return "create";
	}
	
	private String saveMedia(Integer sub_id, Integer q_id){
		
		//build the path
		HttpServletRequest req = ServletActionContext.getRequest();
		Map<String, String[]> params = req.getParameterMap();
		String name = sub_id + "-"+ q_id + "-" + new Date().getTime();
		//TODO: Escape/validate file name
		String path = BASE_DIR + name + getExtension(getMediaContentType());
		File storageLocation = new File(path);
		
		//save the file
		try {
			FileUtils.copyFile(media, storageLocation);
		} catch (IOException e) {
			System.out.println("Copying File Failed!");
			e.printStackTrace();
		}
		
		return path;
	}
		
	private String getExtension(String mimeType){
		// We should decide on our acceptable mimeTypes and
		// throw an exception if the mimeType is not one of our acceptable formats.
		
		//Images
		//image/jpeg
		if (mimeType.equals("image/jpeg")){
			return ".jpg";
		}
		
		//Video
		//video/quicktime
		//video/mp4
		if(mimeType.equals("video/mp4")) {
			return ".mp4";
		}
		if (mimeType.equals("video/quicktime")){
			return ".mov";
		}
		
		//Audio
		//audio/wav
		//audio/3gpp
		if(mimeType.equals("audio/3gpp")) {
			return ".3gpp";
		}
		if (mimeType.equals("audio/wav")){
			return ".wav";
		}
		return "";
	}
	
	
	//Getters and Setters
	public String getMediaContentType(){
		return this.mimeType;
	}
	public void setMediaContentType(String photoContentType) {
		this.mimeType = photoContentType;
	}
	public void setMedia(File photo) {
		this.media = photo;
	}
	public File getMedia() {
		return this.media;
	}

}
