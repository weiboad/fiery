<!DOCTYPE html>
<html>
<head>
    <title>Ragnar分布式调试跟踪系统</title>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <link rel="stylesheet" href="css/bootstrap.min.css"/>
    <link rel="stylesheet" href="css/bootstrap-theme.min.css"/>
    <link rel="stylesheet" href="css/jsoneditor.min.css"/>

    <script src="js/jquery.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script src="js/jsoneditor.min.js"></script>

    <style type="text/css">
        .sorttable th[data-sort] {
            cursor: pointer;
        }

        .sorttable tr:nth-of-type(odd) {
            background: #FFFFFF;
        }

        .sorttable tr:nth-of-type(even) {
            background: #d9edf7;
        }

    </style>

</head>
<body>
<#include "common.ftl">
<#include "header.ftl">

<div class="col-md-12">
    <h4>Traceid信息</h4>
    <form method="get" action="?" name="form1" style="margin:0;padding:0;display: inline">
        <table class="table table-hover">
            <tr>
                <th>机房</th>
                <th>IP</th>
                <th>Date</th>
                <th>Time</th>
                <th>Traceid</th>
                <th>OP</th>
            </tr>
            <tr>
                <td>${idc}</td>
                <td>${ip}</td>
                <td>${starttimedate?string("yyyy-MM-dd HH:mm:ss")}</td>
                <td>${starttime}</td>
                <td>
                    <input type="input" value="${traceid}" name="traceid" id="traceidinput" style="width: 100px;"/>
                </td>
                <td>
                    <input type="submit" value="回放" name="submit" class="btn btn-primary btn-sm"/>
                </td>
            </tr>
        </table>

    </form>
