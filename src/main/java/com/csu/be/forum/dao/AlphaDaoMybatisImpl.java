package com.csu.be.forum.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author nql
 * @version 1.0
 * @date 2020/12/14 20:46
 */

@Repository
@Primary
public class AlphaDaoMybatisImpl implements AlphaDao {

    @Override
    public String find() {
        return "Mybatis";
    }
}
