package com.deepoove.swagger.diff.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.ChangedParameter;
import com.deepoove.swagger.diff.model.ElProperty;
import com.deepoove.swagger.diff.model.Endpoint;

import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

import static j2html.TagCreator.*;

public class HtmlRender implements Render {

    private static final String NON_BACKWARDS_CHANGES = "Non-backwards changes";

    private final String title;

    private final List<String> cssLinks;

    private final List<String> scriptsJsLinks;

    private boolean showBackwardsIncompatibilities;

    public HtmlRender() {
        this("Api Change Log", "http://deepoove.com/swagger-diff/stylesheets/demo.css");
    }

    public HtmlRender(final String title, final String linkCss) {
        this.title = title;
        cssLinks = new ArrayList<String>();
        scriptsJsLinks = new ArrayList<String>();
        cssLinks.add(linkCss); // Keep backward
    }

    /**
     * @param pTitle : page's title
     * @param pCssLinks : list of Css links
     * @param pScriptsJsLinks : list of JS Scripts links
     */
    public HtmlRender(final String pTitle, final List<String> pCssLinks, final List<String> pScriptsJsLinks) {
        super();
        title = pTitle;
        cssLinks = pCssLinks;
        scriptsJsLinks = pScriptsJsLinks;
    }

    @Override
    public String render(final SwaggerDiff diff) {
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
        List<EmptyTag> cssLinksTags = new ArrayList<EmptyTag>();
        if (!cssLinks.isEmpty()) {
            for (String cssLink : cssLinks) {
                cssLinksTags.add(link().withRel("stylesheet").withHref(cssLink));
            }
        }

        List<ContainerTag> cssScriptsJsTags = new ArrayList<ContainerTag>();
        if (!scriptsJsLinks.isEmpty()) {
            for (String scriptJs : scriptsJsLinks) {
                cssScriptsJsTags.add(script().withType("text/javascript").withSrc(scriptJs));
            }
        }

        // Build articles
        List<DomContent> articles = new ArrayList<DomContent>();
        if (showBackwardsIncompatibilities) {
            articles.add(span().with(i_backwardsIncompatibilitiesWarning(), span().withText(" " + NON_BACKWARDS_CHANGES)).withStyle("float:right"));
            articles.add(br());
        }
        articles.add(div_headArticle("What's New", "new", ol_new));
        articles.add(div_headArticle("What's Missed", "missed", ol_miss));
        articles.add(div_headArticle("What's Changed", "changed", ol_changed));

        ContainerTag html = html().attr("lang", "en").with(
                head().with(
                        meta().withCharset("utf-8"),
                        title(title),
                        script(rawHtml("function showHide(id){if(document.getElementById(id).style.display==\'none\'){document.getElementById(id).style.display=\'block\';document.getElementById(\'btn_\'+id).innerHTML=\'&uArr;\';}else{document.getElementById(id).style.display=\'none\';document.getElementById(\'btn_\'+id).innerHTML=\'&dArr;\';}return true;}")).withType("text/javascript")
                ).with(cssLinksTags),
                body().with(
                        header().with(h1(title)),
                        div().withClass("article").with(articles).with(
                                div_headArticle("Versions", "versions", p_versions),
                                div_headArticle("What's New", "new", ol_new),
                                div_headArticle("What's Deprecated", "deprecated", ol_miss),
                                div_headArticle("What's Changed", "changed", ol_changed)
                        )
                ),
                footer().with(cssScriptsJsTags)
        );

        return document().render() + html.render();
    }

    private ContainerTag p_versions(String oldVersion, String newVersion) {
        ContainerTag p = p().withId("versions");
        p.withText("Changes from " + oldVersion + " to " + newVersion + ".");
        return p;
    }

    public HtmlRender withBackwardsIncompatibilities() {
        showBackwardsIncompatibilities = true;
        return this;
    }

    private ContainerTag div_headArticle(final String title, final String type, final ContainerTag ol) {
        return div().with(h2(title).with(a(rawHtml("&uArr;")).withId("btn_" + type).withClass("showhide").withHref("#").attr("onClick", "javascript:showHide('" + type + "');")), hr(), ol);
    }

    private ContainerTag ol_newEndpoint(final List<Endpoint> endpoints) {
        if (null == endpoints) {
            return ol().withId("new");
        }
        ContainerTag ol = ol().withId("new");
        for (Endpoint endpoint : endpoints) {
            ol.with(li_newEndpoint(endpoint.getMethod().toString(),
                    endpoint.getPathUrl(), endpoint.getSummary()));
        }
        return ol;
    }

    private ContainerTag li_newEndpoint(final String method, final String path,
            final String desc) {
        return li().with(span(method).withClass(method)).withText(path)
                .with(span(null == desc ? "" : " " + desc));
    }

    private ContainerTag ol_missingEndpoint(final List<Endpoint> endpoints) {
        if (null == endpoints) {
            return ol().withId("missed");
        }
        ContainerTag ol = ol().withId("missed");
        for (Endpoint endpoint : endpoints) {
            ol.with(li_missingEndpoint(endpoint.getMethod().toString(),
                    endpoint.getPathUrl(), endpoint.getSummary()));
        }
        return ol;
    }

    private ContainerTag li_missingEndpoint(final String method, final String path,
            final String desc) {
        return li().with(span(method).withClass(method),
                del().withText(path)).with(i_backwardsIncompatibilitiesWarning()).with(span(null == desc ? "" : " " + desc));
    }

