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

<div class="container-fluid" style="min-height: 850px;">
    <div class="row">
        <div class="col-md-12">
            <h3>项目依赖接口性能统计</h3>
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
            <table class="table sorttable table-hover" id="listtable">
                <thead>
                <tr>
                    <th data-sort="string-ins" style="width: 500px;">URL<span aria-hidden="true"> </span></th>
                    <th data-sort="int">调用次数<span aria-hidden="true"> </span></th>
                    <th data-sort="float">最长响应(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">最短响应(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">200(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">500(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">1000(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">1000+(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">http_code百分比<span aria-hidden="true"> </span></th>
                    <th>操作</th>
                </tr>
                </thead>
            <#list perfomancelist as key,item>
                <tr>
                    <td style="word-break:break-all;">${key}</td>
                    <td>${item.sumcount}</td>
                    <td>${showStaticsTime(item.slowtime?number)}</td>
                    <td>${showStaticsTime(item.fasttime?number)}</td>
                    <td>${item.two}</td>
                    <td>${item.five}</td>
                    <td>${item.ten}</td>
                    <td>${item.twenty}</td>
                    <td>${item.code}</td>
                    <td>
                        <a type="button" class="btn btn-primary"
                           href='dependstatisticdetail?url=${key?url}&daytime=${datelist_selected}'>查看
                        </a>
                    </td>
                </tr>
            </#list>
            </table>
            <hr/>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        var table = $("#listtable").stupidtable();
        var th_to_sort = table.find("thead th").eq(4);
        th_to_sort.stupidsort("asc");
        table.bind('aftertablesort', function (event, data) {
            // data.column - the ragnarlog of the column sorted after a click
            // data.direction - the sorting direction (either asc or desc)
            // $(this) - this table object
            $("#listtable th span").each(function () {
                $(this).removeClass("glyphicon");
                $(this).removeClass("glyphicon-chevron-up");
                $(this).removeClass("glyphicon-chevron-down");
            });
            $("#listtable th").children().eq(data.column).addClass("glyphicon");
            if (data.direction == "asc") {
                $("#listtable th").children().eq(data.column).addClass("glyphicon-chevron-up");
            } else {
                $("#listtable th").children().eq(data.column).addClass("glyphicon-chevron-down");
            }

        });
        $("#datarange").change(function () {
            $("#message").submit();
        });
        $("#datarange").val("${datelist_selected}");


        /*$("#project").change(function () {
            $("#message").submit();
        });*/
        //$("#project").val("<?=$projectkey?>");
    });
</script>
<#include "footer.ftl">
</body>
</html>