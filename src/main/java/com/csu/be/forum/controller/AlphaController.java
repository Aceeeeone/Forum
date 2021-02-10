package com.csu.be.forum.controller;

import com.csu.be.forum.service.AlphaService;
import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sun.security.pkcs11.Secmod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author nql
 * @version 1.0
 * @date 2020/12/13 17:40
 */

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String HelloC(){
        return "helloSSM";
    }

    @RequestMapping("/search")
    @ResponseBody
    public String search(){
        return alphaService.select();
    }

    @RequestMapping("/date")
    @ResponseBody
    public String date(){
        return alphaService.date();
    }

    @RequestMapping("/httptest")
    public void main(HttpServletRequest request, HttpServletResponse response){

        System.out.println(request.getMethod());
        System.out.println(request.getContextPath());

        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name +":"+ value);
        }
        System.out.println(request.getParameter("code"));

        response.setContentType("text/html;charset=utf-8");

        try (
                PrintWriter writer = response.getWriter();
        ) {
            writer.write("<h1>515论坛</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(path = "/students" , method = RequestMethod.GET)
    @ResponseBody
    public String students(
            @RequestParam(name = "id",required = false,defaultValue = "1") int id,
            @RequestParam(name = "grade",required = false,defaultValue = "100") int grade){

        System.out.println(id);
        System.out.println(grade);
        String s = String.valueOf(id) + " " + String.valueOf(grade);
        return s;
    }

    @RequestMapping(path = "/student/{id}" , method = RequestMethod.GET)
    @ResponseBody
    public String student(
            @PathVariable(name = "id") int id
    ){
        System.out.println(id);
        return String.valueOf(id);
    }

    @RequestMapping(path = "/student" ,method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return "Success";
    }

    @RequestMapping(path = "/teacher" ,method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","法外狂徒");
        mav.addObject("age","50");
        mav.setViewName("/demo/teacher");
        return mav;
    }

    @RequestMapping(path = "/school" ,method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","中南大学");
        model.addAttribute("age","20");
        return "/demo/teacher";
    }

    @RequestMapping(path = "/json" ,method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getJson(Model model){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","Alex");
        map.put("age",23);
        map.put("sex","male");
        return map;
    }

    @RequestMapping(path = "/jsons" ,method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getJsons(Model model){
        List<Map<String,Object>> list = new LinkedList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","Alex");
        map.put("age",23);
        map.put("sex","male");
        list.add(map);

        map = new HashMap<>();
        map.put("name","Bob");
        map.put("age",26);
        map.put("sex","male");
        list.add(map);

        map = new HashMap<>();
        map.put("name","Ceb");
        map.put("age",25);
        map.put("sex","famale");
        list.add(map);


        return list;
    }
}

