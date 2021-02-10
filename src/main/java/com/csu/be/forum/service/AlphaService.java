package com.csu.be.forum.service;

import com.csu.be.forum.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author nql
 * @version 1.0
 * @date 2020/12/14 21:54
 */
@Service
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    public void init(){
        System.out.println("初始化AlphaService");
    }

    public String select(){
        return alphaDao.find();
    }

    public String date(){
        return simpleDateFormat.format(new Date());
    }
}
