package com.sa.quartz.controller;

import com.sa.domain.BaseOutput;
import com.sa.quartz.domain.ScheduleJob;
import com.sa.quartz.service.ScheduleJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;



@Controller
@RequestMapping("/scheduleJob")
@ConditionalOnProperty(name = "quartz.enabled")
public class ScheduleJobController {

	protected static final Logger log = LoggerFactory.getLogger(ScheduleJobController.class);
    @Autowired
    ScheduleJobService scheduleJobService;


	@RequestMapping("/refreshAll.aspx")
	public @ResponseBody
    BaseOutput refreshAll(){
		scheduleJobService.refresh(scheduleJobService.list(null));
		return BaseOutput.success("调度器刷新完成");
	}


    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "scheduleJob/index";
    }





    @RequestMapping(value="/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<ScheduleJob> list(ScheduleJob scheduleJob) {
        return scheduleJobService.list(scheduleJob);
    }





    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(ScheduleJob scheduleJob) throws Exception {
        return scheduleJobService.listEasyuiPageByExample(scheduleJob, true).toString();
    }





    @RequestMapping(value="/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput insert(ScheduleJob scheduleJob) {
        scheduleJobService.insertSelective(scheduleJob, true);
        return BaseOutput.success("新增成功");
    }





    @RequestMapping(value="/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput update(ScheduleJob scheduleJob) {
        scheduleJobService.updateSelective(scheduleJob, true);
        return BaseOutput.success("修改成功");
    }





    @RequestMapping(value="/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput delete(Long id) {
        scheduleJobService.delete(id, true);
        return BaseOutput.success("删除成功");
    }





	@RequestMapping(value="/pause.action", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody
	BaseOutput pause(ScheduleJob scheduleJob) {
		scheduleJobService.pauseJob(scheduleJob);
		return BaseOutput.success("暂停成功");
	}





	@RequestMapping(value="/resume.action", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody
	BaseOutput resume(ScheduleJob scheduleJob) {
		scheduleJobService.resumeJob(scheduleJob);
		return BaseOutput.success("恢复成功");
	}
}