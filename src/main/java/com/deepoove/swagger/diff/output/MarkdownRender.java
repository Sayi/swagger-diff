package com.deepoove.swagger.diff.output;

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

public class MarkdownRender implements Render {
	final String H3 = "### ";

	final String H2 = "## ";

	final String BLOCKQUOTE = "> ";

	final String CODE = "`";

	final String PRE_CODE = "    ";

	final String PRE_LI = "    ";

	final String LI = "* ";

	final String HR = "---\n";

	public MarkdownRender() {}

	@Override
	public String render(final SwaggerDiff diff) {
		final List<Endpoint> newEndpoints = diff.getNewEndpoints();
		final String ol_newEndpoint = ol_newEndpoint(newEndpoints);

		final List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		final String ol_missingEndpoint = ol_missingEndpoint(missingEndpoints);

		final List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
		final String ol_changed = ol_changed(changedEndpoints);

		return renderHtml(diff.getOldVersion(), diff.getNewVersion(), ol_newEndpoint, ol_missingEndpoint, ol_changed);
	}

	public String renderHtml(final String oldVersion,
			final String newVersion,
			final String ol_new,
			final String ol_miss,
			final String ol_changed) {
		final StringBuffer sb = new StringBuffer().append(H2)
				.append("Version " + oldVersion + " to " + newVersion)
				.append("\n")
				.append(HR);

		if (!isEmpty(ol_new)) {
			sb.append(H3).append("What's New").append("\n").append(HR).append(ol_new).append("\n");
		}

		if (!isEmpty(ol_miss)) {
			sb.append(H3).append("What's Deprecated").append("\n").append(HR).append(ol_miss).append("\n");
		}

		if (!isEmpty(ol_changed)) {
			sb.append(H3).append("What's Changed").append("\n").append(HR).append(ol_changed);
		}

		if (isEmpty(ol_new) && isEmpty(ol_miss) && isEmpty(ol_changed)) {
			sb.append("No changes detected");
		}

		return sb.toString();
	}

	private String ol_newEndpoint(final List<Endpoint> endpoints) {
		if (null == endpoints) {
			return "";
		}
		final StringBuffer sb = new StringBuffer();
		for (final Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint.getMethod().toString(), endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return sb.toString();
	}

	private String li_newEndpoint(final String method, final String path, final String desc) {
		final StringBuffer sb = new StringBuffer();
		sb.append(LI).append(CODE).append(method).append(CODE).append(" " + path).append(" " + desc + "\n");
		return sb.toString();
	}

	private String ol_missingEndpoint(final List<Endpoint> endpoints) {
		if (null == endpoints) {
			return "";
		}
		final StringBuffer sb = new StringBuffer();
		for (final Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint.getMethod().toString(), endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return sb.toString();
	}

	private String ol_changed(final List<ChangedEndpoint> changedEndpoints) {
		if (null == changedEndpoints) {
			return "";
		}
		final StringBuffer sb = new StringBuffer();
		for (final ChangedEndpoint changedEndpoint : changedEndpoints) {
			final String pathUrl = changedEndpoint.getPathUrl();
			final Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();
			for (final Entry<HttpMethod, ChangedOperation> entry : changedOperations.entrySet()) {
				final String method = entry.getKey().toString();
				final ChangedOperation changedOperation = entry.getValue();
				final String desc = changedOperation.getSummary();

				final StringBuffer ul_detail = new StringBuffer();
				if (changedOperation.isDiffParam()) {
					ul_detail.append(PRE_LI).append("Parameters").append(ul_param(changedOperation));
				}
				if (changedOperation.isDiffProp()) {
					ul_detail.append(PRE_LI).append("Return Type").append(ul_response(changedOperation));
				}
				if (changedOperation.isDiffProduces()) {
					ul_detail.append(PRE_LI).append("Produces").append(ul_produce(changedOperation));
				}
				if (changedOperation.isDiffConsumes()) {
					ul_detail.append(PRE_LI).append("Consumes").append(ul_consume(changedOperation));
				}
				sb.append(CODE)
						.append(method)
						.append(CODE)
						.append(" " + pathUrl)
						.append(" " + desc + "  \n")
						.append(ul_detail);
			}
		}
		return sb.toString();
	}

	private String ul_response(final ChangedOperation changedOperation) {
		final List<ElProperty> addProps = changedOperation.getAddProps();
		final List<ElProperty> delProps = changedOperation.getMissingProps();
		final List<ElProperty> changedProps = changedOperation.getChangedProps();
		final StringBuffer sb = new StringBuffer("\n\n");

		final String prefix = PRE_LI + PRE_CODE;
		for (final ElProperty prop : addProps) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addProp(prop) + "\n");
		}
		for (final ElProperty prop : delProps) {
			sb.append(prefix).append(li_missingProp(prop) + "\n");
		}
		for (final ElProperty prop : changedProps) {
			sb.append(prefix).append(li_changedProp(prop) + "\n");
		}
		return sb.toString();
	}

