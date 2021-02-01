<%
    /**
     * <#form id="tableId" style="kk" data="${userList}" var="row,index">
     <#tr style="trcss1" name="序号"  > ${index} </#tr>
     <#tr style="trcss1" name="名称" import="true" > ${row.name} </#tr>
     <#tr style="trcss2" name="端口"> ${row.port} </#tr>
     <#tr style="trcss3" name="操作"> 更新</#tr>
     </#form>
     */
%>
<table>
    <tr>
            <%
//tag表示当前标签，chidren属性获得下级标签
var trs = tag.children;
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
            <%} %>
</table>