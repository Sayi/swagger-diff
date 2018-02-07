package com.deepoove.swagger.diff.model;

public interface Changed {

    boolean isDiff();

    /**
     * @return check if the changes are backward compatible.
     */
    boolean isBackwardsCompatible();

}
