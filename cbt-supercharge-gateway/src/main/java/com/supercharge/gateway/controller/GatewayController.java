package com.supercharge.gateway.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

	@PostMapping("/save")
	public String saveCall() throws Exception {
		System.out.println("success");
		return "success";
	}

}
