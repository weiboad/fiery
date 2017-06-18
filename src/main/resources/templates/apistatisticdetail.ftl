<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
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
<#include "header.ftl">
<#include "common.ftl">

<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <h3>慢响应排行:${url}</h3>
            <form action="?" method="get" class="form-horizontal" id="topmessage">

                <div style="float:right;width: 160px;">

                    <label class="control-label">时间范围:</label>
                    <input type="hidden" name="url" value="${url}"/>
                    <select name="topdatarange" class="input-sm" id="topdatarange">
                    <#list datelist as dateitem>
                        <option value="${dateitem?index}">${dateitem}</option>
                    </#list>
                    </select>
                </div>
            </form>
            <table class="table table-hover">
                <thead>
                <tr>
                    <th>URL</th>
                    <th width="200">TraceID</th>
                    <th width="50">RPCID</th>
                    <th width="50">IP</th>
                    <th width="80">HttpCode</th>
                    <th width="80">Cost(ms)</th>
                    <th width="180">Time</th>
                    <th width="100">操作</th>
                </tr>
                </thead>
            <#list resultlist as item>
                <tr class="${["", "info"][item_index%2]}">
                    <td>${item.url}</td>
                    <td>${item.traceid}</td>
                    <td>${item.rpcid}</td>
                    <td>${item.ip}</td>
                    <td>${item.httpcode}</td>
                    <td>${showMSCostTime(item.elapsed_ms * 1000)}</td>
                    <td>${(item.time_date?string("yyyy-MM-dd HH:mm:ss"))!}</td>
                    <td>
                        <a type="button" class="btn btn-primary"
                           href='showtrace?traceid=${item.traceid}&rpcid=${item.rpcid}'>查看
                        </a>
                    </td>
                </tr>
            </#list>
            </table>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function () {

        $("#topdatarange").change(function () {
            $("#topmessage").submit();
        });
        $("#topdatarange").val("${datelist_selected}");

    });
</script>
<#include "footer.ftl">
</body>
</html>