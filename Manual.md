

# Features #

## Easy ORM ##

## Easy SQL Query ##

You can use user defined SQL queries by string or SQL files.<br>
Dbist also supports some template engines, so you can make SQL queries with some control logics.<br>
And you can define SQL queries differently by DB types.<br>
<br>
<h2>Low DB Dependencies</h2>

Dbist currently supports MySQL, Oracle, SQLServer, DB2.<br>
<br>
<h1>Configurations</h1>

<ul><li>config.properties</li></ul>

<table><thead><th> <b>Name</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> applicationContext.name </td><td> Context Path of your Webapp except '/' (default 'dbist') </td></tr>
<tr><td> dmlJdbc.domain </td><td> Owner of oracle, Database Name of sqlserver, Table Schema of mysql </td></tr>
<tr><td> dataSource.driverClassName </td><td> JDBC driver class name </td></tr>
<tr><td> dataSource.url </td><td> JDBC URL           </td></tr>
<tr><td> dataSource.username </td><td> Database username  </td></tr>
<tr><td> dataSource.password  </td><td> Database password  </td></tr>
<tr><td> sqlAspect.enabled </td><td> Print SQL query at log </td></tr>
<tr><td> sqlAspect.prettyPrint </td><td> Print prettily formatted SQL query at log </td></tr>
<tr><td> sqlAspect.combinedPrint </td><td> Print SQL query combined with parameters at log </td></tr></tbody></table>

<ul><li>beans.xml (springframework)</li></ul>

<pre><code>	&lt;context:property-placeholder location="/WEB-INF/*.properties" /&gt;<br>
<br>
	&lt;bean class="net.sf.common.util.BeanUtils"&gt;<br>
		&lt;property name="applicationContextName" value="${applicationContext.name}" /&gt;<br>
	&lt;/bean&gt;<br>
<br>
	&lt;!-- AOP --&gt;<br>
	&lt;aop:config&gt;<br>
		&lt;aop:aspect order="2" ref="sqlAspect"&gt;<br>
			&lt;aop:around method="print" pointcut="execution(* org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.*(..))" /&gt;<br>
		&lt;/aop:aspect&gt;<br>
	&lt;/aop:config&gt;<br>
	&lt;bean id="sqlAspect" class="org.dbist.aspect.SqlAspect"&gt;<br>
		&lt;property name="enabled" value="${sqlAspect.enabled}" /&gt;<br>
		&lt;property name="prettyPrint" value="${sqlAspect.prettyPrint}" /&gt;<br>
		&lt;property name="combinedPrint" value="${sqlAspect.combinedPrint}" /&gt;<br>
	&lt;/bean&gt;<br>
<br>
	&lt;!-- JDBC --&gt;<br>
	&lt;bean id="dmlJdbc" class="org.dbist.dml.impl.DmlJdbc"&gt;<br>
		&lt;property name="domain" value="${dmlJdbc.domain}" /&gt;<br>
		&lt;property name="preprocessor"&gt;<br>
			&lt;bean class="org.dbist.processor.impl.VelocityPreprocessor" /&gt;<br>
		&lt;/property&gt;<br>
		&lt;property name="dataSource" ref="dataSourceJdbc" /&gt;<br>
		&lt;property name="jdbcOperations" ref="jdbcOperations" /&gt;<br>
		&lt;property name="namedParameterJdbcOperations" ref="namedParameterJdbcOperations" /&gt;<br>
	&lt;/bean&gt;<br>
	&lt;bean id="jdbcOperations" class="org.springframework.jdbc.core.JdbcTemplate"&gt;<br>
		&lt;property name="dataSource" ref="dataSourceJdbc" /&gt;<br>
	&lt;/bean&gt;<br>
	&lt;bean id="namedParameterJdbcOperations" class="org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate"&gt;<br>
		&lt;constructor-arg name="dataSource" index="0" ref="dataSourceJdbc" /&gt;<br>
	&lt;/bean&gt;<br>
	&lt;bean id="dataSourceJdbc" class="org.apache.commons.dbcp.BasicDataSource"&gt;<br>
		&lt;property name="driverClassName" value="${dataSource.driverClassName}" /&gt;<br>
		&lt;property name="url" value="${dataSource.url}" /&gt;<br>
		&lt;property name="username" value="${dataSource.username}" /&gt;<br>
		&lt;property name="password" value="${dataSource.password}" /&gt;<br>
		&lt;property name="maxActive" value="${dataSource.maxActive}" /&gt;<br>
	&lt;/bean&gt;<br>
</code></pre>

<h1>How to CRUD</h1>

<h2>How to Select Data</h2>

<h3>Select a Data</h3>

If the "DATA" table has a primary key<br>
<br>
<pre><code>SELECT * FROM DATA WHERE id = 'x'<br>
</code></pre>

<pre><code>Data data = dml.select(Data.class, "x");<br>
</code></pre>

