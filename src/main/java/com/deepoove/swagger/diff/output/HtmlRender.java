package com.deepoove.swagger.diff.output;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.*;
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import j2html.tags.ContainerTag;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static j2html.TagCreator.*;

import org.apache.commons.lang3.tuple.Pair;

public class HtmlRender implements Render {

    private String title;
    private String linkCss;

    public HtmlRender() {
        this("Api Change Log", "http://deepoove.com/swagger-diff/stylesheets/demo.css");
    }

    public HtmlRender(String title, String linkCss) {
        this.title = title;
        this.linkCss = linkCss;
    }


    public String render(SwaggerDiff diff) {
        List<Endpoint> newEndpoints = diff.getNewEndpoints();
        ContainerTag ol_newEndpoint = ol_newEndpoint(newEndpoints);

        List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
        ContainerTag ol_missingEndpoint = ol_missingEndpoint(missingEndpoints);

        ContainerTag changedSummary = div_changedSummary(diff);

        ContainerTag p_versions = p_versions(diff.getOldVersion(), diff.getNewVersion());

        return renderHtml(ol_newEndpoint, ol_missingEndpoint, changedSummary, p_versions);
    }

    public String renderHtml(ContainerTag ol_new, ContainerTag ol_miss, ContainerTag ol_changed, ContainerTag p_versions) {
        ContainerTag html = html().attr("lang", "en").with(
            head().with(
                meta().withCharset("utf-8"),
                title(title),
                script(rawHtml("function showHide(id){if(document.getElementById(id).style.display==\'none\'){document.getElementById(id).style.display=\'block\';document.getElementById(\'btn_\'+id).innerHTML=\'&uArr;\';}else{document.getElementById(id).style.display=\'none\';document.getElementById(\'btn_\'+id).innerHTML=\'&dArr;\';}return true;}")).withType("text/javascript"),
                link().withRel("stylesheet").withHref(linkCss)
            ),
            body().with(
                header().with(h1(title)),
                div().withClass("article").with(
                    div_headArticle("Versions", "versions", p_versions),
                    div_headArticle("What's New", "new", ol_new),
                    div_headArticle("What's Deprecated", "deprecated", ol_miss),
                    div_headArticle("What's Changed", "changed", ol_changed)
                )
            )
        );

        return document().render() + html.render();
    }

    private ContainerTag div_headArticle(final String title, final String type, final ContainerTag ol) {
        return div().with(h2(title).with(a(rawHtml("&uArr;")).withId("btn_" + type).withClass("showhide").withHref("#").attr("onClick", "javascript:showHide('" + type + "');")), hr(), ol);
    }

    private ContainerTag p_versions(String oldVersion, String newVersion) {
        ContainerTag p = p().withId("versions");
        p.withText("Changes from " + oldVersion + " to " + newVersion + ".");
        return p;
    }

    private ContainerTag ol_newEndpoint(List<Endpoint> endpoints) {
        if (null == endpoints) return ol().withId("new");
        ContainerTag ol = ol().withId("new");
        for (Endpoint endpoint : endpoints) {
            ol.with(li_newEndpoint(endpoint.getMethod().toString(),
                endpoint.getPathUrl(), endpoint.getSummary()));
        }
        return ol;
    }

    private ContainerTag li_newEndpoint(String method, String path,
                                        String desc) {
        return li().with(span(method).withClass(method)).withText(path + " ")
            .with(span(null == desc ? "" : desc));
    }

    private ContainerTag ol_missingEndpoint(List<Endpoint> endpoints) {
        if (null == endpoints) return ol().withId("deprecated");
        ContainerTag ol = ol().withId("deprecated");
        for (Endpoint endpoint : endpoints) {
            ol.with(li_missingEndpoint(endpoint.getMethod().toString(),
                endpoint.getPathUrl(), endpoint.getSummary()));
        }
        return ol;
    }

    private ContainerTag li_missingEndpoint(String method, String path,
                                            String desc) {
        return li().with(span(method).withClass(method),
            del().withText(path)).with(span(null == desc ? "" : " " + desc));
    }

