package com.deepoove.swagger.diff.output;

import static j2html.TagCreator.body;
import static j2html.TagCreator.del;
import static j2html.TagCreator.div;
import static j2html.TagCreator.document;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.li;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.ol;
import static j2html.TagCreator.span;
import static j2html.TagCreator.title;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.ChangedParameter;
import com.deepoove.swagger.diff.model.Endpoint;
import com.deepoove.swagger.diff.model.ResponseProperty;

import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import j2html.tags.ContainerTag;
import j2html.tags.Tag;

public class HtmlRender implements OutputRender {
	
	private String title = "API change log";
	private String linkCss = "demo.css";
	
	public HtmlRender() {
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
		Tag ol_changed = ol_changed(changedEndpoints);
		
//		System.out.println(ol_newEndpoint.render());
//		System.out.println(ol_missingEndpoint.render());
//		System.out.println(ol_changed.render());
		

		return reanderHtml(ol_newEndpoint, ol_missingEndpoint, ol_changed);
	}
	
	public String reanderHtml(Tag ol_new, Tag ol_miss, Tag ol_changed){
		ContainerTag html = html().attr("lang", "en").with(
			    head().with(
			    	meta().withCharset("utf-8"),
			        title(title),
			        link().withRel("stylesheet").withHref(linkCss)
			    ),
			    body().with(
			        header().with(h1(title)),
			        div().withClass("article").with(
			        		div().with(h2("What's New"),hr(),ol_new),
			        		div().with(h2("What's Deprecated"),hr(),ol_miss),
			        		div().with(h2("What's Changed"),hr(),ol_changed)
			        	)
			    )
			);
		
		return document().render() + html.render();
	}
	
	

	private ContainerTag ol_newEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints) return ol();
		ContainerTag ol = ol();
		for (Endpoint endpoint : endpoints) {
			ol.with(li_newEndpoint(endpoint.getMethod().toString(),
					endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return ol;
	}

	private ContainerTag li_newEndpoint(String method, String path,
			String desc) {
		return li().with(span(method).withClass(method)).withText(path + " ")
				.with(span(desc));
	}

	private ContainerTag ol_missingEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints) return ol();
		ContainerTag ol = ol();
		for (Endpoint endpoint : endpoints) {
			ol.with(li_missingEndpoint(endpoint.getMethod().toString(),
					endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return ol;
	}

	private ContainerTag li_missingEndpoint(String method, String path,
			String desc) {
		return li().with(span(method).withClass(method),
				del().withText(path + " ")).with(span(desc));
	}
	
	private Tag ol_changed(List<ChangedEndpoint> changedEndpoints){
		if (null == changedEndpoints) return ol();
		ContainerTag ol = ol();
		for (ChangedEndpoint changedEndpoint:changedEndpoints){
			String pathUrl = changedEndpoint.getPathUrl();
			Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();
			for (Entry<HttpMethod, ChangedOperation> entry : changedOperations.entrySet()){
				String method = entry.getKey().toString();
				ChangedOperation changedOperation = entry.getValue();
				String desc = changedOperation.getSummary();
				
				ContainerTag ul_detail = ul().withClass("detail");
				if (changedOperation.isDiffParam()){
					ul_detail.with(li().with(h3("参数")).with(ul_param(changedOperation)));
				}
				if (changedOperation.isDiffProp()){
					ul_detail.with(li().with(h3("返回类型")).with(ul_response(changedOperation)));
				}
				ol.with(li().with(span(method).withClass(method)).withText(pathUrl + " ").with(span(desc))
						.with(ul_detail));
			}
		}
		return ol;
	}

	private Tag ul_response(ChangedOperation changedOperation) {
		List<ResponseProperty> addProps = changedOperation.getAddProps();
		List<ResponseProperty> delProps = changedOperation.getMissingProps();
		ContainerTag ul = ul().withClass("change response");
		for (ResponseProperty prop : addProps){
			ul.with(li_addProp(prop));
		}
		for (ResponseProperty prop : delProps){
			ul.with(li_missingProp(prop));
		}
		return ul;
	}

	private Tag li_missingProp(ResponseProperty prop) {
		Property property = prop.getProperty();
		return li().withClass("missing").withText("Delete").with(del(prop.getEl())).with(span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
	}

	private Tag li_addProp(ResponseProperty prop) {
		Property property = prop.getProperty();
		return li().withText("Add " + prop.getEl()).with(span(null == property.getDescription() ? "" : ("//" + property.getDescription())).withClass("comment"));
	}

	private Tag ul_param(ChangedOperation changedOperation) {
			List<Parameter> addParameters = changedOperation.getAddParameters();
			List<Parameter> delParameters = changedOperation.getMissingParameters();
			List<ChangedParameter> changedParameters = changedOperation.getChangedParameter();
			ContainerTag ul = ul().withClass("change param");
			for (Parameter param : addParameters){
				ul.with(li_addParam(param));
			}
			for (ChangedParameter param : changedParameters){
				ul.with(li_changedParam(param));
			}
			for (Parameter param : delParameters){
				ul.with(li_missingParam(param));
			}
			return ul;
	}
	
	private Tag li_addParam(Parameter param){
		return li().withText("Add " + param.getName()).with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
	}
	private Tag li_missingParam(Parameter param){
		return li().withClass("missing").with(span("Delete")).with(del(param.getName())).with(span(null == param.getDescription() ? "" : ("//" + param.getDescription())).withClass("comment"));
	}
	private Tag li_changedParam(ChangedParameter changeParam){
		boolean changeRequired = changeParam.isChangeRequired();
		boolean changeDescription = changeParam.isChangeDescription();
		Parameter rightParam = changeParam.getRightParameter();
		Parameter leftParam = changeParam.getLeftParameter();
		ContainerTag li = li().withText(rightParam.getName());
		if (changeRequired){
			li.withText(" 修改为" + (rightParam.getRequired() ? "必填" : "非必填"));
		}
		if (changeDescription){
			li.withText(" 注释 ").with(del(leftParam.getDescription()).withClass("comment")).withText(" 改为 ").with(span(rightParam.getDescription()).withClass("comment"));
		}
		return li;
	}

}
