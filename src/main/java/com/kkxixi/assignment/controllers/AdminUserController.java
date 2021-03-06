package com.kkxixi.assignment.controllers;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.kkxixi.assignment.entities.User;

@Controller
public class AdminUserController {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@RequestMapping(value="/adminusermanage")
	public ModelAndView adminUserManage(){
		ModelAndView model = new ModelAndView();
		model.setViewName("adminuserman");
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("from User as user");
		List<User> list = query.list();
		model.addObject("userlist", list);
		session.close();
		return model;
	}
	
	@RequestMapping(value="/adduserpage")
	public ModelAndView addUserPage(){
		ModelAndView model = new ModelAndView();
		model.setViewName("adminadduser");
		return model;
	}
	
	@RequestMapping(value="/addauser",method=RequestMethod.POST)
	public @ResponseBody String addUser(@RequestParam(value="username")String username,@RequestParam(value="name")String name,
			@RequestParam(value="password")String password,@RequestParam(value="type")String usertype){
		User user = new User();
		user.setName(name);
		user.setUsername(username);
		user.setPassword(password);
		user.setUsertype(usertype);
		Session session = sessionFactory.openSession();
		
		Query query = session.createQuery("from User where username=:username");
		
		query.setString("username",username);
		
		List<User> list = query.list();
		
		if( list.size()!=0 ){
			session.close();
			return "{\"status\":\"failed\"}"; 
		}
		
		Transaction tx = session.beginTransaction();
		session.save(user);
		tx.commit();
		session.close();
		return "{\"status\":\"ok\"}";
	}
	
	@RequestMapping(value="/showuser",produces = "text/html;charset=UTF-8" )
	public @ResponseBody String showuser(@RequestParam(value="uid") int uid){
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("from User where uid=:uid");
		query.setString("uid", Integer.toString(uid));
		List<User>  list = query.list();
		User user = list.get(0);
		user.setPassword("");
		JSONObject json = JSONObject.fromObject(user);
		return json.toString();
	}
	
	@RequestMapping(value="/modifyuser",method=RequestMethod.POST)
	public @ResponseBody String modifyUser(@RequestParam(value="uid")int uid,@RequestParam(value="username")String username,
			@RequestParam("usertype")String usertype,@RequestParam("name")String name){
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Query query = session.createQuery("from User where uid=:uid");
		Query query_other = session.createQuery("from User where uid!=:uid and username=:username");
		query.setString("uid", Integer.toString(uid));
		query_other.setString("uid", Integer.toString(uid));
		query_other.setString("username", username);
		List<User> list = query.list();
		List<User> ll = query_other.list();
		if( ll.size()>0 ){
			session.close();
			return "{\"status\":\"failed\"}";
		}
		User user = list.get(0);
		user.setName(name);
		user.setUsername(username);
		user.setUid(uid);
		user.setUsertype(usertype);
		session.update(user);
		tx.commit();
		session.close();
		return "{\"status\":\"ok\"}";
	}
	
	@RequestMapping(value="/delauser",method=RequestMethod.GET,produces = "text/html;charset=UTF-8")
	public @ResponseBody String deleteAUser(HttpServletRequest request,@RequestParam(value="uid")int uid){
		if( uid==1 ){
			return "{\"status\":\"failed\",\"msg\":\"您不能够删除超级管理员！\"}";
		}
		Cookie [] cookies = request.getCookies();
		String usertype = null;
		for(int i=0;i<cookies.length;i++){
			if( cookies[i].getName().equals("type") ){
				usertype = cookies[i].getValue();
				break;
			}
		}
		if( !usertype.equals("admin") ){
			return "{\"status\":\"failed\",\"msg\":\"权限不足！\"}";
		}
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		User user = new User();
		user.setUid(uid);
		session.delete(user);
		tx.commit();
		session.close();
		return "{\"status\":\"ok\"}";
	}
}
