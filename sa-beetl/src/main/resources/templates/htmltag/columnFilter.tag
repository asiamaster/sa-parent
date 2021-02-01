<%
    var formSuffix="Form";
    var dlgSuffix="Dlg";
    var tableSuffix="Table";
    var fieldInfix="_col_";
    var gridIds = strutil.split(_gridId, ",");
    var userTicket = @com.sa.sysadmin.sdk.session.SessionContext.getSessionContext().getUserTicket();
    var userId = null;
//优化使用标签上的_userId属性，如果没有该属性则从权限SDK里面获取userTicket
    if(has(_userId) && _userId != null){
    userId = _userId;
    }else{
    userId = userTicket == null ? null : userTicket.id;
    }
    for(var gridId in gridIds) {
    //去掉字符串两端空格
    gridId = strutil.trim(gridId);
    //避免空串
    if(strutil.length(gridId)<1){
    continue;
    }
%>
<!-- ${gridId}选择列隐藏框 -->
<div id="${gridId}${dlgSuffix}" class="easyui-dialog" resizable="false" constrain="true" shadow="true" draggable="false" title="选择列" style="padding:20px" modal="true" border="thin" closed="true"
     data-options="
                    onOpen:open${gridId}${dlgSuffix},
                    iconCls: 'icon-filter',
                    buttons: [{
                        text:'反选',
                        handler: function(){invertColumn('${gridId}');}
                    },{
                        text:'全选',
                        handler:function(){selectAllColumn('${gridId}');}
                    },{
                        text:'保存(S)',
                        handler:function(){saveColumn('${gridId}');}
                    },{
                        text:'确认(O)',
                        handler:function(){confirmColumn('${gridId}');}
                    },{
                        text:'取消(C)',
                        handler:function(){
                            $('#${gridId}${dlgSuffix}').dialog('close');
                        }
                    }]
                ">
    <form id="${gridId}${formSuffix}" class="easyui-form" method="post" fit="true">
        <table id="${gridId}${tableSuffix}" width="620px">
        </table>
    </form>
</div>
<%
    }
%>

