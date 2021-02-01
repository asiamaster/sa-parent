<style type="text/css">
    /*遮照样式*/
    .window-mask-opaque {
        background-color: #FFF;
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        filter: alpha(opacity=40);
        opacity:1;
        font-size: 1px;
        overflow: hidden;
    }
</style>
<script type="text/javascript">
$.messager.progress({
    title : "提示",
    msg : "加载中,请稍候...",
    text : '{value}%',
    interval : 300
});
$(".window-mask").toggleClass("window-mask-opaque");
$.parser.onComplete = function() {
    $.messager.progress("close");
    $(".window-mask-opaque").toggleClass("window-mask");
}
</script>