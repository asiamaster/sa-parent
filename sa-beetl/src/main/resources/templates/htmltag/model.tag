<table>
    <tr>
<%
var trs = tag.children;
var beanClass = tag.beanClass;
print(beanClass);
for(tr in trs){
if(tr.tagName=="tr"){
//输出表头
%>
<td>${tr.name}</td>
<%} //if end
    }//for end%>
</tr>
<%

    for(row in tag.data) {
//先绑定变量，这样tr能使用
    @tag.binds(row,rowLP.index);
%>
<tr>
    <%

        for(tr in trs){
        if(tr.tagName=="tr"){
//tr.body将执行tr里的内容。另外一个是tr.execute,将调用tr.tag,目前我们不需要调用tr.tag
    %>
    <td class="${tr.style}">${tr.body}</td>
    <%} //if end
        }//for end%>
</tr>
<%} //for tr tag end%>
</table>