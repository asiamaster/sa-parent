<script type="text/javascript">
    //bean对象的fieldMeta
    <#fieldMeta dtoClass="${dtoClass}"/>
        //fieldEditor的对应关系
        <#fieldEditor varName="fieldEditor" />

    var conditionValueFieldInput = '<input name="conditionValueField" id="conditionValueField" data-options="label:\'条件值:\',required:false, width:253, onLoadSuccess:conditionValueFieldLoadSuccess" />';

    //选择关系下拉时切换条件值控件
    function onChangeRelation(newValue, oldValue){
        if(newValue == "Is" || newValue == "IsNot"){
            $("#conditionValueField").textbox({
                value : "空",
                readonly : true
            });
        }else {
            renderConditionValue($("#conditionField").combobox("getValue"));
        }
    }
    //选择条件项下拉时切换条件值控件
    function onChangeCondition(newValue, oldValue){
        //先将关系选中第一个，避免上次选中非后和控件冲突
        var relationFieldData =  $("#relationField").combobox("getData");
        $("#relationField").combobox("select", relationFieldData[0]["value"]);
//        根据条件项渲染条件值控件
        renderConditionValue(newValue);
    }

    /**
     * 私有方法
     * 根据条件项渲染条件值控件
     * @param val
     */
    function renderConditionValue (val){
        var newValueFieldMeta = ${@com.sa.ss.beetl.FieldMetaTag.getVarName(dtoClass)}[val];
        var editor = "";
        //没有meta信息，默认为textbox
        if(!newValueFieldMeta || newValueFieldMeta == null ){
            editor = "textbox";
        }else{
            editor = fieldEditor[newValueFieldMeta["editor"]];
        }
        $("#conditionValueField").textbox("destroy");
        //conditionValueFieldDiv是外层div，该id将作为标签的参数
        $("#${divId!"conditionValueFieldDiv"}").append(conditionValueFieldInput);
        if(editor == 'combobox') {
            //如果没有初始化json参数，直接调用provider
            if(newValueFieldMeta["params"] == ""){
                $("#conditionValueField").combobox({
                    url:"${contextPath}/provider/getLookupList.action"
                    , method : "POST"
                    , valueField : "value"
                    , textField : "text"
                    , panelHeight : "auto"
                    , selectOnNavigation : true //定义是否允许使用键盘导航来选择项目
                    , editable : false //定义用户是否可以直接输入文本到字段中
                    , queryParams : {provider : newValueFieldMeta["provider"]} //从meta中取当前下拉框字段的提供者
                });
            } else {//有初始化json参数，构建动态或静态查询下拉框
                var params = $.parseJSON(newValueFieldMeta["params"]);
                if(params["data"] != null){
                    $("#conditionValueField").combobox({
                        data:params["data"]
                        ,valueField:'${_valueField!"value"}'
                        ,textField:'${_textField!"text"}'
                    })
                }else if (params["table"] != null){
                    var _comboProviderParamObj_conditionValueField = {};
                    _comboProviderParamObj_conditionValueField.queryParams = JSON.stringify(params["queryParams"]);
                    _comboProviderParamObj_conditionValueField.valueField = params["valueField"]||"value";
                    _comboProviderParamObj_conditionValueField.textField = params["textField"]||"text";
                    _comboProviderParamObj_conditionValueField.table = params["table"] || "";
                    _comboProviderParamObj_conditionValueField.provider = params["provider"] || "simpleValueProvider";
                    $("#conditionValueField").combobox({
                        url:"${contextPath}/provider/getLookupList.action"
                        ,method:"POST"
                        ,valueField:"value"
                        ,textField:"text"
                        ,editable : false
                        ,queryParams:_comboProviderParamObj_conditionValueField
                    })
                } else { //既没有data，又没有table参数，就调注解配置的provider,并传入初始化参数
                    //从meta中取当前下拉框字段的提供者
                    params["provider"] = newValueFieldMeta["provider"];
                    $("#conditionValueField").combobox({
                        url:"${contextPath}/provider/getLookupList.action"
                        , method : "POST"
                        , valueField : "value"
                        , textField : "text"
                        , panelHeight : "auto"
                        , selectOnNavigation : true //定义是否允许使用键盘导航来选择项目
                        , editable : false //定义用户是否可以直接输入文本到字段中
                        , queryParams : params
                    });
                }
            }

        } else if(editor == "textbox"){
            $("#conditionValueField").textbox({
            });
        } else if(editor == "numberbox"){
            $("#conditionValueField").numberbox({
            });
        } else if(editor == "datetimebox"){
            $("#conditionValueField").datetimebox({
            });
        } else if(editor == "datebox"){
            $("#conditionValueField").datebox({
            });
        }
    }

    //条件值加载成功后默认选中第一项
    function conditionValueFieldLoadSuccess(){
        $("#conditionValueField").combobox("select", $("#conditionValueField").combobox("getData")[0]["value"]);
    }
    //添加条件项
    function addConditionItem(){
        //获取条件字段名
        var conditionFieldValue = $("#conditionField").combobox("getValue");
        //获取条件关系
        var relationFieldValue = $("#relationField").combobox("getValue");
        ////获取条件值
        var conditionValueFieldValue = $("#conditionValueField").textbox("getValue");
        //获取条件字段名
        var conditionFieldText = $("#conditionField").textbox("getText");
        //获取条件关系
        var relationFieldText = $("#relationField").textbox("getText");
        ////获取条件值
        var conditionValueFieldText = $("#conditionValueField").textbox("getText");
        var colonEncode = '${@com.sa.ss.constant.SsConstants.COLON_ENCODE}';
        var valueEncode = conditionFieldValue+":"+relationFieldValue+":"+conditionValueFieldValue.replace(new RegExp(":","gm"),colonEncode);
        $("#conditionItemsList").datalist("appendRow", {text: conditionFieldText+" "+relationFieldText+" "+conditionValueFieldText, value:valueEncode});
    }
    //双击条件列表时移除选中项
    //         index：点击的行的索引值，该索引值从0开始。
    //         row：对应于点击行的记录。
    function conditionItemsListDblClickRow(index, row){
        $("#conditionItemsList").datalist("deleteRow", index);
    }
    //移除条件项
    function removeConditionItem(){
        var selected = $("#conditionItemsList").datalist("getSelected");
        if(!selected || null == selected) {
            return;
        }
        $("#conditionItemsList").datalist("deleteRow",$("#conditionItemsList").datalist("getRowIndex", selected));
    }

    //根据动态查询条件查询表格
    function queryGridByConditionItems() {
        var opts = $("#${gridId}").datagrid("options");
//        if (null == opts.url || "" == opts.url) {
        opts.url = "${contextPath}/common/listEasyuiPageByConditionItems.action";
//        }
        var param = bindMetadata("${gridId}", true);
        //获取条件项关系
        var conditionRelationFieldValue = $("input[name='conditionRelationField']:checked").val();
        var rows = $("#conditionItemsList").datalist("getRows");
        var conditionItemsArray = new Array();
        for(var row in rows){
            conditionItemsArray.push(rows[row]["value"]);
        }
        var conditionParams = {dtoClass:"${dtoClass}", conditionRelationField:conditionRelationFieldValue, conditionItems:conditionItemsArray.join(",")};
        $.extend( conditionParams, param);
        $("#${gridId}").datagrid("load", conditionParams);
//             $.ajax({
//                 type:'POST',
//                 url:'${contextPath}/scheduleJob/listPageByConditionItems',
//                 data:JSON.stringify(conditionParams),
//                 contentType: 'application/json',
//                 success:function(data){
//                    $("#grid").datagrid("loadData", $.parseJSON(data));
//                 }
//             })
        $("#queryDlg").dialog("close");
    }

    //打开查询窗口
    function openQueryDlg(){
        $('#queryDlg').dialog('open');
        $('#queryDlg').dialog('center');
        selectOne();
    }

    //选中第一个元素
    function selectOne(){
        var conditionFieldData = $("#conditionField").combobox("getData");
        var relationFieldData =  $("#relationField").combobox("getData");
        $("#conditionField").combobox("select", conditionFieldData[0]["value"]);
        $("#relationField").combobox("select", relationFieldData[0]["value"]);
        //由于radio在隐藏的dlg中无法通过html属性默认选中，只能在打开dlg时选中了
        $("#conditionRelationFieldAnd").prop("checked", "checked");
    }
