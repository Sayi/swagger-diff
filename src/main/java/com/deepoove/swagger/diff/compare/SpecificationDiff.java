package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedExtensionGroup;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.ChangedParameter;
import com.deepoove.swagger.diff.model.Endpoint;

import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

/**
 * compare two Swagger
 * 
 * @author Sayi
 *
 */
public class SpecificationDiff extends ChangedExtensionGroup {

	private List<Endpoint> newEndpoints;
	private List<Endpoint> missingEndpoints;
	private List<ChangedEndpoint> changedEndpoints;

	private SpecificationDiff() {
	}

	public static SpecificationDiff diff(Swagger oldSpec, Swagger newSpec) {
		return diff(oldSpec, newSpec, false);
	}

	public static SpecificationDiff diff(Swagger oldSpec, Swagger newSpec, boolean withExtensions) {
		SpecificationDiff instance = new SpecificationDiff();
		VendorExtDiffer extDiffer = new VendorExtDiffer(withExtensions);
		if (null == oldSpec || null == newSpec) {
			throw new IllegalArgumentException("cannot diff null spec.");
		}
		Map<String, Path> oldPaths = oldSpec.getPaths();
		Map<String, Path> newPaths = newSpec.getPaths();
		MapKeyDiff<String, Path> pathDiff = MapKeyDiff.diff(oldPaths, newPaths);
		instance.newEndpoints = convert2EndpointList(pathDiff.getIncreased());
		instance.missingEndpoints = convert2EndpointList(pathDiff.getMissing());
		instance.changedEndpoints = new ArrayList<ChangedEndpoint>();

		instance.setVendorExtsFromGroup(extDiffer.diff(oldSpec, newSpec));
		instance.putSubGroup("info", extDiffer.diff(oldSpec.getInfo(), newSpec.getInfo()));

		List<String> sharedKey = pathDiff.getSharedKey();
		ChangedEndpoint changedEndpoint = null;
		for (String pathUrl : sharedKey) {
			changedEndpoint = new ChangedEndpoint();
			changedEndpoint.setPathUrl(pathUrl);
			Path oldPath = oldPaths.get(pathUrl);
			Path newPath = newPaths.get(pathUrl);

			changedEndpoint.setVendorExtsFromGroup(extDiffer.diff(oldPath, newPath));

			Map<HttpMethod, Operation> oldOperationMap = oldPath.getOperationMap();
			Map<HttpMethod, Operation> newOperationMap = newPath.getOperationMap();
			MapKeyDiff<HttpMethod, Operation> operationDiff = MapKeyDiff.diff(oldOperationMap, newOperationMap);
			Map<HttpMethod, Operation> increasedOperation = operationDiff.getIncreased();
			Map<HttpMethod, Operation> missingOperation = operationDiff.getMissing();
			changedEndpoint.setNewOperations(increasedOperation);
			changedEndpoint.setMissingOperations(missingOperation);

			List<HttpMethod> sharedMethods = operationDiff.getSharedKey();
			Map<HttpMethod, ChangedOperation> operas = new HashMap<HttpMethod, ChangedOperation>();
			ChangedOperation changedOperation = null;
			for (HttpMethod method : sharedMethods) {
				changedOperation = new ChangedOperation();
				Operation oldOperation = oldOperationMap.get(method);
				Operation newOperation = newOperationMap.get(method);
				changedOperation.setSummary(newOperation.getSummary());

				changedOperation.setVendorExtsFromGroup(extDiffer.diff(oldOperation, newOperation));

				List<Parameter> oldParameters = oldOperation.getParameters();
				List<Parameter> newParameters = newOperation.getParameters();
				ParameterDiff parameterDiff = ParameterDiff
				.buildWithDefinition(oldSpec.getDefinitions(), newSpec.getDefinitions())
				.diff(oldParameters, newParameters);
				changedOperation.setAddParameters(parameterDiff.getIncreased());
				changedOperation.setMissingParameters(parameterDiff.getMissing());
				changedOperation.setChangedParameter(parameterDiff.getChanged());

				for (ChangedParameter param : parameterDiff.getChanged()) {
					param.setVendorExtsFromGroup(extDiffer.diff(param.getLeftParameter(), param.getRightParameter()));
				}

				Property oldResponseProperty = getResponseProperty(oldOperation);
				Property newResponseProperty = getResponseProperty(newOperation);
				PropertyDiff propertyDiff = PropertyDiff.buildWithDefinition(oldSpec.getDefinitions(),
					newSpec.getDefinitions());
				propertyDiff.diff(oldResponseProperty, newResponseProperty);
				changedOperation.setAddProps(propertyDiff.getIncreased());
				changedOperation.setMissingProps(propertyDiff.getMissing());

				changedOperation.putSubGroup("responses",
					extDiffer.diffResGroup(oldOperation.getResponses(), newOperation.getResponses()));

				if (changedOperation.isDiff()) {
					operas.put(method, changedOperation);
				}
			}
			changedEndpoint.setChangedOperations(operas);

			instance.newEndpoints
			.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getNewOperations()));
			instance.missingEndpoints
			.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getMissingOperations()));

			if (changedEndpoint.isDiff()) {
				instance.changedEndpoints.add(changedEndpoint);
			}
		}

		instance.putSubGroup("securityDefinitions",
			extDiffer.diffSecGroup(oldSpec.getSecurityDefinitions(), newSpec.getSecurityDefinitions()));

		instance.putSubGroup("tags",
			extDiffer.diffTagGroup(mapTagsByName(oldSpec.getTags()), mapTagsByName(newSpec.getTags())));

		return instance;

	}

	private static Map<String, Tag> mapTagsByName(List<Tag> tags) {
		Map<String, Tag> mappedTags = new LinkedHashMap<String, Tag>();
		for (Tag tag : tags) {
			mappedTags.put(tag.getName(), tag);
		}
		return mappedTags;
	}

	private static Property getResponseProperty(Operation operation) {
		Map<String, Response> responses = operation.getResponses();
		// temporary workaround for missing response messages
		if (responses == null)
			return null;
		Response response = responses.get("200");
		return null == response ? null : response.getSchema();
	}

	private static List<Endpoint> convert2EndpointList(Map<String, Path> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		for (Entry<String, Path> entry : map.entrySet()) {
			String url = entry.getKey();
			Path path = entry.getValue();

			Map<HttpMethod, Operation> operationMap = path.getOperationMap();
			for (Entry<HttpMethod, Operation> entryOper : operationMap.entrySet()) {
				HttpMethod httpMethod = entryOper.getKey();
				Operation operation = entryOper.getValue();

				Endpoint endpoint = new Endpoint();
				endpoint.setPathUrl(url);
				endpoint.setMethod(httpMethod);
				endpoint.setSummary(operation.getSummary());
				endpoint.setPath(path);
				endpoint.setOperation(operation);
				endpoints.add(endpoint);
			}
		}
		return endpoints;
	}

	private static Collection<? extends Endpoint> convert2EndpointList(String pathUrl, Map<HttpMethod, Operation> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		for (Entry<HttpMethod, Operation> entry : map.entrySet()) {
			HttpMethod httpMethod = entry.getKey();
			Operation operation = entry.getValue();
			Endpoint endpoint = new Endpoint();
			endpoint.setPathUrl(pathUrl);
			endpoint.setMethod(httpMethod);
			endpoint.setSummary(operation.getSummary());
			endpoint.setOperation(operation);
			endpoints.add(endpoint);
		}
		return endpoints;
	}

	public List<Endpoint> getNewEndpoints() {
		return newEndpoints;
	}

	public List<Endpoint> getMissingEndpoints() {
		return missingEndpoints;
	}

	public List<ChangedEndpoint> getChangedEndpoints() {
		return changedEndpoints;
	}

	private static class VendorExtDiffer {

		private boolean withExts;

		private VendorExtDiffer(boolean withExts) {
			this.withExts = withExts;
		}

		public ChangedExtensionGroup diff(Parameter left, Parameter right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		public ChangedExtensionGroup diff(Operation left, Operation right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		public ChangedExtensionGroup diff(Swagger left, Swagger right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		public ChangedExtensionGroup diff(Info left, Info right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		public ChangedExtensionGroup diff(Path left, Path right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		public ChangedExtensionGroup diff(Response left, Response right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		public ChangedExtensionGroup diff(Tag left ,Tag right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		public ChangedExtensionGroup diff(SecuritySchemeDefinition left, SecuritySchemeDefinition right) {
			return diff(left.getVendorExtensions(), right.getVendorExtensions());
		}

		private ChangedExtensionGroup diff(Map<String, Object> oldExts, Map<String, Object> newExts) {
			ChangedExtensionGroup group = new ChangedExtensionGroup();
			if (withExts) {
				MapDiff<String, Object> mapDiff = MapDiff.diff(oldExts, newExts);
				group.setMissingVendorExtensions(mapDiff.getMissing());
				group.setChangedVendorExtensions(mapDiff.getChanged());
				group.setIncreasedVendorExtensions(mapDiff.getIncreased());
			}
			return group;
		}

		public ChangedExtensionGroup diffTagGroup(Map<String, Tag> left, Map<String, Tag> right) {
			MapDiff<String, Tag> responseDiff = MapDiff.diff(left, right);
			ChangedExtensionGroup responseGroup = new ChangedExtensionGroup();
			for (Entry<String, Pair<Tag, Tag>> entry : responseDiff.getChanged().entrySet()) {
				String code = entry.getKey();
				Tag oldVal = entry.getValue().getLeft();
				Tag newVal = entry.getValue().getRight();
				responseGroup.putSubGroup(code, diff(oldVal, newVal));
			}
			return responseGroup;
		}

		public ChangedExtensionGroup diffSecGroup(Map<String, SecuritySchemeDefinition> left, Map<String, SecuritySchemeDefinition> right) {
			MapDiff<String, SecuritySchemeDefinition> responseDiff = MapDiff.diff(left, right);
			ChangedExtensionGroup responseGroup = new ChangedExtensionGroup();
			for (Entry<String, Pair<SecuritySchemeDefinition, SecuritySchemeDefinition>> entry : responseDiff.getChanged().entrySet()) {
				String code = entry.getKey();
				SecuritySchemeDefinition oldVal = entry.getValue().getLeft();
				SecuritySchemeDefinition newVal = entry.getValue().getRight();
				responseGroup.putSubGroup(code, diff(oldVal, newVal));
			}
			return responseGroup;
		}

		public ChangedExtensionGroup diffResGroup(Map<String, Response> left, Map<String, Response> right) {
			MapDiff<String, Response> responseDiff = MapDiff.diff(left, right);
			ChangedExtensionGroup responseGroup = new ChangedExtensionGroup();
			for (Entry<String, Pair<Response, Response>> entry : responseDiff.getChanged().entrySet()) {
				String code = entry.getKey();
				Response oldVal = entry.getValue().getLeft();
				Response newVal = entry.getValue().getRight();
				responseGroup.putSubGroup(code, diff(oldVal, newVal));
			}
			return responseGroup;
		}
	}
}
