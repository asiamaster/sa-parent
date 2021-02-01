<script type="text/javascript">
    /**
     * 支持在easyui的form中回车键代替tab键
     * 参数:_formId, _focusId, _submitFun
     */
    $(function () {
        //模拟按tab键的方法
        function tab${_formId}(event) {
            var inputs = $("#${_formId}").find("input[type=text]"); // 获取easyui表单中的所有元素(easyui会把表单元素都体现到input type="text"上，而原有元素不一定会显示)
            var index = inputs.index(event.target); // 获取当前焦点输入框所处的位置
            event.preventDefault(); //停止系统事件
            if (index == inputs.length - 1) // 判断是否是最后一个输入框
            {
//                    if (confirm("最后一个输入框已经输入,是否提交?")) // 用户确认
                <%
                if(has(_submitFun)){
                %>
                ${_submitFun}(); // 提交表单
                <%
                }
                %>
            } else {
                inputs[index + 1].focus(); // 设置焦点
                inputs[index + 1].select(); // 选中文字
            }
            return false;// 取消默认的提交行为
        };
        //绑定页面回车事件，以及初始化页面时的光标定位
        bindFormEvent("${_formId}", "${_focusId!}", tab${_formId});
    })
</script>