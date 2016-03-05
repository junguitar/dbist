# Getting Started #

## Introduction ##

Welcome to be a Dbist! This framework is easy to use. It'll takes only a few minutes before you start DB programming, if you're already prepared to program a java application.<br><br>
Dbist currently supports MySQL, PostgreSQL, Oracle, SQLServer and DB2.<br>
If you're a user of one of these DBMS, you can start right away.<br>
Let's begin...<br>
<br>
<h2>Installation</h2>

<i>Required</i>
<ol><li>DBMS - MySQL 5(or later), PostgreSQL 9.2(or later), Oracle 10g(or later), SQLServer2000(or later) or DB2 9.7(or later)<br>
</li><li>Java SE JDK(or JRE) 6 or later<br>
</li><li>Tomcat 7 (or another WAS which supports Servlet 3.0)</li></ol>

<i>Optional</i>
<ol><li>Maven2 or later<br>
</li><li>Eclipse Indigo or later (IDE for Java EE Developers)<br>
</li><li>Eclipse Plugin - Maven Integration for Eclipse (from Eclipse Marketplace)</li></ol>

<h2>Running Example Sources</h2>

<ol><li>Create an Database to your DBMS (ex. dbist)<br>
</li><li>Run <a href='ExampleSchema.md'>this script</a> to the Database<br>
</li><li>Download this <a href='https://repo-m2.googlecode.com/svn/releases/org/dbist/dbist-example-webapp/2.0.2/dbist-example-webapp-2.0.2.war'>dbist-example-webapp-2.0.2.war</a> file and modify the file name as "dbist-example.war"<br>(or check-out the example project from <a href='http://dbist.googlecode.com/svn/tags/dbist-2.0.2/dbist-example-webapp/'>the dbist SVN</a>)<br>
</li><li>Publish this example to your tomcat server<br>
</li><li>Check and modify <a href='Manual#Configurations.md'>the config.properties</a>(dbist-example/WEB-INF/config.properties) file<br>
</li><li>Run the tomcat server and browse <a href='http://localhost:8080/dbist-example/dbistadmin'>this url</a> on a browser<br>
</li><li>Move to the "Dml" menu and enjoy it. (class: <b>org.dbist.example.blog.jdbc</b> and enter)