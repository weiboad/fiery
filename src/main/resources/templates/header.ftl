<div class="container-fluid navbar navbar-inverse" style="clear: both;">

    <div>
        <div class="navbar-header">
            <a class="navbar-brand" href="/ragnar/">Ragnar Fiery Distributed Tracking</a>
        </div>

        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav">
                <li class="">
                    <a href="/ragnar/">仪表盘<span class="sr-only">(current)</span></a>
                </li>
            </ul>
            <ul class="nav navbar-nav">
                <li class="">
                    <a href="/ragnar/recentrequest">最近访问<span class="sr-only">(current)</span></a>
                </li>
            </ul>
            <ul class="nav navbar-nav">
                <li class="">
                    <a href="/ragnar/apistatistic">性能排行<span class="sr-only">(current)</span></a>
                </li>
            </ul>
            <ul class="nav navbar-nav">
                <li class="">
                    <a href="/ragnar/dependstatistic">依赖服务性能<span class="sr-only">(current)</span></a>
                </li>
            </ul>
            <ul class="nav navbar-nav">
                <li class="">
                    <a href="/ragnar/sqlstatistic">依赖SQL性能<span class="sr-only">(current)</span></a>
                </li>
            </ul>
            <ul class="nav navbar-nav">
                <li class="">
                    <a href="/ragnar/showtrace">日志回放<span class="sr-only">(current)</span></a>
                </li>
            </ul>
            <ul class="nav navbar-nav">
                <li class="">
                    <a href="/ragnar/errorstatic">异常统计<span class="sr-only">(current)</span></a>
                </li>
            </ul>

            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true"
                       aria-expanded="false"><span class="glyphicon glyphicon-search"> </span> </a>
                    <ul class="dropdown-menu">
                        <li style="width: 200px;height: 30px;">
                            <div style="padding-left:5px;width: 200px;height: 20px;">
                                <form action="search" method="get">
                                    <div class="input-group">
                                        <input type="text" class="form-control" name="keyword"/>
                                        <span class="input-group-btn">
                                        <button class="btn btn-default" type="submit">搜索</button>
                                      </span>
                                    </div>
                                </form>
                            </div>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</div>