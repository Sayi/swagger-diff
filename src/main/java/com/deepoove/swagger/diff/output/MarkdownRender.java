package com.deepoove.swagger.diff.output;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.ChangedParameter;
import com.deepoove.swagger.diff.model.ChangedVendorExtensionGroup;
import com.deepoove.swagger.diff.model.Endpoint;
import com.deepoove.swagger.diff.model.ElProperty;

import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class MarkdownRender implements Render {

	final String H3 = "### ";
	final String H2 = "## ";
	final String BLOCKQUOTE = "> ";
	final String CODE = "`";
	final String PRE_CODE = "    ";
	final String PRE_LI = "    ";
	final String LI = "* ";
	final String HR = "---\n\n";

	String IT = "_";
	String BD = "__";
	String ST = "~~";
	String RIGHT_ARROW = "&rarr;";

	public MarkdownRender() {}

	public String render(SwaggerDiff diff) {
	  List<Endpoint> newEndpoints = diff.getNewEndpoints();
		String ol_newEndpoint = ol_newEndpoint(newEndpoints);

		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		String ol_missingEndpoint = ol_missingEndpoint(missingEndpoints);

    String ol_topLevelVendorExt = ol_vendorExtSummary(diff.getChangedTopLevelVendorExtensions(), "");

		List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
		String ol_changed = ol_changed(changedEndpoints);

		String ol_changeSummary = ol_topLevelVendorExt + ol_changed;

		return renderMarkdown(diff.getOldVersion(), diff.getNewVersion(), ol_newEndpoint, ol_missingEndpoint, ol_changeSummary);
	}

	public String renderBasic(SwaggerDiff diff) {
		MarkdownRender renderer = new MarkdownRender();
		renderer.IT = "";
		renderer.BD = "";
		renderer.ST = "";
		renderer.RIGHT_ARROW = "->";
		return renderer.render(diff);
	}

	private String renderMarkdown(String oldVersion, String newVersion, String ol_new, String ol_miss,
                               String ol_changed) {
		StringBuffer sb = new StringBuffer();
		sb.append(H2).append("Version " + oldVersion + " to " + newVersion + "\n").append(HR);
		sb.append(H3).append("What's New\n").append(HR)
				.append(ol_new).append("\n").append(H3)
				.append("What's Deprecated\n").append(HR)
				.append(ol_miss).append("\n").append(H3)
				.append("What's Changed\n").append(HR)
				.append(ol_changed);
		return sb.toString();
	}

	private String ol_newEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints) return "";
		StringBuffer sb = new StringBuffer();
		for (Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint.getMethod().toString(),
					endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return sb.toString();
	}

	private String li_newEndpoint(String method, String path, String desc) {
		StringBuffer sb = new StringBuffer();
		sb.append(LI).append(CODE).append(method).append(CODE)
				.append(" " + path).append(" " + desc + "\n");
		return sb.toString();
	}

	private String ol_missingEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints) return "";
		StringBuffer sb = new StringBuffer();
		for (Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint.getMethod().toString(),
					endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return sb.toString();
	}

	private String ol_changed(List<ChangedEndpoint> changedEndpoints) {
		if (null == changedEndpoints) return "";

		String detailPrefix = PRE_LI + PRE_LI;
		String detailTitlePrefix = detailPrefix + LI + BD;
		String operationPrefix = PRE_LI + LI + CODE;

		StringBuffer sb = new StringBuffer();
		for (ChangedEndpoint changedEndpoint : changedEndpoints) {
			String pathUrl = changedEndpoint.getPathUrl();
			Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint
					.getChangedOperations();

			sb.append(LI).append(pathUrl).append("\n");

			if (changedEndpoint.vendorExtensionsAreDiff()) {
				sb.append(ol_vendorExtSummary(changedEndpoint, "Vendor Extensions", PRE_LI));
			}

			for (Entry<HttpMethod, ChangedOperation> entry : changedOperations
					.entrySet()) {
				String method = entry.getKey().toString();
				ChangedOperation changedOperation = entry.getValue();
				String desc = changedOperation.getSummary();

				StringBuffer ul_detail = new StringBuffer();
				if (changedOperation.vendorExtensionsAreDiff()) {
					ul_detail.append(ol_vendorExtSummary(changedOperation, detailPrefix));
				}
				if (changedOperation.isDiffParam()) {
					ul_detail.append(detailTitlePrefix).append("Parameters")
							.append(BD).append(ul_param(changedOperation));
				}
				if (changedOperation.isDiffProp()) {
					ul_detail.append(detailTitlePrefix).append("Return Type")
							.append(BD).append(ul_response(changedOperation));
				}
				sb.append(operationPrefix).append(method).append(CODE)
						.append(" - " + desc + "  \n")
						.append(ul_detail);
			}
		}
		return sb.toString();
	}

	private String ol_vendorExtSummary(ChangedVendorExtensionGroup container, String pre) {
		return ol_vendorExtSummary(container, null, pre);
	}

	private String ol_vendorExtSummary(ChangedVendorExtensionGroup container, String title, String pre) {
		if (!container.vendorExtensionsAreDiff()) return "";

		Map<String, Object> increased = container.getIncreasedVendorExtensions();
		Map<String, Object> missing = container.getMissingVendorExtensions();
		Map<String, Pair<Object, Object>> changed = container.getChangedVendorExtensions();
		Map<String, ChangedVendorExtensionGroup> subgroups = container.getChangedSubGroups();

		String titlePrefix = pre + LI + BD;
		String vendorExtPrefix = pre + LI;
		String changedExtPre = pre + LI + CODE;
		String changedExtMid = CODE + ": " + ST;
		String changedExtArr = ST + " " + RIGHT_ARROW + " " + IT;
		String incr = "";

		StringBuffer sb = new StringBuffer();

		if (title != null) {
			sb.append(titlePrefix).append(title).append(BD + "\n");
			vendorExtPrefix = PRE_LI + vendorExtPrefix;
			changedExtPre = PRE_LI + changedExtPre;
			incr = PRE_LI;
		}

		for (String vendorExtension : increased.keySet()) {
			sb.append(vendorExtPrefix).append("Add ")
          .append(CODE).append(vendorExtension).append(CODE + "\n");
		}

		for (String vendorExtension : missing.keySet()) {
			sb.append(vendorExtPrefix).append("Remove ")
          .append(CODE).append(vendorExtension).append(CODE + "\n");
		}

		for (String vendorExtension : changed.keySet()) {
			Object left = changed.get(vendorExtension).getLeft();
			Object right = changed.get(vendorExtension).getRight();
			sb.append(changedExtPre).append(vendorExtension).append(changedExtMid)
					.append(left.toString()).append(changedExtArr).append(right.toString())
					.append(IT + "\n");
		}

		for (String groupName : subgroups.keySet()) {
			sb.append(ol_vendorExtSummary(subgroups.get(groupName), groupName, pre + incr));
		}

		return sb.toString();
	}

	private String ul_response(ChangedOperation changedOperation) {
		List<ElProperty> addProps = changedOperation.getAddProps();
		List<ElProperty> delProps = changedOperation.getMissingProps();
		List<ElProperty> changedProps = changedOperation.getChangedProps();
		StringBuffer sb = new StringBuffer("\n");

		String prefix = PRE_LI + PRE_LI + PRE_CODE + LI;
		for (ElProperty prop : addProps) {
			sb.append(prefix).append(li_addProp(prop) + "\n");
		}
		for (ElProperty prop : delProps) {
			sb.append(prefix).append(li_missingProp(prop) + "\n");
		}
		for (ElProperty prop : changedProps) {
			sb.append(prefix).append(ol_vendorExtSummary(prop, PRE_LI + PRE_LI + PRE_LI));
		}
		return sb.toString();
	}

	private String li_missingProp(ElProperty prop) {
		Property property = prop.getProperty();
		String prefix = "Delete " + CODE;
		String desc = " //" + property.getDescription();
		String postfix = CODE +
				(null == property.getDescription() ? "" : desc);

		StringBuffer sb = new StringBuffer("");
		sb.append(prefix).append(prop.getEl())
				.append(postfix);
		return sb.toString();
	}

	private String li_addProp(ElProperty prop) {
		Property property = prop.getProperty();
		String prefix = "Add " + CODE;
		String desc = " //" + property.getDescription();
		String postfix = CODE +
				(null == property.getDescription() ? "" : desc);

		StringBuffer sb = new StringBuffer("");
		sb.append(prefix).append(prop.getEl())
				.append(postfix);
		return sb.toString();
	}

	private String ul_param(ChangedOperation changedOperation) {
		List<Parameter> addParameters = changedOperation.getAddParameters();
		List<Parameter> delParameters = changedOperation.getMissingParameters();
		List<ChangedParameter> changedParameters = changedOperation
				.getChangedParameter();

		String prefix = PRE_LI + PRE_LI + PRE_CODE + LI;

		StringBuffer sb = new StringBuffer("\n");

		for (Parameter param : addParameters) {
			sb.append(prefix).append(li_addParam(param) + "\n");
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> increased = param.getIncreased();
			for (ElProperty prop : increased) {
				sb.append(prefix).append(li_addProp(prop) + "\n");
			}
		}
		for (ChangedParameter param : changedParameters) {
			boolean changeRequired = param.isChangeRequired();
			boolean changeDescription = param.isChangeDescription();
			boolean changeVendorExts = param.vendorExtensionsAreDiff();

			if (changeRequired || changeDescription || changeVendorExts) {
				sb.append(prefix).append(li_changedParam(param));
			}
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> missing = param.getMissing();
			for (ElProperty prop : missing) {
				sb.append(prefix).append(li_missingProp(prop) + "\n");
			}
		}
		for (Parameter param : delParameters) {
			sb.append(prefix).append(li_missingParam(param) + "\n");
		}
		return sb.toString();
	}

	private String li_addParam(Parameter param) {
		String prefix = "Add " + CODE;
		String desc = " //" + param.getDescription();
		String postfix = CODE +
				(null == param.getDescription() ? "" : desc);

		StringBuffer sb = new StringBuffer("");
		sb.append(prefix).append(param.getName())
				.append(postfix);
		return sb.toString();
	}

	private String li_missingParam(Parameter param) {
		StringBuffer sb = new StringBuffer("");
		String prefix = "Delete " + CODE;
		String desc = " //" + param.getDescription();
		String postfix = CODE +
				(null == param.getDescription() ? "" : desc);
		sb.append(prefix).append(param.getName())
				.append(postfix);
		return sb.toString();
	}

	private String li_changedParam(ChangedParameter changeParam) {
		boolean changeRequired = changeParam.isChangeRequired();
		boolean changeDescription = changeParam.isChangeDescription();
		boolean vendorExtsChanged = changeParam.vendorExtensionsAreDiff();
		Parameter rightParam = changeParam.getRightParameter();
		Parameter leftParam = changeParam.getLeftParameter();

		String vendorExtPrefix = PRE_LI + PRE_LI + PRE_LI + PRE_LI;

		StringBuffer sb = new StringBuffer("");
		sb.append(CODE + rightParam.getName() + CODE);
		if (changeRequired) {
			sb.append(" change into " + (rightParam.getRequired() ? "required" : "not required"));
		}
		if (changeDescription) {
			sb.append(" Notes ").append(leftParam.getDescription()).append(" change into ")
					.append(rightParam.getDescription());
		}
		if (vendorExtsChanged) {
			sb.append(" vendor extensions Changed\n");
			sb.append(ol_vendorExtSummary(changeParam, vendorExtPrefix));
		} else {
			sb.append("\n");
		}
		return sb.toString();
	}

}
