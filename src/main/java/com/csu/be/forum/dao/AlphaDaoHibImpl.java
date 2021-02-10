package com.csu.be.forum.dao;

import org.springframework.stereotype.Repository;

/**
 * @author nql
 * @version 1.0
 * @date 2020/12/14 21:45
 */

@Repository("alphahib")
public class AlphaDaoHibImpl implements AlphaDao {
    @Override
    public String find() {
        return "Hib";
    }
}
