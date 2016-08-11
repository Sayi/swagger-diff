package com.deepoove.swagger.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deepoove.swagger.diff.compare.MapKeyDiff;
import com.deepoove.swagger.diff.compare.ParameterDiff;
import com.deepoove.swagger.diff.compare.PropertyDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.ChangedOperation;
import com.deepoove.swagger.diff.model.Endpoint;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerCompatConverter;
import io.swagger.parser.SwaggerParser;

public class SwaggerDiff {
	
	public static final String SWAGGER_VERSION_V2 = "2.0";
	
	private static Logger logger = LoggerFactory.getLogger(SwaggerDiff.class);

	private Swagger oldSpecSwagger;
	private Swagger newSpecSwagger;

	private List<Endpoint> newEndpoints;
	private List<Endpoint> missingEndpoints;
	private List<ChangedEndpoint> changedEndpoints;

	/**
	 * defaut version is 1.x
	 * 
	 * @param oldSpec
	 * @param newSpec
	 */
	public SwaggerDiff(String oldSpec, String newSpec) {
		this(oldSpec, newSpec, null);
	}

	/**
	 * @param oldSpec
	 * @param newSpec
	 * @param version
	 *            SWAGGER_VERSION_V2
	 */
	public SwaggerDiff(String oldSpec, String newSpec, String version) {
		this(oldSpec, newSpec, null, version);
	}
	
	/**
	 * @param oldSpec
	 * @param newSpec
	 * @param auths
	 * @param version
	 */
	public SwaggerDiff(String oldSpec, String newSpec, List<AuthorizationValue> auths, String version) {
		if (SWAGGER_VERSION_V2.equals(version)) {
			SwaggerParser swaggerParser = new SwaggerParser();
			oldSpecSwagger = swaggerParser.read(oldSpec, auths, true);
			newSpecSwagger = swaggerParser.read(newSpec, auths, true);
		} else {
			SwaggerCompatConverter swaggerCompatConverter = new SwaggerCompatConverter();
			try {
				oldSpecSwagger = swaggerCompatConverter.read(oldSpec, auths);
				newSpecSwagger = swaggerCompatConverter.read(newSpec, auths);
			} catch (IOException e) {
				logger.error("cannot read api-doc from spec[version_v1.x]", e);
				return;
			}
		}
		if (null == oldSpecSwagger
				|| null == newSpecSwagger) { throw new RuntimeException(
						"cannot read api-doc from spec."); }
	}

	public SwaggerDiff compare() {
		Map<String, Path> oldPaths = oldSpecSwagger.getPaths();
		Map<String, Path> newPaths = newSpecSwagger.getPaths();
		MapKeyDiff<String, Path> pathDiff = MapKeyDiff.diff(oldPaths, newPaths);
		this.newEndpoints = convert2EndpointList(pathDiff.getIncreased());
		this.missingEndpoints = convert2EndpointList(pathDiff.getMissing());
		
		this.changedEndpoints = new ArrayList<ChangedEndpoint>();
		
		List<String> sharedKey = pathDiff.getSharedKey();
		ChangedEndpoint changedEndpoint = null;
		for (String pathUrl : sharedKey){
			if ("/v1/customers".equals(pathUrl)){
				System.out.println(pathUrl);
			}
			changedEndpoint = new ChangedEndpoint();
			changedEndpoint.setPathUrl(pathUrl);
			Path oldPath = oldPaths.get(pathUrl);
			Path newPath = newPaths.get(pathUrl);
			
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
			for (HttpMethod method : sharedMethods){
				changedOperation = new ChangedOperation();
				Operation oldOperation = oldOperationMap.get(method);
				Operation newOperation = newOperationMap.get(method);
				changedOperation.setSummary(newOperation.getSummary());
				
				List<Parameter> oldParameters = oldOperation.getParameters();
				List<Parameter> newParameters = newOperation.getParameters();
				ParameterDiff parameterDiff = ParameterDiff.diff(oldParameters, newParameters);
				changedOperation.setAddParameters(parameterDiff.getIncreased());
				changedOperation.setMissingParameters(parameterDiff.getMissing());
				changedOperation.setChangedParameter(parameterDiff.getChanged());
				
				Property oldResponseProperty = getResponseProperty(oldOperation);
				Property newResponseProperty = getResponseProperty(newOperation);
				PropertyDiff propertyDiff = PropertyDiff.buildWithDefinition(oldSpecSwagger.getDefinitions(), newSpecSwagger.getDefinitions());
				propertyDiff.diff(oldResponseProperty, newResponseProperty);
				changedOperation.setAddProps(propertyDiff.getIncreased());
				changedOperation.setMissingProps(propertyDiff.getMissing());
				
				if (changedOperation.isDiff()){
					operas.put(method, changedOperation);
				}
			}
			changedEndpoint.setChangedOperations(operas);
			
			this.newEndpoints.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getNewOperations()));
			this.missingEndpoints.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getMissingOperations()));
			
			if (changedEndpoint.isDiff()){
				changedEndpoints.add(changedEndpoint);
			}
		}
		
		return this;
	}

	

	private Property getResponseProperty(Operation operation) {
		Map<String, Response> responses = operation.getResponses();
		Response response = responses.get("200");
		return response.getSchema();
	}

	private List<Endpoint> convert2EndpointList(Map<String, Path> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map) return endpoints;
		for (Entry<String, Path> entry : map.entrySet()) {
			String url = entry.getKey();
			Path path = entry.getValue();
			
			Map<HttpMethod, Operation> operationMap = path.getOperationMap();
			for (Entry<HttpMethod, Operation> entryOper : operationMap.entrySet()){
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
	private Collection<? extends Endpoint> convert2EndpointList(String pathUrl,
			Map<HttpMethod, Operation> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map) return endpoints;
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

}
