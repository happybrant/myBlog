package com.spring.myBlog.controller;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import com.spring.myBlog.service.UserService;

@Controller
@SessionAttributes("users")
public class LoginController {

	@Autowired
	private UserService userService;
	
	
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public void login(String username,String password,Map<String,Object>map,HttpServletResponse response) throws IOException{
		
	if(userService.userCheck(username, password)!=null){
		System.out.println("���ж�");	
		map.put("users", userService.userCheck(username,password));	
		response.getWriter().println("yes");//����Ajax���
		System.out.println("���û�����");
		}
		else{
			System.out.println("�޸��û�");
		response.getWriter().println("no");//����Ajax���
	}
  }
	@RequestMapping("/logout")
	public String logout(Map<String,Object>map,HttpSession session){
		map.remove("users");
		session.invalidate();
		return "redirect:/edit";
	}
}
