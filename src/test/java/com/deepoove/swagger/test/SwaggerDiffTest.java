package com.deepoove.swagger.test;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.*;
import com.deepoove.swagger.diff.output.HtmlRender;
import com.deepoove.swagger.diff.output.JsonRender;
import com.deepoove.swagger.diff.output.MarkdownRender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.BodyParameter;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class SwaggerDiffTest {

	final String SWAGGER_V2_DOC1 = "petstore_v2_1.json";
	final String SWAGGER_V2_DOC2 = "petstore_v2_2.json";
	final String SWAGGER_V2_EMPTY_DOC = "petstore_v2_empty.json";
	final String SWAGGER_V2_HTTP = "http://petstore.swagger.io/v2/swagger.json";

	@Test
	public void testEqual() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC2, SWAGGER_V2_DOC2);
		assertEqual(diff);
	}

	@Test
	public void testNewApi() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_EMPTY_DOC, SWAGGER_V2_DOC2);
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		String html = new HtmlRender("Changelog",
				"http://deepoove.com/swagger-diff/stylesheets/demo.css")
						.render(diff);

		try {
			FileWriter fw = new FileWriter(
					"testNewApi.html");
			fw.write(html);
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(newEndpoints.size() > 0);
		Assert.assertTrue(missingEndpoints.isEmpty());
		Assert.assertTrue(changedEndPoints.isEmpty());

	}

	@Test
	public void testDeprecatedApi() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_EMPTY_DOC);
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		String html = new HtmlRender("Changelog",
				"http://deepoove.com/swagger-diff/stylesheets/demo.css")
						.render(diff);

		try {
			FileWriter fw = new FileWriter(
					"testDeprecatedApi.html");
			fw.write(html);
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(newEndpoints.isEmpty());
		Assert.assertTrue(missingEndpoints.size() > 0);
		Assert.assertTrue(changedEndPoints.isEmpty());

	}
	
	@Test
	public void testDiff() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		String html = new HtmlRender("Changelog",
				"http://deepoove.com/swagger-diff/stylesheets/demo.css")
				.render(diff);

		try {
			FileWriter fw = new FileWriter(
					"testDiff.html");
			fw.write(html);
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertFalse(changedEndPoints.isEmpty());
		
	}
	
	@Test
	public void testDiffAndMarkdown() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
		String render = new MarkdownRender().render(diff);
		try {
			FileWriter fw = new FileWriter(
					"testDiff.md");
			fw.write(render);
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

    @Test
    public void testEqualRaw() throws IOException {
        String rawJson = load(SWAGGER_V2_DOC2);

        SwaggerDiff diff = SwaggerDiff.compareV2Raw(rawJson, rawJson);
        assertEqual(diff);
    }


    @Test
    public void testNewApiRaw() throws IOException {
        SwaggerDiff diff = SwaggerDiff.compareV2Raw(load(SWAGGER_V2_EMPTY_DOC), load(SWAGGER_V2_DOC2));

        List<Endpoint> newEndpoints = diff.getNewEndpoints();
        List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
        List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();

        Assert.assertTrue(newEndpoints.size() > 0);
        Assert.assertTrue(missingEndpoints.isEmpty());
        Assert.assertTrue(changedEndPoints.isEmpty());

    }

    @Test
    public void testDeprecatedApiRaw() throws IOException {
        SwaggerDiff diff = SwaggerDiff.compareV2Raw(load(SWAGGER_V2_DOC1), load(SWAGGER_V2_EMPTY_DOC));
        List<Endpoint> newEndpoints = diff.getNewEndpoints();
        List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
        List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();

        Assert.assertTrue(newEndpoints.isEmpty());
        Assert.assertTrue(missingEndpoints.size() > 0);
        Assert.assertTrue(changedEndPoints.isEmpty());
    }

	@Test
	public void testEqualJson() {
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SWAGGER_V2_DOC1);
			JsonNode json = new ObjectMapper().readTree(inputStream);
			SwaggerDiff diff = SwaggerDiff.compareV2(json, json);
			assertEqual(diff);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

    private String load(String location) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(location)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

	@Test
	public void testJsonRender() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
		String render = new JsonRender().render(diff);
		try {
			FileWriter fw = new FileWriter(
					"testDiff.json");
			fw.write(render);
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInputBodyArray() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
		Map<String, ChangedEndpoint> changedEndpointMap = diff.getChangedEndpoints().stream().collect(Collectors.toMap(ChangedEndpoint::getPathUrl, e -> e));
		Lists.newArrayList("/user/createWithArray", "/user/createWithList").forEach(name -> {
			Assert.assertTrue("Expecting changed endpoint " + name, changedEndpointMap.containsKey(name));
			ChangedEndpoint endpoint = changedEndpointMap.get(name);
			Assert.assertEquals(1, endpoint.getChangedOperations().size());
			Assert.assertTrue("Expecting POST method change", endpoint.getChangedOperations().containsKey(HttpMethod.POST));
			Assert.assertEquals(0, endpoint.getChangedOperations().get(HttpMethod.POST).getMissingParameters().size());
			Assert.assertEquals(0, endpoint.getChangedOperations().get(HttpMethod.POST).getAddParameters().size());
			Assert.assertEquals(1, endpoint.getChangedOperations().get(HttpMethod.POST).getChangedParameter().size());

			// assert changed property counts
			ChangedParameter changedInput = endpoint.getChangedOperations().get(HttpMethod.POST).getChangedParameter().get(0);
			Assert.assertTrue(changedInput.getLeftParameter() instanceof BodyParameter);
			Assert.assertTrue(changedInput.getRightParameter() instanceof BodyParameter);
			Assert.assertEquals(3, changedInput.getIncreased().size());
			Assert.assertEquals(3, changedInput.getMissing().size());
			Assert.assertEquals(1, changedInput.getChanged().size());

			// assert embedded array change is one of the missing properties
			List<ElProperty> missingProperties = changedInput.getMissing();
			Set<String> elementPaths = missingProperties.stream().map(ElProperty::getEl).collect(Collectors.toSet());
			Assert.assertTrue(elementPaths.contains("body.favorite.tags.removedField"));
		});
	}

	@Test
	public void testResponseBodyArray() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
		Map<String, ChangedEndpoint> changedEndpointMap = diff.getChangedEndpoints().stream().collect(Collectors.toMap(ChangedEndpoint::getPathUrl, e -> e));
		Lists.newArrayList("/pet/findByStatus", "/pet/findByTags").forEach(name -> {
			Assert.assertTrue("Expecting changed endpoint " + name, changedEndpointMap.containsKey(name));
			ChangedEndpoint endpoint = changedEndpointMap.get(name);
			Assert.assertEquals(1, endpoint.getChangedOperations().size());
			Assert.assertTrue("Expecting GET method change", endpoint.getChangedOperations().containsKey(HttpMethod.GET));

			// assert changed property counts
			ChangedOperation changedOutput = endpoint.getChangedOperations().get(HttpMethod.GET);
			Assert.assertEquals(3, changedOutput.getAddProps().size());
			Assert.assertEquals(3, changedOutput.getMissingProps().size());
			Assert.assertEquals(1, changedOutput.getChangedProps().size());

			// assert embedded array change is one of the missing properties
			List<ElProperty> missingProperties =changedOutput.getMissingProps();
			Set<String> elementPaths = missingProperties.stream().map(ElProperty::getEl).collect(Collectors.toSet());
			Assert.assertTrue(elementPaths.contains("tags.removedField"));
		});
	}

	@Test
	public void testDetectProducesAndConsumes() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
		Map<String, ChangedEndpoint> changedEndpointMap = diff.getChangedEndpoints().stream().collect(Collectors.toMap(ChangedEndpoint::getPathUrl, e -> e));
		Assert.assertTrue("Expecting changed endpoint " + "/store/order", changedEndpointMap.containsKey("/store/order"));
		ChangedEndpoint endpoint = changedEndpointMap.get("/store/order");
		Assert.assertTrue("Expecting POST method change", endpoint.getChangedOperations().containsKey(HttpMethod.POST));
		ChangedOperation changedOperation = endpoint.getChangedOperations().get(HttpMethod.POST);
		Assert.assertEquals(1, changedOperation.getAddConsumes().size());
		Assert.assertEquals(1, changedOperation.getMissingConsumes().size());
		Assert.assertEquals(0, changedOperation.getAddProduces().size());
		Assert.assertEquals(1, changedOperation.getMissingProduces().size());
	}

	@Test
	public void testChangedPropertyMetadata() {
		SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
		Map<String, ChangedEndpoint> changedEndpointMap = diff.getChangedEndpoints().stream().collect(Collectors.toMap(ChangedEndpoint::getPathUrl, e -> e));
		String postOrder = "/store/order";
		String getOrder = "/store/order/{orderId}";

		Assert.assertTrue("Expecting changed endpoint " + postOrder, changedEndpointMap.containsKey(postOrder));
		ChangedEndpoint postOrderChg = changedEndpointMap.get(postOrder);
		ChangedOperation postOrderChgOp = postOrderChg.getChangedOperations().get(HttpMethod.POST);
		Assert.assertEquals(1, postOrderChgOp.getChangedParameter().size());

		List<ElProperty> postChgProps = postOrderChgOp.getChangedParameter().get(0).getChanged();
		Assert.assertEquals(2, postChgProps.size());
		ElProperty orderIdProp = postChgProps.stream().filter(cp -> {
			return cp.getEl().equalsIgnoreCase("body.id");}).findFirst().get();
		Assert.assertTrue(orderIdProp.isTypeChange());
		Assert.assertFalse(orderIdProp.isNewEnums());
		Assert.assertFalse(orderIdProp.isRemovedEnums());
		ElProperty statusProp = postChgProps.stream().filter(cp -> {
			return cp.getEl().equalsIgnoreCase("body.status");}).findFirst().get();
		Assert.assertFalse(statusProp.isTypeChange());
		Assert.assertTrue(statusProp.isNewEnums());
		Assert.assertTrue(statusProp.isRemovedEnums());


		Assert.assertTrue("Expecting changed endpoint " + getOrder, changedEndpointMap.containsKey(getOrder));
		ChangedEndpoint getOrderChg = changedEndpointMap.get(getOrder);
		ChangedOperation getOrderChgOp = getOrderChg.getChangedOperations().get(HttpMethod.GET);
		List<ElProperty> getChgProps = getOrderChgOp.getChangedProps();
		Assert.assertEquals(2, getChgProps.size());

		orderIdProp = getChgProps.stream().filter(cp -> {
			return cp.getEl().equalsIgnoreCase("id");}).findFirst().get();
		Assert.assertTrue(orderIdProp.isTypeChange());
		Assert.assertFalse(orderIdProp.isNewEnums());
		Assert.assertFalse(orderIdProp.isRemovedEnums());
		statusProp = getChgProps.stream().filter(cp -> {
			return cp.getEl().equalsIgnoreCase("status");}).findFirst().get();
		Assert.assertFalse(statusProp.isTypeChange());
		Assert.assertTrue(statusProp.isNewEnums());
		Assert.assertTrue(statusProp.isRemovedEnums());
	}

	private void assertEqual(SwaggerDiff diff) {
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		Assert.assertTrue(newEndpoints.isEmpty());
		Assert.assertTrue(missingEndpoints.isEmpty());
		Assert.assertTrue(changedEndPoints.isEmpty());

	}

}
