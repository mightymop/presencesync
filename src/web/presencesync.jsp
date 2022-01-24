<%@ page contentType="text/html; charset=UTF-8" %>
<%--
  -
  - Copyright (C) 2004-2008 Jive Software. All rights reserved.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
--%>

<%@ page import="org.jivesoftware.util.*,
                 org.jivesoftware.openfire.*,
                 org.igniterealtime.openfire.plugin.presencesync.PresenceSyncPlugin"
    errorPage="error.jsp"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager"  />
<% webManager.init(request, response, session, application, out ); %>

<html>
<head>
<title><fmt:message key="presencesync.settings.title"/></title>
<meta name="pageID" content="server-presencesync-id"/>
</head>
<body>

<%  // Get parameters:
    boolean update = request.getParameter("update") != null;

    boolean pluginEnabled = ParamUtils.getParameter(request,"pluginEnabled")!=null&&ParamUtils.getParameter(request,"pluginEnabled").equals("on")?true:false;
   
    Cookie csrfCookie = CookieUtils.getCookie(request, "csrf");
    String csrfParam = ParamUtils.getParameter(request, "csrf");

    if (update) {
        if (csrfCookie == null || csrfParam == null || !csrfCookie.getValue().equals(csrfParam)) {
            update = false;
        }
    }
    csrfParam = StringUtils.randomString(15);
    CookieUtils.setCookie(request, response, "csrf", csrfParam, -1);
    pageContext.setAttribute("csrf", csrfParam);

    if (update) {
        PresenceSyncPlugin.XMPP_PRESENCESYNC_ENABLED.setValue(pluginEnabled);
      
        // Log the event
        webManager.logEvent((pluginEnabled ? "enabled" : "disabled")+" presencesync", null);
    %>
    <div class="jive-success">
    <table cellpadding="0" cellspacing="0" border="0">
    <tbody>
        <tr><td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0" alt=""></td>
        <td class="jive-icon-label">
        <fmt:message key="presencesync.settings.update" />
        </td></tr>
    </tbody>
    </table>
    </div><br>
    <%
    
    }

    // Set page vars
    pluginEnabled = PresenceSyncPlugin.XMPP_PRESENCESYNC_ENABLED.getValue();

%>

<form action="presencesync.jsp">
    <input type="hidden" name="csrf" value="${csrf}">
    <div class="jive-contentBoxHeader">
        <fmt:message key="presencesync.settings.title" />
    </div>
    <div class="jive-contentBox">
        <table cellpadding="3" cellspacing="0" border="0">
        <tbody>
            <tr valign="top">
                
                <td >
                    <label for="pluginEnabled">
                     <b><fmt:message key="presencesync.settings.enable" /></b> -
                     <fmt:message key="presencesync.settings.enable_info" />
                    </label>
                </td>
                <td  nowrap>
                    <input type="checkbox" name="pluginEnabled" id="pluginEnabled"  <%=(pluginEnabled?"checked" : "")%>>
                </td>
            </tr>
        </tbody>
        </table>
    </div>
    <input type="submit" name="update" value="<fmt:message key="global.save_settings" />">
</form>
</body>
</html>
