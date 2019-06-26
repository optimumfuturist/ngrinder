package org.ngrinder.common.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ngrinder.infra.config.Config;
import org.ngrinder.operation.service.AnnouncementService;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import static org.ngrinder.common.constant.WebConstants.PARAM_PROCESS_THREAD_POLICY_SCRIPT;
import static org.ngrinder.common.util.NoOp.noOp;

@ControllerAdvice
public class GlobalControllerAdvice {

	@Value("${ngrinder.version}")
	private String version;

	@Autowired
	private UserContext userContext;

	@Autowired
	private AnnouncementService announcementService;

	@Autowired
	private RegionService regionService;

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private Config config;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@ModelAttribute
    public void globalAttributes(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("clustered", config.isClustered());
		model.addAttribute("helpUrl", config.getHelpUrl());
		model.addAttribute("signUpEnabled", config.isSignUpEnabled());
		model.addAttribute("hasNewAnnouncement", announcementService.isNew());
		model.addAttribute(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());

		try {
			model.addAttribute("visibleRegions", objectMapper.writeValueAsString(regionService.getAllVisibleRegionNames()));
		} catch (JsonProcessingException e) {
			noOp();
		}

		try {
			model.addAttribute("currentUser", userContext.getCurrentUser());
		} catch (Exception e) {
			noOp();
		}
	}
}