</div>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">

        </div>
        <div class="col-md-12">
            <div class="form-group">
                <h4>调用关系

                </h4>

                <div class="form-group" style="overflow-y: scroll">
                    <table class="table table-hover" style="width: 1600px">
                        <tr>
                            <th>URL <#if oldstyle == "1">
                                <a href="?traceid=${traceid}&rpcid=0&oldstyle=0" type="button" class="small"
                                   style="color: red;">兼容模式</a>
                            <#else>
                                <a href="?traceid=${traceid}&rpcid=0&oldstyle=1" type="button" class="small"
                                   style="color: blue;">递归渲染</a>
                            </#if></th>
                            <th>RPCID</th>
                            <th>Type/Code</th>
                            <th>响应(ms)</th>
                            <th>TimeLine</th>
                            <th>Server</th>
                            <th>Project</th>
                        </tr>
                        <!--调用关系及timeline列表-->
                    <#list tracelist as itemrpcid,item>
                    ${showInterTimeWarning(item.logintertimewarning)}
                        <tr class="${showSelectedTr(item.selected)}">
                            <td style="width:600px;word-break:break-all;">
                            ${item.indent}
                                <#if item.tag == "">
                                    <span name="showdiv" class="glyphicon glyphicon-minus"
                                          refid="${item.rpcid}"></span>
                                </#if>
                            ${showTag(item.tag)}
                                <a href='?traceid=${traceid}&rpcid=${item.rpcid}#logindexa_${item.logindex}'>
                                ${item.url}
                                </a>
                            ${showTimeOutTag(item.istimeout)}
                            ${showSelected(item.selected)}
                            </td>
                            <td style="width:50px;word-break:break-all;">${item.r}</td>
                            <td style="width: 100px;">
                                <#if item.type == 9>
                                    ${item.tag}
                                <#else>
                                ${showLogTypeName(item.type)}
                                </#if>
                            </td>
                            <td style="width: 100px;">${showTagCostTime(item.elapsed)}</td>
                            <td style="width: 500px;">${showTimeLine(item.timeline_startbar,item.timeline_interbar,item.timeline_costbar,item.timeline_interbar_title,item.timeline_costbar_title)}</td>
                            <td style="width: 100px;">${item.ip}</td>
                            <td style="width: 100px;">${item.project}</td>
                        </tr>

                    </#list>
                        <tr>
                            <td colspan="7">
                                <div
                                        style="width: 100px; float: right;line-height:20px;font-size:10px;text-align: center;">
                                    &nbsp;
                                </div>
                                <div
                                        style="width: 200px;background-color: #337ab7;color: white; float: right;line-height:20px;font-size:10px;text-align: center;">
                                    此埋点之前间隙消耗时间
                                </div>
                                <div
                                        style="width: 200px;background-color: #5cb85c;color: white; float: right;line-height:20px;font-size:10px;text-align: center;">
                                    埋点响应时间
                                </div>
                                <div
                                        style="width: 200px;background-color: #d9534f;color: white; float: right;line-height:20px;font-size:10px;text-align: center;">
                                    用埋点响应时间
                                </div>

                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            <div class="form-group">
                <h4>选中API请求参数</h4>
                <div class="form-group">
                    <div id="jsondiv_param"></div>
                    <script>
                        var options = {
                            "mode": "tree",
                            "search": false,
                            "history": false,
                        };
                        var container = document.getElementById("jsondiv_param");
                        var editor = new JSONEditor(container, options);

                        editor.set(${parameter});
                        editor.expandAll();
                    </script>
                </div>
            </div>
            <div class="table-responsive">
                <h4>选中API相关日志</h4>
                <table class="table table-hover table-condensed">
                <#list loglist as itemrpcid,log>
                ${showLogInterTimeWarning(log.logintertimewarning?eval)}
                    <#if log.t=="1" || log.t=="2" || log.t=="3"|| log.t=="4" || log.t=="5" || log.t=="6" || log.t=="7">
                        <tr>
                            <td>
                                <a name="logindexa_${log.logindex}"></a>
                                <label class='label label-warning'>${showLogTypeName(log.t)}</label>
                                &nbsp;
                                <label class='label label-primary'>RPCID:${log.r}</label>
                                &nbsp;
                                <label class='label label-info'>${log.g}</label>
                                ${(log.e?eval*1000)?number_to_datetime?string("yyyy-MM-dd hh:mm:ss.ms")} ${log.p} (${log.l})
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div id="jsonlog_${log?index}"></div>
                                <script>
                                    try{
                                        var options = {
                                            "mode": "tree",
                                            "search": false,
                                            "history": false,
                                        };
                                        var container = document.getElementById("jsonlog_${log?index}");
                                        var editor = new JSONEditor(container, options);
                                        editor.set(${log.m});
                                        editor.expandAll();
                                    }catch(error){
                                        document.write('${log.m}');
                                    }
                                </script>
                            </td>
                        </tr>
                    <#elseif log.t=="8">
                        <tr>
                            <td>
                                <a name="logindexa_${log.logindex}"></a>
                                <label class='label label-warning'>${showLogTypeName(log.t)}</label>
                                &nbsp;
                                <label class='label label-primary'>RPCID:${log.r}</label>
                            </td>
                        </tr>
                        <tr>
                            <td>
                            ${log.m}
                            </td>
                        </tr>
                    <#elseif log.t=="9">
                        <tr>
                            <td>
                                <a name="logindexa_${log.logindex}"></a>
                                <label class='label label-warning'>${showLogTypeName(log.t)}</label>
                                &nbsp;
                                <label class='label label-primary'>RPCID:${log.r}</label>
                                &nbsp;
                                <label class='label label-info'>${log.g}</label>
                                &nbsp;
                            ${(log.e?eval * 1000)?number_to_datetime} ${showTagCostTime(log.c)} ${log.p} (${log.l})
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div id="jsonlog_${log?index}"></div>
                                <script>
                                    try{
                                        var options = {
                                            "mode": "tree",
                                            "search": false,
                                            "history": false,
                                        };
                                        var container = document.getElementById("jsonlog_${log?index}");
                                        var editor = new JSONEditor(container, options);
                                        editor.set(${log.m});
                                        editor.expandAll();
                                    }catch(error){
                                        document.write('${log.m}');
                                    }
                                </script>
                            </td>
                        </tr>
                    </#if>
                </#list>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        $("span[name='showdiv']").click(function () {
            var divid = "[name='logtr_" + $(this).attr("refid") + "']";
            console.log(divid);
            if ($(this).hasClass("glyphicon-plus")) {
                $(this).removeClass("glyphicon-plus");
                $(this).addClass("glyphicon-minus");
                $(divid).each(function (i) {
                    $(this).show();
                });
                return;
            }
            if ($(this).hasClass("glyphicon-minus")) {
                $(this).removeClass("glyphicon-minus");
                $(this).addClass("glyphicon-plus");
                $(divid).each(function (i) {
                    $(this).hide();
                });
                return;
            }

        });
    });
</script>
<#include "footer.ftl">
</body>
</html>