package com.deepoove.swagger.test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.Endpoint;
import com.deepoove.swagger.diff.output.HtmlRender;
import com.deepoove.swagger.diff.output.MarkdownRender;

public class SwaggerDiffTest {

	final String SWAGGER_V1_DOC = "http://petstore.swagger.io/v2/swagger.json";
	// String swagger_v1_doc = "petstore_v1.json";
	final String SWAGGER_V2_DOC = "petstore_v2.json";

	final String SWAGGER_EMPTY_DOC = "petstore_empty.json";

	@Test
	public void testEqual() {
		SwaggerDiff diff = new SwaggerDiff(SWAGGER_V1_DOC, SWAGGER_V1_DOC,
				SwaggerDiff.SWAGGER_VERSION_V2).compare();
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		Assert.assertTrue(newEndpoints.isEmpty());
		Assert.assertTrue(missingEndpoints.isEmpty());
		Assert.assertTrue(changedEndPoints.isEmpty());

	}

	@Test
	public void testNewApi() {
		SwaggerDiff diff = new SwaggerDiff(SWAGGER_EMPTY_DOC, SWAGGER_V1_DOC,
				SwaggerDiff.SWAGGER_VERSION_V2).compare();
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		String html = new HtmlRender("Changelog",
				"http://deepoove.com/swagger-diff/stylesheets/demo.css")
						.render(diff);

		try {
			FileWriter fw = new FileWriter(
					"src/test/resources/testNewApi.html");
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
		SwaggerDiff diff = new SwaggerDiff(SWAGGER_V1_DOC, SWAGGER_EMPTY_DOC,
				SwaggerDiff.SWAGGER_VERSION_V2).compare();
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		String html = new HtmlRender("Changelog",
				"http://deepoove.com/swagger-diff/stylesheets/demo.css")
						.render(diff);

		try {
			FileWriter fw = new FileWriter(
					"src/test/resources/testDeprecatedApi.html");
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
		SwaggerDiff diff = new SwaggerDiff(SWAGGER_V1_DOC, SWAGGER_V2_DOC,
				SwaggerDiff.SWAGGER_VERSION_V2).compare();
		List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
		String html = new HtmlRender("Changelog",
				"http://deepoove.com/swagger-diff/stylesheets/demo.css")
				.render(diff);
		
		try {
			FileWriter fw = new FileWriter(
					"src/test/resources/testDiff.html");
			fw.write(html);
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertFalse(changedEndPoints.isEmpty());
		
	}
	
	@Test
	public void testDiffAndMarkdown() {
		SwaggerDiff diff = new SwaggerDiff(SWAGGER_V1_DOC, SWAGGER_V2_DOC,
				SwaggerDiff.SWAGGER_VERSION_V2).compare();
		String render = new MarkdownRender().render(diff);
		try {
			FileWriter fw = new FileWriter(
					"src/test/resources/testDiff.md");
			fw.write(render);
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
