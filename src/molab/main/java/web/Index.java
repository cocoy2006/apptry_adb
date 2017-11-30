package molab.main.java.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import molab.main.java.util.Apptry;
import molab.main.java.util.Commands;
import molab.main.java.util.Status;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.google.gson.Gson;

@Controller
public class Index implements ServletContextAware {
	
	@ResponseBody
	@RequestMapping(value = "/install")
	public String install(HttpServletRequest request) throws IOException {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		CommonsMultipartFile file = (CommonsMultipartFile) multipartRequest.getFile("file");
		String apkPath = Apptry.getApkDirectory().concat(file.getOriginalFilename());
		FileCopyUtils.copy(file.getBytes(), new File(apkPath));
		String command = "REINSTALL ".concat(apkPath);
		String[] serialNumbers = request.getParameterValues("serialNumber");
		for(String serialNumber : serialNumbers) {
//			new Commands(serialNumber).executeCommand(command);
			Commands.executeCommand(serialNumber, command);
		}
		return new Gson().toJson(Status.SUCCESS);
	}
	
	@ResponseBody
	@RequestMapping(value = "/singleInstall/")
	public String singleInstall(HttpServletRequest request) throws IOException {
		// parse apk file
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		CommonsMultipartFile file = (CommonsMultipartFile) multipartRequest.getFile("file");
		String apkPath = Apptry.getApkDirectory().concat(file.getOriginalFilename());
		FileCopyUtils.copy(file.getBytes(), new File(apkPath));
		String command = "REINSTALL ".concat(apkPath);
		// parse serialNumber
		String[] serialNumbers = request.getParameterValues("serialNumber");
		// execute installation
//		String result = new Commands(serialNumbers[0]).executeCommand(command);
		String result = Commands.executeCommand(serialNumbers[0], command);
		return new Gson().toJson(result);
	}
	
	@ResponseBody
	@RequestMapping(value = "/singleUninstall/")
	public String singleUninstall(HttpServletRequest request) throws IOException {
		String serialNumbers = request.getParameter("serialNumber");
		String packageName = request.getParameter("packageName");
		String command = "UNINSTALL ".concat(packageName);
		String result = Commands.executeCommand(serialNumbers, command);
		return new Gson().toJson(result);
	}
	
	@ResponseBody
	@RequestMapping(value = "/execute/")
	public String execute(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
//		map.put("result", new Commands(serialNumber).executeCommand(command));
		String serialNumber = request.getParameter("serialNumber");
		String command = request.getParameter("command");
		map.put("result", Commands.executeCommand(serialNumber, command));
		return new Gson().toJson(map);
	}
	
	public void setServletContext(ServletContext sc) {
		Apptry.setServletContext(sc);
	}
	
}