    private ContainerTag div_changedSummary(SwaggerDiff diff) {
        List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
        ContainerTag ol_changed = ol_changed(changedEndpoints);

        ContainerTag container = div().withId("changed");
        ChangedExtensionGroup group;

        ChangedExtensionGroup topLevelExts = diff.getChangedTopLevelVendorExtensions();
        if (topLevelExts.hasSubGroup("info")) {
            group = topLevelExts.getSubGroup("info");
            if (group.vendorExtensionsAreDiff()) {
                container.with(li().withClass("indent").withText("info")
                        .with(ul_changedVendorExtList(group, false, true)));
            }
        }
        if (topLevelExts.hasSubGroup("securityDefinitions")) {
            group = topLevelExts.getSubGroup("securityDefinitions");
            if (group.vendorExtensionsAreDiff()) {
                container.with(li().withClass("indent").withText("securityDefinitions"))
                        .with(ul_changedVendorExtMap(group, true));
            }
        }
        if (topLevelExts.hasSubGroup("tags")) {
            group = topLevelExts.getSubGroup("tags");
            if (group.vendorExtensionsAreDiff()) {
                container.with(li().withClass("indent").withText("tags"))
                        .with(ul_changedVendorExtMap(group, true));
            }
        }

        return container.with(ol_changed);
    }

    private ContainerTag ul_changedVendorExtMap(ChangedExtensionGroup group, boolean styled) {
        ContainerTag ul = ul().withClasses("indent", iff(styled, "extension-container"));;
        for (Entry<String, ChangedExtensionGroup> entry : group.getChangedSubGroups().entrySet()) {
            if (entry.getValue().vendorExtensionsAreDiff()) {
                ul.with(li().with(h3(entry.getKey()))
                    .with(ul_changedVendorExtList(entry.getValue(), true, false)));
            }
        }
        return ul;
    }

    private ContainerTag ul_changedVendorExtList(ChangedExtensionGroup group, boolean indented, boolean styled) {
        ContainerTag ul = ul().withClasses(iff(indented, "indent"), iff(styled, "extension-container"));
        for (ContainerTag li : changedVendorExts(group)) {
            ul.with(li);
        }
        return ul;
    }

    private List<ContainerTag> changedVendorExts(ChangedExtensionGroup group) {
        LinkedList<ContainerTag> list = new LinkedList<ContainerTag>();
        for (String key : group.getIncreasedVendorExtensions().keySet()) {
            list.add(li_addVendorExt(key));
        }
        for (String key : group.getMissingVendorExtensions().keySet()) {
            list.add(li_missingVendorExt(key));
        }
        for (Entry<String, Pair<Object, Object>> entry : group.getChangedVendorExtensions().entrySet()) {
            String key = entry.getKey();
            Object left = entry.getValue().getLeft();
            Object right = entry.getValue().getRight();
            list.add(li_changedVendorExt(key, left, right));
        }
        return list;
    }

    private ContainerTag li_addVendorExt(String key) {
        return li().withText("Add " + key);
    }

    private ContainerTag li_missingVendorExt(String key) {
        return li().withClass("missing").withText("Delete ").with(del(key));
    }

    private ContainerTag li_changedVendorExt(String key, Object oldVal, Object newVal) {
        return li().with(text(key + ": "))
            .with(del(oldVal.toString()))
            .with(text(" -> "))
            .with(i().with(text(newVal.toString())));
    }

    private ContainerTag ol_changed(List<ChangedEndpoint> changedEndpoints) {
        if (null == changedEndpoints) return ol();
        ContainerTag ol = ol();
        ContainerTag ul_detail;
        for (ChangedEndpoint changedEndpoint : changedEndpoints) {
            String pathUrl = changedEndpoint.getPathUrl();
            Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();

            if (changedEndpoint.vendorExtensionsAreDiff()) {
                ul_detail = ul();
                if (changedEndpoint.vendorExtensionsAreDiff()) {
                    ul_detail.with(li().with(ul_changedVendorExtList(changedEndpoint, false, false)));
                }
                ol.with(li().withText(pathUrl).with(ul_detail));
            }

            for (Entry<HttpMethod, ChangedOperation> entry : changedOperations.entrySet()) {
                String method = entry.getKey().toString();
                ChangedOperation changedOperation = entry.getValue();
                String desc = changedOperation.getSummary();

                ul_detail = ul().withClass("detail");
                if (changedOperation.vendorExtensionsAreDiff()) {
                    ul_detail.with(li().with(ul_changedVendorExtList(changedOperation, false, false)));
                }
                if (changedOperation.isDiffParam()) {
                    ul_detail.with(li().with(h3("Parameter")).with(ul_param(changedOperation)));
                }
                if (changedOperation.isDiffProp()) {
                    ul_detail.with(li().with(h3("Return Type")).with(ul_response(changedOperation)));
                }
                if (changedOperation.hasSubGroup("responses")) {
                    ChangedExtensionGroup group = changedOperation.getSubGroup("responses");
                    if (group.vendorExtensionsAreDiff()) {
                        ContainerTag ul_response = ul().with(li().with(h3("Responses")));
                        for (Entry<String, ChangedExtensionGroup> rEntry : group.getChangedSubGroups().entrySet()) {
                            ul_response.with(li().withClass("indent").withText(rEntry.getKey()).with(ul_changedVendorExtList(rEntry.getValue(), true, false)));
                        }
                        ul_detail.with(ul_response);
                    }
                }
                ol.with(li().with(span(method).withClass(method)).withText(pathUrl + " ").with(span(null == desc ? "" : desc))
                    .with(ul_detail));
            }
        }
        return ol;
    }

