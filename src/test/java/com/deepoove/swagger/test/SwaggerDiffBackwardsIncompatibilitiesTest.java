package com.deepoove.swagger.test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.output.HtmlRender;

public class SwaggerDiffBackwardsIncompatibilitiesTest {

    private static final String SWAGGER_V2_DOC1 = "petstore_v2_1.json";

    private static final String SWAGGER_V2_WITH_ALL_BACKWARDS_INCOMPATIBILITIES_DOC = "backwards_incompatibilities/petstore_v2_withAllBackwardsIncompatibilities.json";

    private static final String SWAGGER_V2_WITH_ADD_REQUIRED_PARAM_DOC = "backwards_incompatibilities/petstore_v2_withAddRequiredParam.json";

    private static final String SWAGGER_V2_WITH_DELETE_PARAM_DOC = "backwards_incompatibilities/petstore_v2_withDeleteParam.json";

    private static final String SWAGGER_V2_WITH_DELETE_PROPERTY_DOC = "backwards_incompatibilities/petstore_v2_withDeletePropertyResponse.json";

    private static final String SWAGGER_V2_WITH_CHANGE_PARAM_TO_REQUIRED_DOC = "backwards_incompatibilities/petstore_v2_withChangeParamToRequired.json";

    private static final String SWAGGER_V2_WITH_CHANGE_PROPERTY_BODY_TO_REQUIRED_DOC = "backwards_incompatibilities/petstore_v2_withChangePropertyBodyToRequired.json";

    private static final String SWAGGER_V2_WITH_CHANGE_TYPE_PROPERTY_IN_BODY_DOC = "backwards_incompatibilities/petstore_v2_withTypePropertyBody.json";

    private static final String SWAGGER_V2_WITH_ADD_REQUIRED_PROPERTY_IN_BODY_DOC = "backwards_incompatibilities/petstore_v2_withAddRequiredPropertyBody.json";

    private static final String SWAGGER_V2_WITH_CHANGE_TYPE_PROPERTY_IN_RESPONSE_DOC = "backwards_incompatibilities/petstore_v2_withChangeTypePropertyResponse.json";

    private static final String SWAGGER_V2_WITH_CHANGE_TYPE_PATH_PARAM_DOC = "backwards_incompatibilities/petstore_v2_withChangeTypePathParam.json";

    private static final String SWAGGER_V2_WITH_CHANGE_TYPE_QUERY_PARAM_DOC = "backwards_incompatibilities/petstore_v2_withChangeTypeQueryParam.json";

    private static final String SWAGGER_V2_WITH_CHANGES_COMPATIBLES_DOC = "backwards_incompatibilities/petstore_v2_withChangesCompatibles.json";

    @Test
    public void testBackwardsIncompatibilitiesDeleteParam() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_DELETE_PARAM_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesAddRequiredParam() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_ADD_REQUIRED_PARAM_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesDeleteProperty() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_DELETE_PROPERTY_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesChangeParamToRequired() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_CHANGE_PARAM_TO_REQUIRED_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesChangePropertyBodyToRequired() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_CHANGE_PROPERTY_BODY_TO_REQUIRED_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesChangeTypePropertyBody() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_CHANGE_TYPE_PROPERTY_IN_BODY_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesAddRequiredPropertyBody() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_ADD_REQUIRED_PROPERTY_IN_BODY_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesChangeTypePropertyResponse() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_CHANGE_TYPE_PROPERTY_IN_RESPONSE_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesChangeTypeQueryParam() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_CHANGE_TYPE_QUERY_PARAM_DOC);
    }

    @Test
    public void testBackwardsIncompatibilitiesChangeTypePathParam() {
        verifyBackwardsIncompatibilities(SWAGGER_V2_WITH_CHANGE_TYPE_PATH_PARAM_DOC);
    }

    @Test
    public void testBackwardsCompatibles() {
        SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_WITH_CHANGES_COMPATIBLES_DOC);
        buildRenderHtml(diff, "testBackwardsCompatibles");
        Assert.assertTrue(diff.isBackwardsCompatible());
        Assert.assertFalse(diff.getChangedEndpoints().isEmpty());
        Assert.assertFalse(diff.getNewEndpoints().isEmpty());
    }

    @Test
    public void testAllBackwardsIncompatibilities() {
        SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_WITH_ALL_BACKWARDS_INCOMPATIBILITIES_DOC);
        buildRenderHtml(diff, "testBackwardsAllIncompatibilities");
        List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
        Assert.assertFalse(diff.isBackwardsCompatible());
        Assert.assertFalse(changedEndPoints.isEmpty());

    }

    private SwaggerDiff verifyBackwardsIncompatibilities(final String pNewSpec) {
        SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, pNewSpec);
        List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
        Assert.assertFalse("Contract must be incompatible.", diff.isBackwardsCompatible());
        Assert.assertFalse(changedEndPoints.isEmpty());
        return diff;
    }

    private void buildRenderHtml(final SwaggerDiff pSwaggerDiff, final String pFileName) {
        List<String> css = new ArrayList<String>();
        css.add("https://use.fontawesome.com/releases/v5.0.6/css/all.css");
        css.add("http://deepoove.com/swagger-diff/stylesheets/demo.css");
        List<String> scripts = new ArrayList<String>();
        String html = new HtmlRender("Changelog", css, scripts).withBackwardsIncompatibilities()
                .render(pSwaggerDiff);
        try {
            FileWriter fw = new FileWriter(
                    pFileName + ".html");
            fw.write(html);
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
