package com.deepoove.swagger.test;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.Endpoint;
import com.deepoove.swagger.diff.output.HtmlRender;
import com.deepoove.swagger.diff.output.MarkdownRender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.List;
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

	private void assertEqual(SwaggerDiff diff) {
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		Assert.assertTrue(newEndpoints.isEmpty());
		Assert.assertTrue(missingEndpoints.isEmpty());
		Assert.assertTrue(changedEndPoints.isEmpty());

	}

}