	private String li_missingProp(final ElProperty prop) {
		final Property property = prop.getProperty();
		final StringBuffer sb = new StringBuffer("");
		sb.append("Delete ")
				.append(prop.getEl())
				.append(null == property.getDescription() ? "" : " //" + property.getDescription());
		return sb.toString();
	}

	private String li_addProp(final ElProperty prop) {
		final Property property = prop.getProperty();
		final StringBuffer sb = new StringBuffer("");
		sb.append("Insert ")
				.append(prop.getEl())
				.append(null == property.getDescription() ? "" : " //" + property.getDescription());
		return sb.toString();
	}

	private String li_changedProp(final ElProperty prop) {
		final Property property = prop.getProperty();
		final String prefix = "Modify ";
		final String desc = " //" + property.getDescription();
		final String postfix = null == property.getDescription() ? "" : desc;

		final StringBuffer sb = new StringBuffer("");
		sb.append(prefix).append(prop.getEl()).append(postfix);
		return sb.toString();
	}

	private String ul_param(final ChangedOperation changedOperation) {
		final List<Parameter> addParameters = changedOperation.getAddParameters();
		final List<Parameter> delParameters = changedOperation.getMissingParameters();
		final List<ChangedParameter> changedParameters = changedOperation.getChangedParameter();
		final StringBuffer sb = new StringBuffer("\n\n");
		for (final Parameter param : addParameters) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addParam(param) + "\n");
		}
		for (final ChangedParameter param : changedParameters) {
			final List<ElProperty> increased = param.getIncreased();
			for (final ElProperty prop : increased) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_addProp(prop) + "\n");
			}
		}
		for (final ChangedParameter param : changedParameters) {
			final boolean changeRequired = param.isChangeRequired();
			final boolean changeDescription = param.isChangeDescription();
			if (changeRequired || changeDescription) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_changedParam(param) + "\n");
			}
		}
		for (final ChangedParameter param : changedParameters) {
			final List<ElProperty> missing = param.getMissing();
			final List<ElProperty> changed = param.getChanged();
			for (final ElProperty prop : missing) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_missingProp(prop) + "\n");
			}
			for (final ElProperty prop : changed) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_changedProp(prop) + "\n");
			}
		}
		for (final Parameter param : delParameters) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_missingParam(param) + "\n");
		}
		return sb.toString();
	}

	private String li_addParam(final Parameter param) {
		final StringBuffer sb = new StringBuffer("");
		sb.append("Add ")
				.append(param.getName())
				.append(null == param.getDescription() ? "" : " //" + param.getDescription());
		return sb.toString();
	}

	private String li_missingParam(final Parameter param) {
		final StringBuffer sb = new StringBuffer("");
		sb.append("Delete ")
				.append(param.getName())
				.append(null == param.getDescription() ? "" : " //" + param.getDescription());
		return sb.toString();
	}

	private String li_changedParam(final ChangedParameter changeParam) {
		final boolean changeRequired = changeParam.isChangeRequired();
		final boolean changeDescription = changeParam.isChangeDescription();
		final Parameter rightParam = changeParam.getRightParameter();
		final Parameter leftParam = changeParam.getLeftParameter();
		final StringBuffer sb = new StringBuffer("");
		sb.append(rightParam.getName());
		if (changeRequired) {
			sb.append(" change into " + (rightParam.getRequired() ? "required" : "not required"));
		}
		if (changeDescription) {
			sb.append(" Notes ")
					.append(leftParam.getDescription())
					.append(" change into ")
					.append(rightParam.getDescription());
		}
		return sb.toString();
	}

	private String ul_produce(final ChangedOperation changedOperation) {
		final List<String> addProduce = changedOperation.getAddProduces();
		final List<String> delProduce = changedOperation.getMissingProduces();
		final StringBuffer sb = new StringBuffer("\n\n");

		final String prefix = PRE_LI + PRE_CODE;
		for (final String mt : addProduce) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addMediaType(mt) + "\n");
		}
		for (final String mt : delProduce) {
			sb.append(prefix).append(li_missingMediaType(mt) + "\n");
		}
		return sb.toString();
	}

	private String ul_consume(final ChangedOperation changedOperation) {
		final List<String> addConsume = changedOperation.getAddConsumes();
		final List<String> delConsume = changedOperation.getMissingConsumes();
		final StringBuffer sb = new StringBuffer("\n\n");

		final String prefix = PRE_LI + PRE_CODE;
		for (final String mt : addConsume) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addMediaType(mt) + "\n");
		}
		for (final String mt : delConsume) {
			sb.append(prefix).append(li_missingMediaType(mt) + "\n");
		}
		return sb.toString();
	}

	private String li_missingMediaType(final String type) {
		final StringBuffer sb = new StringBuffer("");
		sb.append("Delete ").append(type);
		return sb.toString();
	}

	private String li_addMediaType(final String type) {
		final StringBuffer sb = new StringBuffer("");
		sb.append("Insert ").append(type);
		return sb.toString();
	}

	private boolean isEmpty(final String item) {
		return null == item || item.isEmpty();
	}
}
