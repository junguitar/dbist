# An Easy Database Management Framework for Java #

The Dbist is an easy Java programming framework to manage dababase.<br>
This is truly the easiest way to do database programming.<br>
You may already know about many other ORM Frameworks (Hibernate, mybatis, Active Record ...).<br>
But it is easier than them.<br>
<br>
Now, you can see why we must use Dbist instead of them.<br>
<br>
<ul><li><a href='GettingStarted.md'>Getting Started</a>
</li><li><a href='Downloads.md'>Downloads</a>
</li><li><a href='Manual.md'>Manual</a>
</li><li><a href='http://java-doc.appspot.com/dbist/2.0/'>Javadoc</a></li></ul>

<h2>Example</h2>

"POST" is a DB table.<br>
The DB table has four columns "title", "author", "content", "created_at" and a primary key "id".<br>

<table><thead><th>column    </th><th>data type</th></thead><tbody>
<tr><td>id (PK)   </td><td>VARCHAR  </td></tr>
<tr><td>title     </td><td>VARCHAR  </td></tr>
<tr><td>author    </td><td>VARCHAR  </td></tr>
<tr><td>content   </td><td>CLOB     </td></tr>
<tr><td>created_at</td><td>DATE     </td></tr></tbody></table>

And we try to map a class to this table. (access modifier, getter and setter methods,... has been omitted to make it easier to understand.)<br>
<pre><code>class Post {<br>
    String id;<br>
    String title;<br>
    String author;<br>
    String content;<br>
    Date createdAt;<br>
    ...<br>
}<br>
</code></pre>
The mapping configuration is not needed any more.<br>
How...? It is mapped automatically, because it is enough to be inferred.<br>
Now you can start DB programming right away.<br>
<br>
<h3>If you want to...<br></h3>

<ol><li>insert a data,<br>
<pre><code>Post post = new Post();<br>
post.setId("1");<br>
post.setTitle("Why Dbist?");<br>
post.setAuthor("Steve, M. Jung");<br>
post.setContent("Now, you can see why we should use Dbist...");<br>
post.setCreatedAt(new Date());<br>
dml.insert(post);<br>
</code></pre>
</li><li>select the data,<br>
<pre><code>Post post = dml.select(Post.class, "1");<br>
</code></pre>
</li><li>update the data,<br>
<pre><code>post.setContent("Now, you can see why we must use Dbist...");<br>
dml.update(post);<br>
</code></pre>
</li><li>select data list, (0, 10 means pageIndex and pageSize)<br>
<pre><code>List&lt;Post&gt; list = dml.selectList(Post.class, new Query(0, 10));<br>
for (Post post : list) {<br>
    ......<br>
}<br>
</code></pre>
</li><li>select some filtered data list, (title contains "Dbist" characters)<br>
<pre><code>List&lt;Post&gt; list = dml.selectList(Post.class, new Query(0, 10).addFitler("title" "like" "%Dbist%"));<br>
for (Post post : list) {<br>
    ......<br>
}<br>
</code></pre></li></ol>

It's really easy, isn't it?