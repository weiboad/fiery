<#setting url_escaping_charset='utf-8'>

<#function showCostTime costtime>
    <#assign costtime = costtime?eval>
    <#if costtime \gt 1>
        <#return " <span style='color: red'>" + costtime?string("0.000")  + "</span> ">
    <#elseif costtime \gt 0.6>
        <#return " <span style='color: orange'>" + costtime?string("0.000") + "</span> ">
    <#else >
        <#return " <span style='color: black'>" + costtime?string("0.000")  + "</span> ">
    </#if>
</#function>

<#function showMSCostTime costtime>
    <#if costtime == "">
        <#return "--">
    </#if>

    <#if costtime \gt 1000>
        <#return " <span style='color: red'>" + costtime?string("0.00")  + "</span> ">
    <#elseif costtime \gt 600>
        <#return " <span style='color: orange'>" + costtime?string("0.00") + "</span> ">
    <#else >
        <#return " <span style='color: black'>" + costtime?string("0.00")  + "</span> ">
    </#if>
</#function>

<#function showTagCostTime costtime>
    <#if costtime == "" >
        <#return "--">
    </#if>

    <#assign costtimeint = costtime?eval>

    <#if costtimeint \gt 1>
        <#return " <span class='label label-danger'>" + costtimeint  + " sec</span> ">
    <#else >
        <#return " <span class='label label-success'>" + (costtimeint * 1000) + " ms</span> ">
    </#if>
</#function>

<#function showStaticsTime costtime>
    <#if costtime \gt 1000>
        <#return "<span style='color: red'>" + costtime?string("0.00") + "</span>">
    <#elseif costtime \gt 600>
        <#return "<span style='color: orange'>" + costtime?string("0.00") + "</span>">
    <#else >
        <#return "<span style='color: black'>" + costtime?string("0.00") + "</span>">
    </#if>
</#function>

<#function showSelected isselect>
    <#if isselect == "selected">
        <#return " <lable class= 'label label-primary'> 选中 </lable> ">
    </#if>
</#function>
<#function showSelectedTr isselect>
    <#if isselect == "selected">
        <#return " success ">
    </#if>
</#function>

<#function showTag tagname>
    <#if tagname == "curl">
        <#return " <label class='label label-success'>Curl</label> ">
    <#elseif tagname == "mysql">
        <#return " <label class='label label-info'>Sql</label> ">
    <#elseif tagname == "api">
        <#return "<label class='label label-danger'>API</label>">
    <#else >
        <#return " <label class='label label-warning'>Custom</label> ">
    </#if>
</#function>

<#function showLogTypeName type>
    <#if type == 1>
        <#return " DEBUG ">
    <#elseif type == 2>
        <#return " TRACE ">
    <#elseif type == 3>
        <#return " NOTICE ">
    <#elseif type == 4>
        <#return " INFO ">
    <#elseif type == 5>
        <#return " ERROR ">
    <#elseif type == 6>
        <#return " ALARM ">
    <#elseif type == 7>
        <#return " EXCEPTION ">
    <#elseif type == 8>
        <#return " XHPROF ">
    <#elseif type == 9>
        <#return " Perform ">
    <#else >
        <#return type>
    </#if>
</#function>

<#function showTimeLineError>
    <#return "<div style='width:490px;background-color:grey;color:white;text-align:center;'> 响应时间长度异常 </div>">
</#function>

<#function showTimeLine timeline_startbar,timeline_interbar,timeline_costbar,interbar_title,costbar_title>
    <#if timeline_startbar == -1 && timeline_interbar == -1  && timeline_costbar == -1>
        <#return "">
    </#if>

    <#if timeline_startbar == 0 && timeline_interbar == 0  && timeline_costbar == 0>
        <#return "<div style='width:490px;background-color:grey;color:white;text-align:center;'> 缺失数据 </div>">
    </#if>

    <#if beforTime == 0>
        <#return "<div style='width:490px;background-color:grey;color:white;text-align:center;'> 上条响应时间长度异常 </div>">
    </#if>

    <#return
    "<div style='font-size:10px;line-height:20px;height:20px;float: left;overflow: hidden;color:white;text-align:center;width:" + timeline_startbar + "px;'></div>" +
    "<div style='background-color:#337ab7;border-color:#337ab7;color:white;font-size:10px;line-height:20px;height:20px;float: left;overflow: hidden;color:white;text-align:center;width:" + timeline_interbar + "px;'>" + interbar_title + "</div>" +
    "<div style='background-color:#5cb85c;font-size:10px;line-height:20px;height:20px;float: left;overflow: hidden;color:white;text-align:center;width:" + timeline_costbar + "px;'>" + costbar_title + "</div>" >

</#function>

<#function showTimeOutTag istimeout>
    <#if istimeout == "1">
        <#return "<label class='label label-danger'> TimeOut </label>">
    </#if>
</#function>

<#function showInterTimeWarning intertime>
    <#if intertime \gt 0.5>
        <#return
        "<tr>
        <td colspan=\"7\"
            style=\"background-color: #337ab7;text-align: center;font-size: 12px;color: white;line-height: 12px;height: 12px;\">
            上下埋点间隔时间 "+ intertime + " 秒
        </td>
    </tr>"
        >
    </#if>
</#function>

<#function showLogInterTimeWarning intertime>
    <#if intertime \gt 0.5>
        <#return '
<tr class="active">
    <td style="word-wrap: break-word;background-color: darkred; color: white;line-height: 50px;text-align: center;">
        <b>埋点之间时间间隔:</b>  '+ intertime+ '秒
    </td>
</tr>'
        >
    </#if>
</#function>