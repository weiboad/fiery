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
            <form action="?" method="get" class="form-horizontal" id="message">
                <div style="float:right;width: 160px;">
                    <label class="control-label">时间范围:</label>
                    <select name="topdatarange" class="input-sm" id="topdatarange">
                    <#list datelist as dateitem>
                        <option value="${dateitem?index}">${dateitem}</option>
                    </#list>
                    </select>
                    <input type="hidden" name="url" value="${url}"/>
                </div>
            </form>
            <div class="col-md-12">

                <!-- reference from http://echarts.baidu.com/echarts2/doc/example/mix2.html#blue -->
                <div id="main" style="height:500px;"></div>
                <script src="js/echarts-all.js"></script>

                <script>

                    var theme = {
                        // 默认色板
                        color: [
                            '#1790cf', '#1bb2d8', '#99d2dd', '#88b0bb',
                            '#1c7099', '#038cc4', '#75abd0', '#afd6dd'
                        ],

                        // 图表标题
                        title: {
                            textStyle: {
                                fontWeight: 'normal',
                                color: '#1790cf'
                            }
                        },

                        // 值域
                        dataRange: {
                            color: ['#1178ad', '#72bbd0']
                        },

                        // 工具箱
                        toolbox: {
                            color: ['#1790cf', '#1790cf', '#1790cf', '#1790cf']
                        },

                        // 提示框
                        tooltip: {
                            backgroundColor: 'rgba(0,0,0,0.5)',
                            axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                type: 'line',         // 默认为直线，可选为：'line' | 'shadow'
                                lineStyle: {          // 直线指示器样式设置
                                    color: '#1790cf',
                                    type: 'dashed'
                                },
                                crossStyle: {
                                    color: '#1790cf'
                                },
                                shadowStyle: {                     // 阴影指示器样式设置
                                    color: 'rgba(200,200,200,0.3)'
                                }
                            }
                        },

                        // 区域缩放控制器
                        dataZoom: {
                            dataBackgroundColor: '#eee',            // 数据背景颜色
                            fillerColor: 'rgba(144,197,237,0.2)',   // 填充颜色
                            handleColor: '#1790cf'     // 手柄颜色
                        },

                        // 网格
                        grid: {
                            borderWidth: 0
                        },

                        // 类目轴
                        categoryAxis: {
                            axisLine: {            // 坐标轴线
                                lineStyle: {       // 属性lineStyle控制线条样式
                                    color: '#1790cf'
                                }
                            },
                            splitLine: {           // 分隔线
                                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
                                    color: ['#eee']
                                }
                            }
                        },

                        // 数值型坐标轴默认参数
                        valueAxis: {
                            axisLine: {            // 坐标轴线
                                lineStyle: {       // 属性lineStyle控制线条样式
                                    color: '#1790cf'
                                }
                            },
                            splitArea: {
                                show: true,
                                areaStyle: {
                                    color: ['rgba(250,250,250,0.1)', 'rgba(200,200,200,0.1)']
                                }
                            },
                            splitLine: {           // 分隔线
                                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
                                    color: ['#eee']
                                }
                            }
                        },

                        timeline: {
                            lineStyle: {
                                color: '#1790cf'
                            },
                            controlStyle: {
                                normal: {color: '#1790cf'},
                                emphasis: {color: '#1790cf'}
                            }
                        },

                        // K线图默认参数
                        k: {
                            itemStyle: {
                                normal: {
                                    color: '#1bb2d8',          // 阳线填充颜色
                                    color0: '#99d2dd',      // 阴线填充颜色
                                    lineStyle: {
                                        width: 1,
                                        color: '#1c7099',   // 阳线边框颜色
                                        color0: '#88b0bb'   // 阴线边框颜色
                                    }
                                }
                            }
                        },

                        map: {
                            itemStyle: {
                                normal: {
                                    areaStyle: {
                                        color: '#ddd'
                                    },
                                    label: {
                                        textStyle: {
                                            color: '#c12e34'
                                        }
                                    }
                                },
                                emphasis: {                 // 也是选中样式
                                    areaStyle: {
                                        color: '#99d2dd'
                                    },
                                    label: {
                                        textStyle: {
                                            color: '#c12e34'
                                        }
                                    }
                                }
                            }
                        },

                        force: {
                            itemStyle: {
                                normal: {
                                    linkStyle: {
                                        color: '#1790cf'
                                    }
                                }
                            }
                        },

                        chord: {
                            padding: 4,
                            itemStyle: {
                                normal: {
                                    borderWidth: 1,
                                    borderColor: 'rgba(128, 128, 128, 0.5)',
                                    chordStyle: {
                                        lineStyle: {
                                            color: 'rgba(128, 128, 128, 0.5)'
                                        }
                                    }
                                },
                                emphasis: {
                                    borderWidth: 1,
                                    borderColor: 'rgba(128, 128, 128, 0.5)',
                                    chordStyle: {
                                        lineStyle: {
                                            color: 'rgba(128, 128, 128, 0.5)'
                                        }
                                    }
                                }
                            }
                        },

                        gauge: {
                            axisLine: {            // 坐标轴线
                                show: true,        // 默认显示，属性show控制显示与否
                                lineStyle: {       // 属性lineStyle控制线条样式
                                    color: [[0.2, '#1bb2d8'], [0.8, '#1790cf'], [1, '#1c7099']],
                                    width: 8
                                }
                            },
                            axisTick: {            // 坐标轴小标记
                                splitNumber: 10,   // 每份split细分多少段
                                length: 12,        // 属性length控制线长
                                lineStyle: {       // 属性lineStyle控制线条样式
                                    color: 'auto'
                                }
                            },
                            axisLabel: {           // 坐标轴文本标签，详见axis.axisLabel
                                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                                    color: 'auto'
                                }
                            },
                            splitLine: {           // 分隔线
                                length: 18,         // 属性length控制线长
                                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
                                    color: 'auto'
                                }
                            },
                            pointer: {
                                length: '90%',
                                color: 'auto'
                            },
                            title: {
                                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                                    color: '#333'
                                }
                            },
                            detail: {
                                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                                    color: 'auto'
                                }
                            }
                        },

                        textStyle: {
                            fontFamily: '微软雅黑, Arial, Verdana, sans-serif'
                        }
                    };

                    var myChart = echarts.init(document.getElementById('main'), theme);

                    option = {
                        tooltip: {
                            trigger: 'axis'
                        },
                        title: {
                            show: true,
                            text: "网址访问量小时统计",
                            subtext: "${url}",
                            textStyle: {
                                fontSize: 25,
                            }
                        },
                        toolbox: {
                            show: true,
                            y: 'bottom',
                            feature: {
                                mark: {show: true},
                                dataView: {show: true, readOnly: false},
                                magicType: {show: true, type: ['line', 'bar', 'stack', 'tiled']},
                                restore: {show: true},
                                saveAsImage: {show: true}
                            }
                        },
                        calculable: true,
                        legend: {
                            data: ['200ms', '500ms', '1000ms', '1000+ms']
                        },
                        xAxis: [
                            {
                                name: '',
                                type: 'category',
                                splitLine: {show: true},
                                data: [
                                <#list urllist as key,item>
                                ${key},
                                </#list>
                                    ''
                                ]
                            }
                        ],
                        yAxis: [
                            {
                                type: 'value',
                                position: 'left'
                            },
                            {
                                type: 'value',
                                position: 'right'
                            }
                        ],
                        series: [
                            {
                                name: '200ms',
                                type: 'bar',
                                stack: 'all',
                                tooltip: {
                                    trigger: 'item',
                                    formatter: '{a} <br/> 请求量: {c}'
                                },
                                data: [
                                <#list urllist as key,item>
                                ${item.getMs200Count()?string('#')},
                                </#list>
                                    0
                                ]
                            },
                            {
                                name: '500ms',
                                type: 'bar',
                                tooltip: {
                                    trigger: 'item',
                                    formatter: '{a} <br/> 请求量: {c}'
                                },
                                stack: 'all',
                                data: [
                                <#list urllist as key,item>
                                ${item.getMs500Count()?string('#')},
                                </#list>
                                    0
                                ]
                            },
                            {
                                name: '1000ms',
                                type: 'bar',
                                tooltip: {
                                    trigger: 'item',
                                    formatter: '{a} <br/> 请求量: {c}'
                                },
                                stack: 'all',
                                data: [
                                <#list urllist as key,item>
                                ${item.getMs1000Count()?string('#')},
                                </#list>
                                    0
                                ]
                            },
                            {
                                name: '1000+ms',
                                type: 'bar',
                                tooltip: {
                                    trigger: 'item',
                                    formatter: '{a} <br/> 请求量: {c}'
                                },
                                stack: 'all',
                                data: [
                                <#list urllist as key,item>
                                ${item.getMsLongCount()?string('#')},
                                </#list>
                                    0
                                ]

                            },
                            {
                                name: '最慢响应',
                                type: 'line',
                                yAxisIndex: 1,
                                data: [
                                <#list urllist as key,item>
                                ${(item.getLongestTime()*1000)?string('#.#')},
                                </#list>
                                    0
                                ]
                            },
                            {
                                name: '最快响应',
                                type: 'line',
                                yAxisIndex: 1,
                                data: [
                                <#list urllist as key,item>
                                ${(item.getShortestTime()*1000)?string('#.#')},
                                </#list>
                                    0
                                ]
                            },
                            {
                                name: 'HTTP Code',
                                type: 'pie',
                                tooltip: {
                                    trigger: 'item',
                                    formatter: '{a} <br/>{b} : {c} ({d}%)'
                                },
                                center: ['90%', 130],
                                radius: [0, 40],
                                itemStyle: {
                                    normal: {
                                        labelLine: {
                                            length: 20
                                        }
                                    }
                                },
                                data: [
                                <#list httpcode as key,item>
                                    {value: '${item?string("#")}', name: '${key}'},
                                </#list>
                                    {}
                                ]
                            }
                        ]
                    };

                    myChart.setOption(option);
                </script>

            </div>
            <table class="table sorttable table-hover" id="listtable">
                <thead>
                <tr>
                    <!--<th data-sort="string-ins">URL<span aria-hidden="true"> </span></th>-->
                    <th data-sort="int">时间段<span aria-hidden="true"> </span></th>
                    <th data-sort="int">调用次数<span aria-hidden="true"> </span></th>
                    <th data-sort="float">最长响应(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">最短响应(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">200(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">500(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">1000(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">1000+(ms)<span aria-hidden="true"> </span></th>
                    <th data-sort="float">http_code百分比<span aria-hidden="true"> </span></th>
                </tr>
                </thead>
            <#list urllist as key,item>
                <tr>
                    <td>${key}</td>
                    <td>${item.getTotalCount()?string("#")}</td>
                    <td>${showMSCostTime(item.getLongestTime()*1000)}</td>
                    <td>${showMSCostTime(item.getShortestTime()*1000)}</td>
                    <td>${(item.getMs200Count()/item.getTotalCount())*100}%</td>
                    <td>${(item.getMs500Count()/item.getTotalCount())*100}%</td>
                    <td>${(item.getMs1000Count()/item.getTotalCount())*100}%</td>
                    <td>${(item.getMsLongCount()/item.getTotalCount())*100}%</td>
                    <td><#list item.getCode_count() as code,param>
                    ${code}:${(param/item.totalCount)*100}%<br/>
                    </#list></td>
                </tr>
            </#list>
            </table>
            <hr/>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        var $table = $("#listtable").stupidtable();
        var $th_to_sort = $table.find("thead th").eq(0);
        $th_to_sort.stupidsort("asc");

        $table.bind('aftertablesort', function (event, data) {
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

        $("#topdatarange").change(function () {
            $("#message").submit();
        });
        $("#topdatarange").val("${datelist_selected}");

    });
</script>
<#include "footer.ftl">
</body>
</html>