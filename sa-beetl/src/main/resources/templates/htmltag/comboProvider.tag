<script type="text/javascript">
    <% if(has(_data)){ %>
    $("#${_id}").combobox({
        data:${_data}
        ,valueField:'${_valueField!"value"}'
        ,textField:'${_textField!"text"}'
    })
    <% }else{ %>
    var _comboProviderParamObj_${_id} = {};
    _comboProviderParamObj_${_id}.provider = '${_provider!"simpleValueProvider"}';
    //仅为simpleValueProvider传入默认参数
    if(_comboProviderParamObj_${_id}.provider == "simpleValueProvider") {
        _comboProviderParamObj_${_id}.queryParams = '${_queryParams!"{\}" }';
        _comboProviderParamObj_${_id}.valueField = '${_valueField!"value"}';
        _comboProviderParamObj_${_id}.textField = '${_textField!"text"}';
    }else{
        _comboProviderParamObj_${_id}.queryParams = '${_queryParams!"" }';
        _comboProviderParamObj_${_id}.valueField = '${_valueField!""}';
        _comboProviderParamObj_${_id}.textField = '${_textField!""}';
    }
    _comboProviderParamObj_${_id}.table = '${_table!""}';

    //注意，这里只能取到value属性中的值，而无法取到combobox的当前值，因为还没有渲染，渲染以后应该使用getValue方法取值
    _comboProviderParamObj_${_id}.value = $("#${_id}").val();
    $("#${_id}").combobox({
        url:"${contextPath}/provider/getLookupList.action"
        ,method:"POST"
        ,valueField:"value"
        ,textField:"text"
        ,queryParams:_comboProviderParamObj_${_id}
        <% if(has(_value)){ %>
        ,onLoadSuccess: function () {
            $(this).combobox('select', '${_value}');
        }
        <% }else if(has(_selectOne) && _selectOne == "true"){ %>
        ,onLoadSuccess: function () {
            onComboLoadSuccessSelectOne();
        }
        <% } %>
    })
    <% } %>
</script>