</script>

<!-- 隐藏编辑框 -->
<div id="queryDlg" class="easyui-dialog" resizable="false" title="查询条件设置" constrain="true" shadow="true" draggable="false" style="padding:20px" modal="true" border="thin" closed="true"
     data-options="
				iconCls: 'icon-save',
				buttons: [{
					text:'确定',
					iconCls:'icon-ok',
					handler:queryGridByConditionItems
				},{
					text:'取消',
					handler:function(){
						$('#queryDlg').dialog('close');
					}
				}]
			">
    <form id="_conditionForm" class="easyui-form" method="post" fit="true">
        <table width="600">
            <tr>
                <td width="280" style="padding:5px;">
                    <input name="conditionField" id="conditionField" data-options="onChange:onChangeCondition, editable:false, width:250" panelWidth="auto" panelHeight="auto" label="条件项:" />
                    <#comboProvider _id="conditionField" _provider="beanFieldProvider" _queryParams='${dtoClass}' />
                </td>
                <td width="40">
                    <a id="addBtn" width="40" href="#" onClick="addConditionItem()" class="easyui-linkbutton" data-options="iconCls:'icon-add'"></a>
                </td>
                <td rowspan="4" width="280">
                    <ul class="easyui-datalist" id="conditionItemsList" data-options="onDblClickRow:conditionItemsListDblClickRow" lines="true" style="width:255px;height:160px">
                    </ul>
                </td>
            </tr>
            <tr>
                <td style="padding:5px;">
                    <input name="relationField" id="relationField" data-options="label:'关系:', editable:false, width:250, onChange:onChangeRelation" required="true" />
                    <#comboProvider _id="relationField" _data='[{"text":"等于","value":"Equal"}, {"text":"不等于","value":"NotEqual"}, {"text":"大于","value":"GreatThan"}, {"text":"大于等于","value":"GreatEqualThan"}, {"text":"小于","value":"LittleThan"}, {"text":"小于等于","value":"LittleEqualThan"}, {"text":"匹配","value":"Match"}, {"text":"不匹配","value":"NotMatch"}, {"text":"是","value":"Is"}, {"text":"非","value":"IsNot"}]' />
                </td>
                <td>
                    <a id="deleteBtn" width="40" href="#" onclick="removeConditionItem()" class="easyui-linkbutton" data-options="iconCls:'icon-remove'"></a>
                </td>
            </tr>
            <tr>
                <td style="padding:5px;">
                    <div id="${divId!"conditionValueFieldDiv"}"></div>
                </td>
                <td></td>
            </tr>
            <tr>
                <td colspan="2">
                    <p style="font-size: 14px;">条件项关系:</p>&nbsp;&nbsp;&nbsp;
                    <input type="radio" id="conditionRelationFieldAnd" name="conditionRelationField" value="and" checked="checked" > 全部"与" </input>
                    <input type="radio" id="conditionRelationFieldOr" name="conditionRelationField" value="or"> 全部"或" </input>
                    <input type="radio" id="conditionRelationFieldNone" name="conditionRelationField" value="none"> 无条件 </input>
                </td>
            </tr>
        </table>
    </form>
</div>