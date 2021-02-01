<script type="text/javascript">

//判断当前字符串是否以str开始 先判断是否存在function是避免和js原生方法冲突，自定义方法的效率不如原生的高
if (typeof String.prototype.startWith != 'function') {
    String.prototype.startWith = function (str) {
        return this.slice(0, str.length) == str;
    };
}

//判断当前字符串是否以str结束
if (typeof String.prototype.endWith != 'function') {
    String.prototype.endWith = function (str) {
        return this.slice(-str.length) == str;
    };
}

// 清除两边的空格
if (typeof String.prototype.trim != 'function') {
    String.prototype.trim = function () {
        return this.replace(/(^\s*)|(\s*$)/g, '');
    };
}

//删除json对象key中的开始字符串,
// 如var json = {_id:1, _name:"value"};
// 调用removeByStart(json, "_")
// 结果是:{id:1, name:"value"};
function removeKeyStartWith(json, startStr) {
    for (key in json) {
        if (key.startWith(startStr)) {
//如果已有remove掉startStr后的同名属性,则跳过，并且移除key
            if (json.hasOwnProperty(key.slice(startStr.length))) {
                delete json[key];
                continue;
            }
            json[key.slice(startStr.length)] = json[key];
            delete json[key];
        }
    }
    return json;
}
//为json对象key中添加开始字符串,如果已经是以startStr开始，则跳过
// 主要是为了获取下拉框等有provider的字段的原值
// 如var json = {id:1, name:"value"};
// addKeyStartWith(json, "_")
// 结果是:{_id:1, _name:"value"};
function addKeyStartWith(json, startStr) {
    for (key in json) {
        if (key.startWith(startStr)) {
            continue;
        }
//如果已有add startStr后的同名属性,则跳过，并且移除key
        if (json.hasOwnProperty(startStr + key)) {
            delete json[key];
            continue;
        }
        json[startStr + key] = json[key];
        delete json[key];
    }
    return json;
}
//表单jquery对象获取提交字段的json信息
$.fn.serializeObject = function (containsNull) {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function () {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push && o[this.name] != null && o[this.name] != "") {
                o[this.name] = [o[this.name]];
                o[this.name].push(this.value || '');
            }else if(this.value != null){
                o[this.name].push(this.value || '');
            }else{
                if(containsNull && containsNull == true){
                    o[this.name].push('');
                }
            }
        } else {
            if(this.value != null && this.value != ""){
                o[this.name] = this.value || '';
            }else{
                if(containsNull && containsNull == true) {
                    o[this.name] = '';
                }
            }
        }
    });
    return o;
};

//从后台获取原始值的key的前缀
var orginal_key_prefix = '${@com.sa.ss.metadata.ValueProviderUtils.ORIGINAL_KEY_PREFIX}';

//获取datagrid行数据中的原始值(有orginal_key_prefix开头的key的值)，用于form load
function getOriginalData(json) {
    var obj = {};
    for (key in json) {
        if (key.startWith(orginal_key_prefix)) {
            continue;
        }
//如果已有orginal_key_prefix为前缀的同名原始属性,则使用原始属性
        if (json.hasOwnProperty(orginal_key_prefix + key)) {
            obj[key] = json[orginal_key_prefix + key];
        } else {
            obj[key] = json[key];
        }
    }
    return obj;
}

//表格表头右键菜单
var cmenu = null;
function createColumnMenu(gridId){
    cmenu = $('<div/>').appendTo('body');
    cmenu.menu({
        onClick: function(item){
            if (item.iconCls == 'icon-ok'){
                $('#'+gridId).datagrid('hideColumn', item.name);
                cmenu.menu('setIcon', {
                    target: item.target,
                    iconCls: 'icon-empty'
                });
            } else {
                $('#'+gridId).datagrid('showColumn', item.name);
                cmenu.menu('setIcon', {
                    target: item.target,
                    iconCls: 'icon-ok'
                });
            }
        }
    });
    var fields = $('#'+gridId).datagrid('getColumnFields');
    for(var i=0; i<fields.length; i++){
        var field = fields[i];
        var col = $('#'+gridId).datagrid('getColumnOption', field);
        cmenu.menu('appendItem', {
            text: col.title,
            name: field,
            iconCls: 'icon-ok'
        });
    }
}

</script>