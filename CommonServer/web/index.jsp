<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.apache.http.protocol.HTTP" %>
<%
  response.setHeader(HTTP.CONN_DIRECTIVE,"Keep-Alive");
  response.setHeader(HTTP.CONN_KEEP_ALIVE,"timeout=3000,max=100");
%>
<html>
  <head>
    <title>$Title$</title>
  </head>
  <body>
  $END$
  </body>
</html>

