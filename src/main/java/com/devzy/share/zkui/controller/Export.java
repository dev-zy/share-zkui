/**
 *
 * Copyright (c) 2014, Deem Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.devzy.share.zkui.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devzy.share.zkui.utils.PropertiesConfigUtil;
import com.devzy.share.zkui.utils.ServletUtil;
import com.devzy.share.zkui.utils.ZooKeeperUtil;
import com.devzy.share.zkui.vo.LeafBean;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/export"})
public class Export extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(Export.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Export Get Action!");
        try {
        	String zkServer = System.getenv("ZK_HOSTS");
            if(StringUtils.isBlank(zkServer)) {
            	zkServer = PropertiesConfigUtil.getString("zkServer","localhost:2181");
            }
            String[] zkServerLst = zkServer.split(",");

            String authRole = (String) request.getSession().getAttribute("authRole");
            if (authRole == null) {
                authRole = ZooKeeperUtil.ROLE_USER;
            }
            String zkPath = request.getParameter("zkPath");
            StringBuilder output = new StringBuilder();
            output.append("#App Config Dashboard (ACD) dump created on :").append(new Date()).append("\n");
            Set<LeafBean> leaves = ZooKeeperUtil.INSTANCE.exportTree(zkPath, ServletUtil.INSTANCE.getZookeeper(request, response, zkServerLst[0]), authRole);
            for (LeafBean leaf : leaves) {
                output.append(leaf.getPath()).append('=').append(leaf.getName()).append('=').append(ServletUtil.INSTANCE.externalizeNodeValue(leaf.getValue())).append('\n');
            }// for all leaves
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.write(output.toString());
            }

        } catch (InterruptedException | KeeperException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            ServletUtil.INSTANCE.renderError(request, response, ex.getMessage());
        }
    }
}
