package com.sa.beetl.tag;

import com.google.common.collect.Lists;
import com.sa.util.AopTargetUtils;
import com.sa.util.POJOUtils;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.tag.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component("menubutton")
@ConditionalOnExpression("'${beetl.enable}'=='true'")
public class MenubuttonTag extends Tag {
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final String TAB = "    ";


	private final String ID_FIELD = "_idField";
	private final String TEXT_FIELD = "_textField";
	private final String PARENT_ID_FIELD = "_parentIdField";
	private final String ICON_CLS_FIELD = "_iconClsField";
	private final String DISABLED_FIELD = "_disabledField";
	private final String SERVICE = "_service";
	private final String METHOD = "_method";
	private final String QUERYPARAMS = "_queryParams";
	private final String DIV_ID = "_divId";
	private final String MENU_WIDTH = "_menuWidth";
	private final String MENU_HEIGHT = "_menuHeight";
	private final String PANEL_ALIGN = "_panelAlign";


	private final String ID_FIELD_DEFAULT = "id";
	private final String TEXT_FIELD_DEFAULT = "text";
	private final String PARENT_ID_FIELD_DEFAULT = "parentId";
	private final String ICON_CLS_FIELD_DEFAULT = "iconCls";
	private final String DISABLED_FIELD_DEFAULT = "disabled";
	private final String DIV_ID_DEFAULT = "_menubuttonDiv";
	private final String MENU_WIDTH_DEFAULT = "80";
	private final String MENU_HEIGHT_DEFAULT = "30";
	private final String PANEL_ALIGN_DEFAULT = "left";


	private final String ALIGN = "align";
	private final String MIN_WIDTH = "minWidth";
	private final String ITEM_WIDTH = "itemWidth";
	private final String ITEM_HEIGHT = "itemHeight";
	private final String DURATION = "duration";
	private final String HIDE_ON_UNHOVER = "hideOnUnhover";
	private final String INLINE = "inline";
	private final String FIT = "fit";
	private final String NOLINE = "noline";
	private final String ON_CLICK = "onClick";
	private final String PLAIN = "plain";
	private final String MENU_ALIGN = "menuAlign";
	private final String HAS_DOWN_ARROW = "hasDownArrow";


	private final String ICON_CLS = "iconCls";
	private final String DISABLED = "disabled";

