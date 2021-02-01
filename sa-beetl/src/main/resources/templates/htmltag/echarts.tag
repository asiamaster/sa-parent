<script type="text/javascript">
    var ${_id};
    $(function(){
    /**
     * 渲染echarts图表
     *  _id 容器id， 必填
     *  _type 图表类型， 必填 (Bar, Line, Pie)
     *  _url 必填
     *  _title 必填
     *  _subTitle 选填
     *  _queryParams 查询json参数, 选填
     *  _nameField 选填
     *  _valueField 选填
     *  _groupField 选填
     */
    var _chartsParamObj = ${_queryParams!"{\}" };
    var _chartsNameField = '${_nameField!"name"}';
    var _chartsValueField = '${_valueField!"value"}';
    var _chartsGroupField = '${_groupField!"group"}';
    //_id参数必填
    var _chartsId = '${_id}';
    //标题参数必填
    var _chartsTitle = '${_title}';
    //副标题参数选填
    var _chartsSubTitle = '${_subTitle!}';
    //其它图表参数
    var _options = ${_options!"{\}" };
    //平滑曲线
    var _smooth = ${_smooth!true};
    <% if(has(_data) && _data != null){%>
    //_data参数和_url必填一个
    var _chartsData = ${_data!};
    var opt;
    <% if(_type!=null && _type=="Pie"){ %>
    opt = MyECharts.ChartOptionTemplates.Pie(_chartsTitle, _chartsSubTitle, _chartsData, _chartsNameField, _chartsValueField, _options);
    <% } else if (_type!=null && _type=="Line"){ %>
    opt = MyECharts.ChartOptionTemplates.Line(_chartsTitle, _chartsSubTitle, _chartsData, _chartsNameField, _chartsValueField, _chartsGroupField, _options, _smooth);
    <% } else if (_type!=null && _type=="Bar"){ %>
    opt = MyECharts.ChartOptionTemplates.Bar(_chartsTitle, _chartsSubTitle, _chartsData, _chartsNameField, _chartsValueField, _chartsGroupField, _options);
    <% } else{ %>
    opt = {};
    <% } %>
    ${_id} = MyECharts.RenderChart(opt, _chartsId);
    <%}else{%>
    //_url参数和_data必填一个
    var _chartsUrl = '${_url!}';
    $.ajax({
        type: "POST",
        url: _chartsUrl,
        data: _chartsParamObj,
        dataType: "json",
        success: function(data){
            //判断如果是BaseOutput，就再取data
            if(data.code != null && data.message != null){
                if(data.code != "200"){
                    alert(data.message);
                    return;
                }
                data = data.data;
            }
            if(data == null || data == ""){
                data = [];
                chartObj.clear();
            }
            var opt;
            <% if(_type!=null && _type=="Pie"){ %>
                opt = MyECharts.ChartOptionTemplates.Pie(_chartsTitle, _chartsSubTitle, data, _chartsNameField, _chartsValueField, _options);
            <% } else if (_type!=null && _type=="Line"){ %>
                opt = MyECharts.ChartOptionTemplates.Line(_chartsTitle, _chartsSubTitle, data, _chartsNameField, _chartsValueField, _chartsGroupField, _options, _smooth);
            <% } else if (_type!=null && _type=="Bar"){ %>
                opt = MyECharts.ChartOptionTemplates.Bar(_chartsTitle, _chartsSubTitle, data, _chartsNameField, _chartsValueField, _chartsGroupField, _options);
            <% } else{ %>
                opt = {};
            <% } %>
            ${_id} = MyECharts.RenderChart(opt, _chartsId);
        },error: function () {
            alert('远程访问失败'+textStatus);
        }
    });
    <%}%>
//    var opt3 = MyECharts.ChartOptionTemplates.Bar("各个城市气温趋势", "°C", data3, _chartsNameField, _chartsValueField, _chartsGroupField);
//    var opt2 = MyECharts.ChartOptionTemplates.Pie("浏览器占比例", "%", data1, _chartsNameField, _chartsValueField, _chartsGroupField);
//    var opt1 = MyECharts.ChartOptionTemplates.Line("各个城市气温趋势", "°C", data2, _chartsNameField, _chartsValueField, _chartsGroupField);
//    var myChart = MyECharts.RenderChart(opt1, "chart");
    });
</script>