If the "DATA" table has composite keys and the field names are 'pk1' and 'pk2'<br>
<br>
<pre><code>SELECT * FROM DATA WHERE pk1 = 'x' and pk2 = 'y'<br>
</code></pre>

<pre><code>// By Array<br>
Data data = dml.select(Data.class, "x", "y");<br>
<br>
// By List<br>
List&lt;Object&gt; id = new ArrayList&lt;Object&gt;();<br>
id.add("x");<br>
id.add("y");<br>
Data data = dml.select(Data.class, id);<br>
<br>
// By HttpServletRequest parameters (parameters must have values of 'pk1', 'pk2')<br>
Data data = dml.select(Data.class, request);<br>
<br>
// By Map<br>
Map&lt;String, Object&gt; paramMap = new HashMap&lt;String, Object&gt;();<br>
paramMap.put("pk1", "x");<br>
paramMap.put("pk2", "y");<br>
Data data = dml.select(Data.class, paramMap);<br>
<br>
// By the object<br>
Data data = new Data();<br>
data.setPk1("x");<br>
data.setPk2("y");<br>
data = dml.select(data);<br>
<br>
// By another object<br>
OtherData otherData = new OtherData();<br>
otherData.setPk1("x");<br>
otherData.setPk2("y");<br>
Data data = dml.select(Data.class, otherData);<br>
<br>
// By Query object<br>
Query query = new Query();<br>
query.addFilter("pk1", "x").addFilter("pk2", "y");<br>
Data data = dml.select(Data.class, query);<br>
</code></pre>

If the "DATA" table has candidate keys and the field names are 'ck1' and 'ck2'<br>
<br>
<pre><code>SELECT * FROM DATA WHERE ck1 = 'x' and ck2 = 'y'<br>
</code></pre>

<pre><code>// By HttpServletRequest parameters (parameters must have values of 'ck1', 'ck2')<br>
Data data = dml.selectByCondition(Data.class, request);<br>
<br>
// By Map<br>
Map&lt;String, Object&gt; paramMap = new HashMap&lt;String, Object&gt;();<br>
paramMap.put("pk1", "x");<br>
paramMap.put("pk2", "y");<br>
Data data = dml.selectByCondition(Data.class, paramMap);<br>
<br>
// By the object<br>
Data data = new Data();<br>
data.setCk1("l");<br>
data.setCk2("m");<br>
data = dml.selectByCondition(Data.class, data);<br>
<br>
// By another object<br>
OtherData otherData = new OtherData();<br>
otherData.setCk1("x");<br>
otherData.setCk2("y");<br>
Data data = dml.select(Data.class, otherData);<br>
<br>
// By Query object<br>
Query query = new Query();<br>
query.addFilter("ck1", "x").addFilter("ck2", "y");<br>
Data data = dml.select(Data.class, query);<br>
</code></pre>

<h3>Select Data List</h3>

<pre><code></code></pre>

<h3>Select Data Size(Count)</h3>

<pre><code></code></pre>

<h2>How to Insert Data</h2>

<h3>Insert a Data</h3>

<pre><code></code></pre>

<h3>Insert Data List</h3>

<pre><code></code></pre>

<h2>How to Update Data</h2>

<h3>Update a Data</h3>

<pre><code></code></pre>

<h3>Insert Data List</h3>

<pre><code></code></pre>

<h2>How to Delete Data</h2>

<h3>Delete a Data</h3>

<pre><code></code></pre>

<h3>Delete Data List</h3>

<pre><code></code></pre>

<h2>How to use Query Class</h2>

<h3>Field</h3>

<pre><code>selecdt a, b from DATA<br>
</code></pre>

<pre><code>List&lt;Data&gt; list = dml.selectList(Data.class, new Query().addField("a", "b"));<br>
<br>
or<br>
<br>
Query query = new Query();<br>
query.addField("a", "b");<br>
List&lt;Data&gt; list = dml.selectList(Data.class, query);<br>
</code></pre>

<h3>Filter</h3>

<h3>Filters</h3>

<h3>Order By</h3>

<pre><code>select * from DATA order by a asc, b desc<br>
</code></pre>

<pre><code>List&lt;Data&gt; list = dml.selectList(Data.class, new Query().addOrder("a", true).addOrder("b", false));<br>
<br>
or<br>
<br>
Query query = new Query();<br>
query.addOrder("a", true);<br>
query.addOrder("b", false);<br>
List&lt;Data&gt; list = dml.selectList(Data.class, query);<br>
</code></pre>

<h3>Group By</h3>

<pre><code>select a, b from DATA group by a, b<br>
</code></pre>

<pre><code>List&lt;Data&gt; list = dml.selectList(Data.class, new Query().addGroup("a", "b"));<br>
<br>
or<br>
<br>
Query query = new Query();<br>
query.addGroup("a", "b");<br>
List&lt;Data&gt; list = dml.selectList(Data.class, query);<br>
</code></pre>

