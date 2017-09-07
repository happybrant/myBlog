package com.spring.myBlog.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import com.spring.myBlog.entity.Blog;
import com.spring.myBlog.entity.Comment;
import com.spring.myBlog.entity.FirstCategory;
import com.spring.myBlog.entity.Log;
import com.spring.myBlog.entity.SecondCategory;
import com.spring.myBlog.entity.Visitor;
import com.spring.myBlog.service.BlogService;
import com.spring.myBlog.service.CommentService;
import com.spring.myBlog.service.LogService;
import com.spring.myBlog.service.UserService;
import com.spring.myBlog.util.HotAlgorithm;
import com.spring.myBlog.util.SortByHotter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Controller
@SessionAttributes("visitor")
public class BlogController {

	@Autowired
	private BlogService blogService;
	@Autowired
	private UserService userService;
	@Autowired
	private SecondCategory secondCategory;
	@Autowired
	private CommentService commentService;
	@Autowired
	private Visitor visitor;
	@Autowired
	private Comment comment;
	@Autowired
	private HotAlgorithm hotAlgorothm;
	@Autowired
	private SortByHotter sortByHotter;
	@Autowired
	private LogService logService;
	@Autowired
	private Log log;
	/**
	 * 新增博文
	 * @param userId
	 * @param blog
	 * @param secondCategory
	 */
	@RequestMapping(value="/addBlog/{userId}",method=RequestMethod.POST)
	public String addBlog(@PathVariable int userId, Blog blog,String secondCategory,int firstCategory  ){
		
		blog.setUser(userService.getUser(userId));						//设置用户
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式		
		blog.setPublishTime(df.format(new Date()));//设置发布时间;new Date()为获取当前系统时间
		blog.setPageViews(0);					   //设置博文浏览量
        
		if(blogService.getSecondCategory(secondCategory)!=null){//说明该二级分类存在
		blog.setCategory(blogService.getSecondCategory(secondCategory));//设置二级分类
        }
        else{//新增二级分类
        this.secondCategory.setCategory(secondCategory);//设置二级分类内容
        this.secondCategory.setFirstCategory(blogService.getFirstCategory(firstCategory));//设置对应一级分类
        blogService.saveSecondCategory(this.secondCategory);//将该二级分类保存到数据库中
        blog.setCategory(this.secondCategory);//设置该新增的二级分类
        }
		
		blogService.saveBlog(blog);//保存博文到数据库中
		return "redirect:../blogContent/"+blog.getId();
	}
	@RequestMapping("/edit")
	public String  redirectManager(Map<String,Object>map){
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "edit";
	}
	@RequestMapping("/showBlog/{blogId}")
	public String redirectShowBlog(@PathVariable int blogId,Map<String,Object>map){
		Blog blog=blogService.getBlog(blogId);
		map.put("blog", blog);
		return "showBlog";
	}
	/**
	 * 跳到首页
	 * @param map
	 * @return
	 */
	@RequestMapping("/blog")
	public String redirectBlog(Map<String,Object>map){
	    if(!map.containsKey("visitor")){//给该游客分配一个固定的游客标识直至session结束
		int visitorNum=(int)commentService.countVisitor();
	    commentService.saveVisitor(visitor);
	    System.out.println("新增一个");
		map.put("visitor",visitorNum+1);
	    }
	    map.put("comments", commentService.getComments());//把博文评论传到首页
	    map.put("backComments", commentService.getBackComments());//把博文评论回复传到首页
		map.put("blogs", blogService.findPage(1));//把博文传到首页
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//把一级分类传到首页
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "blog";
	}
	/**
	 * 跳到博文分类
	 * @param id
	 * @param map
	 * @return
	 */
	@RequestMapping("/blog/{id}")
	public String sort(@PathVariable int id,Map<String,Object>map){
	 
        map.put("comments", commentService.getComments());//把博文评论传到首页
       
        map.put("backComments", commentService.getBackComments());//把博文评论回复传到首页
		map.put("blogs", blogService.getBlogBySecondCate(id));
		map.put("blogsAll", blogService.getBlog());//所有博文
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "blogCategory";
	}
	/**
	 * 点赞
	 * @param blogId
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="/addPraise",method=RequestMethod.POST)
	public void addPraise(String blogId,HttpServletResponse response) throws IOException{
	  int id=Integer.parseInt(blogId.substring(6));
	   blogService.addPraise(id);
	   response.getWriter().println("addSuccess");	 
	}
	/**
	 * 取消点赞
	 * @param blogId
	 * @throws IOException 
	 */
	@RequestMapping(value="/deletePraise",method=RequestMethod.POST)
	public void deletePraise(String blogId,HttpServletResponse response) throws IOException{
		int id=Integer.parseInt(blogId.substring(6));
		blogService.deletePraise(id);	
		response.getWriter().println("deleteSuccess");	 
	}
	/**
	 * 管理员博文列表页面
	 * @param map
	 * @return
	 */
	@RequestMapping("/blogList")
	public String blogList(Map<String,Object>map){
		List<Blog>blogs=new ArrayList<Blog>();
		for(int i=0;i<5;i++){//根据当前页数来选择展示的博文
		    blogs.add( blogService.getBlog().get(i));
			if(blogService.getBlog().size()<=i+1)
				break;
		}
		map.put("blogs", blogs);
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());	
	    }
		map.put("logs", logService.getLog());
		map.put("blogLength", blogService.getBlog().size());//存放博文长度
		map.put("currentPage", 1);//存放当前页面
	    return "blogList";
	}
	/**
	 * 分页
	 * @param map
	 * @param page
	 * @return
	 */
	@RequestMapping("/blogList/page/{page}")
	public String blogListbyPage(Map<String,Object>map,@PathVariable int page){
		List<Blog>blogs=new ArrayList<Blog>();
		for(int i=page*5-5;i<page*5;i++){//根据当前页面来选择展示的博文
		    blogs.add( blogService.getBlog().get(i));
			if(blogService.getBlog().size()<=i+1)
				break;
		}
		map.put("blogs", blogs);
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());	
	  }
		map.put("blogLength", blogService.getBlog().size());//存放博文长度
		map.put("currentPage", page);//存放当前页面
	    return "blogListPage";
	}
	/**
	 * 根据分类展示博文列表
	 * @param id
	 * @param map
	 * @return
	 */
	@RequestMapping("/blogList/{id}")
	public String blogListById(@PathVariable int id,Map<String,Object>map){
		map.put("blogs", blogService.getBlogBySecondCate(id));
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "managerBlogCategory";
	}
	/**
	 * 管理员跳到博文内容页面
	 * @param id
	 * @param map
	 * @return
	 */
	@RequestMapping("/blogContent/{id}")
	public String blogContent(@PathVariable int id,Map<String,Object>map){
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		map.put("blog",blogService.getBlog(id));
		map.put("comments", commentService.getComments());//把博文评论传到博文内容页面
	    map.put("backComments", commentService.getBackComments());//把博文评论回复传到博文内容页面
		return "blogContent";
	}
	/**
	 * 新增评论
	 * @throws IOException
	 */
	@RequestMapping(value="/addComment",method=RequestMethod.POST)
	public void addComment(int visitor,String comment,int blogId,int commentBackId,HttpServletResponse response ) throws IOException{
		
		this.comment.setVisitor(commentService.getVisitor(visitor));//设置游客
		this.comment.setContent(comment);//设置评论内容
		this.comment.setBlog(blogService.getBlog(blogId));//设置博文id
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式		
		this.comment.setCommentTime(df.format(new Date()));//设置发布时间;new Date()为获取当前系统时间
		this.comment.setCommentBackId(commentBackId);
		commentService.saveComment(this.comment);//保存该评论
		log.setTime(df.format(new Date()));
		log.setVisitor(visitor);
		log.setBlogId(blogId);
		log.setLog("博文《"+blogService.getBlog(blogId).getBlogName()+"》有新的评论");
		logService.saveLog(log);//产生日志到后台
		response.getWriter().println("add success");
	}
	/**
	 * 按博文发布时间排序
	 * @param map
	 * @return
	 */
	@RequestMapping(value="/newest")
	public String newest(Map<String,Object>map){
		List<Blog>blogList=new ArrayList<Blog>();
		int length=blogService.getBlog().size();
		for(int i=0;i<length;i++){
			blogList.add(i, blogService.getBlog().get(length-i-1));
		}
	    map.put("comments", commentService.getComments());//把博文评论传到首页
	    map.put("backComments", commentService.getBackComments());//把博文评论回复传到首页
		map.put("blogs",blogList);//把博文传到首页
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//把一级分类传到首页
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "blog";
	}

	/**
	 * 跳到按博文热度排序的页面
	 * @param map
	 * @return
	 */
	@RequestMapping("/hottest")
	public String hottest(Map<String,Object>map){
		int length=blogService.getBlog().size();
		int praise=0;
		int comment=0;
		int hotter[]=new int[length];//用一个数组来存热度
		for(int i=0;i<length;i++){
		  praise=blogService.getBlog().get(i).getPraise();
		  comment=blogService.getBlog().get(i).getComment().size();
		  hotAlgorothm.setComment(comment);
		  hotAlgorothm.setPraise(praise);
		  hotter[i]=hotAlgorothm.getHotter();
		}
		sortByHotter.setHotter(hotter);
		sortByHotter.setList(blogService.getBlog());
		 map.put("blogs", sortByHotter.Sort());//把博文传到首页
		 map.put("comments", commentService.getComments());//把博文评论传到首页
		 map.put("backComments", commentService.getBackComments());//把博文评论回复传到首页
	     List<FirstCategory>list=blogService.getFirstCategory();
		 map.put("firstCategory", list);//把一级分类传到首页
		 for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		 map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
			}
			return "blog";
	}
	@RequestMapping(value="/search")
	public String search(String blogName,Map<String,Object>map) {
		List<Blog>blogs=blogService.searchByKeyWord(blogName);
		map.put("blogs",blogs);//满足搜索条件的博文
		map.put("blogsAll", blogService.getBlog());//所有博文
		map.put("comments", commentService.getComments());//把博文评论传到首页
		map.put("backComments", commentService.getBackComments());//把博文评论回复传到首页
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//把一级分类传到首页
		for(int i=0;i<list.size();i++){//把一级分类名称作为键，二级分类作为值放在map中
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
			}
		return "search";
	}
	@RequestMapping("/loadNewPage")
	public void loadPage(int page,HttpServletResponse response) throws IOException{
		if(page*5>blogService.getBlog().size() && (page-1)*5>=blogService.getBlog().size()){
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("already load all blogs");
		}
		else{	
		List<Blog> blogList=blogService.findPage(page);
		System.out.println("before json");
		JSONArray blogJson = new JSONArray();
		for(int i=0;i<blogList.size();i++){
			JSONObject jo = new JSONObject();
		    jo.put("id",blogList.get(i).getId());
		    jo.put("blogName",blogList.get(i).getBlogName());
		    jo.put("secondCategory", blogList.get(i).getCategory().getCategory());
		    jo.put("firstCategory",blogList.get(i).getCategory().getFirstCategory().getCategory());
		    jo.put("praise", blogList.get(i).getPraise());
		    jo.put("content",blogList.get(i).getContent());
		    jo.put("commentSize", blogList.get(i).getComment().size());

		    //加载评论回复
		    /*List<Comment>commentBackList= commentService.getBackComments();
		    for(int k=0;k<commentBackList.size();k++)
		    	jo.put("commentBack"+k,commentBackList.get(k).getContent());*/
		    blogJson.add(jo);
		}
		  //加载评论
		
	    JSONArray commentJson=new JSONArray();
	    List<Comment>commentList=commentService.getComments();
	    for(int j=0;j<commentList.size();j++){ 
	    	   
	    		JSONObject jo = new JSONObject();
	    		jo.put("id", commentList.get(j).getId());
	    		jo.put("content", commentList.get(j).getContent());
	    		jo.put("time", commentList.get(j).getCommentTime());
	    		jo.put("blogId", commentList.get(j).getBlog().getId());
	    		jo.put("visitor",commentList.get(j).getVisitor().getId());
	    		commentJson.add(jo); 	
	    }
	  
	    //评论回复
	    JSONArray commentBackJson=new JSONArray();
	    List<Comment>commentBackList=commentService.getBackComments();
	    for(int k=0;k<commentBackList.size();k++){   
	    		JSONObject jo = new JSONObject();
	    		jo.put("id", commentBackList.get(k).getId());
	    		jo.put("content", commentBackList.get(k).getContent());
	    		jo.put("time", commentBackList.get(k).getCommentTime());
	    		//jo.put("blogId", commentBackList.get(k).getBlog().getId());
	    		jo.put("visitor",commentBackList.get(k).getVisitor().getId());
	    		jo.put("commentBackId",commentBackList.get(k).getCommentBackId());
	    		commentBackJson.add(jo);
	    }
		response.setCharacterEncoding("UTF-8");
		System.out.println("after json");
		response.getWriter().print(blogJson.toString());
		response.getWriter().print("afghs");
		response.getWriter().print(commentJson.toString());
		response.getWriter().print("afghs");
		response.getWriter().print(commentBackJson.toString());
	}
	}
}