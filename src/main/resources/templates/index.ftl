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
            <div style="width: 600px;height: 200px;margin: auto;"><span style="font-size: 100px">Ragnar Fiery</span></div>
        </div>

        <div class="col-md-4">
            <h3>索引服务</h3>
            <hr/>
            <h4> - 组合索引</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <td>组合索引文档数：</td>
                    <td>${indexedDocCount?string("#")}</td>
                </tr>
                <tr>
                    <td>索引入口队列：</td>
                    <td>${metalogQueueLen?string("#")}</td>
                </tr>
            </table>
            <hr/>
            <h4> - 检索索引</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <th>索引名称</th>
                    <th>数据量</th>
                </tr>
            <#list searchInfoList as dbname,index>
                <tr>
                    <td>${dbname}</td>
                    <td>${index.count}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - 写入索引</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <th>索引名称</th>
                    <th>写入队列</th>
                    <th>内存占用</th>
                    <th>缓存数据</th>
                    <th>数据量</th>
                </tr>
            <#list writeInfoList as dbname,index>
                <tr>
                    <td>${dbname}</td>
                    <td>${index.insertqueue_len}</td>
                    <td>${index.memory_bytes}</td>
                    <th>${index.count_ram}</th>
                    <td>${index.count}</td>
                </tr>
            </#list>
            </table>

        </div>

        <div class="col-md-4">
            <h3>日志</h3>
            <hr/>
            <h4> - 日志队列</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <td>入口推入队列</td>
                    <td>${bizlogQueueLen?string("#")}</td>
                </tr>
            </table>

            <hr/>
            <h4> - 日志存储</h4>

            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>日志DB列表</th>
                </tr>
                </thead>
            <#list dbInfoList as dbname,index>
                <tr>
                    <td>${dbname}</td>
                </tr>
            </#list>
            </table>

        </div>

        <div class="col-md-4">
            <h3>内存统计</h3>

            <hr/>
            <h4> - API排行统计</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>统计项</th>
                    <th>数据量</th>
                </tr>
                </thead>
            <#list apitopStatic as day,datacount>
                <tr>
                    <td>${day}</td>
                    <td>${datacount}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - 错误统计</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>统计项</th>
                    <th>数据量</th>
                </tr>
                </thead>
            <#list errorStatic as day,datacount>
                <tr>
                    <td>${day}</td>
                    <td>${datacount}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - 警告统计</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>统计项</th>
                    <th>数据量</th>
                </tr>
                </thead>
            <#list alarmStatic as day,datacount>
                <tr>
                    <td>${day}</td>
                    <td>${datacount}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - 异常统计</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>统计项</th>
                    <th>数据量</th>
                </tr>
                </thead>
            <#list exceptionStatic as day,datacount>
                <tr>
                    <td>${day}</td>
                    <td>${datacount}</td>
                </tr>
            </#list>
            </table>


        </div>
    </div>
</div>
<#include "footer.ftl">


</body>
</html>