<!-- 数据列过滤器js -->
<script type="text/javascript" >
    $(function () {
        //初始化过滤表头
        function initFilterColumn(gridId){
            //先从后台获取当前用户的列数据
            var data = {};
            data["namespace"]=gridId;
            data["module"]='${_module}';
            data["userId"]='${userId}';
            $.ajax({
                type: "POST",
                url: "${contextPath}/userColumn/get.action",
                dataType: "json",
                data:data,
                processData:true,
                async : true,
                success: function (output) {
                    if(output.code=="200"){
                        var columnsArray = output.data;
                        if(!columnsArray || columnsArray==null){
                            return;
                        }
                        var fields = $('#'+gridId).datagrid('getColumnFields');
                        for(var i=0; i<fields.length; i++) {
                            var field = fields[i];
                            if($.inArray(field, columnsArray)>=0){
                                $('#'+gridId).datagrid('showColumn', field);
                            }else{
                                $('#'+gridId).datagrid('hideColumn', field);
                            }
                        }
                    }else{
                        $.messager.alert('错误',output.message);
                    }
                },
                error: function(err){
                    $.messager.alert('错误','远程访问失败');
                }
            });
        }

        <%
            for(var gridId in gridIds) {
            //去掉字符串两端空格
            gridId = strutil.trim(gridId);
            //避免空串
            if(strutil.length(gridId)<1){
                continue;
            }
        %>
        //迭代调用初始化grid列头过滤
        initFilterColumn("${gridId}");
        <%
        }
        %>
    });

    // ================================== 以下是直接声明的js函数 =====================================
    //判断是否绑定了快捷键
    var _isBindKeypress = false;
    <%
        for(var gridId in gridIds) {
        //去掉字符串两端空格
        gridId = strutil.trim(gridId);
        //避免空串
        if(strutil.length(gridId)<1){
            continue;
        }
    %>
    //迭代声明打开窗口事件，绑定快捷键
    function open${gridId}${dlgSuffix}(){
        if(!_isBindKeypress){
            $("body").bind('keypress',function(event){
                //按S键或s键，保存
                if(event.keyCode == "83" || event.keyCode == "115"){
                    saveColumn('${gridId}');
                }
                //按O键或o键，确认
                if(event.keyCode == "79" || event.keyCode == "111"){
                    confirmColumn('${gridId}');
                }
                //按C键或c键，取消
                if(event.keyCode == "67" || event.keyCode == "99"){
                    $('#${gridId}${dlgSuffix}').dialog('close');
                }

            });
            _isBindKeypress=true;
        }
    }
    <%
    }
    %>

    //反选列
    function invertColumn(gridId){
        //获取选择列表单中的已check的列的JSON信息
        var selectColumnFormData = $("#"+gridId+"${formSuffix}").serializeObject();
        //获取表格的所有列头名
        var fields = $('#'+gridId).datagrid('getColumnFields');
        var invertedColumnFormData = {};
        for(var i=0; i<fields.length; i++) {
            var field = fields[i];
            //如果已经选中该field字段，则取消选择
            var tmp = selectColumnFormData[gridId+"${fieldInfix}" + field];
            if(tmp && tmp != null){

            }else{ //设置反选字段
                invertedColumnFormData[gridId+"${fieldInfix}"+field] = "on";
            }
        }
        $("#"+gridId+"${formSuffix}").form("clear");
        $("#"+gridId+"${formSuffix}").form("load",invertedColumnFormData);
    }

    //全选列
    function selectAllColumn(gridId){
        var fields = $('#'+gridId).datagrid('getColumnFields');
        var selectColumnFormData = {};
        for(var i=0; i<fields.length; i++) {
            var field = fields[i];
            selectColumnFormData[gridId+"${fieldInfix}"+field] = "on";
        }
        $("#"+gridId+"${formSuffix}").form("load",selectColumnFormData);
    }

    //保存列
    function saveColumn(gridId){
        var colForm = confirmColumn(gridId);
        var columnArray = [];
        for(var i in colForm){
            columnArray.push(i);
        }
        var data = {};
        data["columns"] = columnArray.join(",");
        data["namespace"]=gridId;
        data["module"]='${_module}';
        data["userId"]='${userId}';
        $.ajax({
            type: "POST",
            url: "${contextPath}/userColumn/save.action",
            data: data,
            processData:true,
            dataType: "json",
            async : true,
            success: function (data) {
                if(data.code=="200"){
                    $.messager.alert('提示',data.message);
                }else{
                    $.messager.alert('错误',data.message);
                }
            },
            error: function(err){
                $.messager.alert('错误','远程访问失败');
            }
        });
    }

    //确认
    function confirmColumn(gridId){
        $("#"+gridId+"${dlgSuffix}").dialog('close');
        //表单需要去掉前缀gridId和中缀${fieldInfix}
        var colForm = removeKeyStartWith($("#"+gridId+"${formSuffix}").serializeObject(),gridId+"${fieldInfix}");
        var fields = $('#'+gridId).datagrid('getColumnFields');
        for(var i=0; i<fields.length; i++) {
            var field = fields[i];
            if(colForm[field]){
                $('#'+gridId).datagrid('showColumn', field);
            }else{
                $('#'+gridId).datagrid('hideColumn', field);
            }
        }
        return colForm;
    }

    //打开选择列窗口
    function openSelectColumnDlg(gridId){
        $("#"+gridId+"${dlgSuffix}").dialog('open');
        $("#"+gridId+"${dlgSuffix}").dialog('center');
        var fields = $('#'+gridId).datagrid('getColumnFields');
        var html = "";
        //清空table
        $("#"+gridId+"${tableSuffix}").html(html);
        //列索引(临时变量)
        var columnIndex = 0;
        //总列数
        var COLUMN_COUNT = 6;
        for(var i=0; i<fields.length; i++) {
            var field = fields[i];
            var columnOption = $('#${_gridId}').datagrid('getColumnOption',field);
            //COLUMN_COUNT列换行
            if(columnIndex % COLUMN_COUNT == 0){
                html += "<tr>";
            }
            if(columnOption.hidden) {
                html += "<td width='auto'><input type='checkbox' id='"+gridId+"${fieldInfix}" + field + "' name='"+gridId+"${fieldInfix}" + field + "' >" + columnOption['title'] + "</input></td>";
            }else{
                html += "<td width='auto'><input type='checkbox' id='"+gridId+"${fieldInfix}" + field + "' name='"+gridId+"${fieldInfix}" + field + "' checked='checked' >" + columnOption['title'] + "</input></td>";
            }
            if(columnIndex!=0 && ((columnIndex+1)%COLUMN_COUNT == 0 || i == fields.length-1)){
                html += "</tr>";
            }
            columnIndex++;
        }
        $("#"+gridId+"${tableSuffix}").append(html);
    }
</script>