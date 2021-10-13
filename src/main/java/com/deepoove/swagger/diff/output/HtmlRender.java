package com.deepoove.swagger.diff.output;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.*;
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import j2html.tags.ContainerTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static j2html.TagCreator.*;

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

        List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
        ContainerTag ol_changed = ol_changed(changedEndpoints);

        ContainerTag p_versions = p_versions(diff.getOldVersion(), diff.getNewVersion());

        return renderHtml(ol_newEndpoint, ol_missingEndpoint, ol_changed, p_versions);
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
    	return ol.getNumChildren() > 0
    			? div().with(h2(title).with(a(rawHtml("&uArr;")).withId("btn_" + type).withClass("showhide").withHref("#").attr("onClick", "javascript:showHide('" + type + "');")), hr(), ol)
    			: div();
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

    private ContainerTag ol_changed(List<ChangedEndpoint> changedEndpoints) {
        if (null == changedEndpoints) return ol().withId("changed");
        ContainerTag ol = ol().withId("changed");
        for (ChangedEndpoint changedEndpoint : changedEndpoints) {
            String pathUrl = changedEndpoint.getPathUrl();
            Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();
            for (Entry<HttpMethod, ChangedOperation> entry : changedOperations.entrySet()) {
                String method = entry.getKey().toString();
                ChangedOperation changedOperation = entry.getValue();
                String desc = changedOperation.getSummary();

                ContainerTag ul_detail = ul().withClass("detail");
                if (changedOperation.isDiffParam()) {
                    ul_detail.with(li().with(h3("Parameter")).with(ul_param(changedOperation)));
                }
                if (changedOperation.isDiffProp()) {
                    ul_detail.with(li().with(h3("Return Type")).with(ul_response(changedOperation)));
                }
                if (changedOperation.isDiffProduces()) {
                    ul_detail.with(li().with(h3("Produces")).with(ul_produce(changedOperation)));
                }
                if (changedOperation.isDiffConsumes()) {
                    ul_detail.with(li().with(h3("Consumes")).with(ul_consume(changedOperation)));
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
        List<ElProperty> chgProps = changedOperation.getChangedProps();
        ContainerTag ul = ul().withClass("change response");
        for (ElProperty prop : addProps) {
            ul.with(li_addProp(prop));
        }
        for (ElProperty prop : delProps) {
            ul.with(li_missingProp(prop));
        }
        for (ElProperty prop : chgProps) {
            ul.with(li_changedProp(prop));
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

    private ContainerTag li_changedProp(ElProperty prop) {
        List<String> changeDetails = new ArrayList<>();
        String changeDetailsHeading = "";
        if (prop.isTypeChange()) {
            changeDetails.add("Data Type");
        }
        if (prop.isNewEnums()) {
            changeDetails.add("Added Enum");
        }
        if (prop.isRemovedEnums()) {
            changeDetails.add("Removed Enum");
        }
        if (! changeDetails.isEmpty()) {
            changeDetailsHeading = " (" + String.join(", ", changeDetails) + ")";
        }
        return li().withText("Change " + prop.getEl()).with(span(changeDetailsHeading).withClass("comment"));
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
            if (changeRequired || changeDescription)
                ul.with(li_changedParam(param));
        }
        for (ChangedParameter param : changedParameters) {
            List<ElProperty> missing = param.getMissing();
            for (ElProperty prop : missing) {
                ul.with(li_missingProp(prop));
            }
        }
        for (ChangedParameter param : changedParameters) {
            List<ElProperty> changed = param.getChanged();
            for (ElProperty prop : changed) {
                ul.with(li_changedProp(prop));
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
        Parameter rightParam = changeParam.getRightParameter();
        Parameter leftParam = changeParam.getLeftParameter();
        ContainerTag li = li().withText(rightParam.getName());
        if (changeRequired) {
            li.withText(" change into " + (rightParam.getRequired() ? "required" : "not required"));
        }
        if (changeDescription) {
            li.withText(" Notes ").with(del(leftParam.getDescription()).withClass("comment")).withText(" change into ").with(span(span(null == rightParam.getDescription() ? "" : rightParam.getDescription()).withClass("comment")));
        }
        return li;
    }

    private ContainerTag ul_produce(ChangedOperation changedOperation) {
        List<String> addProduce = changedOperation.getAddProduces();
        List<String> delProduce = changedOperation.getMissingProduces();
        ContainerTag ul = ul().withClass("change produces");
        for (String mt : addProduce) {
            ul.with(li_addMediaType(mt));
        }
        for (String mt : delProduce) {
            ul.with(li_missingMediaType(mt));
        }
        return ul;
    }

    private ContainerTag ul_consume(ChangedOperation changedOperation) {
        List<String> addConsume = changedOperation.getAddConsumes();
        List<String> delConsume = changedOperation.getMissingConsumes();
        ContainerTag ul = ul().withClass("change consumes");
        for (String mt : addConsume) {
            ul.with(li_addMediaType(mt));
        }
        for (String mt : delConsume) {
            ul.with(li_missingMediaType(mt));
        }
        return ul;
    }

    private ContainerTag li_missingMediaType(String type) {
        return li().withClass("missing").withText("Delete").with(del(type)).with(span(""));
    }

    private ContainerTag li_addMediaType(String type) {
        return li().withText("Add " + type).with(span(""));
    }
}
