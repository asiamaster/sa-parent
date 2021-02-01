
            <%
                //tag表示当前标签，chidren属性获得下级标签
                var models = tag.children;
                for(model in models){
                   // print(model.execute);
                    %>
                    ${model.execute}
                    <%
                } //for model end
            %>
</table>