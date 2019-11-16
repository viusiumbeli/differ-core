<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Differ service</title>
    <link rel="stylesheet" type="text/css" href="/css/main.css">
    <link rel="stylesheet" type="text/css" href="/css/main.css">
    <script type="text/javascript" src="/js/jquery/jquery-3.4.1.js"></script>
    <script type="text/javascript" src="/js/main.js"></script>

    <link href='https://fonts.googleapis.com/css?family=Bitter' rel='stylesheet'>
</head>
<body>
<div class="topbar">
    <a> <span>Differ</span> </a>
</div>

<div class="differ-ui">
    <div class="wrapper">
        <#list full?values[0].tags as tag>
            <span class="block-tag-section">
                <@buildBlockTagSection tag tag?index/>
            </span>
        </#list>
    </div>
</div>
</body>
</html>

<#macro buildBlockTagSection tag index>
    <div class="tag-section left">
        <@buildLeftBlockTag tag index/>
    </div>

    <div class="tag-section right">
        <@buildRightBlockTag tag index/>
    </div>
</#macro>

<#macro buildLeftBlockTag tag index>
    <#assign render=statics['com.differ.differcore.utils.RenderUtils']>
    <h4 class="block-tag">

        <#if tag.name?is_hash><#assign tagName=tag.name.leftValue()><#else><#assign tagName=tag.name!""></#if>

        <#if tag.name?is_hash>
            <a class="nostyle removed" href="#/${tagName}"><span>${tagName}</span></a>
        <#else>
            <a class="nostyle" href="#/${tagName}"><span>${tagName}</span></a>
        </#if>

        <#if tag.description?is_hash>
            <small class="removed">${tag.description.leftValue()}</small>
        <#else>
            <small>${tag.description}</small>
        </#if>

        <@buildExpandButton/>

    </h4>

    <div class="block-content">
        <#list full?values[0].paths as path, methods>
            <#if methods?has_content>
                <#list methods as method, content>
                    <#if (!(right?values[0].paths[path][method])??)>
                        <#assign shouldRenderMethod = false/>
                        <#if !content?is_string>
                            <#list content.tags as ttag>
                                <#if ttag?is_hash><#assign ttagName=ttag.leftValue()><#else><#assign ttagName=ttag></#if>
                                <#if tagName == ttagName>
                                    <#assign shouldRenderMethod=true>
                                </#if>
                            </#list>
                        </#if>

                        <#if shouldRenderMethod>
                            <#if content.summary?is_hash>
                                <@renderMethod method path content.summary.leftValue() "" "removed"/>
                            <#else>
                                <@renderMethod method path content.summary ""/>
                            </#if>
                        </#if>

                    <#else >
                        <div>path=${path}</div>
                        <#if right?values[0].paths[path][method]?is_string && (right?values[0].paths[path][method] == "null" )>

                            <#assign leftContent=left?values[0].paths[path][method]/>

                            <#assign shouldRenderMethod = false/>
                            <#if !leftContent?is_string>
                                <#list leftContent.tags as ttag>
                                    <#if tagName == ttag>
                                        <#assign shouldRenderMethod=true>
                                    </#if>
                                </#list>
                            </#if>

                            <#if shouldRenderMethod>
                                <@renderMethod method path leftContent.summary "removed" "removed"/>
                            </#if>


                        </#if>
                    </#if>
                </#list>
            </#if>
        </#list>
    </div>

</#macro>

<#macro buildRightBlockTag tag index>
    <#if tag.name?is_hash><#assign tagName=tag.name.rightValue()><#else><#assign tagName=tag.name></#if>
    <#if tag.description?is_hash><#assign tagDescription=tag.description.rightValue()><#else><#assign tagDescription=tag.description></#if>

    <h4 class="block-tag">
        <a class="nostyle" href="#/${tagName}"><span>${tagName}</span></a>
        <small>${tagDescription}</small>
        <@buildExpandButton/>
    </h4>

    <div class="block-content">
        <#list full?values[0].paths as path, methods>
            <#if !(left?values[0].paths[path])??>
                <#if methods?has_content>
                    <#list methods as method, content>
                        <#assign shouldRenderMethod = false/>
                        <#if !content?is_string>
                            <#list content.tags as ttag>
                                <#if ttag?is_hash><#assign ttagName=ttag.rightValue()><#else><#assign ttagName=ttag></#if>
                                <#if tagName == ttagName>
                                    <#assign shouldRenderMethod=true>
                                </#if>
                            </#list>
                        </#if>

                        <#if shouldRenderMethod>
                            <#if content.summary?is_hash><#assign contentSummary=content.summary.rightValue()><#else><#assign contentSummary=content.summary></#if>
                            <@renderMethod method path contentSummary ""/>
                        </#if>
                    </#list>
                </#if>
            </#if>
        </#list>
    </div>
</#macro>

<#macro renderMethod method path content removedMethod removed="">
    <span>
        <div class="opblock opblock-${method}">
            <div class="opblock-summary">
                <span class="opblock-summary-method ${removedMethod}">${method}</span>
                <span class="opblock-summary-path ${removedMethod}">${path}</span>
                <div class="opblock-summary-description ${removed}">${content}</div>
            </div>
        </div>
    </span>
</#macro>

<#macro if if then else=""><#if if>${then}<#else>${else}</#if></#macro>

<#macro buildExpandButton>
    <button class="expand-operation" title="Expand operation">
        <svg class="arrow" width="20" height="20">
            <use href="#large-arrow" xlink:href="#large-arrow"></use>
        </svg>
    </button>
</#macro>