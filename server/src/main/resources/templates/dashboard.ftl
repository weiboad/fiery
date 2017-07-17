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
            <div style="width: 600px;height: 200px;margin: auto;"><span style="font-size: 100px">Ragnar Fiery</span>
            </div>
        </div>
        <div class="col-md-12">

            <div class="col-md-6">
                <h3>Parameter</h3>
                <table class="table sorttable table-hover col-md-4">
                    <tr>
                        <td>DB Path</td>
                        <td style="word-break:break-all;">${fieryConfig.getDbpath()}</td>
                    </tr>
                    <tr>
                        <td>Index Path</td>
                        <td style="word-break:break-all;">${fieryConfig.getIndexpath()}</td>
                    </tr>
                    <tr>
                        <td style="width: 110px">Log Keep Day</td>
                        <td style="word-break:break-all;">${fieryConfig.getKeepdataday()} Day</td>
                    </tr>
                    <tr>
                        <td>Enable Kafka</td>
                        <td style="word-break:break-all;">${fieryConfig.getKafkaenable()}</td>
                    </tr>
                    <tr>
                        <td>Kafka Server</td>
                        <td style="word-break:break-all;">${fieryConfig.getKafkaserver()}</td>
                    </tr>
                    <tr>
                        <td>Kafka Topic</td>
                        <td style="word-break:break-all;">${fieryConfig.getKafkatopic()}</td>
                    </tr>
                    <tr>
                        <td>Kafka Groupid</td>
                        <td style="word-break:break-all;">${fieryConfig.getKafkagroupid()}</td>
                    </tr>

                </table>
            </div>
            <div class="col-md-6">
                <h3>Status</h3>
                <table class="table sorttable table-hover col-md-4">
                    <tr>
                        <td style="width: 110px">Memory Max:</td>
                        <td style="word-break:break-all;">${systemStatus.getMemoryMax()/(1024)} Kb</td>
                    </tr>
                    <tr>
                        <td style="width: 110px">Memory Total:</td>
                        <td style="word-break:break-all;">${systemStatus.getMemoryTotal()/1024} Kb</td>
                    </tr>
                    <tr>
                        <td style="width: 110px">Memory Free:</td>
                        <td style="word-break:break-all;">${systemStatus.getMemoryFree()/1024} Kb</td>
                    </tr>
                    <tr>
                        <td style="width: 110px">Memory Used:</td>
                        <td style="word-break:break-all;">${systemStatus.getMemoryUsed()/1024} Kb</td>
                    </tr>

                </table>
            </div>
        </div>
        <hr/>
        <div class="col-md-4">
            <h3>Index Engine Service</h3>
            <hr/>
            <h4> - Combined Index</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <td>Document Count：</td>
                    <td>${indexedDocCount?string("#")}</td>
                </tr>
                <tr>
                    <td>Process Queue Length：</td>
                    <td>${metalogQueueLen?string("#")}</td>
                </tr>
            </table>
            <hr/>
            <h4> - Index Storage</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <th>Index Name</th>
                    <th>Count</th>
                </tr>
            <#list searchInfoList as dbname,index>
                <tr>
                    <td>${((dbname?eval)*1000)?number_to_date} (${dbname})</td>
                    <td>${index.count}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - Index Write Queue</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <th>Index</th>
                    <th>Queue</th>
                    <th>Mem</th>
                    <th>Cached</th>
                    <th>Total</th>
                </tr>
            <#list writeInfoList as dbname,index>
                <tr>
                    <td>${((dbname?eval)*1000)?number_to_date} (${dbname})</td>
                    <td>${index.insertqueue_len}</td>
                    <td>${index.memory_bytes}</td>
                    <th>${index.count_ram}</th>
                    <td>${index.count}</td>
                </tr>
            </#list>
            </table>

        </div>

        <div class="col-md-4">

            <h3>Log Storage</h3>
            <hr/>
            <h4> - Queue</h4>
            <table class="table sorttable table-hover">
                <tr>
                    <td>Pending Queue</td>
                    <td>${bizlogQueueLen?string("#")}</td>
                </tr>
            </table>

            <hr/>
            <h4> - Storage</h4>

            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>Storage DB</th>
                </tr>
                </thead>
            <#list dbInfoList as dbname,index>
                <tr>
                    <td>${((dbname?eval)*1000)?number_to_date} (${dbname})</td>
                </tr>
            </#list>
            </table>

        </div>

        <div class="col-md-4">
            <h3>Memory Statistics</h3>

            <hr/>
            <h4> - API Statistics</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>Item</th>
                    <th>Count</th>
                </tr>
                </thead>
            <#list apitopStatic as day,datacount>
                <tr>
                    <td>${((day?eval)*1000)?number_to_date} (${day})</td>
                    <td>${datacount}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - Error Statistics</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>Item</th>
                    <th>Count</th>
                </tr>
                </thead>
            <#list errorStatic as day,datacount>
                <tr>
                    <td>${((day?eval)*1000)?number_to_date} (${day})</td>
                    <td>${datacount}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - Warning Statistics</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>Item</th>
                    <th>Count</th>
                </tr>
                </thead>
            <#list alarmStatic as day,datacount>
                <tr>
                    <td>${((day?eval)*1000)?number_to_date} (${day})</td>
                    <td>${datacount}</td>
                </tr>
            </#list>
            </table>

            <hr/>
            <h4> - Exception Statistics</h4>
            <table class="table sorttable table-hover">
                <thead>
                <tr>
                    <th>Item</th>
                    <th>Count</th>
                </tr>
                </thead>
            <#list exceptionStatic as day,datacount>
                <tr>
                    <td>${((day?eval)*1000)?number_to_date} (${day})</td>
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