	@Override
	public void render() {
		try {

			Map<String, Object> argsMap = (Map)this.args[1];
			String service = (String) argsMap.get(SERVICE);
			String method = (String) argsMap.get(METHOD);
			String queryParams = argsMap.get(QUERYPARAMS) == null ? null : String.valueOf(argsMap.get(QUERYPARAMS));

			if(StringUtils.isBlank(service) || StringUtils.isBlank(method)) {
				return;
			}
			Object serviceObj = AopTargetUtils.getTarget(SpringUtil.getBean(service));
			List list = (List)serviceObj.getClass().getMethod(method, String.class).invoke(serviceObj, queryParams);
			if(null == list || list.isEmpty()){
				return;
			}
			writeMenu(list, writeMenubutton(list));
		} catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private List writeMenubutton(List list){
		List rootList = null;
		boolean isMap = false;
		Map<String, String> argsMap = (Map)this.args[1];

		String textField = argsMap.get(TEXT_FIELD) == null ? TEXT_FIELD_DEFAULT : argsMap.get(TEXT_FIELD).toString();
		String idField = argsMap.get(ID_FIELD) == null ? ID_FIELD_DEFAULT : argsMap.get(ID_FIELD).toString();
		String parentIdField = argsMap.get(PARENT_ID_FIELD) == null ? PARENT_ID_FIELD_DEFAULT : argsMap.get(PARENT_ID_FIELD).toString();
		String iconClsField = argsMap.get(ICON_CLS_FIELD) == null ? ICON_CLS_FIELD_DEFAULT : argsMap.get(ICON_CLS_FIELD).toString();
		String disabledField = argsMap.get(DISABLED_FIELD) == null ? DISABLED_FIELD_DEFAULT : argsMap.get(DISABLED_FIELD).toString();
		String divId = argsMap.get(DIV_ID) == null ? DIV_ID_DEFAULT : argsMap.get(DIV_ID).toString();
		String menuWidth = argsMap.get(MENU_WIDTH) == null ? MENU_WIDTH_DEFAULT : argsMap.get(MENU_WIDTH).toString();
		String menuHeight = argsMap.get(MENU_HEIGHT) == null ? MENU_HEIGHT_DEFAULT : argsMap.get(MENU_HEIGHT).toString();
		String panelAlign = argsMap.get(PANEL_ALIGN) == null ? PANEL_ALIGN_DEFAULT : argsMap.get(PANEL_ALIGN).toString();
		if(Map.class.isAssignableFrom(list.get(0).getClass())){
			rootList = getMapRoots(list, parentIdField);
			isMap = true;
		}else{
			rootList = getBeanRoots(list, parentIdField);
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<div id=\""+divId+"\" class=\"easyui-panel\" align=\""+panelAlign+"\" style=\"padding:1px;\">"+ LINE_SEPARATOR);
		for(Object root : rootList) {

			if(getData(root, textField, isMap) == null || getData(root, idField, isMap) == null) {
				continue;
			}
			String id = getData(root, idField, isMap);
			String iconCls = getData(root, iconClsField, isMap);
			String disabled = getData(root, disabledField, isMap);

			if(!hasChild(list, id, isMap, parentIdField)){
				stringBuilder.append("<a id=\"menubutton_" + id + "\" href=\"#\" class=\"easyui-linkbutton\"  data-options=\"width:"+menuWidth+", height:"+menuHeight+", blankKey:''");
			}else{
				stringBuilder.append("<a id=\"menubutton_" + id + "\" href=\"#\" class=\"easyui-menubutton\"  data-options=\"width:"+menuWidth+", height:"+menuHeight+", menu:'#menu_" + id + "'");
			}

			if (argsMap.containsKey(PLAIN)) {
				stringBuilder.append(", plain:" + argsMap.get(PLAIN));
			}
			if (argsMap.containsKey(MENU_ALIGN)) {
				stringBuilder.append(", menuAlign:'" + argsMap.get(MENU_ALIGN) + "'");
			}
			if (argsMap.containsKey(DURATION)) {
				stringBuilder.append(", duration:" + argsMap.get(DURATION) );
			}
			if (argsMap.containsKey(HAS_DOWN_ARROW)) {
				stringBuilder.append(", hasDownArrow:" + argsMap.get(HAS_DOWN_ARROW) );
			}
			if(argsMap.containsKey(ON_CLICK)){
				stringBuilder.append(", onClick:"+argsMap.get(ON_CLICK));
			}
			if (StringUtils.isNotBlank(iconCls)) {
				stringBuilder.append(", iconCls:'" + iconCls + "'");
			}
			if (StringUtils.isNotBlank(disabled)) {
				stringBuilder.append(", disabled:" + disabled);
			}
			stringBuilder.append("," + getDataOptions(root, Map.class.isAssignableFrom(root.getClass())));
			stringBuilder.append("\">" + getData(root, textField, isMap) + "</a>"+ LINE_SEPARATOR);
		}
		stringBuilder.append("</div>"+ LINE_SEPARATOR);
		try {
			ctx.byteWriter.writeString(stringBuilder.toString() );
		} catch (IOException e) {
			e.printStackTrace();
		}

		removeListElement(list, rootList);
		return rootList;
	}


	private void writeMenu(List list, List rootList) throws IOException {

		if(list.isEmpty()) {
			return;
		}
		Map<String, String> argsMap = (Map)this.args[1];

		String textField = argsMap.get(TEXT_FIELD) == null ? TEXT_FIELD_DEFAULT : argsMap.get(TEXT_FIELD).toString();
		String idField = argsMap.get(ID_FIELD) == null ? ID_FIELD_DEFAULT : argsMap.get(ID_FIELD).toString();
		String parentIdField = argsMap.get(PARENT_ID_FIELD) == null ? PARENT_ID_FIELD_DEFAULT : argsMap.get(PARENT_ID_FIELD).toString();
		String iconClsField = argsMap.get(ICON_CLS_FIELD) == null ? ICON_CLS_FIELD_DEFAULT : argsMap.get(ICON_CLS_FIELD).toString();
		String disabledField = argsMap.get(DISABLED_FIELD) == null ? DISABLED_FIELD_DEFAULT : argsMap.get(DISABLED_FIELD).toString();
		StringBuilder stringBuilder = new StringBuilder();
		boolean isMap = false;
		if(Map.class.isAssignableFrom(list.get(0).getClass())){
			isMap = true;
		}
		for(Object root : rootList){
			String rootId = getData(root, idField, isMap).toString();

			if(!hasChild(list, rootId, isMap, parentIdField)){
				continue;
			}

			if(argsMap.containsKey(ITEM_WIDTH)){
				stringBuilder.append("<div id=\"menu_" + rootId + "\" style=\"width:"+argsMap.get(ITEM_WIDTH)+"px;\" data-options=\"zIndex:'110000' ");
			}else {
				stringBuilder.append("<div id=\"menu_" + rootId + "\" data-options=\"zIndex:'110000' ");
			}

			if(argsMap.containsKey(ALIGN)) {
				stringBuilder.append(", align:'" + argsMap.get(ALIGN) + "'");
			}
			if(argsMap.containsKey(MIN_WIDTH)){
				stringBuilder.append(", minWidth:"+argsMap.get(MIN_WIDTH));
			}
			if(argsMap.containsKey(ITEM_HEIGHT)){
				stringBuilder.append(", itemHeight:"+argsMap.get(ITEM_HEIGHT));
			}
			if(argsMap.containsKey(DURATION)){
				stringBuilder.append(", duration:"+argsMap.get(DURATION));
			}
			if(argsMap.containsKey(HIDE_ON_UNHOVER)){
				stringBuilder.append(", hideOnUnhover:"+argsMap.get(HIDE_ON_UNHOVER));
			}
			if(argsMap.containsKey(INLINE)){
				stringBuilder.append(", inline:"+argsMap.get(INLINE));
			}
			if(argsMap.containsKey(FIT)){
				stringBuilder.append(", fit:"+argsMap.get(FIT));
			}
			if(argsMap.containsKey(NOLINE)){
				stringBuilder.append(", noline:"+argsMap.get(NOLINE));
			}
			if(argsMap.containsKey(ON_CLICK)){
				stringBuilder.append(", onClick:"+argsMap.get(ON_CLICK));
			}
			stringBuilder.append("\">"+ LINE_SEPARATOR);

			Iterator it =list.listIterator();

			while(it.hasNext()) {
				Object row = it.next();

				if (getData(row, textField, isMap) == null || getData(row, idField, isMap) == null) {
					it.remove();
					continue;
				}
				Object parentId = getData(row, parentIdField, isMap);

				if (rootId.equals(parentId)) {
					appendMenuItem(stringBuilder, list, parentId, isMap, textField, idField, parentIdField, iconClsField, disabledField,  "", argsMap);
				}

			}
			stringBuilder.append("</div>"+ LINE_SEPARATOR);
		}
		ctx.byteWriter.writeString(stringBuilder.toString());
	}


	public void appendMenuItem(StringBuilder stringBuilder, List list, Object parentId, boolean isMap, String textField, String idField, String parentIdField, String iconClsField, String disabledField, String tab, Map<String, String> argsMap){
		Iterator it =list.listIterator();

		while(it.hasNext()) {
			Object row = it.next();
			Object currentParentId = getData(row, parentIdField, isMap);
			Object id = getData(row, idField, isMap);

			if(id == null){
				continue;
			}
			String iconCls = getData(row, iconClsField, isMap);
			String disabled = getData(row, disabledField, isMap);

			if (parentId.equals(currentParentId)) {

				if(argsMap.containsKey(ITEM_WIDTH)){

					Integer itemWidth = Integer.parseInt(argsMap.get(ITEM_WIDTH)) - 3;
					stringBuilder.append(tab).append(TAB).append("<div parentId=\""+parentId+"\" style=\"width:" + itemWidth + "px;\" id=\""+id+"\" data-options=\"blankKey:''");
				}else {
					stringBuilder.append(tab).append(TAB).append("<div parentId=\""+parentId+"\" id=\""+id+"\" data-options=\"blankKey:''");
				}
				stringBuilder.append("," + getDataOptions(row, isMap));
				if (iconCls != null) {
					stringBuilder.append(", iconCls:'" + iconCls + "'");
				}
				if (disabled != null) {
					stringBuilder.append(", disabled:" + disabled);
				}
				stringBuilder.append(" \">").append(LINE_SEPARATOR);
				stringBuilder.append(tab).append(TAB).append(TAB).append(getData(row, textField, isMap)).append(LINE_SEPARATOR);

				if (hasChild(list, id, isMap, parentIdField)) {
					if(argsMap.containsKey(ITEM_WIDTH)){
						stringBuilder.append(tab).append(TAB).append(TAB).append("<div style=\"width:" + argsMap.get(ITEM_WIDTH) + "px;\">").append(LINE_SEPARATOR);
					}else{
						stringBuilder.append(tab).append(TAB).append(TAB).append("<div>").append(LINE_SEPARATOR);
					}

					appendMenuItem(stringBuilder, list, id, isMap, textField, idField, parentIdField, iconClsField, disabledField, tab + TAB + TAB, argsMap);

					stringBuilder.append(tab).append(TAB).append(TAB).append("</div>").append(LINE_SEPARATOR);
					stringBuilder.append(tab).append(TAB).append("</div>").append(LINE_SEPARATOR);
					setData(row, "id", null , isMap);
				} else {

					stringBuilder.append(tab).append(TAB).append("</div>").append(LINE_SEPARATOR);

					setData(row, idField, null, isMap);
				}
			}
		}
	}


	private List deepCopyList(List src){
		List<String> dest = new ArrayList<String>(Arrays.asList(new String[src.size()]));
		Collections.copy(dest, src);
		return dest;
	}


	private boolean hasChild(List list, Object id, boolean isMap, String parentIdField){
		if(id == null){
			return false;
		}
		for(Object row : list){
			if(id.equals(getData(row, parentIdField, isMap))) {
				return true;
			}
		}
		return false;
	}


	private String getData(Object row, String key, Boolean isMap){
		Object data = isMap ? ((Map)row).get(key) : POJOUtils.getProperty(row, key);
		return data == null ? null : data.toString();
	}


	private String getDataOptions(Object row, Boolean isMap){
		StringBuilder stringBuilder = new StringBuilder();
		if(isMap){
			Map map = (Map) row;
			map.forEach((key, value) ->{
				if(value != null) {
					stringBuilder.append(", "+key+":'"+value.toString()+"'");
				}
			});
			return stringBuilder.substring(1, stringBuilder.length());
		}else{
			Method[] methods = row.getClass().getMethods();
			for(Method method : methods){

				if(POJOUtils.isGetMethod(method) && method.getParameters().length == 0){
					String fieldName = POJOUtils.getBeanField(method);
					try {
						Object value = getObjectStringValue(method.invoke(row));
						if(value != null){
							stringBuilder.append(", "+fieldName+":'"+value+"'");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return stringBuilder.substring(1, stringBuilder.length());
		}
	}


	private String getObjectStringValue (Object obj){
		if(obj == null) {
			return null;
		}
		if(obj instanceof Instant){

			return DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(((Instant)obj));
		}
		if(obj instanceof LocalDateTime){

			return ((LocalDateTime)obj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(obj instanceof Date){
			return sdf.format((Date)obj);
		}
		return String.valueOf(obj);
	}


	private void setData(Object row, String key, Object value,  Boolean isMap){
		if(isMap){
			((Map)row).put(key, value);
		}else{
			POJOUtils.setProperty(row, key, value);
		}
	}


	private List getMapRoots(List<Map> list, String parentIdField){
		List<Map> rootLists = Lists.newArrayList();
		for(Map map : list){
			Object parentId = getData(map, parentIdField, true);
			if(parentId == null || "".equals(parentId) || "-1".equals(parentId)){
				rootLists.add(map);
			}
		}
		return rootLists;
	}


	private List getBeanRoots(List list, String parentIdField){
		List rootLists = Lists.newArrayList();
		for(Object bean : list){
			Object parentId = getData(bean, parentIdField, false);
			if(parentId == null || "".equals(parentId) || "-1".equals(parentId)){
				rootLists.add(bean);
			}
		}
		return rootLists;
	}


	private static void removeListElement(List source, List dest){
		Iterator i = source.listIterator();
		while(i.hasNext()){
			Object o = i.next();
			for(Object s : dest){
				if(o.equals(s)){
					i.remove();
				}
			}
		}
	}


	private static void removeListElement(List source, Object ... dest){
		Iterator i = source.listIterator();
		while(i.hasNext()){
			Object o = i.next();
			for(Object s : dest){
				if(o.equals(s)){
					i.remove();
				}
			}
		}
	}
}
