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
    <script src="js/stupidtable.min.js"></script>
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
<#function renderLog val,type,id>
    <#return
    '<tr class="info">
        <td>
            <label class="label label-info">开始时间: </label>${val.starttime}
            <label class="label label-info">最后更新: </label>${val.endtime}
            <label class="label label-info">捕获次数: </label>${val.count}
            <form method="get" action="showtrace" target="_blank"
                  style="margin:0;display:inline-block; width: 120px;float: right;">
                <input type="hidden" name="traceid" value="${val.oldtraceid}"/>
                <input type="hidden" name="key" value="${val.oldrpcid}"/>
                <input type="submit" name="submit" value="首次调用日志" class="btn btn-primary"
                       style="float: right"/>
            </form>
            <form method="get" action="showtrace" target="_blank"
                  style="margin:0;display:inline-block; width: 120px;float: right;">
                <input type="hidden" name="traceid" value="${val.newtraceid}"/>
                <input type="hidden" name="key" value="${val.newrpcid}"/>
                <input type="submit" name="submit" value="最新调用日志" class="btn btn-primary"
                       style="float: right"/>
            </form>
            <form method="get" action="errorstatic/del"
                  style="margin:0;display: inline-block; width: 100px;float: right;"
                  onsubmit="return checkContinue()">
                <input type="hidden" name="hashcode" value=\"${id}\" />
                <input type="hidden" name="type" value=\"${type}\" />
                <input type="hidden" name="daytime" value=\"${datelist_selected}\" />
                <input type="submit" name="submit" value="清除日志" class="btn btn-danger" style="float: right"/>
            </form>
        </td>
    </tr>
    <tr >
        <td>
            <div id="jsondiv_${id}"></div>
                <script>
                    var options = {
                        "mode": "tree",
                        "search": false,
                        "history": false,
                    };
                    var container = document.getElementById("jsondiv_${id}");
                    var editor = new JSONEditor(container, options);
                    editor.set(${val.content});
                    editor.expandAll();
                </script>
        </td>
    </tr>'>
</#function>
<form action="?" method="get" class="form-horizontal" id="message" accept-charset="utf-8">
    <div style="float:right;width: 160px;">
        <label class="control-label">时间范围:</label>
        <select name="daytime" class="input-sm" id="datarange">
        <#list datelist as dateitem>
            <option value="${dateitem?index}">${dateitem}</option>
        </#list>
        </select>
    </div>
</form>
<div class="col-md-12" style="min-height: 850px;">
    <div class="table-responsive">
        <h4>紧急警报日志(${alarm_count})</h4>
        <table class="table table-hover table-condensed">
        <#--<#if alarm>-->
                <#list alarm as key,val>
        ${renderLog(val,6,key)}
        </#list>
            <#--<#else>
                <tr><td>服务暂未开通...</td></tr>
            </#if>-->
        </table>
        <h4>异常日志(${exception_count})</h4>
        <table class="table table-hover table-condensed">
        <#--<#if exception>-->
            <#list exception as key,val>
        ${renderLog(val,7,key)}
        </#list>
        <#--<#else>
            <tr><td>服务暂未开通...</td></tr>
        </#if>-->
        </table>
        <h4>错误日志(${error_count})</h4>
        <table class="table table-hover table-condensed">
        <#--<#if error!=0>-->
            <#list error as key,val>
        ${renderLog(val,5,key)}
        </#list>
        <#--<#else>
            <tr><td>服务暂未开通...</td></tr>
        </#if>-->
        </table>
    </div>
</div>

<script>
    function checkContinue() {
        var result = confirm("请确认清除此日志历史记录？");
        if (result) {
            return true;
        } else {
            return false;
        }
    }
    $(document).ready(function () {
        $("#datarange").change(function () {
            $("#message").submit();
        });
        $("#datarange").val("${datelist_selected}");
    });
</script>
<#include "footer.ftl">
</body>
</html>