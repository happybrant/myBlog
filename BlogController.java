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
	 * ��������
	 * @param userId
	 * @param blog
	 * @param secondCategory
	 */
	@RequestMapping(value="/addBlog/{userId}",method=RequestMethod.POST)
	public String addBlog(@PathVariable int userId, Blog blog,String secondCategory,int firstCategory  ){
		
		blog.setUser(userService.getUser(userId));						//�����û�
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ		
		blog.setPublishTime(df.format(new Date()));//���÷���ʱ��;new Date()Ϊ��ȡ��ǰϵͳʱ��
		blog.setPageViews(0);					   //���ò��������
        
		if(blogService.getSecondCategory(secondCategory)!=null){//˵���ö����������
		blog.setCategory(blogService.getSecondCategory(secondCategory));//���ö�������
        }
        else{//������������
        this.secondCategory.setCategory(secondCategory);//���ö�����������
        this.secondCategory.setFirstCategory(blogService.getFirstCategory(firstCategory));//���ö�Ӧһ������
        blogService.saveSecondCategory(this.secondCategory);//���ö������ౣ�浽���ݿ���
        blog.setCategory(this.secondCategory);//���ø������Ķ�������
        }
		
		blogService.saveBlog(blog);//���沩�ĵ����ݿ���
		return "redirect:../blogContent/"+blog.getId();
	}
	@RequestMapping("/edit")
	public String  redirectManager(Map<String,Object>map){
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
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
	 * ������ҳ
	 * @param map
	 * @return
	 */
	@RequestMapping("/blog")
	public String redirectBlog(Map<String,Object>map){
	    if(!map.containsKey("visitor")){//�����οͷ���һ���̶����οͱ�ʶֱ��session����
		int visitorNum=(int)commentService.countVisitor();
	    commentService.saveVisitor(visitor);
	    System.out.println("����һ��");
		map.put("visitor",visitorNum+1);
	    }
	    map.put("comments", commentService.getComments());//�Ѳ������۴�����ҳ
	    map.put("backComments", commentService.getBackComments());//�Ѳ������ۻظ�������ҳ
		map.put("blogs", blogService.findPage(1));//�Ѳ��Ĵ�����ҳ
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//��һ�����ഫ����ҳ
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "blog";
	}
	/**
	 * �������ķ���
	 * @param id
	 * @param map
	 * @return
	 */
	@RequestMapping("/blog/{id}")
	public String sort(@PathVariable int id,Map<String,Object>map){
	 
        map.put("comments", commentService.getComments());//�Ѳ������۴�����ҳ
       
        map.put("backComments", commentService.getBackComments());//�Ѳ������ۻظ�������ҳ
		map.put("blogs", blogService.getBlogBySecondCate(id));
		map.put("blogsAll", blogService.getBlog());//���в���
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "blogCategory";
	}
	/**
	 * ����
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
	 * ȡ������
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
	 * ����Ա�����б�ҳ��
	 * @param map
	 * @return
	 */
	@RequestMapping("/blogList")
	public String blogList(Map<String,Object>map){
		List<Blog>blogs=new ArrayList<Blog>();
		for(int i=0;i<5;i++){//���ݵ�ǰҳ����ѡ��չʾ�Ĳ���
		    blogs.add( blogService.getBlog().get(i));
			if(blogService.getBlog().size()<=i+1)
				break;
		}
		map.put("blogs", blogs);
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());	
	    }
		map.put("logs", logService.getLog());
		map.put("blogLength", blogService.getBlog().size());//��Ų��ĳ���
		map.put("currentPage", 1);//��ŵ�ǰҳ��
	    return "blogList";
	}
	/**
	 * ��ҳ
	 * @param map
	 * @param page
	 * @return
	 */
	@RequestMapping("/blogList/page/{page}")
	public String blogListbyPage(Map<String,Object>map,@PathVariable int page){
		List<Blog>blogs=new ArrayList<Blog>();
		for(int i=page*5-5;i<page*5;i++){//���ݵ�ǰҳ����ѡ��չʾ�Ĳ���
		    blogs.add( blogService.getBlog().get(i));
			if(blogService.getBlog().size()<=i+1)
				break;
		}
		map.put("blogs", blogs);
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());	
	  }
		map.put("blogLength", blogService.getBlog().size());//��Ų��ĳ���
		map.put("currentPage", page);//��ŵ�ǰҳ��
	    return "blogListPage";
	}
	/**
	 * ���ݷ���չʾ�����б�
	 * @param id
	 * @param map
	 * @return
	 */
	@RequestMapping("/blogList/{id}")
	public String blogListById(@PathVariable int id,Map<String,Object>map){
		map.put("blogs", blogService.getBlogBySecondCate(id));
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "managerBlogCategory";
	}
	/**
	 * ����Ա������������ҳ��
	 * @param id
	 * @param map
	 * @return
	 */
	@RequestMapping("/blogContent/{id}")
	public String blogContent(@PathVariable int id,Map<String,Object>map){
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		map.put("blog",blogService.getBlog(id));
		map.put("comments", commentService.getComments());//�Ѳ������۴�����������ҳ��
	    map.put("backComments", commentService.getBackComments());//�Ѳ������ۻظ�������������ҳ��
		return "blogContent";
	}
	/**
	 * ��������
	 * @throws IOException
	 */
	@RequestMapping(value="/addComment",method=RequestMethod.POST)
	public void addComment(int visitor,String comment,int blogId,int commentBackId,HttpServletResponse response ) throws IOException{
		
		this.comment.setVisitor(commentService.getVisitor(visitor));//�����ο�
		this.comment.setContent(comment);//������������
		this.comment.setBlog(blogService.getBlog(blogId));//���ò���id
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ		
		this.comment.setCommentTime(df.format(new Date()));//���÷���ʱ��;new Date()Ϊ��ȡ��ǰϵͳʱ��
		this.comment.setCommentBackId(commentBackId);
		commentService.saveComment(this.comment);//���������
		log.setTime(df.format(new Date()));
		log.setVisitor(visitor);
		log.setBlogId(blogId);
		log.setLog("���ġ�"+blogService.getBlog(blogId).getBlogName()+"�����µ�����");
		logService.saveLog(log);//������־����̨
		response.getWriter().println("add success");
	}
	/**
	 * �����ķ���ʱ������
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
	    map.put("comments", commentService.getComments());//�Ѳ������۴�����ҳ
	    map.put("backComments", commentService.getBackComments());//�Ѳ������ۻظ�������ҳ
		map.put("blogs",blogList);//�Ѳ��Ĵ�����ҳ
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//��һ�����ഫ����ҳ
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
		}
		return "blog";
	}

	/**
	 * �����������ȶ������ҳ��
	 * @param map
	 * @return
	 */
	@RequestMapping("/hottest")
	public String hottest(Map<String,Object>map){
		int length=blogService.getBlog().size();
		int praise=0;
		int comment=0;
		int hotter[]=new int[length];//��һ�����������ȶ�
		for(int i=0;i<length;i++){
		  praise=blogService.getBlog().get(i).getPraise();
		  comment=blogService.getBlog().get(i).getComment().size();
		  hotAlgorothm.setComment(comment);
		  hotAlgorothm.setPraise(praise);
		  hotter[i]=hotAlgorothm.getHotter();
		}
		sortByHotter.setHotter(hotter);
		sortByHotter.setList(blogService.getBlog());
		 map.put("blogs", sortByHotter.Sort());//�Ѳ��Ĵ�����ҳ
		 map.put("comments", commentService.getComments());//�Ѳ������۴�����ҳ
		 map.put("backComments", commentService.getBackComments());//�Ѳ������ۻظ�������ҳ
	     List<FirstCategory>list=blogService.getFirstCategory();
		 map.put("firstCategory", list);//��һ�����ഫ����ҳ
		 for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
		 map.put("category"+list.get(i).getId(), list.get(i).getSecondCategory());
			}
			return "blog";
	}
	@RequestMapping(value="/search")
	public String search(String blogName,Map<String,Object>map) {
		List<Blog>blogs=blogService.searchByKeyWord(blogName);
		map.put("blogs",blogs);//�������������Ĳ���
		map.put("blogsAll", blogService.getBlog());//���в���
		map.put("comments", commentService.getComments());//�Ѳ������۴�����ҳ
		map.put("backComments", commentService.getBackComments());//�Ѳ������ۻظ�������ҳ
		List<FirstCategory>list=blogService.getFirstCategory();
		map.put("firstCategory", list);//��һ�����ഫ����ҳ
		for(int i=0;i<list.size();i++){//��һ������������Ϊ��������������Ϊֵ����map��
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

		    //�������ۻظ�
		    /*List<Comment>commentBackList= commentService.getBackComments();
		    for(int k=0;k<commentBackList.size();k++)
		    	jo.put("commentBack"+k,commentBackList.get(k).getContent());*/
		    blogJson.add(jo);
		}
		  //��������
		
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
	  
	    //���ۻظ�
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