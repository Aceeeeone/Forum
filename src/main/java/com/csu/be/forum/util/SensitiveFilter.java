package com.csu.be.forum.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nql
 * @version 1.0
 * @date 2021/2/27 17:06
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    //根结点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode tempNode = this.rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubnode(c, subNode);
            }
            tempNode = subNode;

            if (i == keyword.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    //过滤铭感词
    public String filter(String text) {
        if (text == null) {
            return null;
        }

        TrieNode tempnode = this.rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder res = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            //跳过特殊符号
            if (isSymbol(c)) {
                if (tempnode == rootNode) {
                    res.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            tempnode = tempnode.getSubNode(c);
            if (tempnode == null) {
                res.append(text.charAt(begin));
                position = ++begin;
                tempnode = rootNode;
            } else if (tempnode.isKeywordEnd) {
                res.append(REPLACEMENT);
                begin = ++position;
                tempnode = rootNode;
            } else {
                position++;
            }
        }

        res.append(text.substring(begin));
        return res.toString();
    }

    private boolean isSymbol(char c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode {

        //结束标志
        private boolean isKeywordEnd = false;

        //子结点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeywordEnd;
        }

        public void setKeyWordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubnode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
