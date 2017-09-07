package com.spring.myBlog.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.spring.myBlog.entity.FirstCategory;
import com.spring.myBlog.entity.Log;
import com.spring.myBlog.entity.Question;
import com.spring.myBlog.entity.Message;
import com.spring.myBlog.service.BlogService;
import com.spring.myBlog.service.MessageService;
import com.spring.myBlog.service.QuestionService;
@SessionAttributes("visitor")
@Controller
public class QuestionController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private BlogService blogService;
    @Autowired
    private Question question;
	@Autowired
	private Log log;
    /**
     * 新增问题
     * @param title
     * @param question
     * @param map
     * @return
     */
	@RequestMapping(value="/addQuestion",method=RequestMethod.POST)
	public String saveQuestion(String title,String question,Map<String,Object>map){
		int visitor= (Integer) map.get("visitor");		
		this.question.setTitle(title);
		this.question.setQuestion(question);
		this.question.setVisitor(visitor);
		questionService.saveQuestion(this.question);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		log.setTime(df.format(new Date()));
		log.setBlogId(0);
		log.setVisitor(visitor);
		log.setLog("新的问题发表");
		return "redirect:/question";		
	}
	@RequestMapping("/question")
	public String  Question(Map<String,Object>map){
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//把一级分类传到首页
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		map.put("questions", questionService.getQuestion());
		
		return "question";
	}
	@RequestMapping(value="/addMessage",method=RequestMethod.OPTIONS)
	public String saveMessage(Message message,Map<String,Object>map){
		int visitor= (Integer) map.get("visitor");
		message.setVisitor(visitor);
		messageService.saveMessage(message);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		log.setTime(df.format(new Date()));
		log.setBlogId(0);
		log.setVisitor(visitor);
		log.setLog("新的留言发表");
		return "redirect:/message";
	}
	@RequestMapping("/message")
	public String Message(Map<String,Object>map){
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//把一级分类传到首页
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		map.put("messages",messageService.getMessage());
		return "message";
	}
	
}
