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
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devzy.share.zkui.utils.PropertiesConfigUtil;
import com.devzy.share.zkui.utils.ServletUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/logout"})
public class Logout extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(Logout.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            logger.debug("Logout Action!");
            String zkServer = System.getenv("ZK_HOSTS");
            if(StringUtils.isBlank(zkServer)) {
            	zkServer = PropertiesConfigUtil.getString("zkServer","localhost:2181");
            }
            String[] zkServerLst = zkServer.split(",");
            ZooKeeper zk = ServletUtil.INSTANCE.getZookeeper(request, response, zkServerLst[0]);
            request.getSession().invalidate();
            zk.close();
            response.sendRedirect("/login");
        } catch (InterruptedException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            ServletUtil.INSTANCE.renderError(request, response, ex.getMessage());
        }

    }
}
