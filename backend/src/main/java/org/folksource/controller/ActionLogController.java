package org.folksource.controller;


import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.ServletActionContext;
import org.folksource.model.Actionlog;
import org.folksource.model.ActionlogDto;
import org.folksource.util.ActionLogService;
import org.grouplens.common.dto.DtoContainer;

import javax.servlet.http.HttpServletResponse;

public class ActionlogController implements ModelDriven<DtoContainer<ActionlogDto>>{
	
	private DtoContainer<ActionlogDto> content = new DtoContainer<ActionlogDto>(ActionlogDto.class, false);
	
	@Override
	public DtoContainer<ActionlogDto> getModel() {
		return content;
	}
	
	public String index() {
		HttpServletResponse res = ServletActionContext.getResponse();
		res.setHeader("Access-Control-Allow-Origin", "*");
		res.addHeader("Access-Control-Allow-Headers", "Authorization, AuthToken");
		res.addHeader("Access-Control-Expose-Headers", "Authorization, AuthToken");
		content = new DtoContainer<ActionlogDto>(ActionlogDto.class, true);
		content.set(ActionlogDto.fromActionList(ActionLogService.getActionLogs()));
		return "index";
	}

	public String options() {
		HttpServletResponse res = ServletActionContext.getResponse();
		res.setHeader("Access-Control-Allow-Origin", "*");
		res.addHeader("Access-Control-Allow-Headers", "Authorization, AuthToken");
		res.addHeader("Access-Control-Expose-Headers", "Authorization, AuthToken");
		return "options_success";
	}

    public String create() {

        HttpServletResponse res = ServletActionContext.getResponse();

        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Headers", "Cache-Control");

        ActionlogDto actionLogDto = content.getSingle();
        Actionlog actionLog = actionLogDto.toAction();

        ActionLogService.save(actionLog);

        content.set(new ActionlogDto(actionLog));

        return "create";
    }
}
