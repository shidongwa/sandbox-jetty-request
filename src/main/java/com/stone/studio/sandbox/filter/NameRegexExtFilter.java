package com.stone.studio.sandbox.filter;

import com.alibaba.jvm.sandbox.api.filter.NameRegexFilter;

public class NameRegexExtFilter extends NameRegexFilter {
    private int paraCount;
    private String javaMethodRegex;

    public NameRegexExtFilter(String javaNameRegex, String javaMethodRegex, int paraCount) {
        super(javaNameRegex, javaMethodRegex);
        this.paraCount = paraCount;
        this.javaMethodRegex = javaMethodRegex;
    }

    public boolean doMethodFilter(int access, String javaMethodName, String[] parameterTypeJavaClassNameArray, String[] throwsTypeJavaClassNameArray, String[] annotationTypeJavaClassNameArray) {
        return javaMethodName.matches(this.javaMethodRegex) && parameterTypeJavaClassNameArray != null && parameterTypeJavaClassNameArray.length == paraCount;
    }

}