<h3>Lock</h3>

<h3>Pagination</h3>

<ul><li>Page 1</li></ul>

<pre><code>// oracle<br>
select * from (select * from DATA) where rownum &lt;= 10<br>
<br>
// sqlserver<br>
select top 10 * from DATA<br>
<br>
// mysql<br>
select * from DATA limit 10<br>
<br>
// db2<br>
select * from DATA fetch first 10 rows only<br>
<br>
</code></pre>

<pre><code>List&lt;Data&gt; list = dml.selectList(Data.class, new Query(0, 10));<br>
<br>
or<br>
<br>
Query query = new Query();<br>
query.setPageIndex(0);<br>
query.setPageSize(10);<br>
List&lt;Data&gt; list = dml.selectList(Data.class, query);<br>
</code></pre>

<ul><li>Page 2</li></ul>

<pre><code>// oracle<br>
select * from (<br>
    select pagetbl_.*, rownum rownum_ from (<br>
        select * from DATA<br>
    ) <br>
    pagetbl_ where rownum &lt;= 10<br>
) where rownum_ &gt; 0<br>
<br>
// sqlserver<br>
select top 20 * from DATA<br>
<br>
// mysql<br>
select * from DATA limit 10 : 10<br>
<br>
// db2<br>
select * from (<br>
	select pagetbl_.*, rownumber() over(order by order of pagetbl_) rownumber_ from (<br>
		select * from DATA fetch first 20 rows only<br>
	) pagetbl_<br>
) pagetbl__ <br>
where rownumber_ &gt; 10 order by rownumber_<br>
</code></pre>

<pre><code>List&lt;Data&gt; list = dml.selectList(Data.class, new Query(1, 10));<br>
<br>
or<br>
<br>
Query query = new Query();<br>
query.setPageIndex(1);<br>
query.setPageSize(10);<br>
List&lt;Data&gt; list = dml.selectList(Data.class, query);<br>
</code></pre>

<h1>How to execute SQL Query</h1>

<h2>Select Data by SQL String</h2>

<h2>Select Data by SQL Path</h2>

<ul><li>SQL file content</li></ul>

<pre><code>select * from DATA<br>
</code></pre>

<h3>By SQL Filepath or Classpath</h3>

<ul><li>org/dbist/sample/sql/select_data.sql</li></ul>

<pre><code>// Map List<br>
List&lt;Map&gt; list = selectListBySqlPath("org/dbist/sample/sql/select_data.sql", null, Map.class, 0, 0);<br>
<br>
// Data List<br>
List&lt;Data&gt; list = selectListBySqlPath("org/dbist/sample/sql/select_data.sql", null, Data.class, 0, 0);<br>
</code></pre>

<h3>By SQL Directory Path or Package Path</h3>

<ul><li>org/dbist/sample/sql/select_data<br>
<ul><li>ansi.sql (default)<br>
</li><li>oracle.sql<br>
</li><li>sqlserver.sql<br>
</li><li>mysql.sql</li></ul></li></ul>

If the dbType of dataSource is 'oracle', it'll check oracle.sql, ansi.sql order<br>
<br>
<pre><code>// Map List<br>
List&lt;Map&gt; list = selectListBySqlPath("org/dbist/sample/sql/select_data", null, Map.class, 0, 0);<br>
<br>
// Data List<br>
List&lt;Data&gt; list = selectListBySqlPath("org/dbist/sample/sql/select_data", null, Data.class, 0, 0);<br>
</code></pre>

<h3>Preprocessors</h3>

<h4>Velocity Preprocessor</h4>

<h4>Script Preprocessor</h4>

<h1>Annotations</h1>

<h2>Table</h2>

Table annotation is used to specify a mapped table for a class.<br>
If no Table annotation is specified, the default values are applied.<br>
<br>
<pre><code>	@Table(name = "comments")<br>
	public class Comment {<br>
		...<br>
	}<br>
<br>
	@Table(name = "users")<br>
	public class User {<br>
		...<br>
	}<br>
</code></pre>

<h2>Column</h2>

Column annotation is used to specify a mapped column for a field.<br>
If no Column annotation is specified, the default values are applied.<br>
<br>
<pre><code>@Column(name = "pwd", type = ColumnType.PASSWORD)<br>
private String password;<br>
<br>
@Column(type = ColumnType.TITLE)<br>
private String title;<br>
<br>
@Column(type = ColumnType.LISTED)<br>
private String author;<br>
<br>
@Column(type = ColumnType.TEXT)<br>
private String content;<br>
</code></pre>

<h2>Ignore</h2>

Ignore annotation ignores a field from DB column mapping.<br>
<br>
<h1>Aspects</h1>

<h2>SQL Aspect</h2>

<h1>DML</h1>

<h2>JDBC</h2>

<h2>Hibernate</h2>

<h2>JPA</h2>

<h2>JDO</h2>