    private ContainerTag ul_response(ChangedOperation changedOperation) {
        List<ElProperty> addProps = changedOperation.getAddProps();
        List<ElProperty> delProps = changedOperation.getMissingProps();
        ContainerTag ul = ul().withClass("change response");
        for (ElProperty prop : addProps) {
            ul.with(li_addProp(prop));
        }
        for (ElProperty prop : delProps) {
            ul.with(li_missingProp(prop));
        }
        return ul;
    }

    private ContainerTag li_missingProp(ElProperty prop) {
        Property property = prop.getProperty();
        return li().withClass("missing").withText("Delete").with(del(prop.getEl())).with(span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
    }

    private ContainerTag li_addProp(ElProperty prop) {
        Property property = prop.getProperty();
        return li().withText("Add " + prop.getEl()).with(span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
    }

    private ContainerTag ul_param(ChangedOperation changedOperation) {
        List<Parameter> addParameters = changedOperation.getAddParameters();
        List<Parameter> delParameters = changedOperation.getMissingParameters();
        List<ChangedParameter> changedParameters = changedOperation.getChangedParameter();
        ContainerTag ul = ul().withClass("change param");
        for (Parameter param : addParameters) {
            ul.with(li_addParam(param));
        }
        for (ChangedParameter param : changedParameters) {
            List<ElProperty> increased = param.getIncreased();
            for (ElProperty prop : increased) {
                ul.with(li_addProp(prop));
            }
        }
        for (ChangedParameter param : changedParameters) {
            boolean changeRequired = param.isChangeRequired();
            boolean changeDescription = param.isChangeDescription();
            boolean changeVendorExtensions = param.vendorExtensionsAreDiff();
            if (changeRequired || changeDescription || changeVendorExtensions)
                ul.with(li_changedParam(param));
        }
        for (ChangedParameter param : changedParameters) {
            List<ElProperty> missing = param.getMissing();
            for (ElProperty prop : missing) {
                ul.with(li_missingProp(prop));
            }
        }
        for (Parameter param : delParameters) {
            ul.with(li_missingParam(param));
        }
        return ul;
    }

    private ContainerTag li_addParam(Parameter param) {
        return li().withText("Add " + param.getName()).with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
    }

    private ContainerTag li_missingParam(Parameter param) {
        return li().withClass("missing").with(span("Delete")).with(del(param.getName())).with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
    }

    private ContainerTag li_changedParam(ChangedParameter changeParam) {
        boolean changeRequired = changeParam.isChangeRequired();
        boolean changeDescription = changeParam.isChangeDescription();
        boolean changeVendorExtensions = changeParam.vendorExtensionsAreDiff();
        Parameter rightParam = changeParam.getRightParameter();
        Parameter leftParam = changeParam.getLeftParameter();
        ContainerTag li = li().withText("Change " + rightParam.getName() + ":");
        ContainerTag ul = ul().withClass("indent");
        if (changeRequired) {
            String newValue = (rightParam.getRequired() ? "required" : "not required");
            String oldValue = (!rightParam.getRequired() ? "required" : "not required");
            ul.with(li().with(del(oldValue)).withText(" -> " + newValue));
        }
        if (changeDescription) {
            ul.with(li().withText("Notes:").with(del(leftParam.getDescription()).withClass("comment")).withText(" -> ").with(span(span(null == rightParam.getDescription() ? "" : rightParam.getDescription()).withClass("comment"))));
        }
        if (changeVendorExtensions) {
            ul.with(li().with(ul_changedVendorExtList(changeParam, false, false)));
        }
        return li.with(ul);
    }

}
