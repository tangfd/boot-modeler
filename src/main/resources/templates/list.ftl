<!doctype html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Activiti Model List</title>
    <meta name="description" content="">
    <meta name="viewport"
          content="initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no, width=device-width">
    <link rel="stylesheet" href="editor-app/libs/bootstrap_3.1.1/css/bootstrap.min.css"/>
</head>
<script src="editor-app/libs/jquery_1.11.0/jquery.min.js"></script>
<script src="editor-app/libs/bootstrap_3.1.1/js/bootstrap.min.js"></script>
<body>
<div class="container">
    <div class="row clearfix" style="margin: 50px 0 20px; 0">
        <div class="col-md-12 column">
            <button type="button" class="btn btn-primary btn-default active" data-toggle="modal"
                    data-target="#createModal">
                创建流程
            </button>
        </div>
    </div>
    <div class="row clearfix">
        <div class="col-md-12 column">
            <table class="table table-bordered table-hover">
                <thead>
                <tr>
                    <th>
                        ID
                    </th>
                    <th>
                        名称
                    </th>
                    <th>
                        KEY
                    </th>
                    <th>
                        创建时间
                    </th>
                    <th>
                        版本号
                    </th>
                    <th>
                        操作
                    </th>
                </tr>
                </thead>
                <tbody>
                <#if modelList?? && modelList?size gt 0>
                    <#list modelList as model>
                    <tr <#if model_index % 2 == 1>class="success"</#if>>
                        <td>
                        ${(model.id!)}
                        </td>
                        <td>
                            <a href="img/${(model.id!)}" target="_blank" title="点击查看流程图">${(model.name!)}</a>
                        </td>
                        <td>
                        ${(model.key!)}
                        </td>
                        <td>
                        ${(model.createTime!?string('yyyy-MM-dd HH:mm:ss'))}
                        </td>
                        <td>
                        ${(model.version!)}
                        </td>
                        <td>
                            <a href="modeler?modelId=${(model.id!)}" class="glyphicon glyphicon-pencil"
                               title="重新编辑"></a>
                            <a href="download/${(model.id!)}" class="glyphicon glyphicon-download-alt" target="_blank"
                               title="下载BPMN文件"></a>
                            <a href="javascript:void(0);" data-id="${(model.id!)}" class="glyphicon glyphicon-cloud"
                               title="部署流程"></a>
                        </td>
                    </tr>
                    </#list>
                <#else>
                <tr>
                    <td colspan="6"></td>
                </tr>
                </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="createModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="myModalLabel">
                    创建流程-基本信息
                </h4>
            </div>
            <div class="modal-body">
                <div class="row clearfix">
                    <div class="col-md-12 column">
                        <form class="form-horizontal" role="form" action="/create">
                            <div class="form-group">
                                <label class="col-sm-2 control-label">名称：</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" name="name"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2 control-label">KEY：</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" name="key"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2 control-label">描述：</label>
                                <div class="col-sm-10">
                                    <textarea class="form-control" name="description"></textarea>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-offset-2 col-sm-5">
                                    <button type="submit" class="btn btn-default">保存</button>
                                    <button type="reset" class="btn btn-default" data-dismiss="modal">取消</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script>
    $(function () {
        $(".glyphicon-cloud").click(function (e) {
            e.stopPropagation();
            var modelId = $(this).attr("data-id");
            $.get("deploy/" + modelId, function (data) {
                if (data && data.status == true) {
                    alert("流程部署成功，部署流程ID=" + data.message);
                } else if (data.message === "MODEL_NOT_EXIST") {
                    alert("流程模型不存在！");
                } else {
                    alert("根据模型部署流程失败！");
                }
                location.reload();
            });
        });
    })
</script>
</html>
