package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.ChangedParameter;
import com.deepoove.swagger.diff.model.ChangedExtensionGroup;
import com.deepoove.swagger.diff.model.Endpoint;

import io.swagger.models.HttpMethod;
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
public class SpecificationDiff {

	private List<Endpoint> newEndpoints;
	private List<Endpoint> missingEndpoints;
	private List<ChangedEndpoint> changedEndpoints;

	private ChangedExtensionGroup nonPathVendorExtGroup = new ChangedExtensionGroup();

	private SpecificationDiff() {
	}

	public static SpecificationDiff diff(Swagger oldSpec, Swagger newSpec) {
		return diff(oldSpec, newSpec, true);
	}

	public static SpecificationDiff diff(Swagger oldSpec, Swagger newSpec, boolean withExtensions) {
		SpecificationDiff instance = new SpecificationDiff();
		if (null == oldSpec || null == newSpec) {
			throw new IllegalArgumentException("cannot diff null spec.");
		}
		Map<String, Path> oldPaths = oldSpec.getPaths();
		Map<String, Path> newPaths = newSpec.getPaths();
		MapKeyDiff<String, Path> pathDiff = MapKeyDiff.diff(oldPaths, newPaths);
		instance.newEndpoints = convert2EndpointList(pathDiff.getIncreased());
		instance.missingEndpoints = convert2EndpointList(pathDiff.getMissing());
		instance.changedEndpoints = new ArrayList<ChangedEndpoint>();

		System.out.println(oldSpec.getVendorExtensions());

		Map<String, Object> oldExts;
		Map<String, Object> newExts;

		if (withExtensions) {

			oldExts = oldSpec.getVendorExtensions();
			newExts = newSpec.getVendorExtensions();
			instance.nonPathVendorExtGroup.setVendorExtsFromGroup(getChangedVendorExtsGroup( oldExts, newExts));

			oldExts = oldSpec.getInfo().getVendorExtensions();
			newExts = newSpec.getInfo().getVendorExtensions();
			instance.nonPathVendorExtGroup.getChangedSubGroups()
					.put("info", getChangedVendorExtsGroup(oldExts, newExts));
		}

		List<String> sharedKey = pathDiff.getSharedKey();
		ChangedEndpoint changedEndpoint = null;
		for (String pathUrl : sharedKey) {
			changedEndpoint = new ChangedEndpoint();
			changedEndpoint.setPathUrl(pathUrl);
			Path oldPath = oldPaths.get(pathUrl);
			Path newPath = newPaths.get(pathUrl);

			if (withExtensions) {
				oldExts = oldPath.getVendorExtensions();
				newExts = newPath.getVendorExtensions();
				changedEndpoint.setVendorExtsFromGroup(getChangedVendorExtsGroup(oldExts, newExts));
			}

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

				if (withExtensions) {
					oldExts = oldOperation.getVendorExtensions();
					newExts = newOperation.getVendorExtensions();
					changedOperation.setVendorExtsFromGroup(getChangedVendorExtsGroup( oldExts, newExts));
				}

				List<Parameter> oldParameters = oldOperation.getParameters();
				List<Parameter> newParameters = newOperation.getParameters();
				ParameterDiff parameterDiff = ParameterDiff
						.buildWithDefinition(oldSpec.getDefinitions(), newSpec.getDefinitions())
						.diff(oldParameters, newParameters);
				changedOperation.setAddParameters(parameterDiff.getIncreased());
				changedOperation.setMissingParameters(parameterDiff.getMissing());
				changedOperation.setChangedParameter(parameterDiff.getChanged());

				if (withExtensions) {
					for (ChangedParameter param : parameterDiff.getChanged()) {
						oldExts = param.getLeftParameter().getVendorExtensions();
						newExts = param.getRightParameter().getVendorExtensions();
						param.setVendorExtsFromGroup(getChangedVendorExtsGroup(oldExts, newExts));
					}
				}

				Property oldResponseProperty = getResponseProperty(oldOperation);
				Property newResponseProperty = getResponseProperty(newOperation);
				PropertyDiff propertyDiff = PropertyDiff.buildWithDefinition(oldSpec.getDefinitions(),
						newSpec.getDefinitions());
				propertyDiff.diff(oldResponseProperty, newResponseProperty);
				changedOperation.setAddProps(propertyDiff.getIncreased());
				changedOperation.setMissingProps(propertyDiff.getMissing());

				if (withExtensions) {
					Map<String, Response> oldRes = oldOperation.getResponses();
					Map<String, Response> newRes = newOperation.getResponses();
					MapKeyDiff<String, Response> responseDiff = MapKeyDiff.diff(oldRes, newRes);
					ChangedExtensionGroup responseGroup = new ChangedExtensionGroup();
					changedOperation.putSubGroup("responses", responseGroup);
					for (String key : responseDiff.getSharedKey()) {
						ChangedExtensionGroup group = getChangedVendorExtsGroup(
								oldRes.get(key).getVendorExtensions(), newRes.get(key).getVendorExtensions());
						responseGroup.putSubGroup(key, group);
					}
				}

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

		if (withExtensions) {
			ChangedExtensionGroup securityDefsGroup = new ChangedExtensionGroup();
			Map<String, SecuritySchemeDefinition> oldDefs = oldSpec.getSecurityDefinitions();
			Map<String, SecuritySchemeDefinition> newDefs = newSpec.getSecurityDefinitions();

			MapKeyDiff<String, SecuritySchemeDefinition> securityDefsDiff = MapKeyDiff.diff(oldDefs, newDefs);
			for (String key : securityDefsDiff.getSharedKey()) {
				securityDefsGroup.getChangedSubGroups().put(key, getChangedVendorExtsGroup(
						oldDefs.get(key).getVendorExtensions(), newDefs.get(key).getVendorExtensions()));
			}
			instance.nonPathVendorExtGroup.getChangedSubGroups().put("securityDefinitions", securityDefsGroup);

			ChangedExtensionGroup tagsGroup = new ChangedExtensionGroup();
			Map<String, Tag> oldTags = mapTagsByName(oldSpec.getTags());
			Map<String, Tag> newTags = mapTagsByName(newSpec.getTags());

			MapKeyDiff<String, Tag> tagDiff = MapKeyDiff.diff(oldTags, newTags);
			for (String key : tagDiff.getSharedKey()) {
				tagsGroup.getChangedSubGroups().put(key, getChangedVendorExtsGroup(
						oldSpec.getTag(key).getVendorExtensions(), newSpec.getTag(key).getVendorExtensions()));
			}
			instance.nonPathVendorExtGroup.getChangedSubGroups().put("tags", tagsGroup);
		}

		return instance;

	}

	private static Map<String, Tag> mapTagsByName(List<Tag> tags) {
		Map<String, Tag> mappedTags = new LinkedHashMap<String, Tag>();
		for (Tag tag : tags) {
			mappedTags.put(tag.getName(), tag);
		}
		return mappedTags;
	}

	private static ChangedExtensionGroup getChangedVendorExtsGroup(
			Map<String, Object> oldExts, Map<String, Object> newExts) {
		MapDiff<String, Object> mapDiff = MapDiff.diff(oldExts, newExts);
		ChangedExtensionGroup group = new ChangedExtensionGroup();
		group.setMissingVendorExtensions(mapDiff.getMissing());
		group.setIncreasedVendorExtensions(mapDiff.getIncreased());
		group.setChangedVendorExtensions(mapDiff.getChanged());
		return group;
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

	public ChangedExtensionGroup getNonPathVendorExtGroup() {
		return nonPathVendorExtGroup;
	}
}