    private ContainerTag ol_changed(final List<ChangedEndpoint> changedEndpoints) {
        if (null == changedEndpoints) {
            return ol().withId("changed");
        }
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
                ContainerTag li = li();
                li.with(span(method).withClass(method)).withText(pathUrl);
                if (!changedEndpoint.isBackwardsCompatible()) {
                    li.with(i_backwardsIncompatibilitiesWarning());
                }
                li.with(span(null == desc ? "" : " " + desc)).with(ul_detail);
                ol.with(li);
            }
        }
        return ol;
    }

    private ContainerTag ul_response(final ChangedOperation changedOperation) {
        List<ElProperty> addProps = changedOperation.getAddProps();
        List<ElProperty> delProps = changedOperation.getMissingProps();
        List<ElProperty> changProps = changedOperation.getChangedProps();
        ContainerTag ul = ul().withClass("change response");
        for (ElProperty prop : addProps) {
            ul.with(li_addProp(prop));
        }
        for (ElProperty prop : changProps) {
            ul.with(li_changeTypeProp(prop));
        }
        for (ElProperty prop : delProps) {
            ul.with(li_missingProp(prop));
        }
        return ul;
    }

    private ContainerTag li_missingProp(final ElProperty prop) {
        Property property = prop.getProperty();
        return li().withClass("missing").withText("Delete").with(del().with(textField(prop.getEl()))).with(i_backwardsIncompatibilitiesWarning()).with(span(null == property.getDescription() ? "" : ("// " + property.getDescription())).withClass("comment"));
    }

    private ContainerTag li_addProp(final ElProperty prop) {
        Property property = prop.getProperty();
        ContainerTag li = li().withText("Add ").with(textField(prop.getEl()));
        if (prop.getProperty() != null && prop.getProperty().getRequired()) {
            li.withText(" required").with(i_backwardsIncompatibilitiesWarning());
        }
        return li.with(span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
    }

    private ContainerTag li_changeTypeProp(final ElProperty prop) {
        Property property = prop.getProperty();
        return li().with(textField(prop.getEl())).withText(" changes type").with(i_backwardsIncompatibilitiesWarning()).with(span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
    }

    private ContainerTag textField(final String pField) {
        return span().withText(pField).withClass("field");
    }

    private ContainerTag li_changeRequiredProp(final ElProperty prop) {
        Property property = prop.getProperty();
        return li().with(textField(prop.getEl())).withText(" change into required").with(i_backwardsIncompatibilitiesWarning()).with(span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
    }

    private ContainerTag ul_param(final ChangedOperation changedOperation) {
        List<Parameter> addParameters = changedOperation.getAddParameters();
        List<Parameter> delParameters = changedOperation.getMissingParameters();
        List<ChangedParameter> changedParameters = changedOperation.getChangedParameters();
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
            List<ElProperty> requiredChanges = param.getRequiredChanges();
            for (ElProperty prop : requiredChanges) {
                ul.with(li_changeRequiredProp(prop));
            }
        }
        for (ChangedParameter param : changedParameters) {
            List<ElProperty> typesChanges = param.getTypesChanges();
            for (ElProperty prop : typesChanges) {
                ul.with(li_changeTypeProp(prop));
            }
        }
        for (ChangedParameter param : changedParameters) {
            if (param.isChangeRequired() || param.isChangeDescription() || param.isChangeType()) {
                ul.with(li_changedParam(param));
            }
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

    private ContainerTag li_addParam(final Parameter param) {
        ContainerTag li = li();
        if (param.getRequired()) {
            li.withText("Add required ").with(textField(param.getName())).with(i_backwardsIncompatibilitiesWarning());
        } else {
            li.withText("Add ").with(textField(param.getName()));
        }
        return li.with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
    }

    private ContainerTag li_missingParam(final Parameter param) {
        return li().withClass("missing").with(span("Delete")).with(del(textField(param.getName()))).with(i_backwardsIncompatibilitiesWarning()).with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
    }

    private ContainerTag li_changedParam(final ChangedParameter changeParam) {
        boolean changeRequired = changeParam.isChangeRequired();
        boolean changeDescription = changeParam.isChangeDescription();
        boolean changeType = changeParam.isChangeType();
        Parameter rightParam = changeParam.getRightParameter();
        Parameter leftParam = changeParam.getLeftParameter();
        ContainerTag li = li().with(textField(rightParam.getName()));
        if (changeRequired || changeType || changeDescription) {
            li.withText(" : ");
            if (changeRequired) {
                li.with(br()).withText(" - becomes " + (rightParam.getRequired() ? "required" : "not required"));
            }
            if (changeType) {
                li.with(br()).withText(" - changes type");
            }
            if (changeDescription) {
                li.with(br()).withText(" - notes ").with(del(leftParam.getDescription()).withClass("comment")).withText(" change into ").with(span(span(null == rightParam.getDescription() ? "" : rightParam.getDescription()).withClass("comment")));
            }
        }
        if (!changeParam.isBackwardsCompatible()) {
            li.with(i_backwardsIncompatibilitiesWarning());
        }
        return li;
    }

    /** Add icon if modifications make backwards incompatibilies. */
    private ContainerTag i_backwardsIncompatibilitiesWarning() {
        return showBackwardsIncompatibilities ? i().withStyle("margin-left:0.5em;")
                .withClass("fas fa-exclamation-circle warnbackward").withTitle(NON_BACKWARDS_CHANGES) : null;
    }

}
