package com.deepoove.swagger.diff.output;

import com.deepoove.swagger.diff.SwaggerDiff;

public interface Render {

    String render(SwaggerDiff diff);

}
