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
        body {
            height: 100%;
        }

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

<div class="col-md-12" style="height: 600px;">
    <h4>日志回放</h4>


<#if tips!="">
    <div class="col-md-10 col-md-offset-4">
        <h1>${tips}</h1>
    </div>
</#if>

    <div class="col-md-4 col-md-offset-5">

        <form method="get" action="?" name="form1" style="margin:0;padding:0;display: inline">
            TraceID:
            <input type="input" value="" name="traceid" id="traceidinput" style="width: 100px;"/>
            &nbsp;
            <input type="submit" value="回放" name="submit" class="btn btn-primary btn-sm"/>
        </form>
    </div>
</div>
<div class="container-fluid">
</div>

<#include "footer.ftl">

</body>
</html>